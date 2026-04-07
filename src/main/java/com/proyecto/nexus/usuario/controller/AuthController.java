package com.proyecto.nexus.usuario.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyecto.nexus.usuario.dto.RegistroDTO;
import com.proyecto.nexus.usuario.model.DatosUsuario;
import com.proyecto.nexus.usuario.model.Perfil;
import com.proyecto.nexus.usuario.model.Rol;
import com.proyecto.nexus.usuario.repository.DatosUsuarioRepository;
import com.proyecto.nexus.usuario.repository.PerfilRepository;
import com.proyecto.nexus.usuario.repository.RolRepository;

import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final int MIN_EDAD = 13;
    private static final int MAX_NOMBRE_APELLIDO = 60;
    private static final int MAX_DIRECCION = 120;

    private static final Pattern CEDULA_PATTERN = Pattern.compile("^\\d{6,12}$");
    private static final Pattern NOMBRE_PATTERN =
        Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñÜü]+(?:\\s+[A-Za-zÁÉÍÓÚáéíóúÑñÜü]+)*$");
    private static final Pattern TELEFONO_PATTERN = Pattern.compile("^3\\d{9}$");
    private static final Pattern CORREO_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$");

    private static final Set<String> CORREOS_DESECHABLES = Set.of(
        "mailinator.com", "yopmail.com", "guerrillamail.com", "10minutemail.com", "tempmail.com");

    private static final Set<String> PASSWORDS_COMUNES = Set.of(
        "12345678", "password", "qwerty123", "admin123", "123456789", "abc12345");

    private final DatosUsuarioRepository datosUsuarioRepository;
    private final PerfilRepository perfilRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(DatosUsuarioRepository datosUsuarioRepository,
                          PerfilRepository perfilRepository,
                          RolRepository rolRepository,
                          PasswordEncoder passwordEncoder) {
        this.datosUsuarioRepository = datosUsuarioRepository;
        this.perfilRepository = perfilRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @GetMapping("/login")
    public String mostrarLogin() {
        return "auth/login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "auth/registro"; // ✅ corregido
    }

    @GetMapping("/registroAdmin")
public String mostrarRegistroAdmin() {
    return "auth/registroAdmin"; // Nombre de tu vista HTML para registro de admin
}


    @PostMapping("/registrar")
    @Transactional
    public String registrar(@ModelAttribute RegistroDTO registro,
                            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("🔥 ENTRÓ AL REGISTRO");
            System.out.println("Cédula: " + registro.getCedula());

            String nombre = normalizarTexto(registro.getNombre());
            String apellido = normalizarTexto(registro.getApellido());
            String cedulaTexto = normalizarTexto(registro.getCedula());
            String telefono = normalizarTexto(registro.getTelefono());
            String correo = normalizarCorreo(registro.getCorreo());
            String direccion = normalizarTexto(registro.getDireccion());
            String password = registro.getPassword() == null ? "" : registro.getPassword().trim();
            String confirmarPassword = registro.getConfirmarPassword() == null
                    ? ""
                    : registro.getConfirmarPassword().trim();

            registro.setNombre(nombre);
            registro.setApellido(apellido);
            registro.setCedula(cedulaTexto);
            registro.setTelefono(telefono);
            registro.setCorreo(correo);
            registro.setDireccion(direccion);
            registro.setPassword(password);
            registro.setConfirmarPassword(confirmarPassword);

            // ===== VALIDACIONES =====

            if (nombre == null || nombre.length() < 2 || nombre.length() > MAX_NOMBRE_APELLIDO
                    || !NOMBRE_PATTERN.matcher(nombre).matches()) {
                redirectAttributes.addFlashAttribute("error", "El nombre debe tener entre 2 y 60 caracteres y solo letras");
                return "redirect:/auth/login";
            }

            if (apellido == null || apellido.length() < 2 || apellido.length() > MAX_NOMBRE_APELLIDO
                    || !NOMBRE_PATTERN.matcher(apellido).matches()) {
                redirectAttributes.addFlashAttribute("error", "El apellido debe tener entre 2 y 60 caracteres y solo letras");
                return "redirect:/auth/login";
            }

            if (cedulaTexto == null || cedulaTexto.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "La cédula es obligatoria");
                return "redirect:/auth/login";
            }

            if (!CEDULA_PATTERN.matcher(cedulaTexto).matches()) {
                redirectAttributes.addFlashAttribute("error", "La cédula debe tener entre 6 y 12 dígitos numéricos");
                return "redirect:/auth/login";
            }

            if (esSecuenciaNumerica(cedulaTexto) || sonTodosIguales(cedulaTexto)) {
                redirectAttributes.addFlashAttribute("error", "La cédula ingresada no parece válida");
                return "redirect:/auth/login";
            }

            if (telefono != null
                    && !telefono.isBlank()
                    && !TELEFONO_PATTERN.matcher(telefono).matches()) {
                redirectAttributes.addFlashAttribute("error", "El teléfono debe tener 10 dígitos y empezar por 3");
                return "redirect:/auth/login";
            }

            if (telefono != null
                    && !telefono.isBlank()
                    && datosUsuarioRepository.findByTelefono(telefono).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "El teléfono ya está registrado");
                return "redirect:/auth/login";
            }

            if (correo == null || correo.length() > 254 || !CORREO_PATTERN.matcher(correo).matches()) {
                redirectAttributes.addFlashAttribute("error", "Correo inválido. Ejemplo válido: usuario@email.com");
                return "redirect:/auth/login";
            }

            String dominioCorreo = correo.substring(correo.indexOf('@') + 1);
            if (CORREOS_DESECHABLES.contains(dominioCorreo)) {
                redirectAttributes.addFlashAttribute("error", "No se permiten correos temporales/desechables");
                return "redirect:/auth/login";
            }

            if (direccion != null && direccion.length() > MAX_DIRECCION) {
                redirectAttributes.addFlashAttribute("error", "La dirección no puede superar los 120 caracteres");
                return "redirect:/auth/login";
            }

            if (registro.getFechaNacimiento() == null || registro.getFechaNacimiento().isBlank()) {
                redirectAttributes.addFlashAttribute("error", "La fecha de nacimiento es obligatoria");
                return "redirect:/auth/login";
            }

            LocalDate fechaNacimiento;
            try {
                fechaNacimiento = LocalDate.parse(registro.getFechaNacimiento());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "La fecha de nacimiento no es válida");
                return "redirect:/auth/login";
            }

            if (fechaNacimiento.isAfter(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("error", "La fecha de nacimiento no puede ser futura");
                return "redirect:/auth/login";
            }

            if (Period.between(fechaNacimiento, LocalDate.now()).getYears() < MIN_EDAD) {
                redirectAttributes.addFlashAttribute("error", "Debes tener al menos 13 años para registrarte");
                return "redirect:/auth/login";
            }

            Long cedula;

            try {
                cedula = Long.parseLong(cedulaTexto);
            } catch (NumberFormatException e) {
                redirectAttributes.addFlashAttribute("error", "La cédula debe ser numérica");
                return "redirect:/auth/login";
            }

            if (password.isBlank() || confirmarPassword.isBlank() || !password.equals(confirmarPassword)) {
                redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
                return "redirect:/auth/login";
            }

            if (!PASSWORD_PATTERN.matcher(password).matches()) {
                redirectAttributes.addFlashAttribute("error",
                        "La contraseña debe tener 8-72 caracteres, mayúscula, minúscula, número y símbolo");
                return "redirect:/auth/login";
            }

            if (PASSWORDS_COMUNES.contains(password.toLowerCase(Locale.ROOT))) {
                redirectAttributes.addFlashAttribute("error", "La contraseña es demasiado común. Usa una más segura");
                return "redirect:/auth/login";
            }

            if (datosUsuarioRepository.findByCedula(cedula).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Ya existe un usuario con esta cédula");
                return "redirect:/auth/login";
            }

            if (datosUsuarioRepository.findByCorreo(correo).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "El correo ya está registrado");
                return "redirect:/auth/login";
            }

            // ===== CREAR USUARIO =====

            DatosUsuario usuario = new DatosUsuario();
            usuario.setCedula(cedula);
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setCorreo(correo);
            usuario.setTelefono(telefono);
            usuario.setDireccion(direccion);
            usuario.setGenero(registro.getGenero());
            usuario.setFechaNacimiento(fechaNacimiento);

            usuario.setClasesTotales(0);
            usuario.setFechaRegistro(LocalDateTime.now());
            usuario.setEstado("ACTIVO");

            usuario = datosUsuarioRepository.save(usuario);

            System.out.println("✅ Usuario guardado con ID: " + usuario.getIdUsuario());

            // ===== CREAR PERFIL =====

            Rol rol = rolRepository.findByNombre("ROLE_USUARIO")
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

            Perfil perfil = new Perfil();
            perfil.setNombreUsuario(cedulaTexto);
            perfil.setContrasena(passwordEncoder.encode(password));
            perfil.setUsuario(usuario);
            perfil.setRol(rol);
            perfil.setCreatedAt(LocalDateTime.now());

            perfilRepository.save(perfil);

            System.out.println("✅ Perfil creado correctamente");

            redirectAttributes.addFlashAttribute("exito",
                    "Cuenta creada exitosamente. Ahora puedes iniciar sesión.");

            return "redirect:/auth/login"; // ✅ corregido

        } catch (Exception e) {
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("error",
                    "Error al registrar: " + e.getMessage());

            return "redirect:/auth/login"; // ✅ corregido
        }
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }
        return valor.trim().replaceAll("\\s+", " ");
    }

    private String normalizarCorreo(String correo) {
        if (correo == null) {
            return null;
        }
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private boolean sonTodosIguales(String valor) {
        char primero = valor.charAt(0);
        for (int i = 1; i < valor.length(); i++) {
            if (valor.charAt(i) != primero) {
                return false;
            }
        }
        return true;
    }

    private boolean esSecuenciaNumerica(String valor) {
        boolean ascendente = true;
        boolean descendente = true;

        for (int i = 1; i < valor.length(); i++) {
            int actual = valor.charAt(i) - '0';
            int anterior = valor.charAt(i - 1) - '0';

            if (actual != anterior + 1) {
                ascendente = false;
            }
            if (actual != anterior - 1) {
                descendente = false;
            }
        }
        return ascendente || descendente;
    }
}