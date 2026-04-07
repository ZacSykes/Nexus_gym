package com.proyecto.nexus.usuario.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.nexus.usuario.model.DatosUsuario;

public interface DatosUsuarioRepository extends JpaRepository<DatosUsuario, Integer> {
    Optional<DatosUsuario> findByCedula(Long cedula);
    Optional<DatosUsuario> findByCorreo(String correo);
    Optional<DatosUsuario> findByTelefono(String telefono);
    Optional<DatosUsuario> findByCorreoAndCedula(String correo, Long cedula);
}