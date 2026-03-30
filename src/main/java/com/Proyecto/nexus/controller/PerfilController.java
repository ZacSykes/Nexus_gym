package com.Proyecto.nexus.controller;

import com.Proyecto.nexus.model.DatosUsuario;
import com.Proyecto.nexus.repository.DatosUsuarioRepository;
import com.Proyecto.nexus.repository.PaqueteClaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;

@Controller
public class PerfilController {

    @Autowired
    private DatosUsuarioRepository datosUsuarioRepository;

    @Autowired
    private PaqueteClaseRepository paqueteClaseRepository;

    @GetMapping("/perfil")
    public String perfil(Model model, Authentication auth) {
        String cedula = auth.getName();
        System.out.println(">>> PerfilController - Cédula: " + cedula);
        try {
            Long cedulaLong = Long.parseLong(cedula);
            Optional<DatosUsuario> usuarioOpt = datosUsuarioRepository.findByCedula(cedulaLong);
            if (usuarioOpt.isPresent()) {
                DatosUsuario usuario = usuarioOpt.get();
                System.out.println("Usuario encontrado: " + usuario.getNombre());
                model.addAttribute("usuario", usuario);
                paqueteClaseRepository
                    .findFirstByUsuarioAndEstadoOrderByFechaVencimientoAsc(usuario, "ACTIVO")
                    .ifPresent(paquete -> model.addAttribute("paquete", paquete));
                return "Perfil";
            } else {
                System.out.println("Usuario NO encontrado para cédula: " + cedulaLong);
                return "redirect:/login?error=usuario_no_encontrado";
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: cédula no es un número válido: " + cedula);
            return "redirect:/login?error=cedula_invalida";
        }
    }

    @PostMapping("/perfil/actualizar")
    public String actualizar(@RequestParam String nombre,
                             @RequestParam String apellido,
                             @RequestParam String correo,
                             @RequestParam String telefono,
                             @RequestParam(required = false) String direccion,
                             @RequestParam(required = false) String genero,
                             @RequestParam(required = false) String condicionesMedicas,
                             @RequestParam(required = false) String alergias,
                             @RequestParam(required = false) String notasAdicionales,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {

        String cedula = auth.getName();
        Optional<DatosUsuario> usuarioOpt = datosUsuarioRepository
            .findByCedula(Long.parseLong(cedula));

        if (usuarioOpt.isPresent()) {
            DatosUsuario usuario = usuarioOpt.get();
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setCorreo(correo);
            usuario.setTelefono(telefono);
            usuario.setDireccion(direccion);
            usuario.setGenero(genero);
            usuario.setCondicionesMedicas(condicionesMedicas);
            usuario.setAlergias(alergias);
            usuario.setNotasAdicionales(notasAdicionales);
            datosUsuarioRepository.save(usuario);

            redirectAttributes.addFlashAttribute("mensaje", "✅ Perfil actualizado correctamente");
            redirectAttributes.addFlashAttribute("tipo", "exito");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "⚠️ Error al actualizar el perfil");
            redirectAttributes.addFlashAttribute("tipo", "error");
        }

        return "redirect:/perfil";
    }
}