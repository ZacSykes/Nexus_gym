package com.proyecto.nexus.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.proyecto.nexus.usuario.service.UsuarioDetailsService;

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

    // ✅ SUCCESS HANDLER CORREGIDO
    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {

            String role = authentication.getAuthorities()
                    .stream()
                    .findFirst()
                    .get()
                    .getAuthority();

            switch (role) {
                case "ROLE_ADMINISTRADOR":
                    response.sendRedirect("/admin/usuarios");
                    break;

                case "ROLE_USUARIO":
                    response.sendRedirect("/usuario/inicio");
                    break;

                case "ROLE_INSTRUCTOR":
                    response.sendRedirect("/admin/dashboard"); // o el que definas
                    break;

                default:
                    response.sendRedirect("/auth/login");
                    break;
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", 
                    "/auth/**",
                    "/css/**", "/imagenes/**",
                    "/Videos/**", "/js/**"
                ).permitAll()

                // 🔥 ADMIN
                .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")

                // 🔥 USUARIO
                .requestMatchers("/usuario/**").hasRole("USUARIO")

                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/login") // 👈 AGREGA ESTO
                .successHandler(successHandler())
                .failureUrl("/auth/login?error=true")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .permitAll()
            )

            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}