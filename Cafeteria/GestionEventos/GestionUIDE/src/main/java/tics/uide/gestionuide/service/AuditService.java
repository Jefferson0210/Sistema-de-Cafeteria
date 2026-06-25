package tics.uide.gestionuide.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tics.uide.gestionuide.model.AuditLog;
import tics.uide.gestionuide.repository.AuditLogRepository;

/**
 * Bitácora de auditoría. Se llama DENTRO de la transacción de la acción,
 * por lo que el registro es atómico con ella (si la acción se revierte, el log también).
 */
@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /** Username del autenticado (igual que lo coloca el JWTAuthorizationFilter); "anonymous" si no hay. */
    private String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "anonymous";
    }

    /** true solo si hay un usuario REAL autenticado (no nulo ni anónimo). */
    private boolean hayUsuarioReal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    /** Registra siempre la acción con el username actual. */
    public void registrar(String accion, String entidad, Long entidadId, String detalle) {
        auditLogRepository.save(AuditLog.builder()
                .usuario(usuarioActual()).accion(accion).entidad(entidad)
                .entidadId(entidadId).detalle(detalle).build());
    }

    /** Registra solo si hay un usuario real autenticado (evita ruido de acciones públicas/anónimas). */
    public void registrarSiAutenticado(String accion, String entidad, Long entidadId, String detalle) {
        if (hayUsuarioReal()) {
            registrar(accion, entidad, entidadId, detalle);
        }
    }
}
