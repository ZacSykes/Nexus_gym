package com.Proyecto.nexus.controller;

import com.Proyecto.nexus.dto.RegistroDTO;
import com.Proyecto.nexus.model.DatosUsuario;
import com.Proyecto.nexus.model.Perfil;
import com.Proyecto.nexus.model.Rol;
import com.Proyecto.nexus.repository.DatosUsuarioRepository;
import com.Proyecto.nexus.repository.PerfilRepository;
import com.Proyecto.nexus.repository.RolRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private DatosUsuarioRepository datosUsuarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "registro";
    }

    @PostMapping("/registrar")
    @Transactional
    public String registrar(RegistroDTO registro, RedirectAttributes redirectAttributes) {
        System.out.println(">>> REGISTRO: LLEGÓ AL CONTROLADOR <<<");
        System.out.println("=== INICIO REGISTRO ===");
        System.out.println("Cédula: " + registro.getCedula());
        System.out.println("Correo: " + registro.getCorreo());

        // 1. Validar que las contraseñas coincidan
        if (!registro.getPassword().equals(registro.getConfirmarPassword())) {
            System.out.println("Contraseñas no coinciden");
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
            return "redirect:/login";
        }

        // 2. Validar que la cédula no exista
        if (datosUsuarioRepository.findByCedula(Long.parseLong(registro.getCedula())).isPresent()) {
            System.out.println("Cédula ya existe");
            redirectAttributes.addFlashAttribute("error", "Ya existe un usuario con esta cédula");
            return "redirect:/login";
        }

        // 3. Validar que el correo no exista
        if (datosUsuarioRepository.findByCorreo(registro.getCorreo()).isPresent()) {
            System.out.println("Correo ya existe");
            redirectAttributes.addFlashAttribute("error", "Ya existe un usuario con este correo");
            return "redirect:/login";
        }

        try {
            // 4. Crear DatosUsuario
            DatosUsuario usuario = new DatosUsuario();
            usuario.setCedula(Long.parseLong(registro.getCedula()));
            usuario.setNombre(registro.getNombre());
            usuario.setApellido(registro.getApellido());
            usuario.setCorreo(registro.getCorreo());
            usuario.setTelefono(registro.getTelefono());
            usuario.setDireccion(registro.getDireccion());
            usuario.setGenero(registro.getGenero());

            if (registro.getFechaNacimiento() != null && !registro.getFechaNacimiento().isEmpty()) {
                usuario.setFechaNacimiento(LocalDate.parse(registro.getFechaNacimiento()));
            }

            usuario.setClasesTotales(0);
            usuario.setFechaRegistro(LocalDateTime.now());
            usuario.setEstado("ACTIVO");
            usuario.setRango(null);  // se asignará después si es necesario

            System.out.println("Guardando datos de usuario...");
            datosUsuarioRepository.save(usuario);
            System.out.println("Datos de usuario guardado con ID: " + usuario.getIdUsuario());

            // 5. Crear Perfil
            Perfil perfil = new Perfil();
            perfil.setNombreUsuario(registro.getCedula());
            perfil.setContraseña(passwordEncoder.encode(registro.getPassword()));
            perfil.setUsuario(usuario);

            // Buscar rol USUARIO (id_rol = 3)
            Rol rol = rolRepository.findById(3)
                .orElseThrow(() -> new RuntimeException("Rol USUARIO no encontrado"));
            perfil.setRol(rol);
            perfil.setCreatedAt(LocalDateTime.now());

            System.out.println("Guardando perfil...");
            perfilRepository.save(perfil);
            System.out.println("Perfil guardado exitosamente");

            redirectAttributes.addFlashAttribute("exito", "Cuenta creada exitosamente. Ahora puedes iniciar sesión.");
            return "redirect:/login";

        } catch (Exception e) {
            System.err.println("Error durante el registro: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al registrar: " + e.getMessage());
            return "redirect:/login";
        }
    }
}