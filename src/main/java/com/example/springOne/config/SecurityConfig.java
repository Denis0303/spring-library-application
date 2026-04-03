package com.example.springOne.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers("/login", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/debug-db").permitAll()

                        // admin only
                        .requestMatchers(
                                "/books/new",
                                "/books/edit/**",
                                "/books/update/**",
                                "/books/delete/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/books").hasRole("ADMIN")

                        // logged-in users
                        .requestMatchers(
                                "/books",
                                "/books/borrow/**",
                                "/books/return/**",
                                "/my-borrows"
                        ).authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/books", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}