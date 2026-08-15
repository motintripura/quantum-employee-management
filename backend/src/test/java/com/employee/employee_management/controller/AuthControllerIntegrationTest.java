package com.employee.employee_management.controller;

import com.employee.employee_management.entity.Department;
import com.employee.employee_management.entity.Designation;
import com.employee.employee_management.entity.Role;
import com.employee.employee_management.repository.DepartmentRepository;
import com.employee.employee_management.repository.DesignationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DesignationRepository designationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String loginBody(String username, String password) throws Exception {
        return objectMapper.writeValueAsString(
                java.util.Map.of("username", username, "password", password));
    }

    private String registerBody(String username, String email, String password, Role role) throws Exception {
        return objectMapper.writeValueAsString(java.util.Map.of(
                "username", username,
                "email", email,
                "password", password,
                "role", role));
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(username, password)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.at("/data/token").asText();
    }

    @Test
    void login_success_returnsToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("admin", "Admin@123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.username").value("admin"))
                .andExpect(jsonPath("$.data.user.role").value("ADMIN"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("admin", "WrongPass!")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_blankFields_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.username").value("Username or email is required"))
                .andExpect(jsonPath("$.errors.password").value("Password is required"));
    }

    @Test
    void me_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withToken_returnsCurrentUser() throws Exception {
        String token = loginAndGetToken("admin", "Admin@123");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    void register_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("newuser", "new@ems.com", "Password@1", Role.HR)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_asAdmin_success() throws Exception {
        String token = loginAndGetToken("admin", "Admin@123");

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("hruser1", "hr1@ems.com", "Password@1", Role.HR)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("hruser1"))
                .andExpect(jsonPath("$.data.role").value("HR"));
    }

    @Test
    void register_duplicateUsername_returns409() throws Exception {
        String token = loginAndGetToken("admin", "Admin@123");

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("hruser2", "hr2@ems.com", "Password@1", Role.HR)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("hruser2", "other@ems.com", "Password@1", Role.HR)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void register_invalidPayload_returns400() throws Exception {
        String token = loginAndGetToken("admin", "Admin@123");

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("ab", "not-an-email", "short", Role.HR)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void register_byEmployee_returns403() throws Exception {
        String adminToken = loginAndGetToken("admin", "Admin@123");

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("empuser1", "emp1@ems.com", "Password@1", Role.EMPLOYEE)))
                .andExpect(status().isCreated());

        String empToken = loginAndGetToken("empuser1", "Password@1");

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer " + empToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("another", "another@ems.com", "Password@1", Role.HR)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied. You do not have permission to perform this action."));
    }

    @Test
    void register_roleNull_returns400() throws Exception {
        String token = loginAndGetToken("admin", "Admin@123");
        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "username", "norole",
                "email", "norole@ems.com",
                "password", "Password@1"));

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.role").value("Role is required"));
    }

    @Test
    void login_byEmail_works() throws Exception {
        assertThat(loginAndGetToken("admin", "Admin@123")).isNotBlank();
    }

    @Test
    void profile_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profile_asAdmin_noEmployeeLinked_returns404() throws Exception {
        String token = loginAndGetToken("admin", "Admin@123");

        mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No employee profile is linked to this user"));
    }

    @Test
    void profile_linkedEmployee_returnsEmployeeProfile() throws Exception {
        String adminToken = loginAndGetToken("admin", "Admin@123");

        Department dept = departmentRepository.save(
                Department.builder().name("ProfileIT").description("IT").status(true).build());
        Long deptId = dept.getId();
        Long desigId = designationRepository.save(
                Designation.builder().name("Profile Engineer").department(dept).status(true).build()).getId();

        Map<String, Object> empPayload = new LinkedHashMap<>();
        empPayload.put("firstName", "Profile");
        empPayload.put("lastName", "User");
        empPayload.put("email", "profile.user@ems.com");
        empPayload.put("phone", "1112223333");
        empPayload.put("joiningDate", "2026-02-01");
        empPayload.put("departmentId", deptId);
        empPayload.put("designationId", desigId);
        empPayload.put("salary", 45000);
        empPayload.put("status", true);

        MvcResult empResult = mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empPayload)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode empJson = objectMapper.readTree(empResult.getResponse().getContentAsString());
        long employeeId = empJson.at("/data/id").asLong();
        String employeeCode = empJson.at("/data/employeeCode").asText();

        String empToken = loginAndGetToken(employeeCode, "Default@123");

        mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "Bearer " + empToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeCode").value(employeeCode))
                .andExpect(jsonPath("$.data.firstName").value("Profile"))
                .andExpect(jsonPath("$.data.departmentName").value("ProfileIT"));
    }
}