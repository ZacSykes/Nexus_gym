package com.Proyecto.nexus.repository;

import com.Proyecto.nexus.model.DatosUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DatosUsuarioRepository extends JpaRepository<DatosUsuario, Integer> {
    Optional<DatosUsuario> findByCedula(Long cedula);
    Optional<DatosUsuario> findByCorreo(String correo);
}