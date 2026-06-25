package com.cafeteria.app.webservice;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cafeteria.app.dto.ApiResponse;
import com.cafeteria.app.dto.ChatbotMensajeDto;
import com.cafeteria.app.dto.RespuestaChatbot;
import com.cafeteria.app.exception.TooManyRequestsException;
import com.cafeteria.app.service.ChatbotReservaService;
import com.cafeteria.app.service.RateLimitService;
import com.cafeteria.app.service.UsuarioService;

/**
 * Chatbot de reservas — ETAPA 1: el puente a Gemini. Conversa y devuelve los campos estructurados
 * extraídos, pero NO crea reservas todavía. Rate-limited; la key de Gemini vive solo en el backend.
 */
@RestController
@RequestMapping("/api/chatbot")
public class ChatbotWS {

    @Autowired private ChatbotReservaService chatbotReservaService;
    @Autowired private RateLimitService rateLimitService;
    @Autowired private UsuarioService usuarioService;

    @PostMapping("/mensaje")
    @PreAuthorize("hasAnyAuthority('CLIENTE','ADMIN','MESERO')")
    public ResponseEntity<?> mensaje(@Valid @RequestBody ChatbotMensajeDto dto,
                                     Authentication auth, HttpServletRequest req) {
        long retryIp = rateLimitService.chequearIp(req, "chatbot");
        if (retryIp > 0) throw new TooManyRequestsException(retryIp);
        long retryUser = rateLimitService.chequearEmail("chatbot", auth.getName());
        if (retryUser > 0) throw new TooManyRequestsException(retryUser);

        // El usuario de la reserva sale del TOKEN, jamás de Gemini ni del cliente.
        Long usuarioId = usuarioService.buscarPorUsername(auth.getName()).getId();
        RespuestaChatbot r = chatbotReservaService.procesar(dto.getMensaje(), dto.getHistorial(), usuarioId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reply", r.getReply());
        data.put("degradado", r.isDegradado());
        data.put("reservaId", r.getReservaId());   // != null solo si se creó la reserva
        data.put("campos", r.getCampos());
        return ResponseEntity.ok(new ApiResponse(true, "OK", data));
    }
}
