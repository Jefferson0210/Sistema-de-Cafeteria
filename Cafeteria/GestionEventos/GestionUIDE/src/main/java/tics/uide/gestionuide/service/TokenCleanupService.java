package tics.uide.gestionuide.service;

import java.util.Date;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tics.uide.gestionuide.repository.EmailVerificationTokenRepository;
import tics.uide.gestionuide.repository.PasswordResetTokenRepository;
import tics.uide.gestionuide.repository.RefreshTokenRepository;

/**
 * Job de limpieza de tokens caducados (reset, verificación, refresh). En memoria/cron, sin infra.
 * Criterio único: expiraEn < (ahora - graceDays). El margen de gracia garantiza que un token
 * recién creado nunca se purga, y deja una ventana para inspeccionar incidentes recientes.
 * Nota: un refresh REVOCADO pero reciente (expiraEn futuro) sobrevive -> preserva la detección de reuso.
 */
@Service
public class TokenCleanupService {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupService.class);

    @Value("${app.token-cleanup.enabled:true}")
    private boolean enabled;

    @Value("${app.token-cleanup.grace-days:7}")
    private long graceDays;

    @Autowired private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired private EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;

    /** Disparo programado: 1 vez al día (cron configurable). Respeta el flag enabled. */
    @Scheduled(cron = "${app.token-cleanup.cron:0 0 3 * * *}")
    @Transactional
    public void limpiezaProgramada() {
        if (!enabled) return;
        purgar();
    }

    /** Borra los tokens caducados hace más de graceDays en las 3 tablas. Devuelve el total borrado. */
    @Transactional
    public int purgar() {
        Date cutoff = new Date(System.currentTimeMillis() - graceDays * 24L * 60 * 60 * 1000);
        int reset = passwordResetTokenRepository.deleteExpiradosAntesDe(cutoff);
        int verif = emailVerificationTokenRepository.deleteExpiradosAntesDe(cutoff);
        int refresh = refreshTokenRepository.deleteExpiradosAntesDe(cutoff);
        log.info("Limpieza de tokens: reset={}, verificacion={}, refresh={} (cutoff={})",
                reset, verif, refresh, cutoff);
        return reset + verif + refresh;
    }
}
