package com.Proyecto.nexus.controller;

import com.Proyecto.nexus.model.*;
import com.Proyecto.nexus.repository.DatosUsuarioRepository;
import com.Proyecto.nexus.service.ClaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class ClaseController {

    @Autowired
    private ClaseService claseService;

    @Autowired
    private DatosUsuarioRepository datosUsuarioRepository;

    // ===== CLASES DISPONIBLES =====
    @GetMapping("/clases")
    public String clases(Model model,
                         @RequestParam(required = false) String disciplina,
                         @RequestParam(required = false) String nivel,
                         Authentication auth) {

        // Obtener usuario autenticado
        String cedula = auth.getName();
        Optional<DatosUsuario> usuarioOpt = datosUsuarioRepository.findByCedula(
            Long.parseLong(cedula));

        // Obtener clases según filtros
        List<Clase> clases;
        if (disciplina != null && !disciplina.equals("todas")) {
            clases = claseService.filtrarPorDisciplina(disciplina);
        } else if (nivel != null && !nivel.equals("todos")) {
            clases = claseService.filtrarPorNivel(nivel);
        } else {
            clases = claseService.obtenerClasesDisponibles();
        }

        model.addAttribute("clases", clases);

        // Paquete activo
        usuarioOpt.ifPresent(usuario -> {
            model.addAttribute("usuario", usuario);
            claseService.obtenerPaqueteActivo(usuario).ifPresent(paquete ->
                model.addAttribute("paquete", paquete));
        });

        model.addAttribute("disciplinaSeleccionada", disciplina);
        model.addAttribute("nivelSeleccionado", nivel);

        return "ClasesDisponibles";
    }

    // ===== RESERVAR CLASE =====
   @PostMapping("/reservar")
public String reservar(@RequestParam Integer idClase,
                       @RequestParam String fechaClase,
                       Authentication auth,
                       RedirectAttributes redirectAttributes) {

    String cedula = auth.getName();
    try {
        LocalDate fecha = LocalDate.parse(fechaClase); // formato yyyy-MM-dd
        String resultado = claseService.reservarClase(idClase, cedula, fecha);

        if (resultado.equals("ok")) {
            redirectAttributes.addFlashAttribute("mensaje", "✅ Clase reservada exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "exito");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", resultado.replace("error: ", "⚠️ "));
            redirectAttributes.addFlashAttribute("tipo", "error");
        }
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("mensaje", "⚠️ Error al procesar la fecha");
        redirectAttributes.addFlashAttribute("tipo", "error");
    }

    return "redirect:/clases";
}

    // ===== MIS RESERVAS =====
    @GetMapping("/reservas")
    public String reservas(Model model, Authentication auth) {

        String cedula = auth.getName();
        Optional<DatosUsuario> usuarioOpt = datosUsuarioRepository.findByCedula(
            Long.parseLong(cedula));

        usuarioOpt.ifPresent(usuario -> {
            model.addAttribute("usuario", usuario);
            model.addAttribute("reservas",
                claseService.obtenerReservasUsuario(usuario));
            claseService.obtenerPaqueteActivo(usuario).ifPresent(paquete ->
                model.addAttribute("paquete", paquete));
        });

        return "MisReservas";
    }

    // ===== CANCELAR RESERVA =====
    @PostMapping("/cancelarReserva")
    public String cancelar(@RequestParam Integer idReserva,
                           Authentication auth,
                           RedirectAttributes redirectAttributes) {

        String cedula = auth.getName();
        String resultado = claseService.cancelarReserva(idReserva, cedula);

        if (resultado.equals("ok")) {
            redirectAttributes.addFlashAttribute("mensaje", "✅ Reserva cancelada");
            redirectAttributes.addFlashAttribute("tipo", "exito");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", resultado.replace("error: ", "⚠️ "));
            redirectAttributes.addFlashAttribute("tipo", "error");
        }

        return "redirect:/reservas";
    }
}