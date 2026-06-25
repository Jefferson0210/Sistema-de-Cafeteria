package com.cafeteria.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.cafeteria.app.dto.CamposReserva;
import com.cafeteria.app.dto.ChatbotMensajeDto;
import com.cafeteria.app.dto.GeminiResultado;

/**
 * Puente a la API REST de Gemini (gemini-2.0-flash). Extrae datos ESTRUCTURADOS de reserva
 * (responseSchema / modo JSON). NUNCA lanza: ante timeout/error/JSON inválido devuelve un
 * GeminiResultado degradado con un mensaje amable; el detalle solo va al log. La key vive en
 * el backend (GEMINI_API_KEY) y nunca se expone al frontend.
 */
@Service
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private static final String INSTRUCCION =
            "Eres el asistente de reservas de la Cafetería UIDE. A partir del mensaje del usuario "
          + "extrae: fecha (formato YYYY-MM-DD), hora (formato HH:mm, 24h) y numPersonas (entero). "
          + "Si falta algún dato, pídelo amablemente en 'mensajeAlUsuario' y lista los que faltan en 'faltan' "
          + "(usa los nombres 'fecha','hora','numPersonas'). Responde SIEMPRE solo con el JSON del esquema.";

    private final RestTemplate rest;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    public GeminiClient(@Qualifier("geminiRestTemplate") RestTemplate rest) {
        this.rest = rest;
    }

    /** Consulta a Gemini. NUNCA lanza: ante cualquier fallo devuelve un resultado degradado. */
    public GeminiResultado consultar(String mensaje, List<ChatbotMensajeDto.Turno> historial) {
        if (apiKey == null || apiKey.isBlank()) {
            return GeminiResultado.degradado("El asistente no está disponible ahora mismo.");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String url = String.format(URL, model, apiKey);
            ResponseEntity<String> resp = rest.postForEntity(
                    url, new HttpEntity<>(construirBody(mensaje, historial), headers), String.class);
            return parsear(resp.getBody());
        } catch (Exception e) {
            // timeout (ResourceAccessException), no-2xx (HttpStatusCodeException), JSON inválido, etc.
            log.warn("Gemini no disponible: {}", e.toString());   // detalle SOLO al log
            return GeminiResultado.degradado("Ahora no puedo procesar tu mensaje, intenta de nuevo en un momento.");
        }
    }

    private Map<String, Object> construirBody(String mensaje, List<ChatbotMensajeDto.Turno> historial) {
        List<Map<String, Object>> contents = new ArrayList<>();
        if (historial != null) {
            for (ChatbotMensajeDto.Turno t : historial) {
                String rol = "model".equalsIgnoreCase(t.getRol()) ? "model" : "user";
                contents.add(Map.of("role", rol,
                        "parts", List.of(Map.of("text", t.getTexto() == null ? "" : t.getTexto()))));
            }
        }
        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", mensaje))));

        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "mensajeAlUsuario", Map.of("type", "string"),
                        "fecha", Map.of("type", "string"),
                        "hora", Map.of("type", "string"),
                        "numPersonas", Map.of("type", "integer"),
                        "faltan", Map.of("type", "array", "items", Map.of("type", "string"))),
                "required", List.of("mensajeAlUsuario"));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("systemInstruction", Map.of("parts", List.of(Map.of("text", INSTRUCCION))));
        body.put("contents", contents);
        body.put("generationConfig", Map.of(
                "responseMimeType", "application/json",
                "responseSchema", schema));
        return body;
    }

    private GeminiResultado parsear(String responseBody) throws Exception {
        JsonNode root = mapper.readTree(responseBody);
        JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (textNode.isMissingNode() || textNode.isNull()) {
            return GeminiResultado.degradado("No pude entender la respuesta del asistente.");
        }
        JsonNode datos = mapper.readTree(textNode.asText());   // el JSON estructurado del modelo (puede lanzar -> degradado)

        List<String> faltan = new ArrayList<>();
        if (datos.path("faltan").isArray()) {
            datos.get("faltan").forEach(n -> faltan.add(n.asText()));
        }
        CamposReserva campos = CamposReserva.builder()
                .fecha(textoONull(datos, "fecha"))
                .hora(textoONull(datos, "hora"))
                .numPersonas(datos.path("numPersonas").isInt() ? datos.get("numPersonas").asInt() : null)
                .faltan(faltan)
                .build();
        String mensaje = datos.path("mensajeAlUsuario").asText("¿En qué te ayudo con tu reserva?");
        return GeminiResultado.ok(mensaje, campos);
    }

    private String textoONull(JsonNode n, String campo) {
        JsonNode v = n.path(campo);
        return v.isMissingNode() || v.isNull() || v.asText().isBlank() ? null : v.asText();
    }
}
