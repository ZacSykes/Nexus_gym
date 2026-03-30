package com.Proyecto.nexus.controller;

import com.Proyecto.nexus.model.*;
import com.Proyecto.nexus.repository.*;
import com.Proyecto.nexus.service.ClaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/pago")
public class PagoController {

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private DatosUsuarioRepository datosUsuarioRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private ClaseService claseService;

    // Muestra la página de pago para un plan específico
    @GetMapping("/checkout")
    public String checkout(@RequestParam Integer idPlan, Model model, Authentication auth) {
        String cedula = auth.getName();
        Optional<DatosUsuario> usuarioOpt = datosUsuarioRepository.findByCedula(Long.parseLong(cedula));
        Optional<Plan> planOpt = planRepository.findById(idPlan);

        if (usuarioOpt.isEmpty() || planOpt.isEmpty()) {
            return "redirect:/paquetes?error=datos_invalidos";
        }

        model.addAttribute("usuario", usuarioOpt.get());
        model.addAttribute("plan", planOpt.get());
        return "CheckoutPago";
    }

    // Procesa el pago
    @PostMapping("/procesar")
    public String procesarPago(@RequestParam Integer idPlan,
                               @RequestParam String metodoPago,
                               @RequestParam(required = false) String referencia,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        String cedula = auth.getName();
        Optional<DatosUsuario> usuarioOpt = datosUsuarioRepository.findByCedula(Long.parseLong(cedula));
        Optional<Plan> planOpt = planRepository.findById(idPlan);

        if (usuarioOpt.isEmpty() || planOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Datos inválidos");
            return "redirect:/paquetes";
        }

        DatosUsuario usuario = usuarioOpt.get();
        Plan plan = planOpt.get();

        // Crear el paquete
        PaqueteClase paquete = new PaqueteClase();
        paquete.setUsuario(usuario);
        paquete.setTotalClases(plan.getTotalClases());
        paquete.setClasesRestantes(plan.getTotalClases());
        paquete.setIdPlanes(plan.getIdPlanes());
        paquete.setFechaInicio(LocalDateTime.now());
        paquete.setFechaVencimiento(LocalDateTime.now().plusDays(plan.getDuracionDias()));
        paquete.setEstado("ACTIVO");
        paquete.setCreatedAt(LocalDateTime.now());

        // Guardar paquete (necesitamos tener el ID para asociarlo al pago)
        PaqueteClase paqueteGuardado = claseService.guardarPaquete(paquete); // necesitamos un método en ClaseService

        // Crear el pago
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

        redirectAttributes.addFlashAttribute("mensaje", "✅ Pago realizado exitosamente. Tu plan ha sido activado.");
        redirectAttributes.addFlashAttribute("tipo", "exito");

        return "redirect:/paquetes";
    }
}