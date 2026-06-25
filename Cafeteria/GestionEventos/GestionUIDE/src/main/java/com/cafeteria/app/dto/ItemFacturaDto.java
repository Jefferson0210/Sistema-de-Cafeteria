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
 * DTO para agregar items a una factura
 * @author DELL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemFacturaDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser positiva")
    private Double cantidad;
}
