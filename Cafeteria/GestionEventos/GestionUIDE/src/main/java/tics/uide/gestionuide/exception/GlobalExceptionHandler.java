package tics.uide.gestionuide.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tics.uide.gestionuide.dto.ApiResponse;

/**
 * Manejador global de excepciones - CONSOLIDADO
 * Reemplaza tanto ExeptionHandler.java como exception/GlobalExceptionHandler.java
 * que eran redundantes y potencialmente conflictivos
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // NotFoundException → 404
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, ex.getMessage(), null));
    }

    // BadRequestException → 400
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(false, ex.getMessage(), null));
    }

    // ConflictException → 409
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse(false, ex.getMessage(), null));
    }

    // DTO validation errors → 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Validación fallida", errores));
    }

    // Type mismatch (e.g. /api/pedidos/null) → 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String msg = "Parámetro inválido: '" + ex.getName() + "'. Valor recibido: " +
                (ex.getValue() == null ? "null" : ex.getValue().toString());
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, msg, null));
    }

    // Constraint violations → 400 (sin ex.getMessage(): puede exponer property-paths internos)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraint(ConstraintViolationException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Validación fallida", null));
    }

    // Bad JSON → 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleBadJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "JSON inválido o mal formado", null));
    }

    // Missing request param → 400
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Falta parámetro requerido: " + ex.getParameterName(), null));
    }

    // DB constraint violation → 400
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        logger.error("Data integrity violation", ex);
        return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Error de integridad de datos. Revise duplicados o referencias.", null));
    }

    // Acceso denegado por @PreAuthorize / method security → 403 (no 500).
    // Más específico que el catch-all Exception, así que Spring lo prefiere.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse(false, "No autorizado para esta acción", null));
    }

    // Rate limit excedido (por email) → 429 + Retry-After
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiResponse> handleTooMany(TooManyRequestsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(new ApiResponse(false, ex.getMessage(), null));
    }

    // Catch-all → 500. El cliente ve un mensaje genérico + un ref; el log guarda el stacktrace
    // completo con el MISMO ref para diagnóstico. Nunca se filtra ex.getMessage() ni el stacktrace.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneric(Exception ex) {
        String ref = UUID.randomUUID().toString().substring(0, 8);
        logger.error("Error no controlado [ref={}]", ref, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false,
                        "Ha ocurrido un error interno. Si persiste, contacta soporte (ref: " + ref + ")", null));
    }
}
