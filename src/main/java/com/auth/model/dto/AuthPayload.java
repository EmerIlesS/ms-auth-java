package com.auth.model.dto;

import com.auth.model.User;
import lombok.Data;

@Data
public class AuthPayload {
    private String token;
    private User user;
}
