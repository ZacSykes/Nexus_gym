package com.proyecto.nexus.reserva.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto.nexus.clase.model.Clase;
import com.proyecto.nexus.reserva.model.Reserva;
import com.proyecto.nexus.usuario.model.DatosUsuario;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    List<Reserva> findByUsuarioAndEstadoIn(DatosUsuario usuario, List<String> estados);

    List<Reserva> findByUsuarioOrderByFechaReservaDesc(DatosUsuario usuario);

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.clase = :clase AND r.fechaClase = :fechaClase AND r.estado IN ('Confirmada', 'Pendiente')")
    long countReservasActivasByClaseAndFecha(@Param("clase") Clase clase,
                                             @Param("fechaClase") LocalDate fechaClase);
}