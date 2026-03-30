package com.Proyecto.Nexus.domain;

import jakarta.persistence.*;
import lombok.Data;
import com.Proyecto.Nexus.domain.Rol;

@Data //Generacion de getters, setters, toString, equals y hashCode automáticamente
@Entity //Indica que esta clase es una entidad de JPA y se mapeará a una tabla en la base de datos
@Table(name = "usuarios") //Especifica el nombre de la tabla en la base de datos
public class Usuarios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Generación automática del ID
    @Column(name = "id_usuarios") //Especifica el nombre de la columna en la base de datos

    private Long idUsuarios;
    private String nombre;
    private String apellido;
    private Integer edad;
    private String email;
    private String gimnasio;
    private String documento;
    private String telefono;
    
     @Enumerated(EnumType.STRING) // 🔥 Esto es clave
    private Rol rol;
}
