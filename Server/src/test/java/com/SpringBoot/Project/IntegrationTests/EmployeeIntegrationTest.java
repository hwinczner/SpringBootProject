package com.SpringBoot.Project.IntegrationTests;

import com.SpringBoot.Project.Models.*;
import com.SpringBoot.Project.Repositories.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmployeeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeInterface employeeInterface;

    @Autowired
    private DepartmentInterface departmentInterface;

    @BeforeEach
    void setUp() {
        employeeInterface.deleteAll();
        departmentInterface.deleteAll();
    }

    @Test
    @Transactional
    void completeEmployeeWorkflowTest() throws Exception {
        // Create a department
        Department department = new Department("IT", "Information Technology");
        String departmentJson = objectMapper.writeValueAsString(department);

        // Create department and get its ID from the database
        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(departmentJson))
                .andExpect(status().isCreated());

        // Fetch the created department
        List<Department> departments = departmentInterface.findAll();
        Department createdDepartment = departments.get(0);

        // Create an employee in that department
        Employee employee = new Employee("John Doe", "john.doe@example.com", createdDepartment, "Developer");
        String employeeJson = objectMapper.writeValueAsString(employee);

        String employeeResponse = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Employee createdEmployee = objectMapper.readValue(employeeResponse, Employee.class);

        // Verify employee is in the correct department
        mockMvc.perform(get("/api/employees/" + createdEmployee.getEmployeeId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.department.name").value("IT"));

        // Update employee's role
        createdEmployee.setRole("Senior Developer");
        String updatedEmployeeJson = objectMapper.writeValueAsString(createdEmployee);

        mockMvc.perform(put("/api/employees/" + createdEmployee.getEmployeeId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedEmployeeJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("Senior Developer"));
    }

    @Test
    @Transactional
    void employeeValidationTest() throws Exception {
        // Create department
        Department department = new Department("HR", "Human Resources");
        department = departmentInterface.save(department);

        // Try to create employee with invalid email
        Employee invalidEmployee = new Employee("Jane Doe", "invalid-email", department, "Manager");
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
    void employeeWithNonExistentDepartmentTest() throws Exception {
        // Create an employee referencing a department that doesn't exist in the database
        Department nonExistentDepartment = new Department("Fake Dept", "This department doesn't exist");
        // The department will have id=0 since it's not persisted, which doesn't exist in the database

        Employee employee = new Employee("John Doe", "john.doe@example.com", nonExistentDepartment, "Developer");
        String employeeJson = objectMapper.writeValueAsString(employee);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void employeeDepartmentTransferTest() throws Exception {
        // Create two departments
        Department itDepartment = new Department("IT", "Information Technology");
        Department hrDepartment = new Department("HR", "Human Resources");

        itDepartment = departmentInterface.save(itDepartment);
        hrDepartment = departmentInterface.save(hrDepartment);

        // Create employee in IT department
        Employee employee = new Employee("John Doe", "john.doe@example.com", itDepartment, "Developer");
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