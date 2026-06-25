package com.cafeteria.app.webservice;

import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.cafeteria.app.dto.ApiResponse;
import com.cafeteria.app.dto.PageMeta;
import com.cafeteria.app.util.PageUtils;
import org.springframework.data.domain.Page;
import com.cafeteria.app.dto.CrearPedidoDto;
import com.cafeteria.app.dto.ItemPedidoDto;
import com.cafeteria.app.dto.PedidoMesaDto;
import com.cafeteria.app.enums.EstadoPedido;
import com.cafeteria.app.enums.ModoCuenta;
import com.cafeteria.app.model.Pedido;
import com.cafeteria.app.security.SeguridadService;
import com.cafeteria.app.service.PedidoService;
import com.cafeteria.app.service.CocinaNotificationService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * WebService de Pedidos - LIMPIADO
 * Eliminados System.out.println excesivos, validaciones null redundantes
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoWS {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private SeguridadService seguridad;

    @Autowired
    private CocinaNotificationService cocinaNotificationService;

    // GET /api/pedidos/stream — SSE para la pantalla de cocina.
    // Restringido a ADMIN/MESERO por la regla de seguridad de /api/pedidos/** y por @PreAuthorize.
    // El cliente debe mandar el header Authorization (cliente SSE basado en fetch).
    @GetMapping("/stream")
    @PreAuthorize("hasAnyAuthority('ADMIN','MESERO')")
    public SseEmitter stream() {
        return cocinaNotificationService.suscribir();
    }

    /** Reintenta una operación de stock ante choques de bloqueo optimista (concurrencia baja). */
    private <T> T conReintentoStock(java.util.function.Supplier<T> accion) {
        int intentos = 0;
        while (true) {
            try {
                return accion.get();
            } catch (OptimisticLockingFailureException e) {
                if (++intentos >= 3) throw e;   // 1 intento + 2 reintentos
            }
        }
    }

    // POST /api/pedidos
    @PostMapping
    @PreAuthorize("@seguridad.puedeCrearPedido(#dto.clienteId, authentication)")
    public ResponseEntity<?> crear(@Valid @RequestBody CrearPedidoDto dto, Authentication authentication) {
        try {
            // Si el CLIENTE no envía clienteId, se toma del token (no puede pedir a nombre de otro).
            dto.setClienteId(seguridad.resolverClienteId(dto.getClienteId(), authentication));
            Pedido pedido = conReintentoStock(() -> pedidoService.crear(dto));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Pedido creado", pedido));
        } catch (OptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, "El stock está muy demandado ahora mismo. Reintenta el pedido.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // POST /api/pedidos/mesa/{mesaId} — pedir desde el QR de la mesa (Etapa 1: modo SEPARADA).
    // clienteId se resuelve del token (nunca del body); el cliente solo direcciona una MESA, no un pedido.
    @PostMapping("/mesa/{mesaId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MESERO','CLIENTE')")
    public ResponseEntity<?> pedirEnMesa(@PathVariable Long mesaId,
            @Valid @RequestBody PedidoMesaDto dto,
            @RequestParam(required = false) ModoCuenta modo,
            Authentication authentication) {
        try {
            Long clienteId = seguridad.resolverClienteId(null, authentication);
            Pedido pedido = conReintentoStock(() -> pedidoService.pedirEnMesa(mesaId, dto.getItems(), clienteId, modo));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Pedido en mesa", pedido));
        } catch (OptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, "El stock está muy demandado ahora mismo. Reintenta.", null));
        }
        // BadRequest/NotFound propagan al GlobalExceptionHandler (400/404)
    }

    // GET /api/pedidos
    @GetMapping
    public ResponseEntity<?> listarTodos(
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {
        if (page == null) {
            return ResponseEntity.ok(new ApiResponse(true, "Pedidos", pedidoService.listarTodos()));
        }
        Page<Pedido> p = pedidoService.listarTodos(PageUtils.of(page, size, sort));
        return ResponseEntity.ok(new ApiResponse(true, "Pedidos", p.getContent(), new PageMeta(p)));
    }

    // GET /api/pedidos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            Pedido pedido = pedidoService.buscarPorId(id);
            return ResponseEntity.ok(new ApiResponse(true, "Pedido encontrado", pedido));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // GET /api/pedidos/mesa/{mesaId}
    @GetMapping("/mesa/{mesaId}")
    public ResponseEntity<?> listarPorMesa(@PathVariable Long mesaId) {
        return ResponseEntity.ok(new ApiResponse(true, "Pedidos de la mesa",
                pedidoService.listarPorMesa(mesaId)));
    }

    // GET /api/pedidos/cliente/{clienteId}
    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("@seguridad.esDuenioOStaff(#clienteId, authentication, 'ADMIN', 'MESERO')")
    public ResponseEntity<?> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(new ApiResponse(true, "Pedidos del cliente",
                pedidoService.listarPorCliente(clienteId)));
    }

    // GET /api/pedidos/mesero/{meseroId}
    @GetMapping("/mesero/{meseroId}")
    public ResponseEntity<?> listarPorMesero(@PathVariable Long meseroId) {
        return ResponseEntity.ok(new ApiResponse(true, "Pedidos del mesero",
                pedidoService.listarPorMesero(meseroId)));
    }

    // GET /api/pedidos/estado/{estado}
    @GetMapping("/estado/{estado}")
    public ResponseEntity<?> listarPorEstado(@PathVariable EstadoPedido estado) {
        return ResponseEntity.ok(new ApiResponse(true, "Pedidos " + estado,
                pedidoService.listarPorEstado(estado)));
    }

    // GET /api/pedidos/pendientes
    @GetMapping("/pendientes")
    public ResponseEntity<?> listarPendientes() {
        return ResponseEntity.ok(new ApiResponse(true, "Pedidos pendientes",
                pedidoService.listarPendientes()));
    }

    // GET /api/pedidos/en-preparacion
    @GetMapping("/en-preparacion")
    public ResponseEntity<?> listarEnPreparacion() {
        return ResponseEntity.ok(new ApiResponse(true, "Pedidos en preparación",
                pedidoService.listarEnPreparacion()));
    }

    // PUT /api/pedidos/{id}/estado?nuevoEstado=EN_PREPARACION
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id,
            @RequestParam EstadoPedido nuevoEstado) {
        try {
            Pedido pedido = pedidoService.cambiarEstado(id, nuevoEstado);
            return ResponseEntity.ok(new ApiResponse(true, "Estado actualizado", pedido));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // POST /api/pedidos/{id}/items
    @PostMapping("/{id}/items")
    public ResponseEntity<?> agregarItem(@PathVariable Long id,
            @Valid @RequestBody ItemPedidoDto itemDto) {
        try {
            Pedido pedido = conReintentoStock(() -> pedidoService.agregarItem(id, itemDto));
            return ResponseEntity.ok(new ApiResponse(true, "Item agregado", pedido));
        } catch (OptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, "El stock está muy demandado ahora mismo. Reintenta.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // DELETE /api/pedidos/{pedidoId}/items/{itemId}
    @DeleteMapping("/{pedidoId}/items/{itemId}")
    public ResponseEntity<?> eliminarItem(@PathVariable Long pedidoId, @PathVariable Long itemId) {
        try {
            Pedido pedido = pedidoService.eliminarItem(pedidoId, itemId);
            return ResponseEntity.ok(new ApiResponse(true, "Item eliminado", pedido));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}
