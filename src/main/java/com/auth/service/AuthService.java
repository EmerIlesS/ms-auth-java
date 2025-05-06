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

    public AuthPayload register(RegisterInput input) {
        System.out.println("Registering user: " + input);
        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(input.getEmail());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setRole("CUSTOMER");

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
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!user.getFavorites().contains(productId)) {
            user.getFavorites().add(productId);
            return userRepository.save(user);
        }
        return user;
    }

    public User removeFromFavorites(String userId, String productId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.getFavorites().remove(productId);
        return userRepository.save(user);
    }
}
