package com.SpringBoot.Project.Controllers;

import com.SpringBoot.Project.Models.Department;
import com.SpringBoot.Project.Services.DepartmentService;
import com.SpringBoot.Project.Models.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    private static final Logger logger = LogManager.getLogger(DepartmentController.class);

    @Autowired
    private DepartmentService departmentService;

    // GET all departments
    @Operation(
            summary = "Get all departments",
            description = "Retrieves a list of all departments in the system"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved departments",
            content = @Content(schema = @Schema(implementation = Department.class))
    )
    @GetMapping
    public Result<List<Department>> getAllDepartments() {
        logger.info("Fetching all departments");
        Result<List<Department>> result = departmentService.getAllDepartments();
        logger.info("Retrieved {} departments", result.getData().size());
        return result;
    }

    // GET department by ID
    @Operation(
            summary = "Get department by ID",
            description = "Retrieves a specific department by its ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Department found",
                    content = @Content(schema = @Schema(implementation = Department.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department not found"
            )
    })
    @GetMapping("/{id}")
    public Result<Department> getDepartmentById(@PathVariable Long id) {
        logger.info("Fetching department with ID: {}", id);
        Result<Department> result = departmentService.getDepartmentById(id);

        if (result.isSuccess()) {
            logger.info("Successfully retrieved department: {}", result.getData().getName());
        } else {
            logger.warn("Failed to find department with ID: {}", id);
        }
        return result;
    }

    // POST create a new department
    @Operation(
            summary = "Create a new department",
            description = "Creates a new department in the system"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Department created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid department data provided"
            )
    })
    @PostMapping
    public ResponseEntity<Result<Department>> createDepartment(@Valid @RequestBody Department department) {
        logger.info("Creating new department: {}", department.getName());

        Result<Department> result = departmentService.saveOrUpdateDepartment(department);
        if (result.isSuccess()) {
            logger.info("Successfully created department: {}", department.getName());
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        }
        logger.error("Failed to create department: {}. Reason: {}",
                department.getName(), result.getMessage());
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // PUT update a department
    @Operation(
            summary = "Update a department",
            description = "Updates an existing department's information"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Department updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid department data provided"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department not found"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<Result<Department>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody Department department
    ) {
        logger.info("Updating department with ID: {}", id);

        if (!id.equals(department.getDepartmentId())) {
            logger.warn("Department ID mismatch. Path ID: {}, Body ID: {}",
                    id, department.getDepartmentId());
            return ResponseEntity.badRequest().body(
                    Result.failure("Department ID in the path does not match the ID in the request body.",
                            List.of("ID mismatch"))
            );
        }

        Result<Department> result = departmentService.saveOrUpdateDepartment(department);
        if (result.isSuccess()) {
            logger.info("Successfully updated department: {}", department.getName());
            return ResponseEntity.ok(result);
        } else {
            logger.error("Failed to update department: {}. Reason: {}",
                    department.getName(), result.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    // DELETE remove a department
    @Operation(
            summary = "Delete a department",
            description = "Deletes a department from the system"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Department deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Cannot delete department with existing employees"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteDepartment(@PathVariable Long id) {
        logger.info("Processing delete request for department ID: {}", id);

        Result<Void> result = departmentService.deleteDepartmentById(id);

        if (result.getMessage().contains("Cannot delete department with existing employees")) {
            logger.warn("Cannot delete department ID: {} - Department has existing employees", id);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }

        if (result.isSuccess()) {
            logger.info("Successfully deleted department ID: {}", id);
            return ResponseEntity.ok(result);
        } else {
            logger.warn("Failed to delete department ID: {}. Reason: {}", id, result.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }
}
