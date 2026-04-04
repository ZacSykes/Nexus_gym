package com.proyecto.nexus.web.controller;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.proyecto.nexus.clase.service.ClaseService;
import com.proyecto.nexus.usuario.model.DatosUsuario;
import com.proyecto.nexus.usuario.repository.DatosUsuarioRepository;

@Controller
public class HomeController {

    private final DatosUsuarioRepository datosUsuarioRepository;
    private final ClaseService claseService;

    public HomeController(DatosUsuarioRepository datosUsuarioRepository,
                          ClaseService claseService) {
        this.datosUsuarioRepository = datosUsuarioRepository;
        this.claseService = claseService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    // ===== ADMIN =====
    @GetMapping("/admin/dashboard")
    public String dashboardAdmin() {
        return "admin/dashboard";
    }

    // ===== USUARIO =====
    @GetMapping("/usuario/inicio")
    public String principalUsuario(Model model, Authentication auth) {

        if (auth == null) {
            return "redirect:/auth/login";
        }

        String cedula = auth.getName();

        Optional<DatosUsuario> usuarioOpt =
                datosUsuarioRepository.findByCedula(Long.parseLong(cedula));

        if (usuarioOpt.isPresent()) {

            DatosUsuario usuario = usuarioOpt.get();

            model.addAttribute("usuario", usuario);

            claseService.obtenerPaqueteActivo(usuario)
                    .ifPresent(paquete -> model.addAttribute("paquete", paquete));

            model.addAttribute("reservas",
                    claseService.obtenerReservasProximas(usuario));
        }

        return "usuario/inicio";
    }
}