package Tests;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cafeteria.app.dto.ApiResponse;
import com.cafeteria.app.exception.BadRequestException;
import com.cafeteria.app.exception.GlobalExceptionHandler;

import static org.junit.jupiter.api.Assertions.*;

/**
 * El catch-all 500 NO debe filtrar el mensaje de la excepción ni el stacktrace al cliente.
 * Los mensajes de negocio (BadRequest, etc.) SÍ deben seguir llegando intactos.
 */
public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void catchAll_noFiltraDetalleInterno() {
        String secreto = "detalle interno secreto: tabla usuario, NullPointer en FooService.bar()";
        ResponseEntity<ApiResponse> resp = handler.handleGeneric(new RuntimeException(secreto));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        ApiResponse body = resp.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());

        String msg = body.getMessage();
        // NO debe contener el mensaje de la excepción ni rastros de stacktrace
        assertFalse(msg.contains(secreto), "el mensaje no debe filtrar el detalle de la excepción");
        assertFalse(msg.contains("NullPointer"), "el mensaje no debe filtrar nombres internos");
        assertFalse(msg.contains("FooService"), "el mensaje no debe filtrar nombres de clase");
        assertFalse(msg.toLowerCase().contains("exception"), "el mensaje no debe mencionar excepciones");
        // SÍ debe ser el genérico con ref de correlación
        assertTrue(msg.contains("ref:"), "el mensaje genérico debe incluir el ref de correlación");
    }

    @Test
    void mensajesDeNegocio_siguenIntactos() {
        // Los errores de negocio NO se tocan: su mensaje llega tal cual al cliente.
        ResponseEntity<ApiResponse> resp = handler.handleBadRequest(new BadRequestException("Stock insuficiente para X"));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Stock insuficiente para X", resp.getBody().getMessage());

        ResponseEntity<ApiResponse> resp2 = handler.handleBadRequest(new BadRequestException("Token inválido o expirado"));
        assertEquals("Token inválido o expirado", resp2.getBody().getMessage());
    }
}
