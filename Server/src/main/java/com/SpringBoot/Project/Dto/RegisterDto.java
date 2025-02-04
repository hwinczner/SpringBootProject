package com.SpringBoot.Project.Dto;

public class RegisterDto {
    private String username;
    private String password;

    public RegisterDto() {
    }

    public RegisterDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Note the method names: getUsername() and setUsername()
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // And for password: getPassword() and setPassword()
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}