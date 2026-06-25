package com.cafeteria.app.dto;

import java.io.Serializable;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPerfilDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100)
    private String nombre;

    @Size(max = 100)
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    private String email;

    @Size(max = 20)
    private String telefono;
}