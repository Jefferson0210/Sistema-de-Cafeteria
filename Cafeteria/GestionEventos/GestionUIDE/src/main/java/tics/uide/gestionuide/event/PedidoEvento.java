package tics.uide.gestionuide.event;

import tics.uide.gestionuide.dto.PedidoEventoDto;

/**
 * Evento de dominio publicado por PedidoService cuando se crea/cambia de estado un pedido.
 * Desacopla PedidoService del canal de notificación: PedidoService solo publica este evento,
 * no conoce SSE ni los emitters. Un @TransactionalEventListener(AFTER_COMMIT) lo consume.
 */
public class PedidoEvento {

    private final PedidoEventoDto payload;

    public PedidoEvento(PedidoEventoDto payload) {
        this.payload = payload;
    }

    public PedidoEventoDto getPayload() {
        return payload;
    }
}
