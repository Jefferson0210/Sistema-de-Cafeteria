package com.cafeteria.app.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Datos ESTRUCTURADOS extraídos por Gemini de la conversación de reserva.
 * En Etapa 1 solo se devuelven (NO se actúa sobre ellos). El backend los validará en Etapa 2.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CamposReserva {
    private String fecha;          // "YYYY-MM-DD"
    private String hora;           // "HH:mm"
    private Integer numPersonas;
    private List<String> faltan;   // campos que el modelo considera que faltan
}
