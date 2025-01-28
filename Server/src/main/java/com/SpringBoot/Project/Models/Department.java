package com.SpringBoot.Project.Models;

import jakarta.persistence.*;

@Entity
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long departmentId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    public Department() {
        //Default constructor for JPA
    }

    public Department(Long departmentId, String name, String description){
        this.departmentId = departmentId;
        this.name = name;
        this.description = description;
    }

    public long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(long departmentId) {
        this.departmentId = departmentId;
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
