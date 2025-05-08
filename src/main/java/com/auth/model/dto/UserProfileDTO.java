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