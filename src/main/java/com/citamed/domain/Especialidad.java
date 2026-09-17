package com.citamed.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
@Entity
@Table(name = "especialidad")
public class Especialidad implements Serializable {

    // Catálogo de áreas médicas disponibles en la clínica.
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEspecialidad;

    @Column(unique = true, nullable = false, length = 60)
    @NotNull
    @Size(max = 60)
    private String nombre;

    @Column(length = 250)
    @Size(max = 250)
    private String descripcion;

    private boolean activo;

    @OneToMany(mappedBy = "especialidad")
    private List<Doctor> doctores;
}
