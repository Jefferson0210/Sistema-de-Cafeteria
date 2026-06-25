package Tests;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.cafeteria.app.dto.*;
import com.cafeteria.app.enums.EstadoReserva;
import com.cafeteria.app.model.Reserva;
import com.cafeteria.app.model.Usuario;
import com.cafeteria.app.service.ChatbotReservaService;
import com.cafeteria.app.service.GeminiClient;
import com.cafeteria.app.service.MesaService;
import com.cafeteria.app.service.ReservaService;
import com.cafeteria.app.service.UsuarioService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Chatbot Etapa 2: la frontera de confianza. Gemini SIEMPRE mockeado (nunca real).
 * Gemini PROPONE; el backend DISPONE: ningún dato llega a la reserva sin validar, el usuario sale del token.
 */
@SpringBootTest(classes = com.cafeteria.app.CafeteriaApp.class)
public class ChatbotReservaServiceTest {

    @Autowired private ChatbotReservaService chatbotReservaService;
    @Autowired private ReservaService reservaService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private MesaService mesaService;

    @MockBean private GeminiClient geminiClient;

    private static final String PWD = "Uide2024*";

    private Usuario nuevoUsuario() {
        String s = "res" + UUID.randomUUID().toString().substring(0, 6);
        Usuario u = usuarioService.registrar(UsuarioRegistroDto.builder()
                .username(s).email(s + "@uide.edu.ec").password(PWD).confirmPassword(PWD)
                .nombre("Reserva").apellido("Test").telefono("0990000000").build());
        usuarioService.marcarEmailVerificado(u.getId());
        return u;
    }

    private void nuevaMesa(int capacidad) {
        mesaService.crear(MesaDto.builder()
                .numeroMesa(50000 + Math.abs(UUID.randomUUID().hashCode() % 800000))
                .capacidad(capacidad).ubicacion("x").activo(true).build());
    }

    private void mockGemini(String reply, CamposReserva campos) {
        Mockito.when(geminiClient.consultar(Mockito.anyString(), Mockito.any()))
                .thenReturn(GeminiResultado.ok(reply, campos));
    }

    private CamposReserva campos(String fecha, String hora, Integer personas, List<String> faltan) {
        return CamposReserva.builder().fecha(fecha).hora(hora).numPersonas(personas).faltan(faltan).build();
    }

    // ---- datos completos y válidos -> reserva CREADA (mesa suficiente, usuario del token, PENDIENTE) ----
    @Test
    void datosValidos_creaReserva() {
        Usuario u = nuevoUsuario();
        nuevaMesa(4);
        mockGemini("ok", campos("2030-06-15", "20:00", 4, List.of()));

        RespuestaChatbot r = chatbotReservaService.procesar("quiero mesa para 4 el 15 de junio a las 8", null, u.getId());

        assertNotNull(r.getReservaId(), "debe crearse la reserva");
        Reserva reserva = reservaService.buscarPorId(r.getReservaId());
        assertEquals(u.getId(), reserva.getUsuario().getId(), "la reserva es del usuario del TOKEN");
        assertTrue(reserva.getMesa().getCapacidad() >= 4, "mesa con capacidad suficiente");
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    // ---- faltan datos -> NO crea, el bot pregunta ----
    @Test
    void faltanDatos_noCrea_pregunta() {
        Usuario u = nuevoUsuario();
        mockGemini("¿Para qué hora?", campos("2030-06-15", null, 4, List.of("hora")));

        RespuestaChatbot r = chatbotReservaService.procesar("quiero mesa para 4 el 15", null, u.getId());

        assertNull(r.getReservaId(), "faltan datos -> no se crea reserva");
        assertNotNull(r.getReply());
    }

    // ---- fecha pasada -> NO crea, mensaje claro ----
    @Test
    void fechaPasada_noCrea() {
        Usuario u = nuevoUsuario();
        nuevaMesa(4);
        mockGemini("ok", campos("2020-01-01", "20:00", 2, List.of()));

        RespuestaChatbot r = chatbotReservaService.procesar("mesa para 2 el 1 de enero de 2020", null, u.getId());

        assertNull(r.getReservaId());
        assertTrue(r.getReply().toLowerCase().contains("pas"), "mensaje claro de fecha pasada");
    }

    // ---- sin mesa con capacidad suficiente -> NO crea ----
    @Test
    void sinMesaConCapacidad_noCrea() {
        Usuario u = nuevoUsuario();
        nuevaMesa(4);   // solo hay capacidad 4; se piden 10
        mockGemini("ok", campos("2030-06-15", "20:00", 10, List.of()));

        RespuestaChatbot r = chatbotReservaService.procesar("mesa para 10", null, u.getId());

        assertNull(r.getReservaId());
        assertTrue(r.getReply().toLowerCase().contains("no hay mesa"));
    }

    // ---- DATO MANIPULADO de Gemini (fecha imposible/no parseable) -> rechazado por el BACKEND ----
    @Test
    void datoManipulado_fechaImposible_rechazadoPorBackend() {
        Usuario u = nuevoUsuario();
        nuevaMesa(4);
        // Gemini "dice" una fecha imposible; el backend NO confía y la rechaza al parsear
        mockGemini("ok", campos("2020-13-45", "99:99", 4, List.of()));

        RespuestaChatbot r = chatbotReservaService.procesar("mensaje cualquiera", null, u.getId());

        assertNull(r.getReservaId(), "la frontera de confianza: un dato imposible de Gemini NO crea reserva");
    }
}
