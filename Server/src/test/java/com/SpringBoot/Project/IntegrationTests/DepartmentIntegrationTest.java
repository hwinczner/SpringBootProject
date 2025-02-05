package com.SpringBoot.Project.IntegrationTests;

import com.SpringBoot.Project.Models.Department;
import com.SpringBoot.Project.Models.Employee;
import com.SpringBoot.Project.Repositories.DepartmentInterface;
import com.SpringBoot.Project.Repositories.EmployeeInterface;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

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

    @BeforeEach
    void setUp() {
        employeeInterface.deleteAll();
        departmentInterface.deleteAll();
    }

    @Test
    @Transactional
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
    void testUpdateDepartmentWithEmployees() throws Exception {
        // 1. Create department
        Department department = new Department("IT", "Information Technology");
        department = departmentInterface.save(department);

        // 2. Create employee in department
        Employee employee = new Employee("John Doe", "john.doe@example.com", department, "Developer");
        employeeInterface.save(employee);

        // 3. Update department name and verify the change reflects in employee's department
        department.setName("Information Technology");
        String updateJson = objectMapper.writeValueAsString(department);

        mockMvc.perform(put("/api/departments/" + department.getDepartmentId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Information Technology"));

        // 4. Verify employee's department was updated
        mockMvc.perform(get("/api/employees/" + employee.getEmployeeId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.department.name").value("Information Technology"));
    }

    @Test
    @Transactional
    void testDepartmentDeletionWithConstraints() throws Exception {
        // 1. Create department
        Department department = new Department("IT", "Information Technology");
        department = departmentInterface.save(department);

        // 2. Create employee in department
        Employee employee = new Employee("John Doe", "john.doe@example.com", department, "Developer");
        employeeInterface.save(employee);

        // 3. Attempt to delete department with existing employees (should fail with constraint violation)
        mockMvc.perform(delete("/api/departments/" + department.getDepartmentId()))
                .andExpect(status().isConflict())  // 409 Conflict is more appropriate than 500
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cannot delete department with existing employees"));

        // 4. Remove employee and try delete again
        employeeInterface.deleteAll();
        mockMvc.perform(delete("/api/departments/" + department.getDepartmentId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}