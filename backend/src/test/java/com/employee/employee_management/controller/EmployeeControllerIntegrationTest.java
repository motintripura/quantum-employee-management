package com.employee.employee_management.controller;

import com.employee.employee_management.entity.Department;
import com.employee.employee_management.entity.Designation;
import com.employee.employee_management.entity.Role;
import com.employee.employee_management.repository.DepartmentRepository;
import com.employee.employee_management.repository.DesignationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DesignationRepository designationRepository;

    private Long departmentId;
    private Long designationId;
    private String adminToken;
    private String hrToken;
    private String employeeToken;

    @BeforeEach
    void setUp() throws Exception {
        Department department = departmentRepository.save(
                Department.builder().name("IT").description("IT dept").status(true).build());
        departmentId = department.getId();
        designationId = designationRepository.save(
                Designation.builder().name("Software Engineer").department(department).status(true).build()).getId();

        adminToken = loginAndGetToken("admin", "Admin@123");
        hrToken = registerAndLogin("hruser", "hr@ems.com", Role.HR);
        employeeToken = registerAndLogin("empuser", "emp@ems.com", Role.EMPLOYEE);
    }

    private String registerAndLogin(String username, String email, Role role) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "email", email,
                                "password", "Password@1",
                                "role", role))))
                .andExpect(status().isCreated());
        return loginAndGetToken(username, "Password@1");
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.at("/data/token").asText();
    }

    private Map<String, Object> validEmployeePayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("firstName", "John");
        payload.put("lastName", "Doe");
        payload.put("email", "john.doe@ems.com");
        payload.put("phone", "1234567890");
        payload.put("joiningDate", "2026-01-15");
        payload.put("departmentId", departmentId);
        payload.put("designationId", designationId);
        payload.put("salary", 50000);
        payload.put("status", true);
        return payload;
    }

    @Test
    void getAll_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_asEmployee_returns403() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAll_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void create_asAdmin_success() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEmployeePayload())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.departmentName").value("IT"))
                .andExpect(jsonPath("$.data.temporaryPassword").isNotEmpty());
    }

    @Test
    void create_asEmployee_returns403() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEmployeePayload())))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_invalidPayload_returns400() throws Exception {
        Map<String, Object> payload = validEmployeePayload();
        payload.put("firstName", " ");
        payload.put("email", "not-an-email");
        payload.put("joiningDate", "2030-01-01");

        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.firstName").value("First name is required"))
                .andExpect(jsonPath("$.errors.email").value("Email must be valid"))
                .andExpect(jsonPath("$.errors.joiningDate").value("Joining date cannot be in the future"));
    }

    @Test
    void create_duplicateEmail_returns409() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEmployeePayload())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEmployeePayload())))
                .andExpect(status().isConflict());
    }

    @Test
    void generateCode_asEmployee_returns403() throws Exception {
        mockMvc.perform(get("/api/employees/code/generate")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void generateCode_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/employees/code/generate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("EMP0001"));
    }

    @Test
    void getById_asManager_returns200() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEmployeePayload())))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(created.getResponse().getContentAsString());
        long employeeId = json.at("/data/id").asLong();

        mockMvc.perform(get("/api/employees/{id}", employeeId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeCode").value("EMP0001"))
                .andExpect(jsonPath("$.data.email").value("john.doe@ems.com"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/employees/{id}", 999999L)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_asHr_returns403() throws Exception {
        mockMvc.perform(delete("/api/employees/{id}", 1L)
                        .header("Authorization", "Bearer " + hrToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_asAdmin_success() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEmployeePayload())))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(created.getResponse().getContentAsString());
        long employeeId = json.at("/data/id").asLong();

        mockMvc.perform(delete("/api/employees/{id}", employeeId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void update_asHr_success() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEmployeePayload())))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(created.getResponse().getContentAsString());
        long employeeId = json.at("/data/id").asLong();

        Map<String, Object> payload = validEmployeePayload();
        payload.put("firstName", "Jane");

        mockMvc.perform(put("/api/employees/{id}", employeeId)
                        .header("Authorization", "Bearer " + hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Jane"));
    }

    @Test
    void toggleStatus_asAdmin_flips() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEmployeePayload())))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(created.getResponse().getContentAsString());
        long employeeId = json.at("/data/id").asLong();

        mockMvc.perform(patch("/api/employees/{id}/status", employeeId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(false));
    }
}