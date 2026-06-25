package Tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import tics.uide.gestionuide.dto.GeminiResultado;
import tics.uide.gestionuide.service.GeminiClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * GeminiClient sin red: MockRestServiceServer simula la API de Gemini.
 * Prueba la garantía clave: consultar() NUNCA lanza; ante timeout/500/JSON basura devuelve degradado.
 */
public class GeminiClientTest {

    private GeminiClient client;
    private MockRestServiceServer server;
    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RestTemplate rest = new RestTemplate();
        server = MockRestServiceServer.createServer(rest);
        client = new GeminiClient(rest);
        ReflectionTestUtils.setField(client, "apiKey", "test-key");
        ReflectionTestUtils.setField(client, "model", "gemini-2.0-flash");
    }

    private String respuestaGemini(String innerJson) throws Exception {
        return om.writeValueAsString(Map.of("candidates",
                List.of(Map.of("content", Map.of("parts", List.of(Map.of("text", innerJson)))))));
    }

    @Test
    void respuestaValida_extraeCampos() throws Exception {
        String inner = om.writeValueAsString(Map.of(
                "mensajeAlUsuario", "¿Para cuántas personas?", "numPersonas", 4, "faltan", List.of("hora")));
        server.expect(requestTo(startsWith("https://generativelanguage.googleapis.com")))
                .andRespond(withSuccess(respuestaGemini(inner), MediaType.APPLICATION_JSON));

        GeminiResultado r = client.consultar("quiero mesa para 4", null);

        assertFalse(r.isDegradado());
        assertEquals("¿Para cuántas personas?", r.getMensaje());
        assertEquals(4, r.getCampos().getNumPersonas());
        assertTrue(r.getCampos().getFaltan().contains("hora"));
    }

    @Test
    void timeout_devuelveDegradado_sinLanzar() {
        server.expect(requestTo(startsWith("https://"))).andRespond(withException(new IOException("timeout")));
        GeminiResultado r = client.consultar("hola", null);
        assertTrue(r.isDegradado(), "un timeout debe degradar, no lanzar");
        assertNotNull(r.getMensaje());
    }

    @Test
    void error500_devuelveDegradado() {
        server.expect(requestTo(startsWith("https://"))).andRespond(withServerError());
        GeminiResultado r = client.consultar("hola", null);
        assertTrue(r.isDegradado());
    }

    @Test
    void jsonBasura_devuelveDegradado() {
        server.expect(requestTo(startsWith("https://")))
                .andRespond(withSuccess("no soy json {{{", MediaType.APPLICATION_JSON));
        GeminiResultado r = client.consultar("hola", null);
        assertTrue(r.isDegradado());
    }

    @Test
    void sinApiKey_degradadoSinLlamar() {
        ReflectionTestUtils.setField(client, "apiKey", "");
        GeminiResultado r = client.consultar("hola", null);
        assertTrue(r.isDegradado());
        // no se programó ninguna expectativa en el server -> confirma que no se llamó
    }
}
