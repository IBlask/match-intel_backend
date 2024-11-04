package com.match_intel.backend.dto.request;

public class RegisterUserRequest {

    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;


    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPassword() {
        return password;
    }
}
