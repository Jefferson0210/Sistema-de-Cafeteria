package tics.uide.gestionuide.dto;

import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body del chatbot: el mensaje del usuario y, opcionalmente, el historial de la conversación
 * (que mantiene el frontend; la conversación es efímera, sin persistencia en el backend).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotMensajeDto {

    @NotBlank(message = "El mensaje es obligatorio")
    private String mensaje;

    private List<Turno> historial;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Turno {
        private String rol;     // "user" | "model"
        private String texto;
    }
}
