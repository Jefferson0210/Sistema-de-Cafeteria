/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cafeteria.app.dto;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para DetalleFactura mejorado
 * @author DELL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleFacturaDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "El producto es obligatorio")
    private Long productoId;

    private String productoNombre; // Solo lectura

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser positiva")
    private Double cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser positivo")
    private Double precioUnitario;

    private Double subtotal; // Calculado automáticamente
}
