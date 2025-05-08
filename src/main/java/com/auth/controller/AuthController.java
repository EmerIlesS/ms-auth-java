package com.auth.controller;

import com.auth.model.User;
import com.auth.model.dto.AuthPayload;
import com.auth.model.dto.LoginInput;
import com.auth.model.dto.RegisterInput;
import com.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @QueryMapping
    public User me(@AuthenticationPrincipal User user) {
        return user;
    }

    @MutationMapping
    public AuthPayload register(@Argument RegisterInput input) {
        return authService.register(input, "CUSTOMER");
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AuthPayload registerVendor(@Argument RegisterInput input) {
        return authService.register(input, "SELLER");
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AuthPayload registerAdmin(@Argument RegisterInput input) {
        return authService.register(input, "ADMIN");
    }

    @MutationMapping
    public AuthPayload login(@Argument LoginInput input) {
        return authService.login(input);
    }

    @MutationMapping
    public User addToFavorites(
        @Argument String productId,
        @AuthenticationPrincipal User user
    ) {
        return authService.addToFavorites(user.getId(), productId);
    }

    @MutationMapping
    public User removeFromFavorites(
        @Argument String productId,
        @AuthenticationPrincipal User user
    ) {
        return authService.removeFromFavorites(user.getId(), productId);
    }
}
