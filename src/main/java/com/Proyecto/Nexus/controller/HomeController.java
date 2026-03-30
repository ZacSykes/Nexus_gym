package com.Proyecto.Nexus.controller;

import com.Proyecto.Nexus.domain.Usuarios;
import com.Proyecto.Nexus.domain.Rol;
import com.Proyecto.Nexus.service.IusuariosService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class HomeController {

    private final IusuariosService usuarioService;

    public HomeController(IusuariosService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/interfazAdmin")
    public String GestionUsuarios(Model model, @RequestParam(required = false) String fil) {

        List<Usuarios> lista = usuarioService.listaUsuarios();

        if (fil != null && !fil.isEmpty()) {
            String filtro = fil.toLowerCase();

            lista = lista.stream()
                    .filter(u ->
                            (u.getNombre() != null && u.getNombre().toLowerCase().contains(filtro)) ||
                            (u.getApellido() != null && u.getApellido().toLowerCase().contains(filtro)) ||
                            (u.getGimnasio() != null && u.getGimnasio().toLowerCase().contains(filtro)) ||
                            (u.getDocumento() != null && u.getDocumento().contains(fil))
                    )
                    .toList();
        }

        model.addAttribute("usuarios", lista);
        return "interfazAdmin";
    }

   @GetMapping("/")
    public String index() {
        return "redirect:/interfazAdmin";
    }

    @GetMapping("/dashboardAdmin")
    public String dashboardAdmin() {
        return "dashboardAdmin";
    }


    @GetMapping("/guardarDemo")
    public String guardarDemo() {

        Usuarios nuevo = new Usuarios();
        nuevo.setNombre("Nuevo");
        nuevo.setApellido("Usuario");
        nuevo.setEdad(25);
        nuevo.setEmail("demo@nexus.com");
        nuevo.setGimnasio("Nexus Gym");
        nuevo.setDocumento("99999999");
        nuevo.setTelefono("3000000000");
        nuevo.setRol(Rol.USUARIO);

        usuarioService.guardarUsuario(nuevo);

        return "redirect:/interfazAdmin";
    }

        @GetMapping("/crearUsuario")
    public String crearUsuario(Model model) {
        model.addAttribute("usuario", new Usuarios());
        return "crearUsuario";

}

    @PostMapping("/guardarUsuario")
    public String guardarUsuario(@ModelAttribute Usuarios usuario) {
        usuarioService.guardarUsuario(usuario);
        return "redirect:/interfazAdmin";
    }

    @PostMapping("/editarUsuario")
    public String editarUsuarioSubmit(@ModelAttribute Usuarios usuario) {
        usuarioService.guardarUsuario(usuario);
        return "redirect:/interfazAdmin";
    }

    @PostMapping("/eliminarUsuario/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return "redirect:/interfazAdmin";
    }
}