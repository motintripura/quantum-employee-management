package com.employee.employee_management.service.impl;

import com.employee.employee_management.dto.EmployeeRequest;
import com.employee.employee_management.dto.EmployeeResponse;
import com.employee.employee_management.entity.Department;
import com.employee.employee_management.entity.Designation;
import com.employee.employee_management.entity.Employee;
import com.employee.employee_management.entity.Role;
import com.employee.employee_management.entity.User;
import com.employee.employee_management.exception.BadRequestException;
import com.employee.employee_management.exception.DuplicateResourceException;
import com.employee.employee_management.exception.ResourceNotFoundException;
import com.employee.employee_management.mapper.EmployeeMapper;
import com.employee.employee_management.repository.DepartmentRepository;
import com.employee.employee_management.repository.DesignationRepository;
import com.employee.employee_management.repository.EmployeeRepository;
import com.employee.employee_management.repository.UserRepository;
import com.employee.employee_management.service.EmployeeService;
import com.employee.employee_management.util.Constants;
import com.employee.employee_management.util.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS =
            Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> getAll(String keyword,
                                                 Long departmentId,
                                                 Long designationId,
                                                 Boolean status,
                                                 Pageable pageable) {
        Page<Employee> page = employeeRepository.search(
                normalizeKeyword(keyword), departmentId, designationId, status, pageable);
        return PageResponse.from(page.map(EmployeeMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        return EmployeeMapper.toResponse(findEmployee(id));
    }

    @Override
    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (employeeRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email already exists: " + email);
        }

        String employeeCode = StringUtils.hasText(request.getEmployeeCode())
                ? request.getEmployeeCode().trim().toUpperCase()
                : generateEmployeeCode();
        if (employeeRepository.existsByEmployeeCodeIgnoreCase(employeeCode)) {
            throw new DuplicateResourceException("Employee code already exists: " + employeeCode);
        }

        Department department = findDepartment(request.getDepartmentId());
        Designation designation = findDesignation(request.getDesignationId(), department.getId());

        Employee employee = Employee.builder()
                .employeeCode(employeeCode)
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName() == null ? null : request.getLastName().trim())
                .email(email)
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .address(request.getAddress())
                .joiningDate(request.getJoiningDate())
                .department(department)
                .designation(designation)
                .salary(request.getSalary() == null ? java.math.BigDecimal.ZERO : request.getSalary())
                .profileImage(request.getProfileImage())
                .status(request.getStatus() == null ? Boolean.TRUE : request.getStatus())
                .build();
        employeeRepository.save(employee);

        User user = User.builder()
                .username(employeeCode)
                .email(email)
                .password(passwordEncoder.encode(Constants.DEFAULT_USER_PASSWORD))
                .role(Role.EMPLOYEE)
                .employee(employee)
                .enabled(true)
                .build();
        userRepository.save(user);

        EmployeeResponse response = EmployeeMapper.toResponse(employee);
        response.setTemporaryPassword(Constants.DEFAULT_USER_PASSWORD);
        return response;
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = findEmployee(id);
        String email = request.getEmail().trim().toLowerCase();

        if (employeeRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new DuplicateResourceException("Email already exists: " + email);
        }

        String employeeCode = StringUtils.hasText(request.getEmployeeCode())
                ? request.getEmployeeCode().trim().toUpperCase()
                : employee.getEmployeeCode();
        if (employeeRepository.existsByEmployeeCodeIgnoreCaseAndIdNot(employeeCode, id)) {
            throw new DuplicateResourceException("Employee code already exists: " + employeeCode);
        }

        Department department = findDepartment(request.getDepartmentId());
        Designation designation = findDesignation(request.getDesignationId(), department.getId());

        employee.setEmployeeCode(employeeCode);
        employee.setFirstName(request.getFirstName().trim());
        employee.setLastName(request.getLastName() == null ? null : request.getLastName().trim());
        employee.setEmail(email);
        employee.setPhone(request.getPhone());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setGender(request.getGender());
        employee.setAddress(request.getAddress());
        employee.setJoiningDate(request.getJoiningDate());
        employee.setDepartment(department);
        employee.setDesignation(designation);
        employee.setSalary(request.getSalary());
        employee.setProfileImage(request.getProfileImage());
        if (request.getStatus() != null) {
            employee.setStatus(request.getStatus());
        }
        employeeRepository.save(employee);

        userRepository.findByEmployeeId(id).ifPresent(user -> {
            user.setEmail(email);
            userRepository.save(user);
        });

        return EmployeeMapper.toResponse(employee);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Employee employee = findEmployee(id);
        userRepository.findByEmployeeId(id).ifPresent(userRepository::delete);
        employeeRepository.delete(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse toggleStatus(Long id) {
        Employee employee = findEmployee(id);
        employee.setStatus(!Boolean.TRUE.equals(employee.getStatus()));
        employeeRepository.save(employee);
        return EmployeeMapper.toResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse uploadProfileImage(Long id, MultipartFile file) {
        Employee employee = findEmployee(id);
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please select an image file to upload");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }

        String original = file.getOriginalFilename();
        String extension = "";
        if (original != null && original.contains(".")) {
            extension = original.substring(original.lastIndexOf('.')).toLowerCase();
        }
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Only JPG, JPEG, PNG, GIF and WEBP images are allowed");
        }

        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String filename = "emp_" + id + "_" + UUID.randomUUID() + extension;
            Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            employee.setProfileImage("/uploads/" + filename);
            employeeRepository.save(employee);
            return EmployeeMapper.toResponse(employee);
        } catch (IOException ex) {
            throw new BadRequestException("Failed to upload image: " + ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String generateEmployeeCode() {
        int max = 0;
        List<String> codes = employeeRepository.findCodesByPrefix(Constants.EMPLOYEE_CODE_PREFIX);
        Pattern pattern = Pattern.compile("^(\\d+)$");
        for (String code : codes) {
            String suffix = code.substring(Constants.EMPLOYEE_CODE_PREFIX.length());
            Matcher matcher = pattern.matcher(suffix);
            if (matcher.matches()) {
                max = Math.max(max, Integer.parseInt(suffix));
            }
        }
        return Constants.EMPLOYEE_CODE_PREFIX
                + String.format("%0" + Constants.EMPLOYEE_CODE_PADDING + "d", max + 1);
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    private Designation findDesignation(Long id, Long departmentId) {
        Designation designation = designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found with id: " + id));
        if (!designation.getDepartment().getId().equals(departmentId)) {
            throw new BadRequestException("Designation does not belong to the selected department");
        }
        return designation;
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }
}
