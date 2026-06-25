package tics.uide.gestionuide.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import tics.uide.gestionuide.model.Usuario;
import tics.uide.gestionuide.service.FacturaService;
import tics.uide.gestionuide.service.UsuarioService;

/**
 * Helpers de autorización para @PreAuthorize.
 * Las authorities del token son nombres de Rol sin prefijo (ADMIN, CAJERO, MESERO, CLIENTE).
 */
@Component("seguridad")
public class SeguridadService {

    @Autowired private UsuarioService usuarioService;
    @Autowired private FacturaService facturaService;

    private boolean tieneAlgunRol(Authentication auth, String... roles) {
        if (auth == null) return false;
        for (String rol : roles) {
            for (var a : auth.getAuthorities()) {
                if (rol.equals(a.getAuthority())) return true;
            }
        }
        return false;
    }

    /**
     * Reutilizable: el usuario autenticado es STAFF (alguno de rolesStaff)
     * o es el DUEÑO del recurso, identificado por el id de usuario duenioId.
     */
    public boolean esDuenioOStaff(Long duenioId, Authentication auth, String... rolesStaff) {
        if (auth == null) return false;
        if (tieneAlgunRol(auth, rolesStaff)) return true;
        if (duenioId == null) return false;
        try {
            return usuarioService.buscarPorId(duenioId).getUsername().equals(auth.getName());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Lectura de factura por su id: ADMIN/CAJERO siempre; CLIENTE solo si es el dueño.
     * Resuelve el cliente de la factura y delega en esDuenioOStaff.
     */
    public boolean puedeLeerFactura(Long facturaId, Authentication auth) {
        if (tieneAlgunRol(auth, "ADMIN", "CAJERO")) return true;
        try {
            Usuario cliente = facturaService.buscarPorId(facturaId).getCliente();
            Long clienteId = cliente != null ? cliente.getId() : null;
            return esDuenioOStaff(clienteId, auth, "ADMIN", "CAJERO");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Autorización para crear pedido: ADMIN/MESERO pueden crear cualquiera;
     * CLIENTE solo el suyo. Un clienteId nulo se permite porque se resolverá
     * desde el token en el controller (ver resolverClienteId).
     */
    public boolean puedeCrearPedido(Long clienteId, Authentication auth) {
        if (auth == null) return false;
        if (tieneAlgunRol(auth, "ADMIN", "MESERO")) return true;
        if (clienteId == null) return true;
        try {
            return usuarioService.buscarPorId(clienteId).getUsername().equals(auth.getName());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Resuelve el clienteId efectivo: si viene nulo y el caller es CLIENTE,
     * usa el id del usuario del token. Para STAFF (ADMIN/MESERO) un nulo se
     * respeta (pedido sin cliente registrado).
     */
    public Long resolverClienteId(Long clienteId, Authentication auth) {
        if (clienteId != null) return clienteId;
        if (auth == null || tieneAlgunRol(auth, "ADMIN", "MESERO")) return null;
        try {
            return usuarioService.buscarPorUsername(auth.getName()).getId();
        } catch (Exception e) {
            return null;
        }
    }
}
