package com.proyecto.nexus.pago.controller;

import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.proyecto.nexus.clase.model.PaqueteClase;
import com.proyecto.nexus.clase.service.ClaseService;
import com.proyecto.nexus.pago.model.Pago;
import com.proyecto.nexus.pago.repository.PagoRepository;
import com.proyecto.nexus.plan.model.Plan;
import com.proyecto.nexus.plan.repository.PlanRepository;
import com.proyecto.nexus.usuario.model.DatosUsuario;
import com.proyecto.nexus.usuario.repository.DatosUsuarioRepository;
import org.springframework.http.ResponseEntity;
import com.proyecto.nexus.pago.service.FacturaService;

@Controller
@RequestMapping("/admin/pagos")
public class AdminPagoController {

    private final PlanRepository planRepository;
    private final DatosUsuarioRepository datosUsuarioRepository;
    private final PagoRepository pagoRepository;
    private final ClaseService claseService;
    private final FacturaService facturaService;

    public AdminPagoController(PlanRepository planRepository,
                          DatosUsuarioRepository datosUsuarioRepository,
                          PagoRepository pagoRepository,
                          ClaseService claseService,
                          FacturaService facturaService) {

    this.planRepository = planRepository;
    this.datosUsuarioRepository = datosUsuarioRepository;
    this.pagoRepository = pagoRepository;
    this.claseService = claseService;
    this.facturaService = facturaService;
}

    // ==================== VISTA INICIAL ====================
    @GetMapping
    public String vistaPagos(Model model){
        model.addAttribute("pagos", pagoRepository.findAll());
        model.addAttribute("planes", planRepository.findAll());
        return "admin/pagos";
    }

    // ==================== BUSCAR USUARIO ====================
    @GetMapping("/buscar")
    public String buscarUsuario(@RequestParam String cedula, Model model){

        Long cedulaLong = Long.parseLong(cedula);

        DatosUsuario usuario = datosUsuarioRepository
                .findByCedula(cedulaLong)
                .orElse(null);

        model.addAttribute("usuario", usuario);
        model.addAttribute("pagos", pagoRepository.findAll());
        model.addAttribute("planes", planRepository.findAll());

        return "admin/pagos";
    }

    // ==================== CREAR PAGO ====================
    @PostMapping("/crear")
    public String crearPago(@RequestParam Integer usuarioId,
                            @RequestParam Integer planId){

        DatosUsuario usuario = datosUsuarioRepository.findById(usuarioId).get();
        Plan plan = planRepository.findById(planId).get();

        // CREAR PAQUETE
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

        // CREAR PAGO
        Pago pago = new Pago();
        pago.setUsuario(usuario);
        pago.setPaquete(paqueteGuardado);
        pago.setMonto(plan.getPrecio());
        pago.setEstado("PAGADO");
        pago.setMetodoPago("ADMIN");
        pago.setFechaPago(LocalDateTime.now());
        pago.setFechaConfirmacion(LocalDateTime.now());

        pagoRepository.save(pago);

        return "redirect:/admin/pagos";
    }

    @GetMapping("/factura/{id}")
    public ResponseEntity<byte[]> generarFactura(@PathVariable Integer id){

        Pago pago = pagoRepository.findById(id).orElseThrow();

        byte[] pdf = facturaService.generarFactura(pago);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=factura_" + id + ".pdf")
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }
}