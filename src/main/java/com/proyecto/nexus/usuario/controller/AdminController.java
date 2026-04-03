package com.proyecto.nexus.usuario.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.proyecto.nexus.usuario.dto.UsuarioDTO;
import com.proyecto.nexus.usuario.service.AdminService;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/interfazAdmin")
    public String listarUsuarios(Model model,
                                @RequestParam(required = false) String fil) {

        model.addAttribute("usuarios", adminService.listarUsuarios(fil));

        return "interfazAdmin";
    }

    @PostMapping("/guardarUsuario")
    public String guardarUsuario(@ModelAttribute UsuarioDTO usuarioForm,
                                @RequestParam String password,
                                RedirectAttributes redirectAttributes) {

        String resultado = adminService.crearUsuario(usuarioForm, password);

        if (resultado.equals("ok")) {
            redirectAttributes.addFlashAttribute("mensaje", "Usuario creado correctamente");
        } else {
            redirectAttributes.addFlashAttribute("error", resultado);
        }

        return "redirect:/interfazAdmin";
    }

    @PostMapping("/editarUsuario")
    public String editarUsuario(@ModelAttribute UsuarioDTO usuarioForm,
                               @RequestParam(required = false) String password,
                               RedirectAttributes redirectAttributes) {

        String resultado = adminService.editarUsuario(usuarioForm, password);

        if (resultado.equals("ok")) {
            redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado");
        } else {
            redirectAttributes.addFlashAttribute("error", resultado);
        }

        return "redirect:/interfazAdmin";
    }

    @PostMapping("/eliminarUsuario/{id}")
    public String eliminarUsuario(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {

        String resultado = adminService.eliminarUsuario(id);

        if (resultado.equals("ok")) {
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado");
        } else {
            redirectAttributes.addFlashAttribute("error", resultado);
        }

        return "redirect:/interfazAdmin";
    }
}