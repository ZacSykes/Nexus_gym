package com.Proyecto.nexus.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "paquetes_clases")
public class PaqueteClase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paquete")
    private Integer idPaquete;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private DatosUsuario usuario;

    @Column(name = "total_clases")
    private Integer totalClases;

    @Column(name = "clases_restantes")
    private Integer clasesRestantes;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    @Column(name = "estado")
    private String estado;

    // Relación con plan
    @Column(name = "id_planes")
    private Integer idPlanes;

    // admin que lo creó
    @Column(name = "creado_por")
    private Integer creadoPor;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}