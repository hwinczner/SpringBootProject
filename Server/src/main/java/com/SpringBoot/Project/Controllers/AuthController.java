package com.SpringBoot.Project.Controllers;

import com.SpringBoot.Project.Dto.AuthResponseDto;
import com.SpringBoot.Project.Dto.LoginDto;
import com.SpringBoot.Project.Dto.RegisterDto;
import com.SpringBoot.Project.Models.Roles;
import com.SpringBoot.Project.Models.UserEntity;
import com.SpringBoot.Project.Repositories.RoleInterface;
import com.SpringBoot.Project.Repositories.UserInterface;
import com.SpringBoot.Project.Security.JwtGenerator;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private static final Logger logger = LogManager.getLogger(AuthController.class);
    private AuthenticationManager authenticationManager;
    private UserInterface userInterface;
    private RoleInterface roleInterface;
    private PasswordEncoder passwordEncoder;
    private JwtGenerator jwtGenerator;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserInterface userInterface, RoleInterface roleInterface, PasswordEncoder passwordEncoder, JwtGenerator jwtGenerator) {
        this.authenticationManager = authenticationManager;
        this.userInterface = userInterface;
        this.roleInterface = roleInterface;
        this.passwordEncoder = passwordEncoder;
        this.jwtGenerator = jwtGenerator;
    }

    @PostMapping("login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginDto loginDto) {
        logger.info("Login attempt for user: {}", loginDto.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerator.generateToken(authentication);
            logger.info("User successfully logged in: {}", loginDto.getUsername());
            return new ResponseEntity<>(new AuthResponseDto(token), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Login failed for user: {}", loginDto.getUsername(), e);
            throw e;
        }
    }


    @PostMapping("register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto) {
        logger.info("Registration attempt for username: {}", registerDto.getUsername());

        if (userInterface.existsByUsername(registerDto.getUsername())) {
            logger.warn("Registration failed - username already taken: {}", registerDto.getUsername());
            return new ResponseEntity<>("Username is Taken!", HttpStatus.BAD_REQUEST);
        }

        try {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(registerDto.getUsername());
            userEntity.setPassword(passwordEncoder.encode(registerDto.getPassword()));

            Roles roles = roleInterface.findByName("EMPLOYEE").get();
            userEntity.setRoles(Collections.singletonList(roles));

            userInterface.save(userEntity);
            logger.info("Successfully registered new user: {}", registerDto.getUsername());
            return new ResponseEntity<>("New Employee registered", HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Failed to register user: {}", registerDto.getUsername(), e);
            throw e;
        }
    }
}
