package com.SpringBoot.Project.Repositories;

import com.SpringBoot.Project.Models.Department;
import com.SpringBoot.Project.Models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface EmployeeInterface extends JpaRepository<Employee, Long> {

    List<Employee> findAllByDepartment(Department department);
}
