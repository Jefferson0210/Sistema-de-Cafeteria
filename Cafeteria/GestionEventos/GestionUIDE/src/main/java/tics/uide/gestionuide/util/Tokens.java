package tics.uide.gestionuide.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Generación y hashing de tokens opacos (reset de contraseña, verificación de email). */
public final class Tokens {

    private static final SecureRandom RNG = new SecureRandom();

    private Tokens() {}

    /** Token aleatorio opaco: 32 bytes SecureRandom -> Base64URL (~43 chars). */
    public static String generar() {
        byte[] bytes = new byte[32];
        RNG.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** SHA-256 (hex) del token; en BD se guarda el hash, no el token crudo. */
    public static String sha256(String s) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(h.length * 2);
            for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
