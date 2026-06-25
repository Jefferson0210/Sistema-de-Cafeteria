package com.cafeteria.app.service;

import java.util.Date;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.cafeteria.app.exception.BadRequestException;
import com.cafeteria.app.model.PasswordResetToken;
import com.cafeteria.app.model.Usuario;
import com.cafeteria.app.repository.PasswordResetTokenRepository;
import com.cafeteria.app.util.Tokens;

/**
 * Recuperación de contraseña por token de un solo uso (1 hora), enviado por correo.
 * Se guarda el HASH SHA-256 del token (no el token crudo); el crudo solo viaja en el email.
 */
@Service
@Transactional
public class PasswordResetService {

    private static final long TTL_MS = 60L * 60 * 1000; // 1 hora

    @Autowired private UsuarioService usuarioService;
    @Autowired private PasswordResetTokenRepository tokenRepo;
    @Autowired private EmailService emailService;
    @Autowired private AuditService auditService;
    @Autowired private RefreshTokenService refreshTokenService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /** Genera y envía el enlace de reset. Silencioso: nunca revela si el email existe. */
    public void solicitar(String email) {
        if (email == null || email.trim().isEmpty()) return;
        // Búsqueda que NO lanza: si lanzara dentro de la transacción la marcaría rollback-only
        // y el commit fallaría (500), delatando qué emails existen.
        java.util.Optional<Usuario> opt = usuarioService.buscarPorEmailOpcional(email.trim());
        if (opt.isEmpty()) return;   // email no registrado -> sin token, misma respuesta genérica
        Usuario u = opt.get();
        if (!Boolean.TRUE.equals(u.getActivo())) return;

        // Invalida tokens previos no usados del usuario
        tokenRepo.findByUsuario_IdAndUsadoFalse(u.getId()).forEach(t -> t.setUsado(true));

        String raw = Tokens.generar();
        tokenRepo.save(PasswordResetToken.builder()
                .usuario(u)
                .tokenHash(Tokens.sha256(raw))
                .expiraEn(new Date(System.currentTimeMillis() + TTL_MS))
                .usado(false)
                .build());

        String enlace = frontendUrl + "/restablecer-password?token=" + raw;
        emailService.enviarEnlaceRecuperacion(u.getEmail(), u.getNombre(), enlace);
    }

    /** Valida el token (hash, no usado, no expirado) y cambia la contraseña con BCrypt. */
    public void restablecer(String rawToken, String nuevaPassword) {
        PasswordResetToken t = tokenRepo.findByTokenHash(Tokens.sha256(rawToken))
                .filter(x -> !x.isUsado() && x.getExpiraEn().after(new Date()))
                .orElseThrow(() -> new BadRequestException("Token inválido o expirado"));

        usuarioService.resetPassword(t.getUsuario().getId(), nuevaPassword);
        t.setUsado(true);
        // Seguridad: al cambiar la contraseña, se revocan todas las sesiones (refresh tokens) del usuario.
        refreshTokenService.revocarTodos(t.getUsuario().getId());
        auditService.registrar("PASSWORD_RESET", "Usuario", t.getUsuario().getId(), "via token de recuperación");
    }

}
