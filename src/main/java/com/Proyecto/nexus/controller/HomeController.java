package com.Proyecto.nexus.controller;
import com.Proyecto.nexus.model.DatosUsuario;
import com.Proyecto.nexus.model.Reserva;
import com.Proyecto.nexus.repository.DatosUsuarioRepository;
import com.Proyecto.nexus.service.ClaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private DatosUsuarioRepository datosUsuarioRepository;

    @Autowired
    private ClaseService claseService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "Login";
    }

    // ===== ADMIN =====
    @GetMapping("/dashboardAdmin")
    public String dashboardAdmin() {
        return "dashboardAdmin";
    }

    //@GetMapping("/GestionUsuarios")
    //public String GestionUsuarios(Model model, @RequestParam(required = false) String fil) {
        //List<Usuarios> lista = new ArrayList<>();

       // Usuarios u1 = new Usuarios();
        //u1.setId(1L); u1.setNombre("Juan"); u1.setApellido("Pérez");
       // u1.setEdad(30); u1.setEmail("juan@email.com");
        //u1.setGimnasio("Gimnasio A"); u1.setDocumento("12345678");
       // u1.setTelefono("+573001234567"); u1.setRol("alumno");
        //lista.add(u1);

        //Usuarios u2 = new Usuarios();
        //u2.setId(2L); u2.setNombre("Ana"); u2.setApellido("López");
        //u2.setEdad(25); u2.setEmail("ana@email.com");
       // u2.setGimnasio("Gimnasio B"); u2.setDocumento("87654321");
        //u2.setTelefono("+573007654321"); u2.setRol("admin");
        //lista.add(u2);

        //if (fil != null && !fil.isEmpty()) {
          //  lista = lista.stream()
           //     .filter(u -> u.getNombre().toLowerCase().contains(fil.toLowerCase()) ||
            //                 u.getApellido().toLowerCase().contains(fil.toLowerCase()) ||
             //                u.getGimnasio().toLowerCase().contains(fil.toLowerCase()) ||
              //               u.getDocumento().contains(fil))
               // .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
       // }

        //model.addAttribute("usuarios", lista);
        //return "interfazAdmin";
   // }

    // ===== USUARIO =====
@GetMapping("/principalUsuario")
public String principalUsuario(Model model, Authentication auth) {
    System.out.println(">>> CONTROLADOR principalUsuario llamado <<<");
    String cedula = auth.getName();
    Optional<DatosUsuario> usuarioOpt = datosUsuarioRepository.findByCedula(Long.parseLong(cedula));
    usuarioOpt.ifPresent(usuario -> {
        model.addAttribute("usuario", usuario);
        claseService.obtenerPaqueteActivo(usuario).ifPresent(paquete -> model.addAttribute("paquete", paquete));
        // Obtener las reservas activas (por ejemplo, próximas no canceladas)
        List<Reserva> reservas = claseService.obtenerReservasUsuario(usuario);
        // Puedes filtrar solo las que están en estado "Confirmada" o "Pendiente" y con fecha futura
        List<Reserva> proximas = reservas.stream()
            .filter(r -> (r.getEstado().equals("Confirmada") || r.getEstado().equals("Pendiente"))
                    && r.getFechaClase().isAfter(LocalDate.now().minusDays(1))) // o simplemente todas
            .collect(Collectors.toList());
        model.addAttribute("reservas", proximas);
    });
    return "PrincipalUsuario";
}

    @GetMapping("/recuperar")
    public String recuperar() {
        return "recuperar";
    }
}