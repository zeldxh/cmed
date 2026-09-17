package com.citamed.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
@Entity
@Table(name = "paciente")
public class Paciente implements Serializable {

    // Información personal y de contacto asociada a un usuario paciente.
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPaciente;

    @OneToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(unique = true, nullable = false, length = 20)
    @Size(max = 20)
    private String cedula;

    @Column(length = 25)
    @Size(max = 25)
    private String telefono;

    private LocalDate fechaNacimiento;

    @Column(length = 200)
    @Size(max = 200)
    private String direccion;

    @OneToMany(mappedBy = "paciente")
    private List<Cita> citas;
}
