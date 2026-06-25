package com.cafeteria.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Resultado de una consulta a Gemini. Si {@code degradado} es true, la llamada falló
 * (timeout/error/JSON inválido) y {@code mensaje} es una respuesta amable; nunca se propaga el error.
 */
@Data
@AllArgsConstructor
public class GeminiResultado {
    private boolean degradado;
    private String mensaje;
    private CamposReserva campos;

    public static GeminiResultado ok(String mensaje, CamposReserva campos) {
        return new GeminiResultado(false, mensaje, campos);
    }

    public static GeminiResultado degradado(String mensaje) {
        return new GeminiResultado(true, mensaje, null);
    }
}
