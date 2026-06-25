package com.cafeteria.app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cafeteria.app.dto.CamposReserva;
import com.cafeteria.app.dto.ChatbotMensajeDto;
import com.cafeteria.app.dto.CrearReservaDto;
import com.cafeteria.app.dto.GeminiResultado;
import com.cafeteria.app.dto.RespuestaChatbot;
import com.cafeteria.app.exception.BadRequestException;
import com.cafeteria.app.model.Mesa;
import com.cafeteria.app.model.Reserva;

/**
 * Orquestación del chatbot de reservas (Etapa 2): Gemini PROPONE, el backend DISPONE.
 * Entre la salida de Gemini y ReservaService.crear() está la FRONTERA DE CONFIANZA:
 * todo se valida en el backend (parse, fecha futura, rango, selección de mesa real) y el
 * usuario sale del TOKEN. Ningún dato de Gemini llega a la reserva sin validar.
 */
@Service
public class ChatbotReservaService {

    private static final int DURACION_HORAS = 2;
    private static final int MAX_PERSONAS = 20;   // barrera de cordura; la mesa decide la capacidad real

    @Autowired private GeminiClient geminiClient;
    @Autowired private ReservaService reservaService;

    public RespuestaChatbot procesar(String mensaje, List<ChatbotMensajeDto.Turno> historial, Long usuarioId) {
        GeminiResultado res = geminiClient.consultar(mensaje, historial);
        if (res.isDegradado()) {
            return RespuestaChatbot.degradado(res.getMensaje());
        }
        CamposReserva campos = res.getCampos();

        // Faltan datos (Gemini lo marca, o algún campo es null) -> el bot PREGUNTA, no crea nada
        if (campos == null || (campos.getFaltan() != null && !campos.getFaltan().isEmpty())
                || campos.getFecha() == null || campos.getHora() == null || campos.getNumPersonas() == null) {
            return RespuestaChatbot.conversa(res.getMensaje(), campos);
        }

        // ════════ FRONTERA DE CONFIANZA: Gemini PROPONE, el backend DISPONE ════════
        Date fechaReserva = parsearFechaHora(campos.getFecha(), campos.getHora());
        if (fechaReserva == null) {
            return RespuestaChatbot.conversa("No entendí la fecha u hora. ¿Me la repites? (ej. 'el viernes a las 20:00')", campos);
        }
        if (!fechaReserva.after(new Date())) {
            return RespuestaChatbot.conversa("Esa fecha y hora ya pasaron. Dime una fecha futura, por favor.", campos);
        }
        int numPersonas = campos.getNumPersonas();
        if (numPersonas < 1 || numPersonas > MAX_PERSONAS) {
            return RespuestaChatbot.conversa("El número de personas no es válido (1 a " + MAX_PERSONAS + "). ¿Cuántas son?", campos);
        }

        // El BACKEND elige la mesa (Gemini nunca la propone): capacidad suficiente + libre a esa hora
        Optional<Mesa> mesa = reservaService.seleccionarMesaDisponible(numPersonas, fechaReserva, DURACION_HORAS);
        if (mesa.isEmpty()) {
            return RespuestaChatbot.conversa(
                    "No hay mesa disponible para " + numPersonas + " personas el " + campos.getFecha()
                    + " a las " + campos.getHora() + ". ¿Probamos otra fecha u hora?", campos);
        }

        // usuarioId viene del TOKEN (resuelto en el controlador), nunca de Gemini ni del cliente
        try {
            Reserva r = reservaService.crear(CrearReservaDto.builder()
                    .mesaId(mesa.get().getId())
                    .fechaReserva(fechaReserva)
                    .duracionHoras(DURACION_HORAS)
                    .numPersonas(numPersonas)
                    .notas("Reserva creada por el asistente")
                    .build(), usuarioId);
            String reply = "¡Listo! Reservé la mesa " + mesa.get().getNumeroMesa() + " para " + numPersonas
                    + " personas el " + campos.getFecha() + " a las " + campos.getHora() + ". Estado: pendiente de confirmación.";
            return RespuestaChatbot.creada(reply, r.getId(), campos);
        } catch (BadRequestException e) {
            // doble chequeo de disponibilidad dentro de crear() (carrera): rechaza con gracia, no crea
            return RespuestaChatbot.conversa("No pude completar la reserva: " + e.getMessage() + ". ¿Probamos otra hora?", campos);
        }
    }

    /** Combina "YYYY-MM-DD" + "HH:mm" en Date (zona del servidor). Null si no es parseable. */
    private Date parsearFechaHora(String fecha, String hora) {
        try {
            LocalDate d = LocalDate.parse(fecha.trim());
            LocalTime t = LocalTime.parse(hora.trim());
            return Date.from(LocalDateTime.of(d, t).atZone(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
            return null;   // fecha/hora manipulada o ambigua -> el backend la rechaza
        }
    }
}
