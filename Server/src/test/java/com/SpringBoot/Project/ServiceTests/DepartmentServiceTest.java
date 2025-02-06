package com.SpringBoot.Project.ServiceTests;

import com.SpringBoot.Project.Models.*;
import com.SpringBoot.Project.Repositories.DepartmentInterface;
import com.SpringBoot.Project.Repositories.EmployeeInterface;
import com.SpringBoot.Project.Services.DepartmentService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentInterface departmentInterface;

    @Mock
    private EmployeeInterface employeeInterface;

    @InjectMocks
    private DepartmentService departmentService;

    private Department department;
    private Employee employee;
    private Roles role;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        department = new Department("IT", "Information Technology Department");

        role = new Roles("ROLE_EMPLOYEE");
        try {
            var roleField = Roles.class.getDeclaredField("id");
            roleField.setAccessible(true);
            roleField.set(role, 1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set role ID", e);
        }

        userEntity = new UserEntity();
        userEntity.setUsername("john.doe");
        try {
            var userField = UserEntity.class.getDeclaredField("id");
            userField.setAccessible(true);
            userField.set(userEntity, 1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user ID", e);
        }

        employee = new Employee("John Doe", "john.doe@example.com", department, role, userEntity);
    }

    // Most test methods remain unchanged as they don't involve Employee...

    @Test
    void deleteDepartmentById_HasEmployees() {
        Department department = new Department("IT", "Information Technology");
        Employee employee = new Employee("John Doe", "john@example.com", department, role, userEntity);

        when(departmentInterface.findById(1L)).thenReturn(Optional.of(department));
        when(employeeInterface.findAllByDepartment(department)).thenReturn(List.of(employee));

        Result<Void> result = departmentService.deleteDepartmentById(1L);

        assertFalse(result.isSuccess());
        assertNull(result.getData());
        assertEquals("Cannot delete department with existing employees", result.getMessage());
        assertTrue(result.getErrors().get(0).contains("still has 1 employees"));
        verify(departmentInterface, never()).deleteById(anyLong());
    }

    @Test
    void getAllDepartments_Success() {
        List<Department> departmentList = List.of(department);
        when(departmentInterface.findAll()).thenReturn(departmentList);

        Result<List<Department>> result = departmentService.getAllDepartments();

        assertTrue(result.isSuccess());
        assertEquals(departmentList, result.getData());
        assertEquals("Departments fetched successfully.", result.getMessage());
    }

    @Test
    void getAllDepartments_EmptyList() {
        when(departmentInterface.findAll()).thenReturn(new ArrayList<>());

        Result<List<Department>> result = departmentService.getAllDepartments();

        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
        assertEquals("Departments fetched successfully.", result.getMessage());
    }

    @Test
    void getDepartmentById_Success() {
        when(departmentInterface.findById(1L)).thenReturn(Optional.of(department));

        Result<Department> result = departmentService.getDepartmentById(1L);

        assertTrue(result.isSuccess());
        assertEquals(department, result.getData());
        assertEquals("Department fetched successfully.", result.getMessage());
    }

    @Test
    void getDepartmentById_NotFound() {
        when(departmentInterface.findById(1L)).thenReturn(Optional.empty());

        Result<Department> result = departmentService.getDepartmentById(1L);

        assertFalse(result.isSuccess());
        assertNull(result.getData());
        assertEquals("Department not found.", result.getMessage());
        assertEquals("No departments found with id of 1", result.getErrors().get(0));
    }

    @Test
    void saveOrUpdateDepartment_Success() {
        when(departmentInterface.save(any(Department.class))).thenReturn(department);

        Result<Department> result = departmentService.saveOrUpdateDepartment(department);

        assertTrue(result.isSuccess());
        assertEquals(department, result.getData());
        assertEquals("Department has been saved.", result.getMessage());
    }

    @Test
    void saveOrUpdateDepartment_Exception() {
        when(departmentInterface.save(any(Department.class)))
                .thenThrow(new RuntimeException("Database error"));

        Result<Department> result = departmentService.saveOrUpdateDepartment(department);

        assertFalse(result.isSuccess());
        assertNull(result.getData());
        assertEquals("Department could not be saved.", result.getMessage());
        assertEquals("Database error", result.getErrors().get(0));
    }

    @Test
    void deleteDepartmentById_Success() {
        Department department = new Department("IT", "Information Technology");
        when(departmentInterface.findById(1L)).thenReturn(Optional.of(department));
        when(employeeInterface.findAllByDepartment(department)).thenReturn(new ArrayList<>());
        doNothing().when(departmentInterface).deleteById(1L);

        Result<Void> result = departmentService.deleteDepartmentById(1L);

        assertTrue(result.isSuccess());
        assertNull(result.getData());
        assertEquals("Department was deleted", result.getMessage());
        verify(departmentInterface).deleteById(1L);
    }

    @Test
    void deleteDepartmentById_NotFound() {
        when(departmentInterface.findById(1L)).thenReturn(Optional.empty());

        Result<Void> result = departmentService.deleteDepartmentById(1L);

        assertFalse(result.isSuccess());
        assertNull(result.getData());
        assertEquals("Department not found", result.getMessage());
        assertEquals("No departments found with id of 1", result.getErrors().get(0));
        verify(departmentInterface, never()).deleteById(anyLong());
    }
}