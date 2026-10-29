package com.taskforge.service;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.taskforge.dto.AuthResponse;
import com.taskforge.dto.LoginRequest;
import com.taskforge.dto.RegisterRequest;
import com.taskforge.entity.AppUser;
import com.taskforge.exception.ResourceConflictException;
import com.taskforge.repository.AppUserRepository;
import com.taskforge.security.JwtService;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            JwtService jwtService) {

        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        String email = normalizeEmail(request.email());

        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new ResourceConflictException(
                    "An account with this email already exists");
        }

        AppUser user = new AppUser();
        user.setDisplayName(request.displayName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        AppUser savedUser = appUserRepository.save(user);

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(savedUser.getEmail());

        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                "Bearer",
                token,
                jwtService.getExpirationMs(),
                savedUser.getId(),
                savedUser.getDisplayName(),
                savedUser.getEmail());
    }

    public AuthResponse login(LoginRequest request) {

        String email = normalizeEmail(request.email());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            request.password()));
        } catch (BadCredentialsException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
        }

        AppUser user = appUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"));

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(email);

        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                "Bearer",
                token,
                jwtService.getExpirationMs(),
                user.getId(),
                user.getDisplayName(),
                user.getEmail());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}