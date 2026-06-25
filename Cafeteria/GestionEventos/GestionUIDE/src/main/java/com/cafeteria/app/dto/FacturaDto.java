/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cafeteria.app.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.cafeteria.app.enums.EstadoFactura;

/**
 * DTO para Factura mejorado
 * @author DELL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturaDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "El número de factura es obligatorio")
    private String numeroFactura;

    private Long pedidoId; // Opcional - puede ser factura manual

    private Long clienteId; // Opcional - puede ser cliente sin cuenta

    private String clienteNombre; // Solo lectura

    @NotNull(message = "El cajero es obligatorio")
    private Long cajeroId;

    private String cajeroNombre; // Solo lectura

    private String empresaRuc; // Opcional

    @NotNull(message = "El subtotal es obligatorio")
    @Positive(message = "El subtotal debe ser positivo")
    private Double subtotal;

    @NotNull(message = "El IVA es obligatorio")
    private Double iva;

    private Double descuento;

    @NotNull(message = "El total es obligatorio")
    @Positive(message = "El total debe ser positivo")
    private Double total;

    private EstadoFactura estado;

    private Date fechaEmision;

    private List<DetalleFacturaDto> detalles;

    private List<PagoDto> pagos;
}
