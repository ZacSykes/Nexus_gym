package com.proyecto.nexus.clase.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.nexus.clase.model.PaqueteClase;
import com.proyecto.nexus.usuario.model.DatosUsuario;

public interface PaqueteClaseRepository extends JpaRepository<PaqueteClase, Integer> {
    Optional<PaqueteClase> findFirstByUsuarioAndEstadoOrderByFechaVencimientoAsc(
        DatosUsuario usuario, String estado);
}