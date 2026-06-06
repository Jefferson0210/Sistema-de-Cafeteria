package tics.uide.gestionuide.webservice;

import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tics.uide.gestionuide.dto.ApiResponse;
import tics.uide.gestionuide.dto.CategoryDto;
import tics.uide.gestionuide.model.Category;
import tics.uide.gestionuide.service.CategoryService;

/**
 * WebService de Categorías - Para React Native
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/categorias")
public class CategoryWS {

    @Autowired
    private CategoryService categoryService;

    /**
     * GET /api/categorias
     * Listar todas las categorías
     */
    @GetMapping
    public ResponseEntity<?> listarTodas() {
        List<Category> categorias = categoryService.listarTodas();
        return ResponseEntity.ok(new ApiResponse(true, "Categorías obtenidas", categorias));
    }

    /**
     * GET /api/categorias/activas
     * Listar categorías activas
     */
    @GetMapping("/activas")
    public ResponseEntity<?> listarActivas() {
        List<Category> categorias = categoryService.listarActivas();
        return ResponseEntity.ok(new ApiResponse(true, "Categorías activas", categorias));
    }

    /**
     * GET /api/categorias/{id}
     * Obtener categoría por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            Category category = categoryService.buscarPorId(id);
            return ResponseEntity.ok(new ApiResponse(true, "Categoría encontrada", category));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/categorias
     * Crear nueva categoría (ADMIN)
     */
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody CategoryDto categoryDto) {
        try {
            Category category = categoryService.crear(categoryDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Categoría creada", category));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * PUT /api/categorias/{id}
     * Actualizar categoría (ADMIN)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDto categoryDto) {
        try {
            Category category = categoryService.actualizar(id, categoryDto);
            return ResponseEntity.ok(new ApiResponse(true, "Categoría actualizada", category));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * DELETE /api/categorias/{id}
     * Eliminar categoría (ADMIN)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            categoryService.eliminar(id);
            return ResponseEntity.ok(new ApiResponse(true, "Categoría eliminada", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}
