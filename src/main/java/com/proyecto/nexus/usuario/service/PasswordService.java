package com.proyecto.nexus.usuario.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.proyecto.nexus.usuario.model.DatosUsuario;
import com.proyecto.nexus.usuario.model.PasswordResetToken;
import com.proyecto.nexus.usuario.model.Perfil;
import com.proyecto.nexus.usuario.repository.DatosUsuarioRepository;
import com.proyecto.nexus.usuario.repository.PasswordResetTokenRepository;
import com.proyecto.nexus.usuario.repository.PerfilRepository;

@Service
public class PasswordService {

    @Autowired
    private DatosUsuarioRepository usuarioRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 🔐 ENVIAR TOKEN
    public void enviarToken(String email, String cedula) {

        DatosUsuario usuario = usuarioRepository
                .findByCorreoAndCedula(email, Long.parseLong(cedula))
                .orElseThrow(() -> new RuntimeException("Datos incorrectos"));

        String token = UUID.randomUUID().toString();

        PasswordResetToken reset = new PasswordResetToken();
        reset.setToken(token);
        reset.setUsuario(usuario);
        reset.setFechaExpiracion(LocalDateTime.now().plusMinutes(30));

        tokenRepository.save(reset);

        String link = "http://localhost:8082/reset-password?token=" + token;

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(usuario.getCorreo());
        mail.setSubject("Recuperar contraseña - Nexus");
        mail.setText("Hola " + usuario.getNombre() +
                "\n\nHaz clic en el siguiente enlace:\n" + link +
                "\n\nEste enlace expira en 30 minutos.");

                System.out.println("ENTRÓ A ENVIAR TOKEN");
                System.out.println("Correo: " + email);

        try {
            mailSender.send(mail);
            System.out.println("CORREO ENVIADO CORRECTAMENTE");
        } catch (Exception e) {
            System.out.println("ERROR AL ENVIAR CORREO:");
            e.printStackTrace();
        }
            }

    // 🔑 RESET PASSWORD
    public void resetPassword(String token, String nuevaPassword) {

        PasswordResetToken reset = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (reset.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado");
        }

        DatosUsuario usuario = reset.getUsuario();

        Perfil perfil = perfilRepository.findByUsuario(usuario)
            .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

        perfil.setContrasena(passwordEncoder.encode(nuevaPassword));
        perfilRepository.save(perfil);

        tokenRepository.delete(reset);
    }
}