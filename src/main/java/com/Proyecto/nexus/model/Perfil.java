package com.Proyecto.nexus.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "perfil")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil")
    private Integer idPerfil;

    @Column(name = "nombre_usuario", nullable = false, unique = true)
    private String nombreUsuario;

    @Column(name = "contraseña", nullable = false)
    private String contraseña;

    @ManyToOne
    @JoinColumn(name = "id_rol")
    private Rol rol;

    @OneToOne
    @JoinColumn(name = "id_usuario")
    private DatosUsuario usuario;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}