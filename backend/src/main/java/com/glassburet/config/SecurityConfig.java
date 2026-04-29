package com.glassburet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.glassburet.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/auth/members/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/liners/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/puzzles/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/events/*/attendance/*").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/scores").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/gallery/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/gallery/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/quotes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/quotes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/events/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasRole("ADMIN")
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
