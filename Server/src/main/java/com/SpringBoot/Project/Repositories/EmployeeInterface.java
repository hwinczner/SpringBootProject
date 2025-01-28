package com.SpringBoot.Project.Repositories;

import com.SpringBoot.Project.Models.Employee;

import java.util.List;

public interface EmployeeInterface {
    List<Employee> findAll();
    Employee findById(Employee employee);
    Employee updateEmployee(Employee employee);
    Boolean DeleteEmployee(Employee employee);

}
