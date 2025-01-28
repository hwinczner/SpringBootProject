package com.SpringBoot.Project.Models;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.Objects;

@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Is used to automatically generate unique values
    // for the primary key column in the database. So employeeId does not need to be initialized in constructor.
    private long employeeId;

    @NotNull(message = "Name cannot be null")
    @Column(nullable = false)
    private String name;

    //@NotNull is used for application level, whereas @Column is used for database level. Both ensure data integrity.
    @NotNull(message = "Email cannot be null")
    @Email(message = "Email must be a valid format")
    @Column(nullable = false, unique = true)
    private String email;

    //Used for establishing a relationship between 2 tables, joins many rows into one row.
    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false) //Joins at the specific foreign key column name.
    private Department department;

    @NotNull(message = "Role cannot be null")
    @Column(nullable = false)
    private String role;

    public Employee(){
        //Default constructor for JPA
    }

    public Employee(String name, String email, Department department, String role){
        this.name = name;
        this.email = email;
        this.department = department;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


    //Overriding .equals() as to just compare employeeId and email, ignoring other variables to more logically compare employees.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return employeeId == employee.employeeId && Objects.equals(email, employee.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, email);
    }

}
