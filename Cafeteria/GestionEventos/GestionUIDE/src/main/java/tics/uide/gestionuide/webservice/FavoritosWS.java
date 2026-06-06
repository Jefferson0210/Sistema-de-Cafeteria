package tics.uide.gestionuide.webservice;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tics.uide.gestionuide.dto.ApiResponse;
import tics.uide.gestionuide.service.FavoritoService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/favoritos")
public class FavoritosWS {

    @Autowired
    private FavoritoService favoritoService;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(new ApiResponse(true, "Favoritos", favoritoService.listarPorUsuario(usuarioId)));
    }

    // NUEVO: verificar si un producto es favorito
    @GetMapping("/usuario/{usuarioId}/existe/{productoId}")
    public ResponseEntity<?> existe(@PathVariable Long usuarioId, @PathVariable Long productoId) {
        boolean existe = favoritoService.esFavorito(usuarioId, productoId);
        return ResponseEntity.ok(new ApiResponse(true, existe ? "Es favorito" : "No es favorito",
                Map.of("esFavorito", existe)));
    }

    @PostMapping
    public ResponseEntity<?> agregar(@RequestParam Long usuarioId, @RequestParam Long productoId) {
        try {
            return ResponseEntity.ok(new ApiResponse(true, "Favorito agregado", favoritoService.agregar(usuarioId, productoId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // NUEVO: toggle favorito (agregar si no existe, quitar si existe)
    @PostMapping("/toggle")
    public ResponseEntity<?> toggle(@RequestParam Long usuarioId, @RequestParam Long productoId) {
        try {
            boolean era = favoritoService.esFavorito(usuarioId, productoId);
            if (era) {
                favoritoService.eliminarPorUsuarioYProducto(usuarioId, productoId);
                return ResponseEntity.ok(new ApiResponse(true, "Favorito eliminado", Map.of("esFavorito", false)));
            } else {
                favoritoService.agregar(usuarioId, productoId);
                return ResponseEntity.ok(new ApiResponse(true, "Favorito agregado", Map.of("esFavorito", true)));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> eliminar(@RequestParam Long usuarioId, @RequestParam Long productoId) {
        try {
            favoritoService.eliminarPorUsuarioYProducto(usuarioId, productoId);
            return ResponseEntity.ok(new ApiResponse(true, "Favorito eliminado", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}
