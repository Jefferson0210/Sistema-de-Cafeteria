package com.cafeteria.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * DTO para autenticación con Google/Microsoft OAuth2
 * @author DELL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2LoginDto implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    private String email;
    
    private String nombre;
    private String apellido;
    
    @NotBlank(message = "El provider es obligatorio")
    private String provider; // "google" o "microsoft"
    
    @NotBlank(message = "El idToken es obligatorio")
    private String idToken; // Token ID de Google/Microsoft para validar
}