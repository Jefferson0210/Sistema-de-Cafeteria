/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tics.uide.gestionuide.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear facturas manualmente (sin pedido previo)
 * @author DELL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearFacturaManualDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long clienteId; // Opcional - puede ser null para clientes sin cuenta

    @NotNull(message = "El cajero es obligatorio")
    private Long cajeroId;

    private String empresaRuc; // Opcional

    private BigDecimal descuento; // Opcional

    @NotEmpty(message = "Debe agregar al menos un producto")
    @Valid
    private List<ItemFacturaDto> items;
}
