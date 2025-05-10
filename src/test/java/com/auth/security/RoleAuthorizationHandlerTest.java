package com.auth.security;

import com.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas para validar el sistema de roles y permisos en ms-auth-java
 */
public class RoleAuthorizationHandlerTest {

    private User customerUser;
    private User sellerUser;
    private User adminUser;

    @BeforeEach
    public void setup() {
        // Limpiar el contexto de seguridad antes de cada prueba
        SecurityContextHolder.clearContext();
        
        // Crear usuarios de prueba con diferentes roles
        customerUser = new User();
        customerUser.setId("user1");
        customerUser.setEmail("customer@example.com");
        customerUser.setRole("CUSTOMER");
        
        sellerUser = new User();
        sellerUser.setId("user2");
        sellerUser.setEmail("seller@example.com");
        sellerUser.setRole("SELLER");
        
        adminUser = new User();
        adminUser.setId("user3");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole("ADMIN");
    }
    
    private void authenticateUser(User user) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void getCurrentUser_AuthenticatedUser_ReturnsUser() {
        // Configurar
        authenticateUser(customerUser);
        
        // Ejecutar
        User result = RoleAuthorizationHandler.getCurrentUser();
        
        // Verificar
        assertEquals(customerUser.getId(), result.getId());
        assertEquals(customerUser.getRole(), result.getRole());
    }
    
    @Test
    public void getCurrentUser_NotAuthenticated_ThrowsException() {
        // No autenticar a ningún usuario
        
        // Ejecutar y verificar
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            RoleAuthorizationHandler.getCurrentUser();
        });
        
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
    
    @Test
    public void checkRoles_UserHasRole_ReturnsUser() {
        // Configurar
        authenticateUser(adminUser);
        
        // Ejecutar
        User result = RoleAuthorizationHandler.checkRoles("ADMIN");
        
        // Verificar
        assertEquals(adminUser.getId(), result.getId());
    }
    
    @Test
    public void checkRoles_UserDoesNotHaveRole_ThrowsException() {
        // Configurar
        authenticateUser(customerUser);
        
        // Ejecutar y verificar
        assertThrows(AccessDeniedException.class, () -> {
            RoleAuthorizationHandler.checkRoles("ADMIN", "SELLER");
        });
    }
    
    @Test
    public void checkAdmin_AdminUser_ReturnsUser() {
        // Configurar
        authenticateUser(adminUser);
        
        // Ejecutar
        User result = RoleAuthorizationHandler.checkAdmin();
        
        // Verificar
        assertEquals(adminUser.getId(), result.getId());
    }
    
    @Test
    public void checkAdmin_NonAdminUser_ThrowsException() {
        // Configurar
        authenticateUser(sellerUser);
        
        // Ejecutar y verificar
        assertThrows(AccessDeniedException.class, () -> {
            RoleAuthorizationHandler.checkAdmin();
        });
    }
    
    @Test
    public void checkSeller_SellerUser_ReturnsUser() {
        // Configurar
        authenticateUser(sellerUser);
        
        // Ejecutar
        User result = RoleAuthorizationHandler.checkSeller();
        
        // Verificar
        assertEquals(sellerUser.getId(), result.getId());
    }
    
    @Test
    public void checkCustomer_CustomerUser_ReturnsUser() {
        // Configurar
        authenticateUser(customerUser);
        
        // Ejecutar
        User result = RoleAuthorizationHandler.checkCustomer();
        
        // Verificar
        assertEquals(customerUser.getId(), result.getId());
    }
    
    @Test
    public void checkAdminOrSeller_AdminUser_ReturnsUser() {
        // Configurar
        authenticateUser(adminUser);
        
        // Ejecutar
        User result = RoleAuthorizationHandler.checkAdminOrSeller();
        
        // Verificar
        assertEquals(adminUser.getId(), result.getId());
    }
    
    @Test
    public void checkAdminOrSeller_SellerUser_ReturnsUser() {
        // Configurar
        authenticateUser(sellerUser);
        
        // Ejecutar
        User result = RoleAuthorizationHandler.checkAdminOrSeller();
        
        // Verificar
        assertEquals(sellerUser.getId(), result.getId());
    }
    
    @Test
    public void checkAdminOrSeller_CustomerUser_ThrowsException() {
        // Configurar
        authenticateUser(customerUser);
        
        // Ejecutar y verificar
        assertThrows(AccessDeniedException.class, () -> {
            RoleAuthorizationHandler.checkAdminOrSeller();
        });
    }
    
    @Test
    public void checkOwnerOrAdmin_OwnerUser_ReturnsUser() {
        // Configurar
        authenticateUser(customerUser);
        
        // Ejecutar
        User result = RoleAuthorizationHandler.checkOwnerOrAdmin(customerUser.getId());
        
        // Verificar
        assertEquals(customerUser.getId(), result.getId());
    }
    
    @Test
    public void checkOwnerOrAdmin_AdminUser_ReturnsUser() {
        // Configurar
        authenticateUser(adminUser);
        
        // Ejecutar
        User result = RoleAuthorizationHandler.checkOwnerOrAdmin(customerUser.getId());
        
        // Verificar
        assertEquals(adminUser.getId(), result.getId());
    }
    
    @Test
    public void checkOwnerOrAdmin_NonOwnerNonAdmin_ThrowsException() {
        // Configurar
        authenticateUser(customerUser);
        
        // Ejecutar y verificar
        assertThrows(AccessDeniedException.class, () -> {
            RoleAuthorizationHandler.checkOwnerOrAdmin(sellerUser.getId());
        });
    }
}