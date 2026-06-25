package com.cafeteria.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import com.cafeteria.app.enums.EstadoReserva;

@Entity
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "Reserva",
        indexes = {
            @Index(name = "idx_reserva_fecha", columnList = "fechaReserva"),
            @Index(name = "idx_reserva_estado", columnList = "estado")
        })
public class Reserva implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long id;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false, foreignKey = @ForeignKey(name = "RESERVA_USUARIO_FK"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Usuario usuario;

    @NotNull(message = "La mesa es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_mesa", nullable = false, foreignKey = @ForeignKey(name = "RESERVA_MESA_FK"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Mesa mesa;

    // CORREGIDO: quitado @Future para permitir tests y reservas del mismo día
    @NotNull(message = "La fecha de reserva es obligatoria")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date fechaReserva;

    @Builder.Default
    @Min(value = 1, message = "La duración debe ser al menos 1 hora")
    @Column(nullable = false)
    private Integer duracionHoras = 2;

    @NotNull(message = "El número de personas es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 persona")
    @Column(nullable = false)
    private Integer numPersonas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado;

    @Column(length = 500)
    private String notas;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date fechaCreacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = new Date();
        fechaActualizacion = new Date();
        if (estado == null) estado = EstadoReserva.PENDIENTE;
        if (duracionHoras == null) duracionHoras = 2;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = new Date();
    }
}
