package com.SpringBoot.Project.ControllerTests;

import com.SpringBoot.Project.Controllers.DepartmentController;
import com.SpringBoot.Project.Models.Department;
import com.SpringBoot.Project.Models.Result;
import com.SpringBoot.Project.Services.DepartmentService;
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

@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private Department department;
    private Result<Department> successResult;
    private Result<Department> failureResult;

    @BeforeEach
    void setUp() {
        department = new Department("IT", "Information Technology");
        try {
            var field = Department.class.getDeclaredField("departmentId");
            field.setAccessible(true);
            field.set(department, 1L);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set department ID", e);
        }

        successResult = Result.success(department, "Operation successful");
        failureResult = Result.failure("Operation failed", List.of("Error message"));
    }

    @Test
    void getAllDepartments_Success() throws Exception {
        List<Department> departments = Arrays.asList(department);
        Result<List<Department>> result = Result.success(departments, "Departments fetched successfully");

        when(departmentService.getAllDepartments()).thenReturn(result);

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("IT"))
                .andExpect(jsonPath("$.message").value("Departments fetched successfully"));
    }

    @Test
    void getDepartmentById_Success() throws Exception {
        when(departmentService.getDepartmentById(1L)).thenReturn(successResult);

        mockMvc.perform(get("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("IT"));
    }

    @Test
    void getDepartmentById_NotFound() throws Exception {
        when(departmentService.getDepartmentById(1L))
                .thenReturn(Result.failure("Department not found", List.of("No department with ID 1")));

        mockMvc.perform(get("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Department not found"));
    }

    @Test
    void createDepartment_Success() throws Exception {
        when(departmentService.saveOrUpdateDepartment(any(Department.class))).thenReturn(successResult);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(department)))
                .andExpect(status().isCreated());
    }

    @Test
    void createDepartment_Failure() throws Exception {
        when(departmentService.saveOrUpdateDepartment(any(Department.class))).thenReturn(failureResult);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(department)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateDepartment_Success() throws Exception {
        when(departmentService.saveOrUpdateDepartment(any(Department.class))).thenReturn(successResult);

        mockMvc.perform(put("/api/departments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(department)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateDepartment_IdMismatch() throws Exception {
        mockMvc.perform(put("/api/departments/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(department)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Department ID in the path does not match the ID in the request body."));
    }

    @Test
    void updateDepartment_Failure() throws Exception {
        when(departmentService.saveOrUpdateDepartment(any(Department.class))).thenReturn(failureResult);

        mockMvc.perform(put("/api/departments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(department)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteDepartment_Success() throws Exception {
        when(departmentService.deleteDepartmentById(anyLong()))
                .thenReturn(Result.success(null, "Department deleted successfully"));

        mockMvc.perform(delete("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteDepartment_NotFound() throws Exception {
        when(departmentService.deleteDepartmentById(anyLong()))
                .thenReturn(Result.failure("Department not found", List.of("No department with ID 1")));

        mockMvc.perform(delete("/api/departments/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}