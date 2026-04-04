package com.proyecto.nexus.plan.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyecto.nexus.clase.service.ClaseService;
import com.proyecto.nexus.usuario.model.DatosUsuario;
import com.proyecto.nexus.usuario.repository.DatosUsuarioRepository;

@Controller
@RequestMapping("/usuario")
public class PlanController {

    @Autowired
    private ClaseService claseService;

    @Autowired
    private DatosUsuarioRepository datosUsuarioRepository;

    @GetMapping("/paquetes")
    public String listarPlanes(Model model, Authentication auth) {

        if (auth == null) {
            return "redirect:/auth/login";
        }

        String cedula = auth.getName();

        Optional<DatosUsuario> usuarioOpt =
                datosUsuarioRepository.findByCedula(Long.parseLong(cedula));

        usuarioOpt.ifPresent(usuario -> {
            model.addAttribute("usuario", usuario);

            claseService.obtenerPaqueteActivo(usuario)
                    .ifPresent(paquete -> model.addAttribute("paquete", paquete));
        });

        model.addAttribute("planes", claseService.obtenerPlanesActivos());

        return "usuario/paquetes";
    }

    @PostMapping("/comprar")
    public String comprar(@RequestParam Integer idPlan,
                          Authentication auth,
                          RedirectAttributes redirectAttributes) {

        if (auth == null) {
            return "redirect:/auth/login";
        }

        return "redirect:/usuario/checkout?idPlan=" + idPlan;
    }
}