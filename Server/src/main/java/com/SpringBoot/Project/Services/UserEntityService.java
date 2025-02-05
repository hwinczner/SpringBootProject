package com.SpringBoot.Project.Services;

import com.SpringBoot.Project.Models.Result;
import com.SpringBoot.Project.Models.UserEntity;
import com.SpringBoot.Project.Repositories.UserInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserEntityService {

    private final UserInterface userInterface;

    @Autowired
    public UserEntityService(UserInterface userInterface) {
        this.userInterface = userInterface;
    }

    public Result<UserEntity> getUserByUsername(String username){
        Optional<UserEntity> userEntity = userInterface.findByUsername(username);

        if(userEntity.isPresent()){
            return Result.success(userEntity.get(), "Department fetched successfully.");
        }else{
            return Result.failure("No user found with that username.", List.of("No username found with id of " + username));
        }

    }
}
