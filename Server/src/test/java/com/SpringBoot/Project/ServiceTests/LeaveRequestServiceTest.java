package com.SpringBoot.Project.ServiceTests;

import com.SpringBoot.Project.Models.*;
import com.SpringBoot.Project.Repositories.EmployeeInterface;
import com.SpringBoot.Project.Repositories.LeaveRequestInterface;
import com.SpringBoot.Project.Services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {

    @Mock
    private LeaveRequestInterface leaveRequestInterface;

    @Mock
    private EmployeeInterface employeeInterface;

    @InjectMocks
    private LeaveRequestService leaveRequestService;

    private Employee employee;
    private LeaveRequest leaveRequest;
    private Department department;
    private Roles role;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        // Setup Department
        department = new Department("IT", "Information Technology");
        try {
            var deptField = Department.class.getDeclaredField("departmentId");
            deptField.setAccessible(true);
            deptField.set(department, 1L);
        } catch (Exception e) {
            fail("Failed to set department ID");
        }

        // Setup Role
        role = new Roles("ROLE_EMPLOYEE");
        try {
            var roleField = Roles.class.getDeclaredField("id");
            roleField.setAccessible(true);
            roleField.set(role, 1);
        } catch (Exception e) {
            fail("Failed to set role ID");
        }

        // Setup UserEntity
        userEntity = new UserEntity();
        userEntity.setUsername("john.doe");
        try {
            var userField = UserEntity.class.getDeclaredField("id");
            userField.setAccessible(true);
            userField.set(userEntity, 1);
        } catch (Exception e) {
            fail("Failed to set user ID");
        }

        // Setup Employee
        employee = new Employee("John Doe", "john.doe@example.com", department, role, userEntity);

        // Setup LeaveRequest
        leaveRequest = new LeaveRequest(
                employee,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(10),
                Status.PENDING,
                "Vacation",
                ""
        );
    }

    @Test
    void testGetAllLeaveRequests() {
        when(leaveRequestInterface.findAll()).thenReturn(List.of(leaveRequest));

        Result<List<LeaveRequest>> result = leaveRequestService.getAllLeaveRequests();

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertEquals("All leave requests retrieved successfully.", result.getMessage());
    }

    @Test
    void testSubmitLeaveRequest_Success() {
        when(employeeInterface.findById(anyLong())).thenReturn(Optional.of(employee));
        when(leaveRequestInterface.save(any(LeaveRequest.class))).thenReturn(leaveRequest);
        when(leaveRequestInterface.findOverlappingLeaveRequests(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        Result<LeaveRequest> result = leaveRequestService.submitLeaveRequest(leaveRequest, 1L);

        assertTrue(result.isSuccess());
        assertEquals("Leave request submitted successfully!", result.getMessage());
        assertNotNull(result.getData());
    }

    @Test
    void testSubmitLeaveRequest_Failure_EmployeeNotFound() {
        when(employeeInterface.findById(anyLong())).thenReturn(Optional.empty());

        Result<LeaveRequest> result = leaveRequestService.submitLeaveRequest(leaveRequest, 1L);

        assertFalse(result.isSuccess());
        assertEquals("Employee not found", result.getMessage());
    }

    @Test
    void testUpdateLeaveRequest_Success() {
        when(leaveRequestInterface.findById(anyLong())).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestInterface.save(any(LeaveRequest.class))).thenReturn(leaveRequest);

        leaveRequest.setStatus(Status.APPROVED);
        Result<LeaveRequest> result = leaveRequestService.updateLeaveRequest(1L, leaveRequest);

        assertTrue(result.isSuccess());
        assertEquals(Status.APPROVED, result.getData().getStatus());
        assertEquals("Leave request updated successfully.", result.getMessage());
    }

    @Test
    void testUpdateLeaveRequest_Failure_NotFound() {
        when(leaveRequestInterface.findById(anyLong())).thenReturn(Optional.empty());

        Result<LeaveRequest> result = leaveRequestService.updateLeaveRequest(1L, leaveRequest);

        assertFalse(result.isSuccess());
        assertEquals("Leave request not found.", result.getMessage());
    }

    @Test
    void testDeleteLeaveRequest_Success() {
        when(leaveRequestInterface.existsById(anyLong())).thenReturn(true);
        doNothing().when(leaveRequestInterface).deleteById(anyLong());

        Result<Void> result = leaveRequestService.deleteLeaveRequest(1L);

        assertTrue(result.isSuccess());
        assertEquals("Leave request deleted successfully.", result.getMessage());
    }

    @Test
    void testDeleteLeaveRequest_Failure_NotFound() {
        when(leaveRequestInterface.existsById(anyLong())).thenReturn(false);

        Result<Void> result = leaveRequestService.deleteLeaveRequest(1L);

        assertFalse(result.isSuccess());
        assertEquals("Leave request not found.", result.getMessage());
    }

    @Test
    void testSubmitLeaveRequest_Failure_OverlappingDates() {
        when(employeeInterface.findById(anyLong())).thenReturn(Optional.of(employee));
        when(leaveRequestInterface.findOverlappingLeaveRequests(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(new LeaveRequest())); // Return a non-empty list

        Result<LeaveRequest> result = leaveRequestService.submitLeaveRequest(leaveRequest, 1L);

        assertFalse(result.isSuccess());
        assertEquals("Overlapping leave request", result.getMessage());
        assertTrue(result.getErrors().get(0).contains("already exists"));
    }
}