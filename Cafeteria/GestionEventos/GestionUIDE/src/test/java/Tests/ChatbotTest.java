package Tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tics.uide.gestionuide.dto.GeminiResultado;
import tics.uide.gestionuide.dto.UsuarioRegistroDto;
import tics.uide.gestionuide.model.Usuario;
import tics.uide.gestionuide.service.GeminiClient;
import tics.uide.gestionuide.service.UsuarioService;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Chatbot Etapa 1 (endpoint). Mockea GeminiClient (no se llama a Gemini real: sería lento, costaría
 * dinero y dependería de la red). Rate limiting desactivado aquí para no interferir con login/chatbot.
 */
@SpringBootTest(classes = tics.uide.gestionuide.GestionUIDE.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.ratelimit.enabled=false")
public class ChatbotTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UsuarioService usuarioService;

    @MockBean private GeminiClient geminiClient;

    private static final String PWD = "Uide2024*";

    private String loginCliente() throws Exception {
        String s = "chat" + UUID.randomUUID().toString().substring(0, 6);
        Usuario u = usuarioService.registrar(UsuarioRegistroDto.builder()
                .username(s).email(s + "@uide.edu.ec").password(PWD).confirmPassword(PWD)
                .nombre("Chat").apellido("Test").telefono("0990000000").build());
        usuarioService.marcarEmailVerificado(u.getId());
        String body = objectMapper.writeValueAsString(Map.of("usernameOrEmail", s, "password", PWD));
        MvcResult res = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn();
        return res.getResponse().getHeader("Authorization");   // "Bearer xxx"
    }

    @Test
    void sinAuth_denegado() throws Exception {
        mvc.perform(post("/api/chatbot/mensaje").contentType(MediaType.APPLICATION_JSON)
                .content("{\"mensaje\":\"hola\"}"))
                .andExpect(status().is4xxClientError());   // la seguridad bloquea antes del controlador
    }

    @Test
    void geminiOk_devuelveRespuesta() throws Exception {
        Mockito.when(geminiClient.consultar(Mockito.anyString(), Mockito.any()))
                .thenReturn(GeminiResultado.ok("¿Para cuántas personas?", null));
        String token = loginCliente();
        mvc.perform(post("/api/chatbot/mensaje").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"mensaje\":\"quiero una mesa\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reply").value("¿Para cuántas personas?"))
                .andExpect(jsonPath("$.data.degradado").value(false));
    }

    @Test
    void geminiDegradado_respondeAmable_sin500() throws Exception {
        Mockito.when(geminiClient.consultar(Mockito.anyString(), Mockito.any()))
                .thenReturn(GeminiResultado.degradado("Ahora no puedo procesar tu mensaje, intenta de nuevo."));
        String token = loginCliente();
        mvc.perform(post("/api/chatbot/mensaje").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"mensaje\":\"quiero una mesa\"}"))
                .andExpect(status().isOk())   // NO 500
                .andExpect(jsonPath("$.data.degradado").value(true))
                .andExpect(jsonPath("$.data.reply").value("Ahora no puedo procesar tu mensaje, intenta de nuevo."));
    }
}
