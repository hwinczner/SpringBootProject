package com.SpringBoot.Project.Repositories;

import com.SpringBoot.Project.Models.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleInterface extends JpaRepository<Roles, Integer> {

    Optional<Roles> findByName(String name);
}
