package com.proyecto.nexus.usuario.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
@Table(name = "datos_usuarios")
public class DatosUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "cedula")
    private Long cedula;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "apellido")
    private String apellido;

    @Column(name = "correo")
    private String correo;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "telefono_emergencia")
    private String telefonoEmergencia;

    @Column(name = "genero")
    private String genero;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "peso")
    private Double peso;

    @Column(name = "altura")
    private Double altura;

    @Column(name = "objetivo")
    private String objetivo;

    @Column(name = "experiencia")
    private String experiencia;

    @Column(name = "condiciones_medicas")
    private String condicionesMedicas;

    @Column(name = "alergias")
    private String alergias;

    @Column(name = "notas_adicionales")
    private String notasAdicionales;

    @Column(name = "clases_totales")
    private Integer clasesTotales;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "estado")
    private String estado;

    @ManyToOne
    @JoinColumn(name = "id_rango")
    private Rango rango;
}