package com.proyecto.nexus.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.proyecto.nexus.usuario.service.UsuarioDetailsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;

    public SecurityConfig(UsuarioDetailsService usuarioDetailsService) {
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authBuilder
                .userDetailsService(usuarioDetailsService)
                .passwordEncoder(passwordEncoder());

        return authBuilder.build();
    }

    // 🔥 Mejorado: handler limpio
    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (HttpServletRequest request,
                HttpServletResponse response,
                Authentication authentication) -> {

            String role = authentication.getAuthorities()
                    .stream()
                    .findFirst()
                    .get()
                    .getAuthority();

            switch (role) {
                case "ROLE_ADMINISTRADOR":
                    response.sendRedirect("/dashboardAdmin");
                    break;
                case "ROLE_USUARIO":
                    response.sendRedirect("/principalUsuario");
                    break;
                case "ROLE_INSTRUCTOR":
                    response.sendRedirect("/dashboardInstructor");
                    break;
                default:
                    response.sendRedirect("/");
                    break;
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/login", "/recuperar",
                    "/auth/**",
                    "/css/**", "/imagenes/**",
                    "/Videos/**", "/js/**"
                ).permitAll()

                .requestMatchers("/dashboardAdmin/**").hasRole("ADMINISTRADOR")

                .requestMatchers(
                        "/principalUsuario/**",
                        "/clases/**",
                        "/reservas/**",
                        "/perfil/**",
                        "/paquetes/**"
                ).hasRole("USUARIO")

                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(successHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )

            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}