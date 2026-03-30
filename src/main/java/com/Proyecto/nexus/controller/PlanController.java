package com.Proyecto.nexus.controller;

import com.Proyecto.nexus.model.DatosUsuario;
import com.Proyecto.nexus.repository.DatosUsuarioRepository;
import com.Proyecto.nexus.service.ClaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/paquetes")
public class PlanController {

    @Autowired
    private ClaseService claseService;

    @Autowired
    private DatosUsuarioRepository datosUsuarioRepository;

    @GetMapping
    public String listarPlanes(Model model, Authentication auth) {
        String cedula = auth.getName();
        Optional<DatosUsuario> usuarioOpt = datosUsuarioRepository.findByCedula(Long.parseLong(cedula));
        usuarioOpt.ifPresent(usuario -> {
            model.addAttribute("usuario", usuario);
            claseService.obtenerPaqueteActivo(usuario).ifPresent(paquete -> model.addAttribute("paquete", paquete));
        });
        model.addAttribute("planes", claseService.obtenerPlanesActivos());
        return "Paquetes";
    }

   @PostMapping("/comprar")
public String comprar(@RequestParam Integer idPlan,
                      Authentication auth,
                      RedirectAttributes redirectAttributes) {
    return "redirect:/pago/checkout?idPlan=" + idPlan;
}
}