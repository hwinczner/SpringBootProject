package com.SpringBoot.Project.ServiceTests;

import com.SpringBoot.Project.Models.*;
import com.SpringBoot.Project.Services.*;
import com.SpringBoot.Project.Repositories.EmployeeInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeInterface employeeInterface;

    @Mock
    private DepartmentService departmentService;

    @Mock
    private RoleService roleService;

    @Mock
    private UserEntityService userEntityService;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private Department department;
    private Roles role;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        department = new Department("IT", "Information Technology");
        try {
            var field = Department.class.getDeclaredField("departmentId");
            field.setAccessible(true);
            field.set(department, 1L);
        } catch (Exception e) {
            fail("Failed to set department ID");
        }

        role = new Roles("ROLE_EMPLOYEE");
        try {
            var roleField = Roles.class.getDeclaredField("id");
            roleField.setAccessible(true);
            roleField.set(role, 1);
        } catch (Exception e) {
            fail("Failed to set role ID");
        }

        userEntity = new UserEntity();
        userEntity.setUsername("john.doe");
        try {
            var userField = UserEntity.class.getDeclaredField("id");
            userField.setAccessible(true);
            userField.set(userEntity, 1);
        } catch (Exception e) {
            fail("Failed to set user ID");
        }

        employee = new Employee("John Doe", "john.doe@example.com", department, role, userEntity);
    }

    @Test
    void getAllEmployees_Success() {
        List<Employee> employeeList = List.of(employee);
        when(employeeInterface.findAll()).thenReturn(employeeList);

        Result<List<Employee>> result = employeeService.getAllEmployees();

        assertTrue(result.isSuccess());
        assertEquals(employeeList, result.getData());
        assertEquals("Employees fetched successfully.", result.getMessage());
    }

    @Test
    void getAllEmployees_EmptyList() {
        when(employeeInterface.findAll()).thenReturn(new ArrayList<>());

        Result<List<Employee>> result = employeeService.getAllEmployees();

        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
        assertEquals("Employees fetched successfully.", result.getMessage());
    }

    @Test
    void getEmployeeById_Success() {
        when(employeeInterface.findById(1L)).thenReturn(Optional.of(employee));

        Result<Employee> result = employeeService.getEmployeeById(1L);

        assertTrue(result.isSuccess());
        assertEquals(employee, result.getData());
        assertEquals("Employee found!", result.getMessage());
    }

    @Test
    void getEmployeeById_NotFound() {
        when(employeeInterface.findById(1L)).thenReturn(Optional.empty());

        Result<Employee> result = employeeService.getEmployeeById(1L);

        assertFalse(result.isSuccess());
        assertNull(result.getData());
        assertEquals("Employee not found.", result.getMessage());
        assertEquals(1, result.getErrors().size());
        assertEquals("No employees found with id of: 1", result.getErrors().get(0));
    }

    @Test
    void getEmployeeByDepartment_Success() {
        List<Employee> employeeList = List.of(employee);
        when(employeeInterface.findAllByDepartment(department)).thenReturn(employeeList);

        Result<List<Employee>> result = employeeService.getEmployeeByDepartment(department);

        assertTrue(result.isSuccess());
        assertEquals(employeeList, result.getData());
        assertEquals("Employees fetched successfully for department: " + department.getName(), result.getMessage());
    }

    @Test
    void getEmployeeByDepartment_Empty() {
        when(employeeInterface.findAllByDepartment(department)).thenReturn(new ArrayList<>());

        Result<List<Employee>> result = employeeService.getEmployeeByDepartment(department);

        assertFalse(result.isSuccess());
        assertEquals("No employees found in this department" + department.getName(), result.getMessage());
    }

    @Test
    void saveOrUpdateEmployee_Success() {
        when(departmentService.getDepartmentById(1L)).thenReturn(Result.success(department, "Department found"));
        when(roleService.getRoleById(1)).thenReturn(Result.success(role, "Role found"));
        when(userEntityService.getUserByUsername("john.doe")).thenReturn(Result.success(userEntity, "User found"));
        when(employeeInterface.save(any(Employee.class))).thenReturn(employee);

        Result<Employee> result = employeeService.saveOrUpdateEmployee(employee);

        assertTrue(result.isSuccess());
        assertEquals("Employee saved successfully!", result.getMessage());
        assertEquals(employee, result.getData());
    }

    @Test
    void saveOrUpdateEmployee_RoleNotFound() {
        when(departmentService.getDepartmentById(1L)).thenReturn(Result.success(department, "Department found"));
        when(roleService.getRoleById(1)).thenReturn(Result.failure("Role not found", List.of("No role found")));

        Result<Employee> result = employeeService.saveOrUpdateEmployee(employee);

        assertFalse(result.isSuccess());
        assertEquals("Failed to find role", result.getMessage());
    }

    @Test
    void saveOrUpdateEmployee_UserNotFound() {
        when(departmentService.getDepartmentById(1L)).thenReturn(Result.success(department, "Department found"));
        when(roleService.getRoleById(1)).thenReturn(Result.success(role, "Role found"));
        when(userEntityService.getUserByUsername("john.doe")).thenReturn(Result.failure("User not found", List.of("No user found")));

        Result<Employee> result = employeeService.saveOrUpdateEmployee(employee);

        assertFalse(result.isSuccess());
        assertEquals("Failed to find username", result.getMessage());
    }

    @Test
    void saveOrUpdateEmployee_NullEmployee() {
        Result<Employee> result = employeeService.saveOrUpdateEmployee(null);

        assertFalse(result.isSuccess());
        assertEquals("Invalid input", result.getMessage());
        assertEquals("Employee and department must not be null", result.getErrors().get(0));
    }

    @Test
    void saveOrUpdateEmployee_NullDepartment() {
        employee.setDepartment(null);
        Result<Employee> result = employeeService.saveOrUpdateEmployee(employee);

        assertFalse(result.isSuccess());
        assertEquals("Invalid input", result.getMessage());
        assertEquals("Employee and department must not be null", result.getErrors().get(0));
    }

    @Test
    void saveOrUpdateEmployee_DepartmentNotFound() {
        when(departmentService.getDepartmentById(1L))
                .thenReturn(Result.failure("Department not found", List.of("No department found")));

        Result<Employee> result = employeeService.saveOrUpdateEmployee(employee);

        assertFalse(result.isSuccess());
        assertEquals("Failed to find department", result.getMessage());
    }

    @Test
    void saveOrUpdateEmployee_SaveError() {
        when(departmentService.getDepartmentById(1L)).thenReturn(Result.success(department, "Department found"));
        when(roleService.getRoleById(1)).thenReturn(Result.success(role, "Role found"));
        when(userEntityService.getUserByUsername("john.doe")).thenReturn(Result.success(userEntity, "User found"));
        when(employeeInterface.save(any(Employee.class))).thenThrow(new RuntimeException("Database error"));

        Result<Employee> result = employeeService.saveOrUpdateEmployee(employee);

        assertFalse(result.isSuccess());
        assertEquals("Failed to save employee", result.getMessage());
        assertEquals("Database error", result.getErrors().get(0));
    }

    @Test
    void deleteEmployeeById_Success() {
        when(employeeInterface.existsById(1L)).thenReturn(true);
        doNothing().when(employeeInterface).deleteById(1L);

        Result<Void> result = employeeService.deleteEmployeeById(1L);

        assertTrue(result.isSuccess());
        assertEquals("Employee was deleted!", result.getMessage());
        verify(employeeInterface).deleteById(1L);
    }

    @Test
    void deleteEmployeeById_NotFound() {
        when(employeeInterface.existsById(1L)).thenReturn(false);

        Result<Void> result = employeeService.deleteEmployeeById(1L);

        assertFalse(result.isSuccess());
        assertEquals("Employee not found.", result.getMessage());
        assertEquals("No employees found with id of: 1", result.getErrors().get(0));
        verify(employeeInterface, never()).deleteById(anyLong());
    }
}