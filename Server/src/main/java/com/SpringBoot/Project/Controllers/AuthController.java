package com.SpringBoot.Project.Controllers;

import com.SpringBoot.Project.Dto.LoginDto;
import com.SpringBoot.Project.Dto.RegisterDto;
import com.SpringBoot.Project.Models.Roles;
import com.SpringBoot.Project.Models.UserEntity;
import com.SpringBoot.Project.Repositories.RoleInterface;
import com.SpringBoot.Project.Repositories.UserInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private AuthenticationManager authenticationManager;
    private UserInterface userInterface;
    private RoleInterface roleInterface;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserInterface userInterface, RoleInterface roleInterface, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userInterface = userInterface;
        this.roleInterface = roleInterface;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("login")
    public ResponseEntity<String> login(@RequestBody LoginDto loginDto){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return new ResponseEntity<>("User has signed in", HttpStatus.OK);
    }

    @PostMapping("register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto){
        if(userInterface.existsByUsername(registerDto.getUsername())){
            return new ResponseEntity<>("Username is Taken!", HttpStatus.BAD_REQUEST);
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(registerDto.getUsername());
        userEntity.setPassword(passwordEncoder.encode(registerDto.getPassword()));


        Roles roles = roleInterface.findByName("EMPLOYEE").get();
        userEntity.setRoles(Collections.singletonList(roles));

        userInterface.save(userEntity);

        return new ResponseEntity<>("New Employee registered", HttpStatus.CREATED);
    }

}
