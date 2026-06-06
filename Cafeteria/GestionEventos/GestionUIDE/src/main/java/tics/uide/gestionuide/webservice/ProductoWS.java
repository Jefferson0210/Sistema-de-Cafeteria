package tics.uide.gestionuide.webservice;

import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tics.uide.gestionuide.dto.ApiResponse;
import tics.uide.gestionuide.dto.ProductoDto;
import tics.uide.gestionuide.model.Producto;
import tics.uide.gestionuide.service.ProductoService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/productos")
public class ProductoWS {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public ResponseEntity<?> listarTodos() {
        return ResponseEntity.ok(new ApiResponse(true, "Productos obtenidos", productoService.listarTodos()));
    }

    @GetMapping("/disponibles")
    public ResponseEntity<?> listarDisponibles() {
        return ResponseEntity.ok(new ApiResponse(true, "Productos disponibles", productoService.listarDisponibles()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(new ApiResponse(true, "Producto encontrado", productoService.buscarPorId(id)));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/categoria/{categoryId}")
    public ResponseEntity<?> listarPorCategoria(@PathVariable Long categoryId) {
        return ResponseEntity.ok(new ApiResponse(true, "Productos de la categoría", productoService.buscarPorCategoria(categoryId)));
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(new ApiResponse(true, "Resultados de búsqueda", productoService.buscarPorNombreContiene(nombre)));
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody ProductoDto productoDto) {
        try {
            Producto producto = productoService.crear(productoDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Producto creado", producto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoDto productoDto) {
        try {
            Producto producto = productoService.actualizar(id, productoDto);
            return ResponseEntity.ok(new ApiResponse(true, "Producto actualizado", producto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            productoService.eliminar(id);
            return ResponseEntity.ok(new ApiResponse(true, "Producto eliminado", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}/stock/agregar")
    public ResponseEntity<?> agregarStock(@PathVariable Long id, @RequestParam Integer cantidad) {
        try {
            Producto producto = productoService.actualizarStock(id, cantidad);
            return ResponseEntity.ok(new ApiResponse(true, "Stock actualizado", producto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivar(@PathVariable Long id) {
        try {
            productoService.desactivar(id);
            return ResponseEntity.ok(new ApiResponse(true, "Producto desactivado", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // NUEVO: activar producto
    @PutMapping("/{id}/activar")
    public ResponseEntity<?> activar(@PathVariable Long id) {
        try {
            Producto producto = productoService.buscarPorId(id);
            producto.setDisponible(true);
            productoService.actualizarStock(id, 0); // fuerza save
            return ResponseEntity.ok(new ApiResponse(true, "Producto activado", producto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PostMapping("/{id}/imagen")
    public ResponseEntity<?> subirImagen(@PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo) {
        try {
            Producto p = productoService.actualizarImagen(id, archivo);
            return ResponseEntity.ok(new ApiResponse(true, "Imagen actualizada", p));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}
