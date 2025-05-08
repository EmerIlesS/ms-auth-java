package com.auth.model.dto;

import lombok.Data;

@Data
public class AdminRegisterInput {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String role;
}