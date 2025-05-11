package com.auth.controller;

import com.auth.model.User;
import com.auth.model.dto.AuthPayload;
import com.auth.model.dto.RegisterInput;
import com.auth.security.RoleAuthorizationHandler;
import com.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_Customer_Success() {
        // Arrange
        RegisterInput input = new RegisterInput();
        input.setEmail("cliente@example.com");
        input.setPassword("Password123");
        input.setFirstName("Cliente");
        input.setLastName("Ejemplo");

        User user = new User();
        user.setEmail(input.getEmail());
        user.setRole("CUSTOMER");

        AuthPayload expectedPayload = new AuthPayload();
        expectedPayload.setToken("jwt-token");
        expectedPayload.setUser(user);

        when(authService.register(any(RegisterInput.class), eq("CUSTOMER"))).thenReturn(expectedPayload);

        // Act
        AuthPayload result = authController.register(input);

        // Assert
        assertNotNull(result);
        assertEquals(expectedPayload.getToken(), result.getToken());
        assertEquals(expectedPayload.getUser().getEmail(), result.getUser().getEmail());
        assertEquals(expectedPayload.getUser().getRole(), result.getUser().getRole());
    }

    @Test
    void registerVendor_AdminRole_Success() {
        // Arrange
        RegisterInput input = new RegisterInput();
        input.setEmail("vendedor@example.com");
        input.setPassword("Password123");
        input.setFirstName("Vendedor");
        input.setLastName("Ejemplo");

        User user = new User();
        user.setEmail(input.getEmail());
        user.setRole("SELLER");

        AuthPayload expectedPayload = new AuthPayload();
        expectedPayload.setToken("jwt-token");
        expectedPayload.setUser(user);

        // Crear un usuario admin para simular autenticación
        User adminUser = new User();
        adminUser.setId("admin-id");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole("ADMIN");

        // Usar MockedStatic para simular el método estático checkAdmin en RoleAuthorizationHandler
        try (MockedStatic<RoleAuthorizationHandler> mockedStatic = mockStatic(RoleAuthorizationHandler.class)) {
            // El método checkAdmin debe devolver el usuario admin
            mockedStatic.when(RoleAuthorizationHandler::checkAdmin).thenReturn(adminUser);

            when(authService.register(any(RegisterInput.class), eq("SELLER"))).thenReturn(expectedPayload);

            // Act
            AuthPayload result = authController.registerVendor(input);

            // Assert
            assertNotNull(result);
            assertEquals(expectedPayload.getToken(), result.getToken());
            assertEquals(expectedPayload.getUser().getEmail(), result.getUser().getEmail());
            assertEquals("SELLER", result.getUser().getRole());
        }
    }

    @Test
    void registerAdmin_AdminRole_Success() {
        // Arrange
        RegisterInput input = new RegisterInput();
        input.setEmail("admin@example.com");
        input.setPassword("Password123");
        input.setFirstName("Admin");
        input.setLastName("Ejemplo");

        User user = new User();
        user.setEmail(input.getEmail());
        user.setRole("ADMIN");

        AuthPayload expectedPayload = new AuthPayload();
        expectedPayload.setToken("jwt-token");
        expectedPayload.setUser(user);

        // Crear un usuario admin para simular autenticación
        User adminUser = new User();
        adminUser.setId("admin-id");
        adminUser.setEmail("existing-admin@example.com");
        adminUser.setRole("ADMIN");

        // Usar MockedStatic para simular el método estático checkAdmin en RoleAuthorizationHandler
        try (MockedStatic<RoleAuthorizationHandler> mockedStatic = mockStatic(RoleAuthorizationHandler.class)) {
            // El método checkAdmin debe devolver el usuario admin
            mockedStatic.when(RoleAuthorizationHandler::checkAdmin).thenReturn(adminUser);

            when(authService.register(any(RegisterInput.class), eq("ADMIN"))).thenReturn(expectedPayload);

            // Act
            AuthPayload result = authController.registerAdmin(input);

            // Assert
            assertNotNull(result);
            assertEquals(expectedPayload.getToken(), result.getToken());
            assertEquals(expectedPayload.getUser().getEmail(), result.getUser().getEmail());
            assertEquals("ADMIN", result.getUser().getRole());
        }
    }
}