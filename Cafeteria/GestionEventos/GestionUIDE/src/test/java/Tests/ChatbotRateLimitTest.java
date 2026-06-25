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
 * Rate limit del chatbot: con per-user-max=2, el 3er mensaje del mismo usuario → 429 + Retry-After.
 * Rate limiting ENABLED, pero login generoso para poder autenticarse.
 */
@SpringBootTest(classes = tics.uide.gestionuide.GestionUIDE.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.ratelimit.enabled=true",
        "app.ratelimit.chatbot.per-user-max=2",
        "app.ratelimit.chatbot.max=50",
        "app.ratelimit.login.max=50"
})
public class ChatbotRateLimitTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UsuarioService usuarioService;

    @MockBean private GeminiClient geminiClient;

    private static final String PWD = "Uide2024*";

    private String loginCliente() throws Exception {
        String s = "crl" + UUID.randomUUID().toString().substring(0, 6);
        Usuario u = usuarioService.registrar(UsuarioRegistroDto.builder()
                .username(s).email(s + "@uide.edu.ec").password(PWD).confirmPassword(PWD)
                .nombre("Chat").apellido("Test").telefono("0990000000").build());
        usuarioService.marcarEmailVerificado(u.getId());
        String body = objectMapper.writeValueAsString(Map.of("usernameOrEmail", s, "password", PWD));
        MvcResult res = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn();
        return res.getResponse().getHeader("Authorization");
    }

    @Test
    void excedeLimitePorUsuario_429ConRetryAfter() throws Exception {
        Mockito.when(geminiClient.consultar(Mockito.anyString(), Mockito.any()))
                .thenReturn(GeminiResultado.ok("ok", null));
        String token = loginCliente();

        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/api/chatbot/mensaje").header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON).content("{\"mensaje\":\"hola\"}"))
                    .andExpect(status().isOk());
        }
        // 3er mensaje del mismo usuario -> excede per-user-max=2 -> 429
        mvc.perform(post("/api/chatbot/mensaje").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"mensaje\":\"hola\"}"))
                .andExpect(status().is(429))
                .andExpect(header().exists("Retry-After"));
    }
}
