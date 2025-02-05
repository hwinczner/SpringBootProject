package com.SpringBoot.Project.Controllers;

import com.SpringBoot.Project.Models.LeaveRequest;
import com.SpringBoot.Project.Models.Result;
import com.SpringBoot.Project.Services.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PostMapping
    public ResponseEntity<Result<LeaveRequest>> submitLeaveRequest(
            @RequestBody LeaveRequest leaveRequest,
            @RequestParam Long employeeId
    ) {
        Result<LeaveRequest> result = leaveRequestService.submitLeaveRequest(leaveRequest, employeeId);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else if (result.getMessage().startsWith("Invalid")) {
            // This catches both "Invalid dates" and "Invalid date range"
            return ResponseEntity.badRequest().body(result);
        } else if (result.getMessage().contains("Employee not found")) {
            return ResponseEntity.notFound().build();
        } else if (result.getMessage().contains("Overlapping leave request")) {
            return ResponseEntity.badRequest().body(result);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    // Update a leave request status (Manager only)
    @PutMapping("/{id}")
    public ResponseEntity<Result<LeaveRequest>> updateLeaveRequest(@PathVariable Long id, @RequestBody LeaveRequest updatedRequest) {
        return ResponseEntity.ok(leaveRequestService.updateLeaveRequest(id, updatedRequest));
    }

    // Delete a leave request (Admin only)
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteLeaveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.deleteLeaveRequest(id));
    }
}
