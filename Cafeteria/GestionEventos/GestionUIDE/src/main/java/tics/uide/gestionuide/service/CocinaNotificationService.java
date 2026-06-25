package tics.uide.gestionuide.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tics.uide.gestionuide.dto.PedidoEventoDto;
import tics.uide.gestionuide.event.PedidoEvento;

/**
 * Canal de notificación a cocina por SSE. Registro de emitters EN MEMORIA (efímero, sin tabla).
 *
 * Garantía clave: emitir() NUNCA propaga excepciones, así que el canal jamás puede tumbar la
 * creación/cambio de un pedido. Además el listener es AFTER_COMMIT, por lo que solo notifica
 * pedidos que de verdad se confirmaron (importante por el retry de stock).
 */
@Service
public class CocinaNotificationService {

    // 30 min por conexión; el cliente reconecta (y refresca el access token) periódicamente.
    private static final long TIMEOUT_MS = 30L * 60 * 1000;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /** Alta de un suscriptor (pantalla de cocina). */
    public SseEmitter suscribir() {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        registrar(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> { emitters.remove(emitter); emitter.complete(); });
        emitter.onError(e -> emitters.remove(emitter));
        enviar(emitter, "conectado", "ok");   // confirma la conexión al cliente
        return emitter;
    }

    /** Registra un emitter ya construido (usado por suscribir() y por pruebas). */
    public void registrar(SseEmitter emitter) {
        emitters.add(emitter);
    }

    /** Empuja el evento a cocina SOLO tras el commit del pedido. Nunca puede tumbar la transacción. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPedidoEvento(PedidoEvento evento) {
        emitir(evento.getPayload());
    }

    /** Emite a todos los suscriptores; elimina los que fallen. Aislado: NUNCA lanza. */
    public void emitir(PedidoEventoDto payload) {
        for (SseEmitter emitter : emitters) {
            if (!enviar(emitter, "pedido", payload)) {
                emitters.remove(emitter);
            }
        }
    }

    /** Heartbeat: mantiene viva la conexión a través de proxies y purga emitters muertos. */
    @Scheduled(fixedRate = 25_000)
    public void heartbeat() {
        for (SseEmitter emitter : emitters) {
            if (!enviar(emitter, "ping", "")) {
                emitters.remove(emitter);
            }
        }
    }

    /** Nº de suscriptores activos (para pruebas/diagnóstico). */
    public int suscriptores() {
        return emitters.size();
    }

    /** Envía un evento a un emitter. Devuelve false si la conexión falló (para purgarlo). NUNCA lanza. */
    private boolean enviar(SseEmitter emitter, String nombreEvento, Object data) {
        try {
            emitter.send(SseEmitter.event().name(nombreEvento).data(data));
            return true;
        } catch (Exception e) {
            // conexión caída / emitter ya completado / error de serialización -> se descarta
            return false;
        }
    }
}
