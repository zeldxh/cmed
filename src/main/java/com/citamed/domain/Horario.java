package com.citamed.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalTime;
import lombok.Data;

@Data
@Entity
@Table(name = "horario")
public class Horario implements Serializable {

    // Bloque semanal recurrente en el que un doctor puede atender citas.
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idHorario;

    @ManyToOne
    @JoinColumn(name = "id_doctor")
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiaSemana diaSemana;

    private LocalTime horaInicio;
    private LocalTime horaFin;
}
