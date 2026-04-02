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

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.clase = :clase AND r.fechaClase = :fechaClase AND r.estado IN ('Confirmada', 'Pendiente')")
    long countReservasActivasByClaseAndFecha(@Param("clase") Clase clase, @Param("fechaClase") LocalDate fechaClase);

    @Query("SELECT COUNT(r) > 0 FROM Reserva r WHERE r.usuario = :usuario AND r.clase = :clase AND r.fechaClase = :fechaClase AND r.estado != 'Cancelada'")
    boolean existsByUsuarioAndClaseAndFechaClaseAndEstadoNotCancelada(@Param("usuario") DatosUsuario usuario,
                                                                       @Param("clase") Clase clase,
                                                                       @Param("fechaClase") LocalDate fechaClase);
}