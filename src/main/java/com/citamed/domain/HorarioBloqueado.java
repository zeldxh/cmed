package com.citamed.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "horario_bloqueado")
public class HorarioBloqueado implements Serializable {

    // Período específico en el que un doctor no se encuentra disponible.
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idHorarioBloqueado;

    @ManyToOne
    @JoinColumn(name = "id_doctor")
    private Doctor doctor;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    @Column(length = 200)
    @Size(max = 200)
    private String motivo;
}
