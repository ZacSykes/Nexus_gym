package com.proyecto.nexus.usuario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "rangos")
public class Rango {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rango")
    private Integer idRango;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "clases_requeridas")
    private Integer clasesRequeridas;

    @Column(name = "color_hex")
    private String colorHex;

    @Column(name = "orden")
    private Integer orden;
}