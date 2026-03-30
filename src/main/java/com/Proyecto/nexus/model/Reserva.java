package com.Proyecto.nexus.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reservas")
    private Integer idReservas;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private DatosUsuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_clases")
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