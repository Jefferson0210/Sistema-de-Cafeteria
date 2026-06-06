package tics.uide.gestionuide.webservice;

import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tics.uide.gestionuide.dto.ApiResponse;
import tics.uide.gestionuide.dto.CrearPedidoDto;
import tics.uide.gestionuide.dto.ItemPedidoDto;
import tics.uide.gestionuide.enums.EstadoPedido;
import tics.uide.gestionuide.model.Pedido;
import tics.uide.gestionuide.service.PedidoService;

/**
 * WebService de Pedidos - LIMPIADO
 * Eliminados System.out.println excesivos, validaciones null redundantes
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/pedidos")
public class PedidoWS {

    @Autowired
    private PedidoService pedidoService;

    // POST /api/pedidos
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody CrearPedidoDto dto) {
        try {
            Pedido pedido = pedidoService.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Pedido creado", pedido));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // GET /api/pedidos
    @GetMapping
    public ResponseEntity<?> listarTodos() {
        return ResponseEntity.ok(new ApiResponse(true, "Pedidos", pedidoService.listarTodos()));
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
            Pedido pedido = pedidoService.agregarItem(id, itemDto);
            return ResponseEntity.ok(new ApiResponse(true, "Item agregado", pedido));
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
