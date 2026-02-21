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

                        // ✅ Swagger should be public
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // public
                        .requestMatchers("/login", "/css/**", "/js/**").permitAll()

                        // admin
                        .requestMatchers(
                                "/books/new",
                                "/books",
                                "/books/edit/**",
                                "/books/update/**",
                                "/books/delete/**"
                        ).authenticated()


                        .requestMatchers(
                                "/books/borrow/**",
                                "/books/return/**",
                                "/my-borrows").authenticated()

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
                        .defaultSuccessUrl("/my-borrows", true)
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