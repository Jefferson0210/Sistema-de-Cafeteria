/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tics.uide.gestionuide.dto;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import tics.uide.gestionuide.enums.Sexo;

/**
 *
 * @author Usuario iTC
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class PersonaDto {

       public static final String NO_VACIO = "Este campo no puede estar vacio";
    public static final String WRONG_SIZE = "Medida equivocada";
    public static final String WRONG_INFORMATION = "informacion incorrecta";
    public static final String WRONG_DATE_ALFANUMERIC_NO_ACCENTS = "Debe solo ingresar letras y numeros sin espaciado";
    public static final String WRONG_DATE_ALFA_NO_ACCENTS = "Debe solo ingresa letras sin espaciado ";
    public static final String WRONG_DATE_CONCATENATED_WORD= "Puede ingresar letras y espaciado pero no numeros";
    public static final String WRONG_DATE_NUMERIC ="Solo se puede ingresar numeros";
    public static final String ALFANUMERIC_NO_ACCENTS = "[A-Za-z0-9]+";
    public static final String ALFA_NO_ACCENTS = "[A-Za-z]+";
    public static final String CONCATENATED_WORD = "[a-zA-Z\\ ]+";
    public static final String DESCRIPTION_USAGE= "[A-Za-z0-9\\ \\.\\-\\_\\*\\@\\#\\$\\%\\&\\^\\+\\=\\{\\[\\}\\]\\:\\;\\<\\>\\,\\?\\¿\\/]+";
    public static final String PASSWORD = "[a-zA-Z0-9\\.\\-\\_\\*\\@\\#\\$\\%\\&\\^\\+\\=\\{\\[\\}\\]\\:\\;\\<\\>\\,\\?\\¿\\/]+";
    public static final String NUMERIC = "[0-9]+";

    @NotBlank(message = NO_VACIO)
    @Size(min = 8, max = 11, message = WRONG_SIZE)
    @Pattern(regexp = NUMERIC, message = WRONG_INFORMATION)
    private String cedula;

    @NotBlank(message = NO_VACIO)
    @Size(min = 0, max = 50, message = WRONG_SIZE)
    @Pattern(regexp = CONCATENATED_WORD, message = WRONG_INFORMATION)
    private String nombres;

    @Enumerated(EnumType.STRING)
    private Sexo sexo;

    //PORNER MENSAJE
    @NotNull
    @Size(min = 1, max = 255, message = WRONG_SIZE)
    @Pattern(regexp = PASSWORD, message = WRONG_INFORMATION)
    private String urlFoto;

    private Long fechaNacimiento;

    @NotBlank
    @Size(min = 8, max = 10, message = WRONG_SIZE)
    @Pattern(regexp = NUMERIC, message = WRONG_INFORMATION)
    private String celular;

    @NotBlank
    @Size(min = 1, max = 80, message = WRONG_SIZE)
    @Email(message = WRONG_INFORMATION)
    private String email;

}
