package com.Proyecto.nexus.repository;

import com.Proyecto.nexus.model.PaqueteClase;
import com.Proyecto.nexus.model.DatosUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaqueteClaseRepository extends JpaRepository<PaqueteClase, Integer> {
    Optional<PaqueteClase> findFirstByUsuarioAndEstadoOrderByFechaVencimientoAsc(
        DatosUsuario usuario, String estado);
}