package tics.uide.gestionuide.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tics.uide.gestionuide.dto.ApiResponse;
import tics.uide.gestionuide.dto.CrearReservaDto;
import tics.uide.gestionuide.enums.EstadoReserva;
import tics.uide.gestionuide.model.Reserva;
import tics.uide.gestionuide.service.ReservaService;

/**
 * WebService de Reservas - CORREGIDO: añadidos endpoints faltantes
 * Antes solo tenía crear, listarPorUsuario, confirmar
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/reservas")
public class ReservaWS {

    @Autowired
    private ReservaService reservaService;

    // POST /api/reservas?usuarioId=1
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody CrearReservaDto dto, @RequestParam Long usuarioId) {
        try {
            Reserva reserva = reservaService.crear(dto, usuarioId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Reserva creada", reserva));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // GET /api/reservas (NUEVO)
    @GetMapping
    public ResponseEntity<?> listarTodas() {
        return ResponseEntity.ok(new ApiResponse(true, "Reservas", reservaService.listarTodas()));
    }

    // GET /api/reservas/{id} (NUEVO)
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(new ApiResponse(true, "Reserva", reservaService.buscarPorId(id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // GET /api/reservas/usuario/{usuarioId}
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(new ApiResponse(true, "Reservas", reservaService.listarPorUsuario(usuarioId)));
    }

    // GET /api/reservas/mesa/{mesaId} (NUEVO)
    @GetMapping("/mesa/{mesaId}")
    public ResponseEntity<?> listarPorMesa(@PathVariable Long mesaId) {
        return ResponseEntity.ok(new ApiResponse(true, "Reservas de mesa", reservaService.listarPorMesa(mesaId)));
    }

    // GET /api/reservas/estado/{estado} (NUEVO)
    @GetMapping("/estado/{estado}")
    public ResponseEntity<?> listarPorEstado(@PathVariable EstadoReserva estado) {
        return ResponseEntity.ok(new ApiResponse(true, "Reservas " + estado, reservaService.listarPorEstado(estado)));
    }

    // PUT /api/reservas/{id}/confirmar
    @PutMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmar(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(new ApiResponse(true, "Reserva confirmada", reservaService.confirmar(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // PUT /api/reservas/{id}/cancelar (NUEVO)
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(new ApiResponse(true, "Reserva cancelada", reservaService.cancelar(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // PUT /api/reservas/{id}/completar (NUEVO)
    @PutMapping("/{id}/completar")
    public ResponseEntity<?> completar(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(new ApiResponse(true, "Reserva completada", reservaService.completar(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}
