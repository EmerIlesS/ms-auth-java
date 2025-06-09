package com.auth.model.dto;

import com.auth.model.User;
import lombok.Data;

@Data
public class AuthPayload {
    private String token;
    private User user;
    
    // Getters y setters explícitos
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
}
