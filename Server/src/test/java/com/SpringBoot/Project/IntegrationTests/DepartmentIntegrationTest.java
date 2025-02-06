package com.SpringBoot.Project.IntegrationTests;

import com.SpringBoot.Project.Models.Department;
import com.SpringBoot.Project.Models.Employee;
import com.SpringBoot.Project.Models.Roles;
import com.SpringBoot.Project.Models.UserEntity;
import com.SpringBoot.Project.Repositories.DepartmentInterface;
import com.SpringBoot.Project.Repositories.EmployeeInterface;
import com.SpringBoot.Project.Repositories.RoleInterface;
import com.SpringBoot.Project.Repositories.UserInterface;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DepartmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartmentInterface departmentInterface;

    @Autowired
    private EmployeeInterface employeeInterface;

    @Autowired
    private RoleInterface rolesInterface;

    @Autowired
    private UserInterface userInterface;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Roles roles;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        employeeInterface.deleteAll();
        departmentInterface.deleteAll();
    }

    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    void testDepartmentCreationAndValidation() throws Exception {
        // Test creating department with valid data
        Department department = new Department("IT", "Information Technology");
        String departmentJson = objectMapper.writeValueAsString(department);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(departmentJson))
                .andExpect(status().isCreated());

        // Test creating department with null name (should fail)
        Department invalidDepartment = new Department(null, "Test Description");
        String invalidJson = objectMapper.writeValueAsString(invalidDepartment);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    void testDepartmentDuplicateNameValidation() throws Exception {
        // Create first department
        Department firstDepartment = new Department("IT", "Information Technology");
        String firstDepartmentJson = objectMapper.writeValueAsString(firstDepartment);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstDepartmentJson))
                .andExpect(status().isCreated());

        // Try to create second department with same name
        Department duplicateDepartment = new Department("IT", "Different Description");
        String duplicateJson = objectMapper.writeValueAsString(duplicateDepartment);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateJson))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")  // Since we're accessing employee endpoints
    void testUpdateDepartmentWithEmployees() throws Exception {
        // First create a role
        Roles role = new Roles();
        role.setName("ROLE_EMPLOYEE");
        role = rolesInterface.save(role);  // Save and get the persisted role

        // Create a user entity
        UserEntity user = new UserEntity();
        user.setUsername("johndoe");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRoles(Collections.singletonList(role));  // UserEntity takes a List<Roles>
        user = userInterface.save(user);

        // Create department
        Department department = new Department("IT", "Information Technology");
        department = departmentInterface.save(department);

        // Create employee with single role (not a list)
        Employee employee = new Employee("John Doe", "john.doe@example.com", department, role, user);  // Changed here
        employeeInterface.save(employee);

        // Rest of the test remains the same...
        department.setName("Information Technology");
        String updateJson = objectMapper.writeValueAsString(department);

        mockMvc.perform(put("/api/departments/" + department.getDepartmentId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Information Technology"));

        mockMvc.perform(get("/api/employees/" + employee.getEmployeeId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.department.name").value("Information Technology"));
    }

    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")  // Since we're accessing department delete endpoint
    void testDepartmentDeletionWithConstraints() throws Exception {
        // First create a role
        Roles role = new Roles();
        role.setName("ROLE_EMPLOYEE");
        role = rolesInterface.save(role);

        // Create a user entity
        UserEntity user = new UserEntity();
        user.setUsername("johndoe");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRoles(Collections.singletonList(role));
        user = userInterface.save(user);

        // Create department
        Department department = new Department("IT", "Information Technology");
        department = departmentInterface.save(department);

        // Create employee with proper role and user entity
        Employee employee = new Employee("John Doe", "john.doe@example.com", department, role, user);
        employeeInterface.save(employee);

        // Attempt to delete department with existing employees (should fail with constraint violation)
        mockMvc.perform(delete("/api/departments/" + department.getDepartmentId()))
                .andExpect(status().isConflict())  // 409 Conflict is more appropriate than 500
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cannot delete department with existing employees"));

        // Remove employee and try delete again
        employeeInterface.deleteAll();
        mockMvc.perform(delete("/api/departments/" + department.getDepartmentId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}