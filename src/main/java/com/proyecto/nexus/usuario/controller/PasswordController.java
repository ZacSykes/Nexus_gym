package com.proyecto.nexus.usuario.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.proyecto.nexus.usuario.service.PasswordService;

@Controller
public class PasswordController {

    @Autowired
    private PasswordService passwordService;

    @PostMapping("/recuperar-password")
    public String recuperar(@RequestParam String email,
                            @RequestParam String cedula,
                            Model model) {

        try {
            passwordService.enviarToken(email, cedula);
            model.addAttribute("mensaje", "Revisa tu correo");
        } catch (Exception e) {
            model.addAttribute("error", "Datos incorrectos");
        }
        System.out.println("ENTRÓ AL CONTROLLER");

        return "recuperar";
    }

    @GetMapping("/reset-password")
    public String mostrarReset(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String reset(@RequestParam String token,
                        @RequestParam String password) {

        passwordService.resetPassword(token, password);

        return "redirect:/login?resetExitoso";
    }

    

    @GetMapping("/recuperar")
    public String mostrarRecuperar() {
        return "recuperar";
    }
}