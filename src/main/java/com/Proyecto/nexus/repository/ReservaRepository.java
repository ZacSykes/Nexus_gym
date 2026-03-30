package com.Proyecto.nexus.repository;

import com.Proyecto.nexus.model.Clase;
import com.Proyecto.nexus.model.DatosUsuario;
import com.Proyecto.nexus.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    List<Reserva> findByUsuarioAndEstadoIn(DatosUsuario usuario, List<String> estados);

    List<Reserva> findByUsuarioOrderByFechaReservaDesc(DatosUsuario usuario);

    // Método para contar reservas activas (Confirmada o Pendiente) para una clase en una fecha
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.clase = :clase AND r.fechaClase = :fechaClase AND r.estado IN ('Confirmada', 'Pendiente')")
    long countReservasActivasByClaseAndFecha(@Param("clase") Clase clase, @Param("fechaClase") LocalDate fechaClase);
}