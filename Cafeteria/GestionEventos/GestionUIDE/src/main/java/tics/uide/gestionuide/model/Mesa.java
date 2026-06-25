/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tics.uide.gestionuide.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import tics.uide.gestionuide.enums.EstadoMesa;
import tics.uide.gestionuide.enums.ModoCuenta;

/**
 * Entidad que representa una mesa del restaurante
 * @author DELL
 */
@Entity
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "Mesa")
public class Mesa implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mesa")
    private Long id;

    @NotNull(message = "El número de mesa es obligatorio")
    @Min(value = 1, message = "El número de mesa debe ser mayor a 0")
    @Column(nullable = false, unique = true)
    private Integer numeroMesa;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser al menos 1")
    @Column(nullable = false)
    private Integer capacidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMesa estado;

    @Column(length = 50)
    private String ubicacion;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    // Modo de cuenta de la sesión activa (null = mesa sin sesión / idle). Se fija en el primer escaneo.
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ModoCuenta modoCuenta;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date fechaCreacion;

    // Relaciones
    @JsonIgnore
    @OneToMany(mappedBy = "mesa", fetch = FetchType.LAZY)
    private List<Pedido> pedidos;

    @JsonIgnore
    @OneToMany(mappedBy = "mesa", fetch = FetchType.LAZY)
    private List<Reserva> reservas;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = new Date();
        if (estado == null) {
            estado = EstadoMesa.LIBRE;
        }
        if (activo == null) {
            activo = true;
        }
    }
}
