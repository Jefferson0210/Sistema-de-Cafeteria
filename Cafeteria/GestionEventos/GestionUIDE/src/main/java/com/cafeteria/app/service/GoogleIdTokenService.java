package com.cafeteria.app.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.cafeteria.app.exception.BadRequestException;

@Service
public class GoogleIdTokenService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleIdTokenService(@Value("${google.oauth.client-id}") String webClientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance()
        )
        .setAudience(Collections.singletonList(webClientId))
        .build();
    }

    public GoogleIdToken.Payload verificar(String idToken) {
        if (idToken == null || idToken.trim().isEmpty()) {
            throw new BadRequestException("El idToken es obligatorio");
        }

        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new BadRequestException("Token de Google inválido o expirado");
            }

            GoogleIdToken.Payload payload = token.getPayload();

            Object ev = payload.get("email_verified");
            boolean emailVerified = (ev instanceof Boolean) && (Boolean) ev;
            if (!emailVerified) {
                throw new BadRequestException("El email de Google no está verificado");
            }

            return payload;

        } catch (GeneralSecurityException | IOException e) {
            throw new BadRequestException("No se pudo verificar el token de Google");
        }
    }
}
