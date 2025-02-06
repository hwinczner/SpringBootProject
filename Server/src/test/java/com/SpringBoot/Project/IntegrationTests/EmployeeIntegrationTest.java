package com.SpringBoot.Project.IntegrationTests;

import com.SpringBoot.Project.Models.*;
import com.SpringBoot.Project.Repositories.*;
import com.fasterxml.jackson.core.type.TypeReference;
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

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WithMockUser(roles = "ADMIN")
public class EmployeeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeInterface employeeInterface;

    @Autowired
    private DepartmentInterface departmentInterface;

    @Autowired
    private RoleInterface roleInterface;

    @Autowired
    private UserInterface userInterface;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserEntity userEntity;
    private Roles roles;
    private Department department;

    @BeforeEach
    void setUp() {
        // Clean up existing data
        employeeInterface.deleteAll();
        departmentInterface.deleteAll();
        userInterface.deleteAll();  // This should cascade delete roles due to CascadeType.ALL
        roleInterface.deleteAll();  // Cleanup any remaining roles

        // Create role
        roles = new Roles("ROLE_EMPLOYEE");
        roles = roleInterface.save(roles);

        // Create user entity with role
        List<Roles> rolesList = new ArrayList<>();
        rolesList.add(roles);
        userEntity = new UserEntity("testuser", passwordEncoder.encode("password"), rolesList);
        userEntity = userInterface.save(userEntity);

        // Initialize department
        department = new Department("Test Dept", "Test Department");
        department = departmentInterface.save(department);
    }

    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    void completeEmployeeWorkflowTest() throws Exception {
        // Create a department
        Department department = new Department("IT", "Information Technology");
        String departmentJson = objectMapper.writeValueAsString(department);

        String departmentResponse = mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(departmentJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Parse department response
        TypeReference<Result<Department>> departmentTypeRef = new TypeReference<>() {};
        Result<Department> departmentResult = objectMapper.readValue(departmentResponse, departmentTypeRef);
        Department createdDepartment = departmentResult.getData();

        // Create an employee in that department
        Employee employee = new Employee("John Doe", "john.doe@example.com", createdDepartment, roles, userEntity);
        String employeeJson = objectMapper.writeValueAsString(employee);

        String employeeResponse = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Parse employee response directly as Employee since it's not wrapped in Result
        Employee createdEmployee = objectMapper.readValue(employeeResponse, Employee.class);

        // Verify employee is in the correct department
        mockMvc.perform(get("/api/employees/" + createdEmployee.getEmployeeId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.department.name").value("IT"));

        // Create a new role for update
        Roles newRole = new Roles("ROLE_MANAGER");
        newRole = roleInterface.save(newRole);

        // Update employee's role
        createdEmployee.setRole(newRole);
        String updatedEmployeeJson = objectMapper.writeValueAsString(createdEmployee);

        mockMvc.perform(put("/api/employees/" + createdEmployee.getEmployeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedEmployeeJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role.name").value("ROLE_MANAGER"));
    }

    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    void employeeValidationTest() throws Exception {
        // Create department
        Department department = new Department("HR", "Human Resources");
        department = departmentInterface.save(department);

        // Try to create employee with invalid email format
        Employee invalidEmployee = new Employee("John Doe", "invalid-email-format", department, roles, userEntity);
        String invalidEmployeeJson = objectMapper.writeValueAsString(invalidEmployee);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEmployeeJson))
                .andExpect(status().isBadRequest());

        // Try to create employee with null name
        invalidEmployee.setEmail("jane.doe@example.com");
        invalidEmployee.setName(null);
        invalidEmployeeJson = objectMapper.writeValueAsString(invalidEmployee);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEmployeeJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    void employeeWithNonExistentDepartmentTest() throws Exception {
        Department nonExistentDepartment = new Department("Fake Dept", "This department doesn't exist");

        Employee employee = new Employee("John Doe", "john.doe@example.com", nonExistentDepartment, roles, userEntity);
        String employeeJson = objectMapper.writeValueAsString(employee);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    void employeeDepartmentTransferTest() throws Exception {
        // Create two departments
        Department itDepartment = new Department("IT", "Information Technology");
        Department hrDepartment = new Department("HR", "Human Resources");

        itDepartment = departmentInterface.save(itDepartment);
        hrDepartment = departmentInterface.save(hrDepartment);

        // Create employee in IT department
        Employee employee = new Employee("John Doe", "john.doe@example.com", itDepartment, roles, userEntity);
        String employeeJson = objectMapper.writeValueAsString(employee);

        String response = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Employee createdEmployee = objectMapper.readValue(response, Employee.class);

        // Transfer employee to HR department
        createdEmployee.setDepartment(hrDepartment);
        String updatedEmployeeJson = objectMapper.writeValueAsString(createdEmployee);

        mockMvc.perform(put("/api/employees/" + createdEmployee.getEmployeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedEmployeeJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.department.name").value("HR"));
    }
}