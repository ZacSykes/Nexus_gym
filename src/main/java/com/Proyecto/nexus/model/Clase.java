package com.Proyecto.nexus.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "clases")
public class Clase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_clases")
    private Integer idClases;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "disciplina")
    private String disciplina;

    @Column(name = "nivel")
    private String nivel;

    @Column(name = "dia_semana")
    private String diaSemana;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Column(name = "cupo_maximo")
    private Integer cupoMaximo;

    @Column(name = "cupo_disponible")
    private Integer cupoDisponible;

    @Column(name = "salon")
    private String salon;

    @Column(name = "disponible")
    private Boolean disponible;

    @ManyToOne
    @JoinColumn(name = "id_profesor")
    private Profesor profesor;
}
