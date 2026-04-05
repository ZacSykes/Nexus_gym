package com.proyecto.nexus.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneradorHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123"; // Cambia por la contraseña que quieras
        String hash = encoder.encode(password);
        System.out.println("Hash BCrypt: " + hash);
    }
}