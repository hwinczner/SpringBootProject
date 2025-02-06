package com.SpringBoot.Project.Controllers;

import com.SpringBoot.Project.Models.Employee;
import com.SpringBoot.Project.Models.Department;
import com.SpringBoot.Project.Services.DepartmentService;
import com.SpringBoot.Project.Services.EmployeeService;
import com.SpringBoot.Project.Models.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentService departmentService;

    // GET all employees
    @Operation(
            summary = "Get all employees",
            description = "Retrieves a list of all employees in the system"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved employees",
            content = @Content(schema = @Schema(implementation = Employee.class))
    )
    @GetMapping
    public Result<List<Employee>> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    // GET employee by ID
    @Operation(
            summary = "Get employee by ID",
            description = "Retrieves a specific employee by their ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee found",
                    content = @Content(schema = @Schema(implementation = Employee.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Result<Employee>> getEmployeeById(@PathVariable Long id) {
        Result<Employee> result = employeeService.getEmployeeById(id);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }

    // POST create a new employee
    @Operation(
            summary = "Create a new employee",
            description = "Creates a new employee with the provided details"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Employee created successfully",
                    content = @Content(schema = @Schema(implementation = Employee.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid employee data or department not found"
            )
    })
    @PostMapping
    public ResponseEntity<?> createEmployee(
            @Parameter(description = "Employee details to create", required = true)
            @Valid @RequestBody Employee employee
    ) {
        if (employee.getDepartment() == null) {
            return new ResponseEntity<>("Department ID is required", HttpStatus.BAD_REQUEST);
        }

        long departmentId = employee.getDepartment().getDepartmentId();
        Result<Department> department = departmentService.getDepartmentById(departmentId);

        if (department == null) {
            return new ResponseEntity<>("Department with ID " + departmentId + " not found", HttpStatus.BAD_REQUEST);
        }

        employee.setDepartment(department.getData());

        Result<Employee> result = employeeService.saveOrUpdateEmployee(employee);

        if (result.isSuccess()) {
            return new ResponseEntity<>(result.getData(), HttpStatus.CREATED);
        }

        return new ResponseEntity<>(result.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // PUT update an employee
    @Operation(
            summary = "Update an existing employee",
            description = "Updates an employee's details using their employee ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee updated successfully",
                    content = @Content(schema = @Schema(implementation = Result.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid employee ID or request body"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during employee update"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<Result<Employee>> updateEmployee(
            @Parameter(description = "Employee ID to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated employee details", required = true)
            @RequestBody Employee employee
    ) {
        // Check if employee ID in the path matches the ID in the request body
        if (!id.equals(employee.getEmployeeId())) {
            return ResponseEntity.badRequest().body(Result.failure("Employee ID in the path does not match the ID in the request body.", List.of("ID mismatch")));
        }

        Result<Employee> result = employeeService.saveOrUpdateEmployee(employee);

        // Return response based on result success
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }


    // DELETE employee by ID
    @Operation(
            summary = "Delete an employee by ID",
            description = "Removes an employee from the system using their unique identifier"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee successfully deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee not found or could not be deleted"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteEmployee(
            @Parameter(description = "Unique identifier of the employee to delete", required = true)
            @PathVariable Long id
    ) {

        Result<Void> result = employeeService.deleteEmployeeById(id);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }


}
