package com.proyecto.nexus.usuario.controller;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyecto.nexus.clase.repository.PaqueteClaseRepository;
import com.proyecto.nexus.usuario.model.DatosUsuario;
import com.proyecto.nexus.usuario.repository.DatosUsuarioRepository;

@Controller
@RequestMapping("/usuario")
public class PerfilController {

    private final DatosUsuarioRepository datosUsuarioRepository;
    private final PaqueteClaseRepository paqueteClaseRepository;

    public PerfilController(DatosUsuarioRepository datosUsuarioRepository,
                            PaqueteClaseRepository paqueteClaseRepository) {
        this.datosUsuarioRepository = datosUsuarioRepository;
        this.paqueteClaseRepository = paqueteClaseRepository;
    }

    // ==================== VER PERFIL ====================

    @GetMapping("/perfil")
    public String perfil(Model model, Authentication auth) {

        Long cedula = obtenerCedula(auth);

        if (cedula == null) {
            return "redirect:/auth/login?error=cedula_invalida";
        }

        Optional<DatosUsuario> usuarioOpt =
                datosUsuarioRepository.findByCedula(cedula);

        if (usuarioOpt.isEmpty()) {
            return "redirect:/auth/login?error=usuario_no_encontrado";
        }

        DatosUsuario usuario = usuarioOpt.get();

        model.addAttribute("usuario", usuario);

        paqueteClaseRepository
                .findFirstByUsuarioAndEstadoOrderByFechaVencimientoAsc(usuario, "ACTIVO")
                .ifPresent(paquete -> model.addAttribute("paquete", paquete));

        return "usuario/perfil";
    }

    // ==================== ACTUALIZAR ====================

    @PostMapping("/perfil/actualizar")
    public String actualizar(@RequestParam String nombre,
                             @RequestParam String apellido,
                             @RequestParam String correo,
                             @RequestParam String telefono,
                             @RequestParam(required = false) String direccion,
                             @RequestParam(required = false) String genero,
                             @RequestParam(required = false) String condicionesMedicas,
                             @RequestParam(required = false) String alergias,
                             @RequestParam(required = false) String notasAdicionales,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {

        Long cedula = obtenerCedula(auth);

        if (cedula == null) {
            redirectAttributes.addFlashAttribute("mensaje", "Sesión inválida");
            redirectAttributes.addFlashAttribute("tipo", "error");
            return "redirect:/auth/login";
        }

        Optional<DatosUsuario> usuarioOpt =
                datosUsuarioRepository.findByCedula(cedula);

        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "Usuario no encontrado");
            redirectAttributes.addFlashAttribute("tipo", "error");
            return "redirect:/usuario/perfil";
        }

        DatosUsuario usuario = usuarioOpt.get();

        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setCorreo(correo);
        usuario.setTelefono(telefono);
        usuario.setDireccion(direccion);
        usuario.setGenero(genero);
        usuario.setCondicionesMedicas(condicionesMedicas);
        usuario.setAlergias(alergias);
        usuario.setNotasAdicionales(notasAdicionales);

        datosUsuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado correctamente");
        redirectAttributes.addFlashAttribute("tipo", "exito");

        return "redirect:/usuario/perfil";
    }

    // ==================== MÉTODO PRIVADO ====================

    private Long obtenerCedula(Authentication auth) {
        try {
            return Long.parseLong(auth.getName());
        } catch (Exception e) {
            return null;
        }
    }
}