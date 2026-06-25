/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tics.uide.gestionuide.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tics.uide.gestionuide.enums.MetodoPago;

/**
 * DTO para Pago
 * @author DELL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "La factura es obligatoria")
    private Long facturaId;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal monto;

    private String referencia;

    private Date fechaPago;
}
