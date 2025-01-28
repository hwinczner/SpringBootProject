package com.SpringBoot.Project.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long departmentId;

    @NotNull(message = "Name cannot be Null")
    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    public Department() {
        //Default constructor for JPA
    }

    public Department(String name, String description){
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
