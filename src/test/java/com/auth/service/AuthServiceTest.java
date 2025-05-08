package com.auth.service;

import com.auth.model.User;
import com.auth.model.dto.AuthPayload;
import com.auth.model.dto.RegisterInput;
import com.auth.repository.UserRepository;
import com.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerCustomer_Success() {
        // Arrange
        RegisterInput input = new RegisterInput();
        input.setEmail("cliente@example.com");
        input.setPassword("Password123");
        input.setFirstName("Cliente");
        input.setLastName("Ejemplo");

        User savedUser = new User();
        savedUser.setId("1");
        savedUser.setEmail(input.getEmail());
        savedUser.setFirstName(input.getFirstName());
        savedUser.setLastName(input.getLastName());
        savedUser.setRole("CUSTOMER");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.createToken(anyString(), anyString())).thenReturn("jwt-token");

        // Act
        AuthPayload result = authService.register(input, "CUSTOMER");

        // Assert
        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("CUSTOMER", result.getUser().getRole());
        assertEquals(input.getEmail(), result.getUser().getEmail());
    }

    @Test
    void registerVendor_Success() {
        // Arrange
        RegisterInput input = new RegisterInput();
        input.setEmail("vendedor@example.com");
        input.setPassword("Password123");
        input.setFirstName("Vendedor");
        input.setLastName("Ejemplo");

        User savedUser = new User();
        savedUser.setId("2");
        savedUser.setEmail(input.getEmail());
        savedUser.setFirstName(input.getFirstName());
        savedUser.setLastName(input.getLastName());
        savedUser.setRole("SELLER");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.createToken(anyString(), anyString())).thenReturn("jwt-token");

        // Act
        AuthPayload result = authService.register(input, "SELLER");

        // Assert
        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("SELLER", result.getUser().getRole());
        assertEquals(input.getEmail(), result.getUser().getEmail());
    }

    @Test
    void registerAdmin_Success() {
        // Arrange
        RegisterInput input = new RegisterInput();
        input.setEmail("admin@example.com");
        input.setPassword("Password123");
        input.setFirstName("Admin");
        input.setLastName("Ejemplo");

        User savedUser = new User();
        savedUser.setId("3");
        savedUser.setEmail(input.getEmail());
        savedUser.setFirstName(input.getFirstName());
        savedUser.setLastName(input.getLastName());
        savedUser.setRole("ADMIN");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.createToken(anyString(), anyString())).thenReturn("jwt-token");

        // Act
        AuthPayload result = authService.register(input, "ADMIN");

        // Assert
        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("ADMIN", result.getUser().getRole());
        assertEquals(input.getEmail(), result.getUser().getEmail());
    }

    @Test
    void register_EmailAlreadyExists() {
        // Arrange
        RegisterInput input = new RegisterInput();
        input.setEmail("existente@example.com");
        input.setPassword("Password123");

        User existingUser = new User();
        existingUser.setEmail(input.getEmail());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.register(input, "CUSTOMER");
        });

        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void register_InsecurePassword() {
        // Arrange
        RegisterInput input = new RegisterInput();
        input.setEmail("nuevo@example.com");
        input.setPassword("short"); // Contraseña demasiado corta

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.register(input, "CUSTOMER");
        });

        assertEquals("Password must be at least 8 characters long and contain both letters and numbers", exception.getMessage());
    }
}