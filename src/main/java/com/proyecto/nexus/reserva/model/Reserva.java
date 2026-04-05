package com.proyecto.nexus.reserva.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.proyecto.nexus.clase.model.Clase;
import com.proyecto.nexus.clase.model.PaqueteClase;
import com.proyecto.nexus.usuario.model.DatosUsuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Integer idReserva;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private DatosUsuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_clase")
    private Clase clase;

    @ManyToOne
    @JoinColumn(name = "id_paquete")
    private PaqueteClase paquete;

    @Column(name = "fecha_clase")
    private LocalDate fechaClase;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "estado")
    private String estado;

    @Column(name = "fecha_reserva")
    private LocalDateTime fechaReserva;

    @Column(name = "cancelada_en")
    private LocalDateTime canceladaEn;

    @Column(name = "clase_devuelta")
    private Boolean claseDevuelta;
}