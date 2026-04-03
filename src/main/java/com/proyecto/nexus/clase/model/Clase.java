package com.proyecto.nexus.clase.model;

import java.time.LocalTime;

import com.proyecto.nexus.usuario.model.Profesor;

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
@Table(name = "clases")
public class Clase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_clase")
    private Integer idClases;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "disciplina", nullable = false)
    private String disciplina;

    @Column(name = "nivel", nullable = false)
    private String nivel;

    @Column(name = "dia_semana", nullable = false)
    private String diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "cupo_maximo", nullable = false)
    private Integer cupoMaximo;

    // ⚠️ OPCIONAL: puedes eliminar este campo si no lo usas
    @Column(name = "cupo_disponible")
    private Integer cupoDisponible;

    @Column(name = "salon")
    private String salon;

    @Column(name = "disponible")
    private Boolean disponible = true;

    @ManyToOne
    @JoinColumn(name = "id_profesor")
    private Profesor profesor;
}