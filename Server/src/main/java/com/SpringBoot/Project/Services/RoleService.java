package com.SpringBoot.Project.Services;

import com.SpringBoot.Project.Models.Result;
import com.SpringBoot.Project.Models.Roles;
import com.SpringBoot.Project.Repositories.RoleInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    private final RoleInterface roleInterface;

    @Autowired
    public RoleService(RoleInterface roleInterface) {
        this.roleInterface = roleInterface;
    }

    public Result<Roles> getRoleById(Integer id){
        Optional<Roles> roles = roleInterface.findById(id);
        if(roles.isPresent()){
            return Result.success(roles.get(), "Role fetched successfully.");
        }else{
            return Result.failure("Role not found.", List.of("No Role found with id of " + id));
        }
    }
}
