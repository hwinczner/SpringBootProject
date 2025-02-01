package com.SpringBoot.Project.Services;

import com.SpringBoot.Project.Models.Employee;
import com.SpringBoot.Project.Models.LeaveRequest;
import com.SpringBoot.Project.Models.Result;
import com.SpringBoot.Project.Repositories.EmployeeInterface;
import com.SpringBoot.Project.Repositories.LeaveRequestInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveRequestService {
    @Autowired
    private LeaveRequestInterface leaveRequestInterface;

    @Autowired
    private EmployeeInterface employeeInterface;

    // Get all leave requests
    public Result<List<LeaveRequest>> getAllLeaveRequests() {
        List<LeaveRequest> leaveRequests = leaveRequestInterface.findAll();
        return Result.success(leaveRequests, "All leave requests retrieved successfully.");
    }

    // Submit a new leave request
    public Result<LeaveRequest> submitLeaveRequest(LeaveRequest leaveRequest, Long employeeId) {
        Optional<Employee> employeeOptional = employeeInterface.findById(employeeId);

        if (employeeOptional.isEmpty()) {
            return Result.failure("Employee not found", List.of("No employee found with ID: " + employeeId));
        }

        Employee employee = employeeOptional.get();
        leaveRequest.setEmployee(employee);

        LeaveRequest savedLeaveRequest = leaveRequestInterface.save(leaveRequest);

        return Result.success(savedLeaveRequest, "Leave request submitted successfully!");
    }

    // Update a leave request (approve/reject)
    public Result<LeaveRequest> updateLeaveRequest(Long id, LeaveRequest updatedRequest) {
        Optional<LeaveRequest> existingRequest = leaveRequestInterface.findById(id);
        if (existingRequest.isPresent()) {
            LeaveRequest request = existingRequest.get();
            request.setStatus(updatedRequest.getStatus());
            leaveRequestInterface.save(request);
            return Result.success(request, "Leave request updated successfully.");
        } else {
            return Result.failure("Leave request not found.", List.of("No leave request found with id: " + id));
        }
    }

    // Delete a leave request
    public Result<Void> deleteLeaveRequest(Long id) {
        if (leaveRequestInterface.existsById(id)) {
            leaveRequestInterface.deleteById(id);
            return Result.success(null, "Leave request deleted successfully.");
        } else {
            return Result.failure("Leave request not found.", List.of("No leave request found with id: " + id));
        }
    }
}
