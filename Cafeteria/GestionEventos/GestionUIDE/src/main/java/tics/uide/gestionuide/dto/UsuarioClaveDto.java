/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tics.uide.gestionuide.dto;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import static tics.uide.gestionuide.dto.PersonaDto.ALFANUMERIC_NO_ACCENTS;
import static tics.uide.gestionuide.dto.PersonaDto.PASSWORD;
import static tics.uide.gestionuide.dto.PersonaDto.WRONG_INFORMATION;
import static tics.uide.gestionuide.dto.PersonaDto.WRONG_SIZE;

/**
 *
 * @author Dante
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioClaveDto {

    @NotBlank
    @Size(min = 4, max = 45, message = WRONG_SIZE)
    @Pattern(regexp = ALFANUMERIC_NO_ACCENTS, message = WRONG_INFORMATION)
    private String usuario;

    @Size(min = 8, max = 16, message = WRONG_SIZE)
    @Pattern(regexp = PASSWORD, message = WRONG_INFORMATION)
    private String clave;
}
