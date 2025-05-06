package com.auth.model.dto;

import lombok.Data;

@Data
public class RegisterInput {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
}
