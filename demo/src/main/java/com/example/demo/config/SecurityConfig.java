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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

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

    /*@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Using BCrypt for password encoding
    }*/

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserService);
        //provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 🔥 Disabilita CSRF per Swagger e API REST
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/player/**").permitAll() // ✅ Permette tutte le richieste a /api/player
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // ✅ Richiede ruolo ADMIN per /api/admin
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // ✅ Permette Swagger
                        .requestMatchers("/api/register").permitAll() // ✅ Permette la registrazione
                        .anyRequest().authenticated()) // All other requests must be authenticated
                .formLogin(withDefaults()) // Specifica la pagina di login
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
}
