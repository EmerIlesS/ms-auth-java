package com.auth.controller;

import com.auth.model.User;
import com.auth.model.dto.AuthPayload;
import com.auth.model.dto.LoginInput;
import com.auth.model.dto.RegisterInput;
import com.auth.security.RoleAuthorizationHandler;
import com.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @QueryMapping
    public User me() {
        // Utiliza el manejador centralizado para verificar autenticación
        return RoleAuthorizationHandler.getCurrentUser();
    }

    @MutationMapping
    public AuthPayload register(@Argument RegisterInput input) {
        // No requiere autenticación - cualquier usuario puede registrarse como cliente
        return authService.register(input, "CUSTOMER");
    }
    
    @MutationMapping
    public AuthPayload registerVendor(@Argument RegisterInput input) {
        // Verifica que el usuario sea administrador
        RoleAuthorizationHandler.checkAdmin();
        return authService.register(input, "SELLER");
    }
    
    @MutationMapping
    public AuthPayload registerAdmin(@Argument RegisterInput input) {
        // Verifica que el usuario sea administrador
        RoleAuthorizationHandler.checkAdmin();
        return authService.register(input, "ADMIN");
    }

    @MutationMapping
    public AuthPayload login(@Argument LoginInput input) {
        // No requiere autenticación - cualquier usuario puede iniciar sesión
        return authService.login(input);
    }

    @MutationMapping
    public User addToFavorites(@Argument String productId) {
        // Verifica que el usuario sea cliente
        User user = RoleAuthorizationHandler.checkCustomer();
        return authService.addToFavorites(user.getId(), productId);
    }

    @MutationMapping
    public User removeFromFavorites(@Argument String productId) {
        // Verifica que el usuario sea cliente
        User user = RoleAuthorizationHandler.checkCustomer();
        return authService.removeFromFavorites(user.getId(), productId);
    }
}
