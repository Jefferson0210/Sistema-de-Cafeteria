/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tics.uide.gestionuide.dto;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tics.uide.gestionuide.enums.EstadoPedido;

/**
 * DTO para cambiar el estado de un pedido
 * @author DELL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambiarEstadoPedidoDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoPedido nuevoEstado;
}
