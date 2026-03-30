package com.Proyecto.nexus.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

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
    @JoinColumn(name = "id_paquete")
    private PaqueteClase paquete;

    @Column(name = "monto", nullable = false)
    private Double monto;

    @Column(name = "estado")
    private String estado; // PENDIENTE, PAGADO, FALLIDO, REEMBOLSADO

    @Column(name = "metodo_pago")
    private String metodoPago; // Efectivo, Tarjeta, Transferencia, Nequi, Daviplata

    @Column(name = "referencia")
    private String referencia;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;
}