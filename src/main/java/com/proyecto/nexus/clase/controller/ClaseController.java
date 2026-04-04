package com.proyecto.nexus.clase.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyecto.nexus.clase.model.Clase;
import com.proyecto.nexus.clase.service.ClaseService;
import com.proyecto.nexus.usuario.model.DatosUsuario;
import com.proyecto.nexus.usuario.repository.DatosUsuarioRepository;

@Controller
public class ClaseController {

    private final ClaseService claseService;
    private final DatosUsuarioRepository datosUsuarioRepository;

    public ClaseController(ClaseService claseService,
                           DatosUsuarioRepository datosUsuarioRepository) {
        this.claseService = claseService;
        this.datosUsuarioRepository = datosUsuarioRepository;
    }

    // ==================== CLASES DISPONIBLES ====================

    @GetMapping("/usuario/clases")
    public String clases(Model model,
                         @RequestParam(required = false) String disciplina,
                         @RequestParam(required = false) String nivel,
                         Authentication auth) {

        Long cedula = obtenerCedula(auth);

        Optional<DatosUsuario> usuarioOpt = Optional.empty();
        if (cedula != null) {
            usuarioOpt = datosUsuarioRepository.findByCedula(cedula);
        }

        List<Clase> clases;

        if (disciplina != null && !disciplina.equals("todas")) {
            clases = claseService.filtrarPorDisciplina(disciplina);
        } else if (nivel != null && !nivel.equals("todos")) {
            clases = claseService.filtrarPorNivel(nivel);
        } else {
            clases = claseService.obtenerClasesDisponibles();
        }

        model.addAttribute("clases", clases);

        usuarioOpt.ifPresent(usuario -> {
            model.addAttribute("usuario", usuario);

            claseService.obtenerPaqueteActivo(usuario)
                    .ifPresent(paquete -> model.addAttribute("paquete", paquete));
        });

        model.addAttribute("disciplinaSeleccionada", disciplina);
        model.addAttribute("nivelSeleccionado", nivel);

        return "usuario/clases";
    }

    // ==================== RESERVAR ====================

    @PostMapping("/usuario/reservar")
    public String reservar(@RequestParam Integer idClase,
                           @RequestParam String fechaClase,
                           Authentication auth,
                           RedirectAttributes redirectAttributes) {

        String cedula = auth.getName();

        try {
            LocalDate fecha = LocalDate.parse(fechaClase);

            String resultado = claseService.reservarClase(idClase, cedula, fecha);

            if (resultado.equals("ok")) {
                redirectAttributes.addFlashAttribute("mensaje", "Clase reservada exitosamente");
                redirectAttributes.addFlashAttribute("tipo", "exito");
            } else {
                redirectAttributes.addFlashAttribute("mensaje", resultado.replace("error: ", ""));
                redirectAttributes.addFlashAttribute("tipo", "error");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al procesar la reserva");
            redirectAttributes.addFlashAttribute("tipo", "error");
        }

        return "redirect:/usuario/clases";

    }

    // ==================== MIS RESERVAS ====================

    @GetMapping("/usuario/reservas")
    public String reservas(Model model, Authentication auth) {

        Long cedula = obtenerCedula(auth);

        if (cedula == null) {
            return "redirect:/auth/login";
        }

        Optional<DatosUsuario> usuarioOpt =
                datosUsuarioRepository.findByCedula(cedula);

        usuarioOpt.ifPresent(usuario -> {
            model.addAttribute("usuario", usuario);

            model.addAttribute("reservas",
                    claseService.obtenerReservasUsuario(usuario));

            claseService.obtenerPaqueteActivo(usuario)
                    .ifPresent(paquete -> model.addAttribute("paquete", paquete));
        });

        return "usuario/reservas";
    }

    // ==================== CANCELAR ====================

    @PostMapping("/usuario/cancelarReserva")
    public String cancelar(@RequestParam Integer idReserva,
                           Authentication auth,
                           RedirectAttributes redirectAttributes) {

        String cedula = auth.getName();

        String resultado = claseService.cancelarReserva(idReserva, cedula);

        if (resultado.equals("ok")) {
            redirectAttributes.addFlashAttribute("mensaje", "Reserva cancelada");
            redirectAttributes.addFlashAttribute("tipo", "exito");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", resultado.replace("error: ", ""));
            redirectAttributes.addFlashAttribute("tipo", "error");
        }

        return "redirect:/usuario/reservas";
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