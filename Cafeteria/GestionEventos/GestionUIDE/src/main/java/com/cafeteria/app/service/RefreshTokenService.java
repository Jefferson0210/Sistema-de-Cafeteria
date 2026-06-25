package com.cafeteria.app.service;

import java.util.Date;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.cafeteria.app.exception.BadRequestException;
import com.cafeteria.app.model.RefreshToken;
import com.cafeteria.app.model.Usuario;
import com.cafeteria.app.repository.RefreshTokenRepository;
import com.cafeteria.app.util.Tokens;

/**
 * Refresh tokens OPACOS persistidos (hash SHA-256), revocables y rotados.
 * El access token (JWT corto) lo sigue emitiendo AutenticacionWS; aquí solo gestionamos el refresh.
 */
@Service
@Transactional
public class RefreshTokenService {

    @Value("${app.jwt.refresh-ttl-days:7}")
    private long ttlDays;

    @Autowired
    private RefreshTokenRepository tokenRepo;

    /** Emite un refresh token nuevo y devuelve el valor CRUDO (solo se entrega aquí). */
    public String emitir(Usuario u) {
        String raw = Tokens.generar();
        tokenRepo.save(RefreshToken.builder()
                .usuario(u)
                .tokenHash(Tokens.sha256(raw))
                .expiraEn(new Date(System.currentTimeMillis() + ttlDays * 24L * 60 * 60 * 1000))
                .revocado(false)
                .build());
        return raw;
    }

    /**
     * Valida y ROTA el refresh: revoca el actual y emite uno nuevo.
     * Detección de reuso: si llega un refresh ya REVOCADO (señal de robo) revoca TODAS las sesiones del usuario.
     */
    // dontRollbackOn: en el caso de reuso revocamos TODOS los tokens y lanzamos excepción;
    // sin esto, el rollback por la excepción desharía esa revocación (la detección de robo no surtiría efecto).
    @Transactional(dontRollbackOn = BadRequestException.class)
    public Rotacion rotar(String rawRefresh) {
        if (rawRefresh == null || rawRefresh.isBlank()) {
            throw new BadRequestException("Refresh token requerido");
        }
        RefreshToken t = tokenRepo.findByTokenHash(Tokens.sha256(rawRefresh))
                .orElseThrow(() -> new BadRequestException("Refresh token inválido"));

        if (t.isRevocado()) {
            // reuso de un token ya revocado -> posible robo -> cerrar todas las sesiones
            revocarTodos(t.getUsuario().getId());
            throw new BadRequestException("Refresh token revocado (posible reuso); se cerraron todas las sesiones");
        }
        if (t.getExpiraEn().before(new Date())) {
            throw new BadRequestException("Refresh token expirado");
        }

        t.setRevocado(true);                  // rota: invalida el actual
        Usuario u = t.getUsuario();
        String nuevoRaw = emitir(u);          // y emite uno nuevo
        return new Rotacion(u, nuevoRaw);
    }

    /** Revoca un refresh concreto (logout). */
    public void revocar(String rawRefresh) {
        if (rawRefresh == null || rawRefresh.isBlank()) return;
        tokenRepo.findByTokenHash(Tokens.sha256(rawRefresh)).ifPresent(t -> t.setRevocado(true));
    }

    /** Revoca TODAS las sesiones (refresh tokens) del usuario. */
    public void revocarTodos(Long usuarioId) {
        tokenRepo.findByUsuario_IdAndRevocadoFalse(usuarioId).forEach(t -> t.setRevocado(true));
    }

    /** Resultado de la rotación: el usuario y el nuevo refresh crudo. */
    public static class Rotacion {
        public final Usuario usuario;
        public final String nuevoRefresh;
        public Rotacion(Usuario usuario, String nuevoRefresh) {
            this.usuario = usuario;
            this.nuevoRefresh = nuevoRefresh;
        }
    }
}
