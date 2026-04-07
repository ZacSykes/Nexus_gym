package com.proyecto.nexus.usuario.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyecto.nexus.usuario.dto.RegistroDTO;
import com.proyecto.nexus.usuario.model.DatosUsuario;
import com.proyecto.nexus.usuario.model.Perfil;
import com.proyecto.nexus.usuario.model.Rol;
import com.proyecto.nexus.usuario.repository.DatosUsuarioRepository;
import com.proyecto.nexus.usuario.repository.PerfilRepository;
import com.proyecto.nexus.usuario.repository.RolRepository;
import com.proyecto.nexus.usuario.repository.GimnasioRepository;   // ← Importar
import com.proyecto.nexus.usuario.model.Gimnasio;     
import java.util.List;            // ← Importar

import jakarta.transaction.Transactional;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final DatosUsuarioRepository datosUsuarioRepository;
    private final PerfilRepository perfilRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final GimnasioRepository gimnasioRepository;   // ← Nueva dependencia

    // Constructor actualizado
    public AuthController(DatosUsuarioRepository datosUsuarioRepository,
                          PerfilRepository perfilRepository,
                          RolRepository rolRepository,
                          PasswordEncoder passwordEncoder,
                          GimnasioRepository gimnasioRepository) {   // ← Nuevo parámetro
        this.datosUsuarioRepository = datosUsuarioRepository;
        this.perfilRepository = perfilRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.gimnasioRepository = gimnasioRepository;   // ← Inicializar
    }
    
    @GetMapping("/login")
    public String mostrarLogin() {
        return "auth/login";
    }

    
        @GetMapping("/api/gimnasios")
        @ResponseBody
        public List<Gimnasio> listarGimnasios() {
            return gimnasioRepository.findAll();
}

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
    model.addAttribute("gimnasios", gimnasioRepository.findAll());
    return "auth/registro";
}

    @GetMapping("/registroAdmin")
    public String mostrarRegistroAdmin() {
        return "auth/registroAdmin";
    }

    @PostMapping("/registrar")
    @Transactional
    public String registrar(@ModelAttribute RegistroDTO registro,
                            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("🔥 ENTRÓ AL REGISTRO");
            System.out.println("Cédula: " + registro.getCedula());

            // ===== VALIDACIONES =====
            if (registro.getCedula() == null || registro.getCedula().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "La cédula es obligatoria");
                return "redirect:/auth/login";
            }

            Long cedula;
            try {
                cedula = Long.parseLong(registro.getCedula());
            } catch (NumberFormatException e) {
                redirectAttributes.addFlashAttribute("error", "La cédula debe ser numérica");
                return "redirect:/auth/login";
            }

            if (!registro.getPassword().equals(registro.getConfirmarPassword())) {
                redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
                return "redirect:/auth/login";
            }

            if (datosUsuarioRepository.findByCedula(cedula).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Ya existe un usuario con esta cédula");
                return "redirect:/auth/login";
            }

            if (datosUsuarioRepository.findByCorreo(registro.getCorreo()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "El correo ya está registrado");
                return "redirect:/auth/login";
            }

            // Validar que se haya seleccionado un gimnasio
            if (registro.getGimnasioId() == null) {
                redirectAttributes.addFlashAttribute("error", "Debes seleccionar un gimnasio");
                return "redirect:/auth/login";
            }

            // ===== CREAR USUARIO =====
            DatosUsuario usuario = new DatosUsuario();
            usuario.setCedula(cedula);
            usuario.setNombre(registro.getNombre());
            usuario.setApellido(registro.getApellido());
            usuario.setCorreo(registro.getCorreo());
            usuario.setTelefono(registro.getTelefono());
            usuario.setDireccion(registro.getDireccion());
            usuario.setGenero(registro.getGenero());

            if (registro.getFechaNacimiento() != null && !registro.getFechaNacimiento().isEmpty()) {
                usuario.setFechaNacimiento(LocalDate.parse(registro.getFechaNacimiento()));
            }

            usuario.setClasesTotales(0);
            usuario.setFechaRegistro(LocalDateTime.now());
            usuario.setEstado("ACTIVO");

            // Asignar el gimnasio al usuario
            Gimnasio gimnasio = gimnasioRepository.findById(registro.getGimnasioId())
                    .orElseThrow(() -> new RuntimeException("Gimnasio no encontrado con ID: " + registro.getGimnasioId()));
            usuario.setGimnasio(gimnasio);   // ← Relación ManyToOne

           usuario = datosUsuarioRepository.save(usuario);
            System.out.println("✅ Usuario guardado con ID: " + usuario.getIdUsuario());

            // ===== CREAR PERFIL =====
            Rol rol = rolRepository.findByNombre("ROLE_USUARIO")
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

            Perfil perfil = new Perfil();
            perfil.setNombreUsuario(registro.getCedula());
            perfil.setContrasena(passwordEncoder.encode(registro.getPassword()));
            perfil.setUsuario(usuario);
            perfil.setRol(rol);
            perfil.setCreatedAt(LocalDateTime.now());

            perfilRepository.save(perfil);
            System.out.println("✅ Perfil creado correctamente");

            redirectAttributes.addFlashAttribute("exito",
                    "Cuenta creada exitosamente. Ahora puedes iniciar sesión.");

            return "redirect:/auth/login";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Error al registrar: " + e.getMessage());
            return "redirect:/auth/login";
        }
    }
}