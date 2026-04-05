package com.proyecto.nexus.usuario.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        Long cedula;

        try {
            cedula = Long.parseLong(dto.getDocumento());
        } catch (NumberFormatException e) {
            return "La cédula debe ser numérica";
        }

        if (datosUsuarioRepository.findByCedula(cedula).isPresent()) {
            return "La cédula ya existe";
        }

        if (datosUsuarioRepository.findByCorreo(dto.getEmail()).isPresent()) {
            return "El correo ya existe";
        }

        // Crear usuario
        DatosUsuario usuario = new DatosUsuario();
        usuario.setCedula(cedula);
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setCorreo(dto.getEmail());
        usuario.setTelefono(dto.getTelefono());
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setEstado("ACTIVO");
        usuario.setClasesTotales(0);

        datosUsuarioRepository.save(usuario);

        // Crear perfil
        Perfil perfil = new Perfil();
        perfil.setNombreUsuario(dto.getDocumento());
        perfil.setContrasena(passwordEncoder.encode(password));
        perfil.setUsuario(usuario);
        perfil.setRol(obtenerRol(dto.getRol()));
        perfil.setCreatedAt(LocalDateTime.now());

        perfilRepository.save(perfil);

        return "ok";
    }

    // ==================== EDITAR ====================

    public String editarUsuario(UsuarioDTO dto, String password) {

        Optional<DatosUsuario> usuarioOpt =
                datosUsuarioRepository.findById(dto.getId().intValue());

        if (usuarioOpt.isEmpty()) {
            return "Usuario no encontrado";
        }

        DatosUsuario usuario = usuarioOpt.get();

        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setCorreo(dto.getEmail());
        usuario.setTelefono(dto.getTelefono());

        datosUsuarioRepository.save(usuario);

        Optional<Perfil> perfilOpt =
                perfilRepository.findByNombreUsuario(String.valueOf(usuario.getCedula()));

        if (perfilOpt.isPresent()) {
            Perfil perfil = perfilOpt.get();

            perfil.setRol(obtenerRol(dto.getRol()));

            if (password != null && !password.isEmpty()) {
                perfil.setContrasena(passwordEncoder.encode(password));
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
}