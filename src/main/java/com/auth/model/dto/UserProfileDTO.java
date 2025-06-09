package com.auth.model.dto;

import com.auth.model.User;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserProfileDTO {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private List<String> favorites = new ArrayList<>();
    
    // Getters y setters explícitos
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public List<String> getFavorites() {
        return favorites;
    }
    
    public void setFavorites(List<String> favorites) {
        this.favorites = favorites;
    }
    
    public static UserProfileDTO fromUser(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole());
        
        // Solo incluir favoritos si el rol es CUSTOMER
        if ("CUSTOMER".equals(user.getRole())) {
            dto.setFavorites(user.getFavorites());
        }
        
        return dto;
    }
}