package com.citamed.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Data;

@Data
@Entity
@Table(name = "cita")
public class Cita implements Serializable {

    // Transacción principal que relaciona paciente, doctor, fecha y estado.
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCita;

    @ManyToOne
    @JoinColumn(name = "id_paciente")
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "id_doctor")
    private Doctor doctor;

    @NotNull
    private LocalDate fecha;

    @NotNull
    private LocalTime hora;

    @Column(nullable = false, length = 250)
    @NotNull
    @Size(max = 250)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;

    @Column(columnDefinition = "TEXT")
    private String notas;

    private LocalDateTime fechaCreacion;

    @Transient
    public boolean isCancelable() {
        // La cancelación requiere más de 24 horas y una cita todavía activa.
        if (estado == EstadoCita.CANCELADA || estado == EstadoCita.COMPLETADA) {
            return false;
        }
        return LocalDateTime.of(fecha, hora).isAfter(LocalDateTime.now().plusHours(24));
    }
}
