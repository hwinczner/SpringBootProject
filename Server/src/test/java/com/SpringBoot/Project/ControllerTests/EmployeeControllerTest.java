package com.SpringBoot.Project.ControllerTests;

import com.SpringBoot.Project.Controllers.EmployeeController;
import com.SpringBoot.Project.Models.Department;
import com.SpringBoot.Project.Models.Employee;
import com.SpringBoot.Project.Models.Result;
import com.SpringBoot.Project.Services.DepartmentService;
import com.SpringBoot.Project.Services.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private DepartmentService departmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee employee;
    private Department department;
    private Result<Employee> successResult;
    private Result<Employee> failureResult;

    @BeforeEach
    void setUp() {
        department = new Department("IT", "Information Technology");
        try {
            var deptField = Department.class.getDeclaredField("departmentId");
            deptField.setAccessible(true);
            deptField.set(department, 1L);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set department ID", e);
        }

        employee = new Employee("John Doe", "john.doe@example.com", department, "Developer");
        try {
            var empField = Employee.class.getDeclaredField("employeeId");
            empField.setAccessible(true);
            empField.set(employee, 1L);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set employee ID", e);
        }

        successResult = Result.success(employee, "Operation successful");
        failureResult = Result.failure("Operation failed", List.of("Error message"));
    }

    @Test
    void getAllEmployees_Success() throws Exception {
        List<Employee> employees = Arrays.asList(employee);
        Result<List<Employee>> result = Result.success(employees, "Employees fetched successfully");

        when(employeeService.getAllEmployees()).thenReturn(result);

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("John Doe"))
                .andExpect(jsonPath("$.message").value("Employees fetched successfully"));
    }

    @Test
    void getEmployeeById_Success() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(successResult);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("John Doe"));
    }

    @Test
    void getEmployeeById_NotFound() throws Exception {
        Result<Employee> notFoundResult = Result.failure("Employee not found", List.of("No employee with ID 1"));
        when(employeeService.getEmployeeById(1L)).thenReturn(notFoundResult);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    @Test
    void createEmployee_Success() throws Exception {
        Result<Department> departmentResult = Result.success(department, "Department found");
        when(departmentService.getDepartmentById(1L)).thenReturn(departmentResult);
        when(employeeService.saveOrUpdateEmployee(any(Employee.class))).thenReturn(successResult);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void createEmployee_NullDepartment() throws Exception {
        employee.setDepartment(null);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Department ID is required"));
    }

    @Test
    void createEmployee_DepartmentNotFound() throws Exception {
        when(departmentService.getDepartmentById(anyLong())).thenReturn(null);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Department with ID 1 not found"));
    }

    @Test
    void createEmployee_ValidationFailure() throws Exception {
        Result<Department> departmentResult = Result.success(department, "Department found");
        when(departmentService.getDepartmentById(1L)).thenReturn(departmentResult);
        when(employeeService.saveOrUpdateEmployee(any(Employee.class)))
                .thenReturn(Result.failure("Validation failed", List.of("Invalid email format")));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Validation failed"));
    }

    @Test
    void updateEmployee_Success() throws Exception {
        when(employeeService.saveOrUpdateEmployee(any(Employee.class))).thenReturn(successResult);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateEmployee_IdMismatch() throws Exception {
        mockMvc.perform(put("/api/employees/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Employee ID in the path does not match the ID in the request body."));
    }

    @Test
    void updateEmployee_Failure() throws Exception {
        when(employeeService.saveOrUpdateEmployee(any(Employee.class))).thenReturn(failureResult);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteEmployee_Success() throws Exception {
        when(employeeService.deleteEmployeeById(anyLong()))
                .thenReturn(Result.success(null, "Employee deleted successfully"));

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteEmployee_NotFound() throws Exception {
        when(employeeService.deleteEmployeeById(anyLong()))
                .thenReturn(Result.failure("Employee not found", List.of("No employee with ID 1")));

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}