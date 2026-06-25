package tics.uide.gestionuide.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Item de la comanda para la pantalla de cocina (SSE). Plano e inmutable: solo lo que cocina
 * necesita para preparar (qué plato, cuántos, con qué notas). Sin precio/subtotal: cocina prepara, no cobra.
 * Se construye con datos copiados DENTRO de la transacción (sin proxies de Hibernate).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemEventoDto {
    private String productoNombre;
    private Integer cantidad;
    private String notas;
}
