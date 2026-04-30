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
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Auth endpoints
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/members").hasRole("OWNER")
                .requestMatchers(HttpMethod.PUT, "/api/auth/members/**").hasRole("OWNER")

                // Quotes — like (authenticated), create/edit/delete (ADMIN)
                .requestMatchers(HttpMethod.PUT, "/api/quotes/*/like").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/quotes/**").hasAnyRole("ADMIN", "OWNER")
                .requestMatchers(HttpMethod.POST, "/api/quotes/**").hasAnyRole("ADMIN", "OWNER")
                .requestMatchers(HttpMethod.DELETE, "/api/quotes/**").hasAnyRole("ADMIN", "OWNER")

                // Liners — said (public), like (authenticated), CRUD (ADMIN+OWNER)
                .requestMatchers(HttpMethod.PUT, "/api/liners/*/said").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/liners/*/like").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/liners/**").hasAnyRole("ADMIN", "OWNER")
                .requestMatchers(HttpMethod.POST, "/api/liners/**").hasAnyRole("ADMIN", "OWNER")
                .requestMatchers(HttpMethod.DELETE, "/api/liners/**").hasAnyRole("ADMIN", "OWNER")

                // Gallery — like (authenticated), create/edit/delete (ADMIN)
                .requestMatchers(HttpMethod.PUT, "/api/gallery/*/like").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/gallery/**").hasAnyRole("ADMIN", "OWNER")
                .requestMatchers(HttpMethod.POST, "/api/gallery/**").hasAnyRole("ADMIN", "OWNER")
                .requestMatchers(HttpMethod.DELETE, "/api/gallery/**").hasAnyRole("ADMIN", "OWNER")

                // Events — attendance (authenticated), create/edit/delete (ADMIN)
                .requestMatchers(HttpMethod.PUT, "/api/events/*/attendance/*").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/events/**").hasAnyRole("ADMIN", "OWNER")
                .requestMatchers(HttpMethod.POST, "/api/events/**").hasAnyRole("ADMIN", "OWNER")
                .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasAnyRole("ADMIN", "OWNER")

                // Scores — submit (any member), completed list (authenticated)
                .requestMatchers(HttpMethod.POST, "/api/scores").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/scores/completed").authenticated()

                // Puzzles — create (authenticated), list/latest (public)
                .requestMatchers(HttpMethod.POST, "/api/puzzles/**").authenticated()

                // Everything else public
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
