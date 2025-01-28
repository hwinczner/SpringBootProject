package com.SpringBoot.Project.Repositories;

import com.SpringBoot.Project.Models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EmployeeInterface extends JpaRepository<Employee, Long> {

    //Add custom classes if we need them (probably won't since were just doing basic CRUD operations).
}
