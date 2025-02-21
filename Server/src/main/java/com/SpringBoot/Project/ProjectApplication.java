package com.SpringBoot.Project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SpringBootApplication
public class ProjectApplication {
	private static final Logger logger = LogManager.getLogger(ProjectApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Employee Leave Management System...");
		SpringApplication.run(ProjectApplication.class, args);
		logger.info("Application started successfully");
	}
}
