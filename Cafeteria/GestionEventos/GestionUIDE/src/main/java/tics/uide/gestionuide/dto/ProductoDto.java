package tics.uide.gestionuide.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para Producto - CORREGIDO
 * Eliminado @NotNull duplicado + @JsonAlias mal ubicado en campo nombre
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonAlias({"idProducto", "productoId", "id_producto"})
    private Long id;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser positivo")
    private BigDecimal precio;

    private Long categoryId;

    private String categoryName; // Solo lectura

    private Integer stock;

    private Boolean disponible;

    private String imagenUrl;
}
