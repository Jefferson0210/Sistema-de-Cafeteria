/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cafeteria.app.dto;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.cafeteria.app.enums.EstadoMesa;

/**
 * DTO para cambiar el estado de una mesa
 * @author DELL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambiarEstadoMesaDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoMesa nuevoEstado;
}
