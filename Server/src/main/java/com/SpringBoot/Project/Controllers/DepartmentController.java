package com.SpringBoot.Project.Controllers;

import com.SpringBoot.Project.Models.Department;
import com.SpringBoot.Project.Services.DepartmentService;
import com.SpringBoot.Project.Models.Result;
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
    @GetMapping
    public Result<List<Department>> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    // GET department by ID
    @GetMapping("/{id}")
    public Result<Department> getDepartmentById(@PathVariable Long id) {
        return departmentService.getDepartmentById(id);
    }

    // POST create a new department
    @PostMapping
    public Department createDepartment(@RequestBody Department department) {
        return null;
    }

    // PUT update a department
    @PutMapping("/{id}")
    public ResponseEntity<Result<Department>> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        // Check if department ID in the path matches the ID in the request body
        if (!id.equals(department.getDepartmentId())) {
            return ResponseEntity.badRequest().body(Result.failure("Department ID in the path does not match the ID in the request body.", List.of("ID mismatch")));
        }

        // Call service to save or update the department
        Result<Department> result = departmentService.saveOrUpdateDepartment(department);

        // Return response based on result success
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    // DELETE remove a department
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteDepartment(@PathVariable Long id) {
        // Call the service to delete the department
        Result<Void> result = departmentService.deleteDepartmentById(id);

        // If the department was successfully deleted
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);  // Return 200 OK if successful
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);  // Return 404 if department not found
        }
    }

}
