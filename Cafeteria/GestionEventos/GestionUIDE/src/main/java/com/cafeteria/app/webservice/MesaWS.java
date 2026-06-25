package com.cafeteria.app.webservice;

import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cafeteria.app.dto.ApiResponse;
import com.cafeteria.app.dto.MesaDto;
import com.cafeteria.app.enums.EstadoMesa;
import com.cafeteria.app.model.Mesa;
import com.cafeteria.app.service.MesaService;

/**
 * WebService de Mesas - CORREGIDO: añadido CRUD completo
 * Antes solo tenía GET y cambiar estado
 */
@RestController
@RequestMapping("/api/mesas")
public class MesaWS {

    @Autowired
    private MesaService mesaService;

    @GetMapping
    public ResponseEntity<?> listarTodas() {
        return ResponseEntity.ok(new ApiResponse(true, "Mesas", mesaService.listarTodas()));
    }

    @GetMapping("/disponibles")
    public ResponseEntity<?> listarDisponibles() {
        return ResponseEntity.ok(new ApiResponse(true, "Mesas disponibles", mesaService.listarDisponibles()));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<?> listarPorEstado(@PathVariable EstadoMesa estado) {
        return ResponseEntity.ok(new ApiResponse(true, "Mesas " + estado, mesaService.listarPorEstado(estado)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(new ApiResponse(true, "Mesa", mesaService.buscarPorId(id)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // NUEVO: Crear mesa
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody MesaDto dto) {
        try {
            Mesa mesa = mesaService.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Mesa creada", mesa));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // NUEVO: Actualizar mesa
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody MesaDto dto) {
        try {
            Mesa mesa = mesaService.actualizar(id, dto);
            return ResponseEntity.ok(new ApiResponse(true, "Mesa actualizada", mesa));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam EstadoMesa estado) {
        try {
            return ResponseEntity.ok(new ApiResponse(true, "Estado actualizado", mesaService.cambiarEstado(id, estado)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // NUEVO: Eliminar mesa
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            mesaService.eliminar(id);
            return ResponseEntity.ok(new ApiResponse(true, "Mesa eliminada", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}
