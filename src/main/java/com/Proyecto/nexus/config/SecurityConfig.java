package com.Proyecto.nexus.config;

import com.Proyecto.nexus.service.UsuarioDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;
            
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

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                    HttpServletResponse response,
                    Authentication authentication) throws IOException {

                if (authentication.getAuthorities().contains(
                        new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))) {
                    response.sendRedirect("/dashboardAdmin");

                } else if (authentication.getAuthorities().contains(
                        new SimpleGrantedAuthority("ROLE_USUARIO"))) {
                    response.sendRedirect("/principalUsuario");

                } else if (authentication.getAuthorities().contains(
                        new SimpleGrantedAuthority("ROLE_INSTRUCTOR"))) {
                    response.sendRedirect("/dashboardInstructor");

                } else {
                    response.sendRedirect("/");
                }
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/recuperar",
                                 "/css/**", "/imagenes/**",
                                 "/Videos/**", "/js/**").permitAll()
                .requestMatchers("/dashboardAdmin/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/principalUsuario/**",
                                 "/clases/**", "/reservas/**",
                                 "/perfil/**", "/paquetes/**").hasRole("USUARIO")
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