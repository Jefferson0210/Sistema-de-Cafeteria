package tics.uide.gestionuide.ratelimit;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import tics.uide.gestionuide.service.RateLimitService;


public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws IOException {
        String endpoint = endpointDe(req.getRequestURI());
        if (endpoint == null) return true;   // ruta no limitada
        long retry = rateLimitService.chequearIp(req, endpoint);
        if (retry > 0) {
            res.setStatus(429);   // 429 Too Many Requests
            res.setHeader("Retry-After", String.valueOf(retry));
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write(
                    "{\"success\":false,\"message\":\"Demasiados intentos, espera un momento\",\"data\":null}");
            return false;
        }
        return true;
    }

    private String endpointDe(String uri) {
        if (uri == null) return null;
        if (uri.endsWith("/api/auth/login"))                 return "login";
        if (uri.endsWith("/api/auth/registro"))              return "register";
        if (uri.endsWith("/api/auth/recuperar-password"))    return "recuperar";
        if (uri.endsWith("/api/auth/restablecer-password"))  return "restablecer";
        if (uri.endsWith("/api/auth/reenviar-verificacion")) return "reenviar";
        if (uri.endsWith("/api/auth/verificar-email"))       return "verificar-email";
        if (uri.endsWith("/api/auth/refresh"))               return "refresh";
        return null;
    }
}
