package com.SpringBoot.Project.ControllerTests;

import com.SpringBoot.Project.Controllers.EmployeeController;
import com.SpringBoot.Project.Models.*;
import com.SpringBoot.Project.Services.DepartmentService;
import com.SpringBoot.Project.Services.EmployeeService;
import com.SpringBoot.Project.Services.RoleService;
import com.SpringBoot.Project.Services.UserEntityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

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

    @MockBean
    private RoleService roleService;

    @MockBean
    private UserEntityService userEntityService;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee employee;
    private Department department;
    private Roles role;
    private UserEntity userEntity;
    private Result<Employee> successResult;
    private Result<Employee> failureResult;
    private Roles roles;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        department = new Department("IT", "Information Technology");
        role = new Roles("ROLE_EMPLOYEE");
        userEntity = new UserEntity();
        userEntity.setUsername("john.doe");

        try {
            var deptField = Department.class.getDeclaredField("departmentId");
            deptField.setAccessible(true);
            deptField.set(department, 1L);

            var roleField = Roles.class.getDeclaredField("id");
            roleField.setAccessible(true);
            roleField.set(role, 1);

            var userField = UserEntity.class.getDeclaredField("id");
            userField.setAccessible(true);
            userField.set(userEntity, 1);

        } catch (Exception e) {
            throw new RuntimeException("Failed to set IDs", e);
        }

        employee = new Employee("John Doe", "john.doe@example.com", department, role, userEntity);
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
    @WithMockUser
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
    @WithMockUser
    void getEmployeeById_Success() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(successResult);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("John Doe"));
    }

    @Test
    @WithMockUser
    void getEmployeeById_NotFound() throws Exception {
        Result<Employee> notFoundResult = Result.failure("Employee not found", List.of("No employee with ID 1"));
        when(employeeService.getEmployeeById(1L)).thenReturn(notFoundResult);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    @Test
    @WithMockUser
    void createEmployee_Success() throws Exception {
        Result<Department> departmentResult = Result.success(department, "Department found");
        Result<Roles> roleResult = Result.success(role, "Role found");
        Result<UserEntity> userResult = Result.success(userEntity, "User found");

        when(departmentService.getDepartmentById(1L)).thenReturn(departmentResult);
        when(roleService.getRoleById(1)).thenReturn(roleResult);
        when(userEntityService.getUserByUsername("john.doe")).thenReturn(userResult);
        when(employeeService.saveOrUpdateEmployee(any(Employee.class))).thenReturn(successResult);

        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser
    void createEmployee_NullDepartment() throws Exception {
        employee.setDepartment(null);

        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Department ID is required"));
    }

    @Test
    @WithMockUser
    void createEmployee_DepartmentNotFound() throws Exception {
        when(departmentService.getDepartmentById(anyLong())).thenReturn(null);

        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Department with ID 1 not found"));
    }

    @Test
    @WithMockUser
    void createEmployee_ValidationFailure() throws Exception {
        Result<Department> departmentResult = Result.success(department, "Department found");
        when(departmentService.getDepartmentById(1L)).thenReturn(departmentResult);
        when(employeeService.saveOrUpdateEmployee(any(Employee.class)))
                .thenReturn(Result.failure("Validation failed", List.of("Invalid email format")));

        mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Validation failed"));
    }

    @Test
    @WithMockUser
    void updateEmployee_Success() throws Exception {
        when(employeeService.saveOrUpdateEmployee(any(Employee.class))).thenReturn(successResult);

        mockMvc.perform(put("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    void updateEmployee_IdMismatch() throws Exception {
        mockMvc.perform(put("/api/employees/2")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Employee ID in the path does not match the ID in the request body."));
    }

    @Test
    @WithMockUser
    void updateEmployee_Failure() throws Exception {
        when(employeeService.saveOrUpdateEmployee(any(Employee.class))).thenReturn(failureResult);

        mockMvc.perform(put("/api/employees/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    void deleteEmployee_Success() throws Exception {
        when(employeeService.deleteEmployeeById(anyLong()))
                .thenReturn(Result.success(null, "Employee deleted successfully"));

        mockMvc.perform(delete("/api/employees/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    void deleteEmployee_NotFound() throws Exception {
        when(employeeService.deleteEmployeeById(anyLong()))
                .thenReturn(Result.failure("Employee not found", List.of("No employee with ID 1")));

        mockMvc.perform(delete("/api/employees/1")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}