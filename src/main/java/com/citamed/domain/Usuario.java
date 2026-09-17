package com.citamed.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {

    // Datos comunes utilizados para el acceso y la identificación de cada usuario.
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuario;

    @Column(nullable = false, length = 80)
    @NotNull
    @Size(max = 80)
    private String nombre;

    @Column(unique = true, nullable = false, length = 100)
    @NotNull
    @Email
    @Size(max = 100)
    private String correo;

    @Column(nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    private String contrasena;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    private boolean activo;
}
