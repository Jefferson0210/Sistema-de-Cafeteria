package tics.uide.gestionuide.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload LIGERO que viaja a la pantalla de cocina por SSE.
 * No es la entidad Pedido (evita lazy-loading tras commit, payloads enormes y ciclos de serialización).
 * Si cocina necesita el detalle completo, hace GET /api/pedidos/{id}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoEventoDto {

    public enum Tipo { NUEVO, CAMBIO_ESTADO, ITEMS_AGREGADOS }

    private Tipo tipo;
    private Long pedidoId;
    private String estado;          // EstadoPedido.name()
    private Integer mesaNumero;     // null si el pedido no tiene mesa
    private String meseroNombre;    // null si no hay mesero
    private BigDecimal total;
    private Integer numItems;
    private Date fecha;
    private List<ItemEventoDto> items;   // la comanda (platos, cantidades, notas); construida dentro de la transacción
}
