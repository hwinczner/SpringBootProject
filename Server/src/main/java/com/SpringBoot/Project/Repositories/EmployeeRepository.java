package com.SpringBoot.Project.Repositories;

import com.SpringBoot.Project.Models.Employee;

import java.util.List;

public class EmployeeRepository implements EmployeeInterface {
    @Override
    public List<Employee> findAll() {
        return List.of();
    }

    @Override
    public Employee findById(Employee employee) {
        return null;
    }

    @Override
    public Employee updateEmployee(Employee employee) {
        return null;
    }

    @Override
    public Boolean DeleteEmployee(Employee employee) {
        return null;
    }
}
