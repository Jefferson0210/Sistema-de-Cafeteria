package com.cafeteria.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Respuesta del chatbot de reservas. {@code reservaId} != null solo si se CREÓ una reserva
 * (tras pasar toda la validación de backend). {@code degradado} = Gemini no disponible.
 */
@Data
@AllArgsConstructor
public class RespuestaChatbot {
    private String reply;          // mensaje conversacional para el usuario
    private boolean degradado;     // Gemini falló (Etapa 1)
    private Long reservaId;        // != null solo si se creó la reserva
    private CamposReserva campos;  // datos extraídos (eco, puede ser null)

    public static RespuestaChatbot degradado(String reply) {
        return new RespuestaChatbot(reply, true, null, null);
    }

    /** Conversa o rechaza: NO se creó reserva. */
    public static RespuestaChatbot conversa(String reply, CamposReserva campos) {
        return new RespuestaChatbot(reply, false, null, campos);
    }

    /** Reserva creada. */
    public static RespuestaChatbot creada(String reply, Long reservaId, CamposReserva campos) {
        return new RespuestaChatbot(reply, false, reservaId, campos);
    }
}
