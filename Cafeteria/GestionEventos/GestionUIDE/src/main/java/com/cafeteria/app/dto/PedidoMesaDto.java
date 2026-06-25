package com.cafeteria.app.dto;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body para pedir desde el QR de una mesa: POST /api/pedidos/mesa/{mesaId}.
 * El mesaId va en la ruta y el clienteId se resuelve del token; aquí solo viajan los items.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoMesaDto {

    @NotEmpty(message = "Debe incluir al menos un item")
    @Valid
    private List<ItemPedidoDto> items;
}
