/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tics.uide.gestionuide.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para Empresa
 * @author DELL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "El RUC es obligatorio")
    @Size(min = 10, max = 13, message = "El RUC debe tener entre 10 y 13 caracteres")
    private String ruc;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @Size(max = 100, message = "El nombre comercial no puede exceder 100 caracteres")
    private String nombreComercial;

    private BigDecimal iva;

    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String direccion;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @Email(message = "El email no es válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    private String logoUrl;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    private Boolean activo;
}