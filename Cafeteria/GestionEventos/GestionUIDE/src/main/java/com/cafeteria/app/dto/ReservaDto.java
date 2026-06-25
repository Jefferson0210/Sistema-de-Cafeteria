/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cafeteria.app.dto;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.cafeteria.app.enums.EstadoReserva;

/**
 * DTO para Reserva
 * @author DELL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "El usuario es obligatorio")
    private Long usuarioId;

    private String usuarioNombre; // Solo lectura

    @NotNull(message = "La mesa es obligatoria")
    private Long mesaId;

    private Integer numeroMesa; // Solo lectura

    @NotNull(message = "La fecha de reserva es obligatoria")
    @Future(message = "La fecha de reserva debe ser futura")
    private Date fechaReserva;

    @Min(value = 1, message = "La duración debe ser al menos 1 hora")
    private Integer duracionHoras;

    @NotNull(message = "El número de personas es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 persona")
    private Integer numPersonas;

    private EstadoReserva estado;

    private String notas;

    private Date fechaCreacion;
}
