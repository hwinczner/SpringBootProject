package com.SpringBoot.Project.Swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI myOpenAPI() {
        Info info = new Info()
                .title("Employee Leave Management System API")
                .version("1.0")
                .description("This API exposes endpoints to manage employee leave requests.");

        return new OpenAPI().info(info);
    }
}