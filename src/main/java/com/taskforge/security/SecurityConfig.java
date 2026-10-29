package com.taskforge.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.taskforge.repository.AppUserRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService(
            AppUserRepository appUserRepository) {

        return email -> appUserRepository.findByEmailIgnoreCase(email)
                .map(appUser -> User.withUsername(appUser.getEmail())
                        .password(appUser.getPasswordHash())
                        .roles("USER")
                        .build())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationProvider authenticationProvider)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint((request, response, exception) ->
                            response.sendError(
                                    401, "Authentication required")))

            .authorizeHttpRequests(auth -> auth
                    // Public authentication endpoints
                    .requestMatchers("/api/auth/**").permitAll()

                    // Public Swagger UI and OpenAPI documentation
                    .requestMatchers(
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/v3/api-docs/**"
                    ).permitAll()

                    // All remaining endpoints require authentication
                    .anyRequest().authenticated())

            .authenticationProvider(authenticationProvider)

            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}