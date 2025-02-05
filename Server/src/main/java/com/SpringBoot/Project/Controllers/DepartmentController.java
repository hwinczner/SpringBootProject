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

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

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
        return departmentService.getAllDepartments();
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
        return departmentService.getDepartmentById(id);
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
        Result<Department> result = departmentService.saveOrUpdateDepartment(department);
        if(result.isSuccess()) {
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        }
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
        if (!id.equals(department.getDepartmentId())) {
            return ResponseEntity.badRequest().body(
                    Result.failure("Department ID in the path does not match the ID in the request body.",
                            List.of("ID mismatch"))
            );
        }

        Result<Department> result = departmentService.saveOrUpdateDepartment(department);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
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
        Result<Void> result = departmentService.deleteDepartmentById(id);

        if (result.getMessage().contains("Cannot delete department with existing employees")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }

}
