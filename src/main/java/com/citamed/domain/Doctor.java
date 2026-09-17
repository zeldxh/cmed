package com.citamed.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
@Entity
@Table(name = "doctor")
public class Doctor implements Serializable {

    // Información profesional, especialidad y relaciones del médico.
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDoctor;

    @OneToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_especialidad")
    private Especialidad especialidad;

    @Column(unique = true, nullable = false, length = 40)
    @NotNull
    @Size(max = 40)
    private String numeroColegiado;

    private boolean activo;

    @OneToMany(mappedBy = "doctor")
    private List<Horario> horarios;

    @OneToMany(mappedBy = "doctor")
    private List<Cita> citas;
}
