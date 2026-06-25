package com.cafeteria.app.service;

import java.util.Date;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.cafeteria.app.exception.BadRequestException;
import com.cafeteria.app.model.EmailVerificationToken;
import com.cafeteria.app.model.Usuario;
import com.cafeteria.app.repository.EmailVerificationTokenRepository;
import com.cafeteria.app.util.Tokens;

/**
 * Verificación de correo (doble opt-in) con token de un solo uso (24h), enviado por correo.
 * Guarda el HASH SHA-256 del token (no el crudo); el crudo solo viaja en el email.
 */
@Service
@Transactional
public class EmailVerificationService {

    private static final long TTL_MS = 24L * 60 * 60 * 1000; // 24 horas

    @Autowired private UsuarioService usuarioService;
    @Autowired private EmailVerificationTokenRepository tokenRepo;
    @Autowired private EmailService emailService;
    @Autowired private AuditService auditService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /** Genera token y envía el correo de verificación. El envío es best-effort (no tumba el registro). */
    public void enviarVerificacion(Usuario u) {
        if (u == null || Boolean.TRUE.equals(u.getEmailVerificado())) return;

        // Invalida tokens previos no usados del usuario
        tokenRepo.findByUsuario_IdAndUsadoFalse(u.getId()).forEach(t -> t.setUsado(true));

        String raw = Tokens.generar();
        tokenRepo.save(EmailVerificationToken.builder()
                .usuario(u)
                .tokenHash(Tokens.sha256(raw))
                .expiraEn(new Date(System.currentTimeMillis() + TTL_MS))
                .usado(false)
                .build());

        String enlace = frontendUrl + "/verificar-email?token=" + raw;
        try {
            emailService.enviarEnlaceVerificacion(u.getEmail(), u.getNombre(), enlace);
        } catch (Exception e) {
            // best-effort: el token queda creado; el usuario puede reenviar la verificación
        }
    }

    /** Marca el email como verificado si el token es válido (no usado, no expirado). */
    public void verificar(String rawToken) {
        EmailVerificationToken t = tokenRepo.findByTokenHash(Tokens.sha256(rawToken))
                .filter(x -> !x.isUsado() && x.getExpiraEn().after(new Date()))
                .orElseThrow(() -> new BadRequestException("Token inválido o expirado"));

        Usuario u = t.getUsuario();
        u.setEmailVerificado(true);
        usuarioService.guardar(u);
        t.setUsado(true);
        auditService.registrar("EMAIL_VERIFICADO", "Usuario", u.getId(), "verificación de correo");
    }

    /** Reenvía la verificación. Silencioso: no revela si el email existe ni su estado. */
    public void reenviar(String email) {
        if (email == null || email.trim().isEmpty()) return;
        Optional<Usuario> opt = usuarioService.buscarPorEmailOpcional(email.trim());
        if (opt.isEmpty()) return;
        Usuario u = opt.get();
        if (Boolean.TRUE.equals(u.getEmailVerificado())) return; // ya verificado: nada que reenviar
        enviarVerificacion(u);
    }
}
