package com.SpringBoot.Project.ControllerTests;

import com.SpringBoot.Project.Controllers.LeaveRequestController;
import com.SpringBoot.Project.Models.*;
import com.SpringBoot.Project.Services.LeaveRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeaveRequestController.class)
class LeaveRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaveRequestService leaveRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    private LeaveRequest leaveRequest;
    private Result<LeaveRequest> successResult;

    @BeforeEach
    void setUp() {
        //Setup Department
        Department department = new Department("IT", "Information Technology");
        try {
            var deptField = Department.class.getDeclaredField("departmentId");
            deptField.setAccessible(true);
            deptField.set(department, 1L);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set department ID", e);
        }

        // Setup Employee
        Employee employee = new Employee("John Doe", "john.doe@example.com", department, "Developer");
        try {
            var empField = Employee.class.getDeclaredField("employeeId");
            empField.setAccessible(true);
            empField.set(employee, 1L);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set employee ID", e);
        }

        // Setup LeaveRequest
        leaveRequest = new LeaveRequest(
                employee,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5),
                Status.PENDING,
                "Vacation",
                null
        );

        successResult = Result.success(leaveRequest, "Operation successful");
        Result<LeaveRequest> failureResult = Result.failure("Operation failed", List.of("Error message"));
    }

    @Test
    void getAllLeaveRequests_Success() throws Exception {
        List<LeaveRequest> leaveRequests = Arrays.asList(leaveRequest);
        Result<List<LeaveRequest>> result = Result.success(leaveRequests, "Leave requests fetched successfully");

        when(leaveRequestService.getAllLeaveRequests()).thenReturn(result);

        mockMvc.perform(get("/api/leaves"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].reason").value("Vacation"))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    void submitLeaveRequest_Success() throws Exception {
        when(leaveRequestService.submitLeaveRequest(any(LeaveRequest.class), anyLong()))
                .thenReturn(successResult);

        mockMvc.perform(post("/api/leaves")
                        .param("employeeId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leaveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void submitLeaveRequest_ValidationFailure() throws Exception {
        Result<LeaveRequest> validationFailure = Result.failure(
                "Invalid date range",  // Changed to match our new error message pattern
                List.of("Start date must be before end date")
        );

        when(leaveRequestService.submitLeaveRequest(any(LeaveRequest.class), anyLong()))
                .thenReturn(validationFailure);

        mockMvc.perform(post("/api/leaves")
                        .param("employeeId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leaveRequest)))
                .andExpect(status().isBadRequest())  // Changed from isOk() to isBadRequest()
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0]").value("Start date must be before end date"));
    }

    @Test
    void updateLeaveRequest_Success() throws Exception {
        leaveRequest.setStatus(Status.APPROVED);
        Result<LeaveRequest> approvedResult = Result.success(leaveRequest, "Leave request approved");

        when(leaveRequestService.updateLeaveRequest(anyLong(), any(LeaveRequest.class)))
                .thenReturn(approvedResult);

        mockMvc.perform(put("/api/leaves/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leaveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    void updateLeaveRequest_NotFound() throws Exception {
        Result<LeaveRequest> notFoundResult = Result.failure(
                "Leave request not found",
                List.of("No leave request found with ID 1")
        );

        when(leaveRequestService.updateLeaveRequest(anyLong(), any(LeaveRequest.class)))
                .thenReturn(notFoundResult);

        mockMvc.perform(put("/api/leaves/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leaveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Leave request not found"));
    }

    @Test
    void deleteLeaveRequest_Success() throws Exception {
        Result<Void> deleteResult = Result.success(null, "Leave request deleted successfully");

        when(leaveRequestService.deleteLeaveRequest(anyLong())).thenReturn(deleteResult);

        mockMvc.perform(delete("/api/leaves/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Leave request deleted successfully"));
    }

    @Test
    void deleteLeaveRequest_NotFound() throws Exception {
        Result<Void> notFoundResult = Result.failure(
                "Leave request not found",
                List.of("No leave request found with ID 1")
        );

        when(leaveRequestService.deleteLeaveRequest(anyLong())).thenReturn(notFoundResult);

        mockMvc.perform(delete("/api/leaves/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Leave request not found"));
    }
}