package Tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Rate limiting con su PROPIO @TestPropertySource (enabled=true + límites bajos), aislado del resto.
 * Cada método usa IP/email distintos para que sus contadores no interfieran entre sí.
 */
@SpringBootTest(classes = tics.uide.gestionuide.GestionUIDE.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.ratelimit.enabled=true",
        "app.ratelimit.window-seconds=60",
        "app.ratelimit.login.max=3",
        "app.ratelimit.login.per-email-max=2"
})
public class RateLimitTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper om;

    private MockHttpServletRequestBuilder login(String usernameOrEmail, String ip) throws Exception {
        return post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("usernameOrEmail", usernameOrEmail, "password", "wrong")))
                .with(r -> { r.setRemoteAddr(ip); return r; });
    }

    @Test
    void bajoLimite_respondeNormal() throws Exception {
        // 1 intento (bajo el límite): credenciales inválidas -> 404, NO 429
        mvc.perform(login("nadie" + UUID.randomUUID() + "@x.com", "10.0.0.1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void excedeLimitePorIp_429ConRetryAfter() throws Exception {
        String ip = "10.0.0.2";
        // 3 intentos (emails distintos para que NO salte el límite por email) -> dentro del límite por IP
        for (int i = 0; i < 3; i++) {
            mvc.perform(login("u" + i + "-" + UUID.randomUUID() + "@x.com", ip))
                    .andExpect(status().isNotFound());
        }
        // 4º intento misma IP -> 429 + Retry-After
        mvc.perform(login("u9-" + UUID.randomUUID() + "@x.com", ip))
                .andExpect(status().is(429))
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void excedeLimitePorEmail_429ConRetryAfter() throws Exception {
        String email = "victima" + UUID.randomUUID() + "@x.com";
        // 2 intentos del mismo email desde IPs distintas (IP nunca acumula) -> dentro del límite por email
        for (int i = 0; i < 2; i++) {
            mvc.perform(login(email, "10.1.0." + i))
                    .andExpect(status().isNotFound());
        }
        // 3er intento mismo email -> salta el límite por email -> 429 + Retry-After
        mvc.perform(login(email, "10.1.0.99"))
                .andExpect(status().is(429))
                .andExpect(header().exists("Retry-After"));
    }
}
