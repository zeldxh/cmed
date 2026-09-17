package com.citamed.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "notificacion")
public class Notificacion implements Serializable {

    // Registro de los correos enviados por cambios en el ciclo de vida de una cita.
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idNotificacion;

    @ManyToOne
    @JoinColumn(name = "id_cita")
    private Cita cita;

    @Column(nullable = false, length = 30)
    @NotNull
    @Size(max = 30)
    private String tipo;

    @Column(nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    private String destinatario;

    @Column(nullable = false)
    private boolean exito;

    private LocalDateTime fechaEnvio;
}
