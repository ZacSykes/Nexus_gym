package com.Proyecto.nexus.controller;

import com.Proyecto.nexus.domain.Usuarios;
import com.Proyecto.nexus.model.*;
import com.Proyecto.nexus.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class AdminController {

    @Autowired
    private DatosUsuarioRepository datosUsuarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/interfazAdmin")
    public String listarUsuarios(Model model, @RequestParam(required = false) String fil) {
        List<DatosUsuario> usuariosDB = datosUsuarioRepository.findAll();
        List<Usuarios> usuariosDTO = new ArrayList<>();

        for (DatosUsuario u : usuariosDB) {
            Usuarios dto = new Usuarios();
            dto.setIdUsuarios(u.getIdUsuario().longValue());
            dto.setNombre(u.getNombre());
            dto.setApellido(u.getApellido());
            dto.setEmail(u.getCorreo());
            dto.setTelefono(u.getTelefono());
            dto.setDocumento(String.valueOf(u.getCedula()));

            Optional<Perfil> perfilOpt = perfilRepository.findByNombreUsuario(String.valueOf(u.getCedula()));
            String rolNombre = perfilOpt.map(p -> p.getRol().getNombre()).orElse("USUARIO");
            dto.setRol(rolNombre);

            usuariosDTO.add(dto);
        }

        if (fil != null && !fil.isEmpty()) {
            usuariosDTO = usuariosDTO.stream()
                .filter(u -> u.getNombre().toLowerCase().contains(fil.toLowerCase()) ||
                             u.getApellido().toLowerCase().contains(fil.toLowerCase()) ||
                             u.getDocumento().contains(fil))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }

        model.addAttribute("usuarios", usuariosDTO);
        return "interfazAdmin";
    }

    @PostMapping("/guardarUsuario")
    public String guardarUsuario(@ModelAttribute Usuarios usuarioForm,
                                 @RequestParam String password,
                                 RedirectAttributes redirectAttributes) {
        System.out.println(">>> GUARDAR USUARIO llamado");
        System.out.println("Datos recibidos: " + usuarioForm);
        System.out.println("Password: " + password);

        try {
            // Validar cédula y correo únicos
            Long cedula;
            try {
                cedula = Long.parseLong(usuarioForm.getDocumento());
            } catch (NumberFormatException e) {
                redirectAttributes.addFlashAttribute("error", "La cédula debe ser un número válido");
                return "redirect:/interfazAdmin";
            }

            if (datosUsuarioRepository.findByCedula(cedula).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "La cédula ya está registrada");
                return "redirect:/interfazAdmin";
            }
            if (datosUsuarioRepository.findByCorreo(usuarioForm.getEmail()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "El correo ya está registrado");
                return "redirect:/interfazAdmin";
            }

            // Crear DatosUsuario
            DatosUsuario usuario = new DatosUsuario();
            usuario.setCedula(cedula);
            usuario.setNombre(usuarioForm.getNombre());
            usuario.setApellido(usuarioForm.getApellido());
            usuario.setCorreo(usuarioForm.getEmail());
            usuario.setTelefono(usuarioForm.getTelefono());
            usuario.setFechaRegistro(LocalDateTime.now());
            usuario.setEstado("ACTIVO");
            usuario.setClasesTotales(0);
            datosUsuarioRepository.save(usuario);
            System.out.println("DatosUsuario guardado con ID: " + usuario.getIdUsuario());

            // Crear Perfil
            Perfil perfil = new Perfil();
            perfil.setNombreUsuario(usuarioForm.getDocumento());
            perfil.setContraseña(passwordEncoder.encode(password));
            perfil.setUsuario(usuario);

            Rol rol = determinarRol(usuarioForm.getRol());
            perfil.setRol(rol);
            perfil.setCreatedAt(LocalDateTime.now());

            perfilRepository.save(perfil);
            System.out.println("Perfil guardado");

            redirectAttributes.addFlashAttribute("mensaje", "Usuario creado exitosamente");
        } catch (Exception e) {
            System.err.println("Error al guardar usuario: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al crear usuario: " + e.getMessage());
        }
        return "redirect:/interfazAdmin";
    }

    @PostMapping("/editarUsuario")
    public String editarUsuario(@ModelAttribute Usuarios usuarioForm,
                                @RequestParam(required = false) String password,
                                RedirectAttributes redirectAttributes) {
        try {
            Optional<DatosUsuario> usuarioOpt = datosUsuarioRepository.findById(usuarioForm.getIdUsuarios().intValue());
            if (usuarioOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
                return "redirect:/interfazAdmin";
            }

            DatosUsuario usuario = usuarioOpt.get();
            usuario.setNombre(usuarioForm.getNombre());
            usuario.setApellido(usuarioForm.getApellido());
            usuario.setCorreo(usuarioForm.getEmail());
            usuario.setTelefono(usuarioForm.getTelefono());
            datosUsuarioRepository.save(usuario);

            Optional<Perfil> perfilOpt = perfilRepository.findByNombreUsuario(String.valueOf(usuario.getCedula()));
            if (perfilOpt.isPresent()) {
                Perfil perfil = perfilOpt.get();
                perfil.setRol(determinarRol(usuarioForm.getRol()));
                if (password != null && !password.trim().isEmpty()) {
                    perfil.setContraseña(passwordEncoder.encode(password));
                }
                perfilRepository.save(perfil);
            }

            redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
        }
        return "redirect:/interfazAdmin";
    }

    @PostMapping("/eliminarUsuario/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<DatosUsuario> usuarioOpt = datosUsuarioRepository.findById(id.intValue());
            if (usuarioOpt.isPresent()) {
                DatosUsuario usuario = usuarioOpt.get();
                perfilRepository.findByNombreUsuario(String.valueOf(usuario.getCedula()))
                    .ifPresent(perfil -> perfilRepository.delete(perfil));
                datosUsuarioRepository.delete(usuario);
                redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado correctamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/interfazAdmin";
    }

    private Rol determinarRol(String rolStr) {
        switch (rolStr.toUpperCase()) {
            case "ADMIN":
                return rolRepository.findById(1).orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));
            case "INSTRUCTOR":
                return rolRepository.findById(2).orElseThrow(() -> new RuntimeException("Rol INSTRUCTOR no encontrado"));
            default:
                return rolRepository.findById(3).orElseThrow(() -> new RuntimeException("Rol USUARIO no encontrado"));
        }
    }
}