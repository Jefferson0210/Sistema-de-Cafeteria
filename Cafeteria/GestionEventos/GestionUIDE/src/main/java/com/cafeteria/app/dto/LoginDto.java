package com.cafeteria.app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginDto {

    // El service usa getUsernameOrEmail()
    // Esto permite que el frontend mande: usuario, user, email, username, etc.
    @JsonAlias({"usernameOrEmail", "username", "usuario", "user", "email"})
    private String usernameOrEmail;

    @JsonAlias({"password", "pass", "clave", "contrasena"})
    private String password;
}
