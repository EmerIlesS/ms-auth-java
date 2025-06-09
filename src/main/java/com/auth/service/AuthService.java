package com.auth.service;

import com.auth.model.User;
import com.auth.repository.UserRepository;
import com.auth.model.dto.AuthPayload;
import com.auth.model.dto.LoginInput;
import com.auth.model.dto.RegisterInput;
import com.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ProductService productService;

    public AuthPayload register(RegisterInput input, String role) {
        System.out.println("Registering user: " + input + " with role: " + role);
        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        // Validar que la contraseña sea segura (al menos 8 caracteres, con números y letras)
        if (input.getPassword().length() < 8 || !input.getPassword().matches(".*[0-9].*") || !input.getPassword().matches(".*[a-zA-Z].*")) {
            throw new RuntimeException("Password must be at least 8 characters long and contain both letters and numbers");
        }

        User user = new User();
        user.setEmail(input.getEmail());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setRole(role);

        user = userRepository.save(user);
        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole());

        AuthPayload authPayload = new AuthPayload();
        authPayload.setToken(token);
        authPayload.setUser(user);
        return authPayload;
    }

    public AuthPayload login(LoginInput input) {
        User user = userRepository.findByEmail(input.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(input.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole());

        AuthPayload authPayload = new AuthPayload();
        authPayload.setToken(token);
        authPayload.setUser(user);
        return authPayload;
    }

    public User addToFavorites(String userId, String productId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Validar que el usuario tenga rol de CUSTOMER
        if (!"CUSTOMER".equals(user.getRole())) {
            throw new RuntimeException("Solo los clientes pueden modificar su lista de favoritos");
        }
        
        try {
            // Intentar validar que el producto exista utilizando el circuit breaker
            boolean exists = productService.productExists(productId);
            if (!exists) {
                System.out.println("Advertencia: El producto " + productId + " no se encontró, pero se agregará a favoritos de todas formas.");
            }
        } catch (Exception ex) {
            // Registrar el error pero continuar con la operación
            System.err.println("Error al verificar existencia del producto: " + ex.getMessage());
            // Registramos que el circuito podría estar abierto
            System.err.println("La operación continuará, pero se recomienda verificar la disponibilidad del servicio de productos");
        }
        
        // Verificar duplicados antes de agregar
        if (!user.getFavorites().contains(productId)) {
            // Evitamos modificar directamente la lista por si es inmutable o nula
            if (user.getFavorites() == null) {
                user.setFavorites(new java.util.ArrayList<>());
            }
            user.getFavorites().add(productId);
            
            try {
                return userRepository.save(user);
            } catch (Exception ex) {
                System.err.println("Error al guardar usuario con favorito: " + ex.getMessage());
                throw new RuntimeException("No se pudo guardar el producto en favoritos. Intente nuevamente más tarde.");
            }
        }
        return user;
    }

    public User removeFromFavorites(String userId, String productId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Validar que el usuario tenga rol de CUSTOMER
        if (!"CUSTOMER".equals(user.getRole())) {
            throw new RuntimeException("Solo los clientes pueden modificar su lista de favoritos");
        }
        
        if (user.getFavorites() == null) {
            return user; // Si la lista es nula, no hay nada que eliminar
        }
        
        // Eliminar el producto de la lista de favoritos
        boolean removed = user.getFavorites().remove(productId);
        
        if (removed) {
            try {
                return userRepository.save(user);
            } catch (Exception ex) {
                System.err.println("Error al guardar usuario después de eliminar favorito: " + ex.getMessage());
                throw new RuntimeException("No se pudo eliminar el producto de favoritos. Intente nuevamente más tarde.");
            }
        }
        return user; // Si no se eliminó nada, devolver el usuario sin cambios
    }
}
