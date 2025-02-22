package com.SpringBoot.Project.Controllers;

import com.SpringBoot.Project.Models.LeaveRequest;
import com.SpringBoot.Project.Models.Result;
import com.SpringBoot.Project.Services.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@Tag(name = "Leave Requests", description = "Endpoints for managing leave requests")
public class LeaveRequestController {
    private static final Logger logger = LogManager.getLogger(LeaveRequestController.class);

    @Autowired
    private LeaveRequestService leaveRequestService;

    // Get all leave requests (Admin and Managers only)
    @Operation(
            summary = "Get all leave requests",
            description = "Retrieves a list of all leave requests (Admin and Managers only)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved leave requests",
                    content = @Content(schema = @Schema(implementation = Result.class))
            )
    })
    @GetMapping
    public ResponseEntity<Result<List<LeaveRequest>>> getAllLeaveRequests() {
        logger.info("Fetching all leave requests");
        Result<List<LeaveRequest>> result = leaveRequestService.getAllLeaveRequests();
        logger.info("Retrieved {} leave requests", result.getData().size());
        return ResponseEntity.ok(result);
    }

    // Submit a new leave request (Employee only)
    @Operation(
            summary = "Submit a new leave request",
            description = "Submits a leave request for a specific employee"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Leave request submitted successfully",
                    content = @Content(schema = @Schema(implementation = Result.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid leave request details",
                    content = @Content(schema = @Schema(implementation = Result.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @PostMapping("submit")
    public ResponseEntity<Result<LeaveRequest>> submitLeaveRequest(
            @Parameter(description = "Leave request details", required = true)
            @RequestBody LeaveRequest leaveRequest,
            @Parameter(description = "ID of the employee submitting the leave request", required = true)
            @RequestParam Long employeeId
    ) {
        logger.info("Processing leave request submission for employee ID: {}", employeeId);
        logger.debug("Leave request details - Start Date: {}, End Date: {}, Reason: {}",
                leaveRequest.getStartDate(), leaveRequest.getEndDate(), leaveRequest.getReason());
        Result<LeaveRequest> result = leaveRequestService.submitLeaveRequest(leaveRequest, employeeId);

        if (result.isSuccess()) {
            logger.info("Successfully submitted leave request for employee ID: {}", employeeId);
        } else {
            logger.warn("Failed to submit leave request for employee ID: {}. Reason: {}",
                    employeeId, result.getMessage());
        }

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else if (result.getMessage().startsWith("Invalid")) {
            // This catches both "Invalid dates" and "Invalid date range"
            logger.warn("Invalid leave request submitted - {}", result.getMessage());
            return ResponseEntity.badRequest().body(result);
        } else if (result.getMessage().contains("Employee not found")) {
            logger.warn("Leave request submission failed - Employee not found with ID: {}", employeeId);
            return ResponseEntity.notFound().build();
        } else if (result.getMessage().contains("Overlapping leave request")) {
            logger.warn("Leave request submission failed - Overlapping request for employee ID: {}", employeeId);
            return ResponseEntity.badRequest().body(result);
        } else {
            logger.error("Unexpected error processing leave request for employee ID: {}", employeeId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    // Update a leave request status (Manager only)
    @Operation(
            summary = "Update a leave request status",
            description = "Updates the status of an existing leave request (Manager only)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Leave request updated successfully",
                    content = @Content(schema = @Schema(implementation = Result.class))
            )
    })
    @PutMapping("update/{id}")
    public ResponseEntity<Result<LeaveRequest>> updateLeaveRequest(
            @Parameter(description = "ID of the leave request to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated leave request details", required = true)
            @RequestBody LeaveRequest updatedRequest
    ) {
        logger.info("Processing leave request update for request ID: {}", id);
        logger.debug("Updated status: {}", updatedRequest.getStatus());

        Result<LeaveRequest> result = leaveRequestService.updateLeaveRequest(id, updatedRequest);

        if (result.isSuccess()) {
            logger.info("Successfully updated leave request ID: {} to status: {}",
                    id, updatedRequest.getStatus());
        } else {
            logger.warn("Failed to update leave request ID: {}. Reason: {}", id, result.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    // Delete a leave request (Admin only)
    @Operation(
            summary = "Delete a leave request",
            description = "Deletes a leave request by its ID (Admin only)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Leave request deleted successfully"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteLeaveRequest(
            @Parameter(description = "ID of the leave request to delete", required = true)
            @PathVariable Long id
    ) {
        logger.info("Processing delete request for leave request ID: {}", id);

        Result<Void> result = leaveRequestService.deleteLeaveRequest(id);

        if (result.isSuccess()) {
            logger.info("Successfully deleted leave request ID: {}", id);
        } else {
            logger.warn("Failed to delete leave request ID: {}. Reason: {}", id, result.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}
