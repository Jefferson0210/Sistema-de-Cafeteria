package tics.uide.gestionuide.service;

import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Rate limiting en memoria (ventana fija) para endpoints sensibles. Una sola instancia, sin infra.
 * Núcleo atómico por clave (ConcurrentHashMap.compute). Devuelve los segundos de Retry-After cuando
 * se excede, o 0 si está dentro del límite. NUNCA lanza. Desactivable con app.ratelimit.enabled=false.
 */
@Service
public class RateLimitService {

    @Value("${app.ratelimit.enabled:true}")
    private boolean enabled;
    @Value("${app.ratelimit.trust-forwarded-for:false}")
    private boolean trustForwardedFor;
    @Value("${app.ratelimit.window-seconds:60}")
    private long windowSeconds;

    @Value("${app.ratelimit.login.max:20}")            private int loginMax;
    @Value("${app.ratelimit.login.per-email-max:5}")   private int loginEmailMax;
    @Value("${app.ratelimit.register.max:5}")          private int registerMax;
    @Value("${app.ratelimit.recuperar.max:5}")         private int recuperarMax;
    @Value("${app.ratelimit.recuperar.per-email-max:3}") private int recuperarEmailMax;
    @Value("${app.ratelimit.restablecer.max:10}")      private int restablecerMax;
    @Value("${app.ratelimit.reenviar.max:5}")          private int reenviarMax;
    @Value("${app.ratelimit.reenviar.per-email-max:3}") private int reenviarEmailMax;
    @Value("${app.ratelimit.verificar-email.max:10}")  private int verificarEmailMax;
    @Value("${app.ratelimit.refresh.max:30}")          private int refreshMax;

    private final ConcurrentHashMap<String, Ventana> contadores = new ConcurrentHashMap<>();

    private static final class Ventana {
        long inicio;
        int count;
        Ventana(long inicio) { this.inicio = inicio; }
    }

    /** Cuenta un intento para la clave; devuelve segundos de Retry-After si se excede, o 0 si OK. */
    private long consumir(String clave, int max) {
        if (!enabled) return 0;
        final long ahora = System.currentTimeMillis();
        final long ventanaMs = windowSeconds * 1000L;
        final long[] retry = {0};
        contadores.compute(clave, (k, v) -> {
            if (v == null || ahora - v.inicio >= ventanaMs) {
                v = new Ventana(ahora);          // ventana nueva (o expirada)
            }
            v.count++;
            if (v.count > max) {
                long restanteMs = ventanaMs - (ahora - v.inicio);
                retry[0] = Math.max(1, (restanteMs + 999) / 1000);   // segundos hacia arriba, mínimo 1
            }
            return v;
        });
        return retry[0];
    }

    /** Límite por IP para un endpoint. Retry-After (s) o 0. */
    public long chequearIp(HttpServletRequest req, String endpoint) {
        return consumir("ip:" + endpoint + ":" + ipDe(req), maxIp(endpoint));
    }

    /** Límite por email para un endpoint (login, recuperar, reenviar). Retry-After (s) o 0. */
    public long chequearEmail(String endpoint, String email) {
        if (email == null || email.trim().isEmpty()) return 0;
        int max = maxEmail(endpoint);
        if (max <= 0) return 0;   // endpoint sin límite por email
        return consumir("email:" + endpoint + ":" + email.trim().toLowerCase(), max);
    }

    /** IP del cliente. Solo confía en X-Forwarded-For si está habilitado (si no, es spoofeable). */
    public String ipDe(HttpServletRequest req) {
        if (trustForwardedFor) {
            String xff = req.getHeader("X-Forwarded-For");
            if (xff != null && !xff.trim().isEmpty()) {
                return xff.split(",")[0].trim();   // primer token = cliente original
            }
        }
        return req.getRemoteAddr();
    }

    private int maxIp(String endpoint) {
        switch (endpoint) {
            case "login":           return loginMax;
            case "register":        return registerMax;
            case "recuperar":       return recuperarMax;
            case "restablecer":     return restablecerMax;
            case "reenviar":        return reenviarMax;
            case "verificar-email": return verificarEmailMax;
            case "refresh":         return refreshMax;
            default:                return Integer.MAX_VALUE;
        }
    }

    private int maxEmail(String endpoint) {
        switch (endpoint) {
            case "login":     return loginEmailMax;
            case "recuperar": return recuperarEmailMax;
            case "reenviar":  return reenviarEmailMax;
            default:          return 0;
        }
    }

    /** Barrido periódico: elimina ventanas expiradas para no fugar memoria. */
    @Scheduled(fixedRate = 300_000)
    public void limpiar() {
        long ahora = System.currentTimeMillis();
        long ventanaMs = windowSeconds * 1000L;
        contadores.entrySet().removeIf(e -> ahora - e.getValue().inicio >= ventanaMs);
    }
}
