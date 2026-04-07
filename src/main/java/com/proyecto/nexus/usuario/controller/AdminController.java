package com.proyecto.nexus.usuario.controller;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;

import com.proyecto.nexus.usuario.dto.UsuarioDTO;
import com.proyecto.nexus.usuario.service.AdminService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model,
                                @RequestParam(required = false) String fil) {

        model.addAttribute("usuarios", adminService.listarUsuarios(fil));
        model.addAttribute("fil", fil);

        return "admin/usuarios";
    }

    @GetMapping("/usuarios/exportar")
    public void exportarUsuariosExcel(@RequestParam(required = false) String fil,
                                      HttpServletResponse response) throws IOException {

        var usuarios = adminService.listarUsuarios(fil);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=usuarios_nexus.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Usuarios");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Nombre");
            header.createCell(2).setCellValue("Apellido");
            header.createCell(3).setCellValue("Correo");
            header.createCell(4).setCellValue("Telefono");
            header.createCell(5).setCellValue("Documento");

            int rowIdx = 1;
            for (UsuarioDTO usuario : usuarios) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(usuario.getId() == null ? 0 : usuario.getId());
                row.createCell(1).setCellValue(usuario.getNombre() == null ? "" : usuario.getNombre());
                row.createCell(2).setCellValue(usuario.getApellido() == null ? "" : usuario.getApellido());
                row.createCell(3).setCellValue(usuario.getEmail() == null ? "" : usuario.getEmail());
                row.createCell(4).setCellValue(usuario.getTelefono() == null ? "" : usuario.getTelefono());
                row.createCell(5).setCellValue(usuario.getDocumento() == null ? "" : usuario.getDocumento());
            }

            for (int i = 0; i <= 5; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute UsuarioDTO usuarioForm,
                                @RequestParam String password,
                                RedirectAttributes redirectAttributes) {

        String resultado = adminService.crearUsuario(usuarioForm, password);

        if (resultado.equals("ok")) {
            redirectAttributes.addFlashAttribute("mensaje", "Usuario creado correctamente");
        } else {
            redirectAttributes.addFlashAttribute("error", resultado);
        }

        return "redirect:/admin/usuarios";
    }

    @PostMapping("/editar")
    public String editarUsuario(@ModelAttribute UsuarioDTO usuarioForm,
                               @RequestParam(required = false) String password,
                               RedirectAttributes redirectAttributes) {

        String resultado = adminService.editarUsuario(usuarioForm, password);

        if (resultado.equals("ok")) {
            redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado");
        } else {
            redirectAttributes.addFlashAttribute("error", resultado);
        }

        return "redirect:/admin/usuarios";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {

        String resultado = adminService.eliminarUsuario(id);

        if (resultado.equals("ok")) {
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado");
        } else {
            redirectAttributes.addFlashAttribute("error", resultado);
        }

        return "redirect:/admin/usuarios";
    }
}