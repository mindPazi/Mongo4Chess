package com.example.demo.config;

import com.example.demo.service.CustomUserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import static org.springframework.security.config.Customizer.withDefaults;

@SuppressWarnings("unused")
@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    @Autowired
    private final CustomUserService customUserService;

    @Bean
    public UserDetailsService userDetailsService() {
        return customUserService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Using BCrypt for password encoding
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 🔥 Disabilita CSRF per Swagger e API REST
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/player/**").hasRole("PLAYER") // ✅ Permette tutte le
                        // richieste a /api/player
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // ✅ Richiede ruolo ADMIN
                        // per /api/admin
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // ✅ Permette Swagger
                        .requestMatchers("/api/register").permitAll() // ✅ Permette la registrazione
                        .requestMatchers("/login").permitAll()
                        .anyRequest().authenticated()) // All other requests must be authenticated
                .formLogin(form -> form
                        .successHandler(new SavedRequestAwareAuthenticationSuccessHandler())) // Use default success
                                                                                              // handler
                .httpBasic(httpBasic -> httpBasic.disable())
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(new AccessDeniedHandlerImpl() {
                            {
                                setErrorPage("/login"); // Redirect to login page on access denied
                            }
                        }))

        /*
         * .sessionManagement(session -> session
         * .maximumSessions(1) // Allow only one session per user
         * .maxSessionsPreventsLogin(true) // Prevent new login if max sessions reached
         * )
         */;

        return http.build();
    }
}
