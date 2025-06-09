package com.auth.model.dto;

import lombok.Data;

@Data
public class LoginInput {
    private String email;
    private String password;
    
    // Getters y setters explícitos
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
