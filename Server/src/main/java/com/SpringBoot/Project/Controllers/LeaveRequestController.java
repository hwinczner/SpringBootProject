package com.SpringBoot.Project.Controllers;

import com.SpringBoot.Project.Models.LeaveRequest;
import com.SpringBoot.Project.Models.Result;
import com.SpringBoot.Project.Repositories.LeaveRequestInterface;
import com.SpringBoot.Project.Services.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/leaves")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    // Get all leave requests (Admin and Managers only)
    @GetMapping
    public ResponseEntity<Result<List<LeaveRequest>>> getAllLeaveRequests() {
        return ResponseEntity.ok(leaveRequestService.getAllLeaveRequests());
    }

    // Submit a new leave request (Employee only)
    @PostMapping("submit")
    public ResponseEntity<Result<LeaveRequest>> submitLeaveRequest(
            @RequestBody LeaveRequest leaveRequest,
            @RequestParam Long employeeId
    ) {
        Result<LeaveRequest> result = leaveRequestService.submitLeaveRequest(leaveRequest, employeeId);
        return ResponseEntity.ok(result);
    }



    // Update a leave request status (Manager only)
    @PutMapping("update/{id}")
    public ResponseEntity<Result<LeaveRequest>> updateLeaveRequest(@PathVariable Long id, @RequestBody LeaveRequest updatedRequest) {
        return ResponseEntity.ok(leaveRequestService.updateLeaveRequest(id, updatedRequest));
    }

    // Delete a leave request (Admin only)
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteLeaveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.deleteLeaveRequest(id));
    }
}
