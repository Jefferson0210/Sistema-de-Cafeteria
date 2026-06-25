package Tests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Actuator: solo el healthcheck es público (UP/DOWN, sin detalle); cualquier otro endpoint
 * (p.ej. /env, que filtraría secretos) NO es accesible ni expuesto.
 */
@SpringBootTest(classes = com.cafeteria.app.CafeteriaApp.class)
@AutoConfigureMockMvc
public class ActuatorTest {

    @Autowired private MockMvc mvc;

    @Test
    void health_publico_devuelveUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void env_bloqueado_noAccesible() throws Exception {
        // Endpoint sensible (variables de entorno, incluye secretos): ni expuesto ni accesible.
        mvc.perform(get("/actuator/env"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void health_noFiltraDetalleDeComponentes() throws Exception {
        // show-details=never -> solo el status agregado, sin desglose (BD, disco...).
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components").doesNotExist());
    }
}
