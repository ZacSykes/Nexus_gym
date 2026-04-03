package com.proyecto.nexus.pago.model;

import java.time.LocalDateTime;

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
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer idPago;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private DatosUsuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_paquete", nullable = false)
    private PaqueteClase paquete;

    @Column(name = "monto", nullable = false)
    private Double monto;

    @Column(name = "estado")
    private String estado; // PAGADO, PENDIENTE, FALLIDO

    @Column(name = "metodo_pago")
    private String metodoPago; // tarjeta, efectivo, etc

    @Column(name = "referencia")
    private String referencia;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;
}