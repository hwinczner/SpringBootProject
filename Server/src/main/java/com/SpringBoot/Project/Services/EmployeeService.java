package com.SpringBoot.Project.Services;

import com.SpringBoot.Project.Models.*;
import com.SpringBoot.Project.Repositories.EmployeeInterface;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeInterface employeeInterface;
    private final DepartmentService departmentService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    public EmployeeService(EmployeeInterface employeeInterface){
        this.employeeInterface = employeeInterface;
        this.departmentService = departmentService;
    }

    public Result<List<Employee>> getAllEmployees(){
        List<Employee> employees = employeeInterface.findAll();
        return Result.success(employees, "Employees fetched successfully.");
    }

    public Result<Employee> getEmployeeById(long id){
        //For JPA methods, single search returns a type of Optional<T>
        Optional<Employee> employee = employeeInterface.findById(id);

        //.isPresent() is a type of Optional method.
        if (employee.isPresent()) {
            return Result.success(employee.get(), "Employee found!");
        }else{
            return Result.failure("Employee not found.", List.of("No employees found with id of: " + id));
        }
    }

    public Result<List<Employee>> getEmployeeByDepartment(Department department){
        List<Employee> departmentEmployees = employeeInterface.findAllByDepartment(department);
        if(departmentEmployees.isEmpty()){
            return Result.failure("No employees found in this department" + department.getName(), List.of());
        }else{
            return Result.success(departmentEmployees, "Employees fetched successfully for department: " + department.getName());
        }
    }

    public Result<Employee> saveOrUpdateEmployee(Employee employee){

        if (employee == null || employee.getDepartment() == null) {
            return Result.failure("Not a valid Department id", List.of("Employee and department must not be null"));
        }

        long deptId = employee.getDepartment().getDepartmentId();
        Result<Department> deptResult;

        try {
            deptResult = departmentService.getDepartmentById(deptId);
        } catch (Exception e) {
            return Result.failure("Error retrieving department", List.of(e.getMessage()));
        }

        if (!deptResult.isSuccess()) {
            return Result.failure("Failed to find department", List.of("No department of id " + deptId));
        }

        employee.setDepartment(deptResult.getData());


        int roleId = employee.getRole().getId(); // adjust based on your Roles class's method
        Result<Roles> roleResult;

        try {
            roleResult = roleService.getRoleById(roleId);
        } catch (Exception e) {
            return Result.failure("Error retrieving role", List.of(e.getMessage()));
        }

        if (!roleResult.isSuccess()) {
            return Result.failure("Failed to find role",
                    List.of("No role of id " + roleId));
        }

        employee.setRole(roleResult.getData());

        String username = employee.getUserEntity().getUsername(); // adjust based on your Roles class's method
        Result<UserEntity> userEntityResult;

        try {
            userEntityResult = userEntityService.getUserByUsername(username);
        } catch (Exception e) {
            return Result.failure("Error retrieving username", List.of(e.getMessage()));
        }

        if (!userEntityResult.isSuccess()) {
            return Result.failure("Failed to find username",
                    List.of("No username of " + username));
        }

        employee.setUserEntity(userEntityResult.getData());

        try {
            Employee savedEmployee = employeeInterface.save(employee);
            return Result.success(savedEmployee, "Employee saved successfully!");
        } catch (Exception e) {
            return Result.failure("Failed to save employee", List.of(e.getMessage()));
        }

    }

    public Result<Void> deleteEmployeeById(long id){
        if(employeeInterface.existsById(id)){
            employeeInterface.deleteById(id);
            return Result.success(null,"Employee was deleted!");
        }else{
            return Result.failure("Employee not found.", List.of("No employees found with id of: " + id));
        }

    }
}
