package com.mcs.auth.auth;

import com.mcs.auth.config.JwtService;
import com.mcs.auth.user.User;
import com.mcs.auth.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email()))
            throw new IllegalArgumentException("Email already registered");
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash()))
            throw new IllegalArgumentException("Invalid credentials");
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole().name());
    }
}
