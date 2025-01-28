package com.SpringBoot.Project.Repositories;

import com.SpringBoot.Project.Models.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentInterface extends JpaRepository<Department, Long> {
}
