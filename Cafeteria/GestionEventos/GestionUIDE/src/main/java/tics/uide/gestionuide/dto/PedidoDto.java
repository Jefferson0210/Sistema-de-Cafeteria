/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tics.uide.gestionuide.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tics.uide.gestionuide.enums.EstadoPedido;

/**
 * DTO para Pedido
 * @author DELL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long mesaId; // Opcional - puede ser null para pedidos para llevar

    private Integer numeroMesa; // Solo lectura

    private Long clienteId; // Opcional - puede ser null para clientes sin cuenta

    private String clienteNombre; // Solo lectura

    @NotNull(message = "El mesero es obligatorio")
    private Long meseroId;

    private String meseroNombre; // Solo lectura

    private EstadoPedido estado;

    private Double subtotal;

    private Double iva;

    private Double total;

    private String notas;

    private Date fechaPedido;

    private Date fechaActualizacion;

    private List<DetallePedidoDto> detalles;
}
