package com.proyecto.nexus.pago.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyecto.nexus.clase.model.PaqueteClase;
import com.proyecto.nexus.clase.service.ClaseService;
import com.proyecto.nexus.pago.model.Pago;
import com.proyecto.nexus.pago.repository.PagoRepository;
import com.proyecto.nexus.plan.model.Plan;
import com.proyecto.nexus.plan.repository.PlanRepository;
import com.proyecto.nexus.usuario.model.DatosUsuario;
import com.proyecto.nexus.usuario.repository.DatosUsuarioRepository;

@Controller
@RequestMapping("/usuario")
public class PagoController {

    private final PlanRepository planRepository;
    private final DatosUsuarioRepository datosUsuarioRepository;
    private final PagoRepository pagoRepository;
    private final ClaseService claseService;

    public PagoController(PlanRepository planRepository,
                          DatosUsuarioRepository datosUsuarioRepository,
                          PagoRepository pagoRepository,
                          ClaseService claseService) {
        this.planRepository = planRepository;
        this.datosUsuarioRepository = datosUsuarioRepository;
        this.pagoRepository = pagoRepository;
        this.claseService = claseService;
    }

    // ==================== CHECKOUT ====================

    @GetMapping("/checkout")
    public String checkout(@RequestParam Integer idPlan,
                           Model model,
                           Authentication auth) {

        Long cedula = obtenerCedula(auth);

        if (cedula == null) {
            return "redirect:/auth/login";
        }

        Optional<DatosUsuario> usuarioOpt =
                datosUsuarioRepository.findByCedula(cedula);

        Optional<Plan> planOpt =
                planRepository.findById(idPlan);

        if (usuarioOpt.isEmpty() || planOpt.isEmpty()) {
            return "redirect:/usuario/paquetes";
        }

        model.addAttribute("usuario", usuarioOpt.get());
        model.addAttribute("plan", planOpt.get());

        return "usuario/checkout";
    }

    // ==================== PROCESAR PAGO ====================

    @PostMapping("/procesar-pago")
    public String procesarPago(@RequestParam Integer idPlan,
                               @RequestParam String metodoPago,
                               @RequestParam(required = false) String referencia,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {

        Long cedula = obtenerCedula(auth);

        if (cedula == null) {
            return "redirect:/auth/login";
        }

        Optional<DatosUsuario> usuarioOpt =
                datosUsuarioRepository.findByCedula(cedula);

        Optional<Plan> planOpt =
                planRepository.findById(idPlan);

        if (usuarioOpt.isEmpty() || planOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Datos inválidos");
            return "redirect:/usuario/paquetes";
        }

        DatosUsuario usuario = usuarioOpt.get();
        Plan plan = planOpt.get();

        try {
            // ===== CREAR PAQUETE =====

            PaqueteClase paquete = new PaqueteClase();
            paquete.setUsuario(usuario);
            paquete.setTotalClases(plan.getTotalClases());
            paquete.setClasesRestantes(plan.getTotalClases());
            paquete.setIdPlanes(plan.getIdPlanes());
            paquete.setFechaInicio(LocalDateTime.now());
            paquete.setFechaVencimiento(LocalDateTime.now().plusDays(plan.getDuracionDias()));
            paquete.setEstado("ACTIVO");
            paquete.setCreatedAt(LocalDateTime.now());

            PaqueteClase paqueteGuardado = claseService.guardarPaquete(paquete);

            // ===== CREAR PAGO =====

            Pago pago = new Pago();
            pago.setUsuario(usuario);
            pago.setPaquete(paqueteGuardado);
            pago.setMonto(plan.getPrecio());
            pago.setEstado("PAGADO");
            pago.setMetodoPago(metodoPago);
            pago.setReferencia(referencia);
            pago.setFechaPago(LocalDateTime.now());
            pago.setFechaConfirmacion(LocalDateTime.now());

            pagoRepository.save(pago);

            redirectAttributes.addFlashAttribute("mensaje",
                    "Pago realizado exitosamente. Tu plan ha sido activado.");
            redirectAttributes.addFlashAttribute("tipo", "exito");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje",
                    "Error al procesar el pago");
            redirectAttributes.addFlashAttribute("tipo", "error");
        }

        return "redirect:/usuario/paquetes";
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