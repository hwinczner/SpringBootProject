package com.SpringBoot.Project;

import com.SpringBoot.Project.Models.Department;
import com.SpringBoot.Project.Models.Employee;
import com.SpringBoot.Project.Repositories.DepartmentInterface;
import com.SpringBoot.Project.Repositories.EmployeeInterface;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectApplication.class, args);
	}


}
