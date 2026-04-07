package com.proyecto.nexus.usuario.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.proyecto.nexus.usuario.dto.UsuarioDTO;
import com.proyecto.nexus.usuario.model.DatosUsuario;
import com.proyecto.nexus.usuario.model.Perfil;
import com.proyecto.nexus.usuario.model.Rol;
import com.proyecto.nexus.usuario.repository.DatosUsuarioRepository;
import com.proyecto.nexus.usuario.repository.PerfilRepository;
import com.proyecto.nexus.usuario.repository.RolRepository;

@Service
public class AdminService {

    private static final int MAX_NOMBRE_APELLIDO = 60;
    private static final int MAX_EMAIL = 254;
    private static final Pattern CEDULA_PATTERN = Pattern.compile("^\\d{6,12}$");
    private static final Pattern NOMBRE_PATTERN =
        Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñÜü]+(?:\\s+[A-Za-zÁÉÍÓÚáéíóúÑñÜü]+)*$");
    private static final Pattern TELEFONO_PATTERN = Pattern.compile("^3\\d{9}$");
    private static final Pattern CORREO_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$");

    private static final Set<String> PASSWORDS_COMUNES = Set.of(
        "12345678", "password", "qwerty123", "admin123", "123456789", "abc12345");

    private final DatosUsuarioRepository datosUsuarioRepository;
    private final PerfilRepository perfilRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(DatosUsuarioRepository datosUsuarioRepository,
                        PerfilRepository perfilRepository,
                        RolRepository rolRepository,
                        PasswordEncoder passwordEncoder) {
        this.datosUsuarioRepository = datosUsuarioRepository;
        this.perfilRepository = perfilRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ==================== LISTAR ====================

    public List<UsuarioDTO> listarUsuarios(String filtro) {

        List<DatosUsuario> usuariosDB = datosUsuarioRepository.findAll();
        List<UsuarioDTO> lista = new ArrayList<>();

        for (DatosUsuario u : usuariosDB) {

            UsuarioDTO dto = new UsuarioDTO();
            dto.setId(u.getIdUsuario().longValue());
            dto.setNombre(u.getNombre());
            dto.setApellido(u.getApellido());
            dto.setEmail(u.getCorreo());
            dto.setTelefono(u.getTelefono());
            dto.setDocumento(String.valueOf(u.getCedula()));

            Optional<Perfil> perfilOpt =
                    perfilRepository.findByNombreUsuario(String.valueOf(u.getCedula()));

            String rolNombre = perfilOpt
                    .map(p -> p.getRol().getNombre())
                    .orElse("ROLE_USUARIO");

            dto.setRol(rolNombre);

            lista.add(dto);
        }

        if (filtro != null && !filtro.isEmpty()) {
            return lista.stream()
                    .filter(u -> u.getNombre().toLowerCase().contains(filtro.toLowerCase()) ||
                                 u.getApellido().toLowerCase().contains(filtro.toLowerCase()) ||
                                 u.getDocumento().contains(filtro))
                    .toList();
        }

        return lista;
    }

    // ==================== CREAR ====================

    public String crearUsuario(UsuarioDTO dto, String password) {
        String nombre = normalizarTexto(dto.getNombre());
        String apellido = normalizarTexto(dto.getApellido());
        String documento = normalizarTexto(dto.getDocumento());
        String email = normalizarCorreo(dto.getEmail());
        String telefono = normalizarTexto(dto.getTelefono());
        String passwordLimpia = password == null ? "" : password.trim();

        String errorBasico = validarCamposBasicos(nombre, apellido, documento, email, telefono);
        if (errorBasico != null) {
            return errorBasico;
        }

        String errorPassword = validarPassword(passwordLimpia, true);
        if (errorPassword != null) {
            return errorPassword;
        }

        Long cedula = Long.parseLong(documento);

        if (datosUsuarioRepository.findByCedula(cedula).isPresent()) {
            return "La cédula ya existe";
        }

        if (datosUsuarioRepository.findByCorreo(email).isPresent()) {
            return "El correo ya existe";
        }

        if (datosUsuarioRepository.findByTelefono(telefono).isPresent()) {
            return "El teléfono ya está registrado";
        }

        // Crear usuario
        DatosUsuario usuario = new DatosUsuario();
        usuario.setCedula(cedula);
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setCorreo(email);
        usuario.setTelefono(telefono);
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setEstado("ACTIVO");
        usuario.setClasesTotales(0);

        datosUsuarioRepository.save(usuario);

        // Crear perfil
        Perfil perfil = new Perfil();
        perfil.setNombreUsuario(documento);
        perfil.setContrasena(passwordEncoder.encode(passwordLimpia));
        perfil.setUsuario(usuario);
        perfil.setRol(obtenerRol(dto.getRol()));
        perfil.setCreatedAt(LocalDateTime.now());

        perfilRepository.save(perfil);

        return "ok";
    }

    // ==================== EDITAR ====================

    public String editarUsuario(UsuarioDTO dto, String password) {

        if (dto.getId() == null) {
            return "Usuario inválido";
        }

        Optional<DatosUsuario> usuarioOpt =
                datosUsuarioRepository.findById(dto.getId().intValue());

        if (usuarioOpt.isEmpty()) {
            return "Usuario no encontrado";
        }

        DatosUsuario usuario = usuarioOpt.get();

        String nombre = normalizarTexto(dto.getNombre());
        String apellido = normalizarTexto(dto.getApellido());
        String email = normalizarCorreo(dto.getEmail());
        String telefono = normalizarTexto(dto.getTelefono());
        String passwordLimpia = password == null ? "" : password.trim();

        if (nombre == null || nombre.length() < 2 || nombre.length() > MAX_NOMBRE_APELLIDO
                || !NOMBRE_PATTERN.matcher(nombre).matches()) {
            return "El nombre debe tener entre 2 y 60 caracteres y solo letras";
        }

        if (apellido == null || apellido.length() < 2 || apellido.length() > MAX_NOMBRE_APELLIDO
                || !NOMBRE_PATTERN.matcher(apellido).matches()) {
            return "El apellido debe tener entre 2 y 60 caracteres y solo letras";
        }

        if (email == null || email.length() > MAX_EMAIL || !CORREO_PATTERN.matcher(email).matches()) {
            return "Correo inválido. Ejemplo válido: usuario@email.com";
        }

        if (telefono == null || !TELEFONO_PATTERN.matcher(telefono).matches()) {
            return "El teléfono debe tener 10 dígitos y empezar por 3";
        }

        Optional<DatosUsuario> otroConCorreo = datosUsuarioRepository.findByCorreo(email);
        if (otroConCorreo.isPresent() && !otroConCorreo.get().getIdUsuario().equals(usuario.getIdUsuario())) {
            return "El correo ya existe";
        }

        Optional<DatosUsuario> otroConTelefono = datosUsuarioRepository.findByTelefono(telefono);
        if (otroConTelefono.isPresent() && !otroConTelefono.get().getIdUsuario().equals(usuario.getIdUsuario())) {
            return "El teléfono ya está registrado";
        }

        if (!passwordLimpia.isBlank()) {
            String errorPassword = validarPassword(passwordLimpia, false);
            if (errorPassword != null) {
                return errorPassword;
            }
        }

        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setCorreo(email);
        usuario.setTelefono(telefono);

        datosUsuarioRepository.save(usuario);

        Optional<Perfil> perfilOpt =
                perfilRepository.findByNombreUsuario(String.valueOf(usuario.getCedula()));

        if (perfilOpt.isPresent()) {
            Perfil perfil = perfilOpt.get();
            perfil.setRol(obtenerRol(dto.getRol()));

            if (!passwordLimpia.isBlank()) {
                perfil.setContrasena(passwordEncoder.encode(passwordLimpia));
            }

            perfilRepository.save(perfil);
        }

        return "ok";
    }

    // ==================== ELIMINAR ====================

    public String eliminarUsuario(Long id) {

        Optional<DatosUsuario> usuarioOpt =
                datosUsuarioRepository.findById(id.intValue());

        if (usuarioOpt.isEmpty()) {
            return "Usuario no encontrado";
        }

        DatosUsuario usuario = usuarioOpt.get();

        perfilRepository.findByNombreUsuario(String.valueOf(usuario.getCedula()))
                .ifPresent(perfilRepository::delete);

        datosUsuarioRepository.delete(usuario);

        return "ok";
    }

    // ==================== ROLES ====================

    private Rol obtenerRol(String rolStr) {

        if (rolStr == null) {
            return rolRepository.findByNombre("ROLE_USUARIO")
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        }

        switch (rolStr.toUpperCase()) {

            case "ADMIN":
            case "ROLE_ADMINISTRADOR":
                return rolRepository.findByNombre("ROLE_ADMINISTRADOR")
                        .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));

            case "INSTRUCTOR":
            case "ROLE_INSTRUCTOR":
                return rolRepository.findByNombre("ROLE_INSTRUCTOR")
                        .orElseThrow(() -> new RuntimeException("Rol INSTRUCTOR no encontrado"));

            default:
                return rolRepository.findByNombre("ROLE_USUARIO")
                        .orElseThrow(() -> new RuntimeException("Rol USUARIO no encontrado"));
        }
    }

    private String validarCamposBasicos(String nombre,
                                        String apellido,
                                        String documento,
                                        String email,
                                        String telefono) {
        if (nombre == null || nombre.length() < 2 || nombre.length() > MAX_NOMBRE_APELLIDO
                || !NOMBRE_PATTERN.matcher(nombre).matches()) {
            return "El nombre debe tener entre 2 y 60 caracteres y solo letras";
        }

        if (apellido == null || apellido.length() < 2 || apellido.length() > MAX_NOMBRE_APELLIDO
                || !NOMBRE_PATTERN.matcher(apellido).matches()) {
            return "El apellido debe tener entre 2 y 60 caracteres y solo letras";
        }

        if (documento == null || !CEDULA_PATTERN.matcher(documento).matches()) {
            return "La cédula debe tener entre 6 y 12 dígitos numéricos";
        }

        if (email == null || email.length() > MAX_EMAIL || !CORREO_PATTERN.matcher(email).matches()) {
            return "Correo inválido. Ejemplo válido: usuario@email.com";
        }

        if (telefono == null || !TELEFONO_PATTERN.matcher(telefono).matches()) {
            return "El teléfono debe tener 10 dígitos y empezar por 3";
        }

        return null;
    }

    private String validarPassword(String password, boolean obligatorio) {
        if ((password == null || password.isBlank()) && obligatorio) {
            return "La contraseña es obligatoria";
        }

        if (password == null || password.isBlank()) {
            return null;
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return "La contraseña debe tener 8-72 caracteres, mayúscula, minúscula, número y símbolo";
        }

        if (PASSWORDS_COMUNES.contains(password.toLowerCase(Locale.ROOT))) {
            return "La contraseña es demasiado común. Usa una más segura";
        }

        return null;
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
}