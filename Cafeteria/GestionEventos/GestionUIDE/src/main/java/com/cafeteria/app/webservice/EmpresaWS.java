package com.cafeteria.app.webservice;

import java.util.List;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cafeteria.app.dto.ApiResponse;
import com.cafeteria.app.dto.EmpresaDto;
import com.cafeteria.app.model.Empresa;
import com.cafeteria.app.service.EmpresaService;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaWS {

    @Autowired
    private EmpresaService empresaService;

    @GetMapping
    public ResponseEntity<?> listarTodas() {
        List<Empresa> empresas = empresaService.listarTodas();
        return ResponseEntity.ok(new ApiResponse(true, "Empresas", empresas));
    }

    @GetMapping("/{ruc}")
    public ResponseEntity<?> obtenerPorRuc(@PathVariable String ruc) {
        try {
            Empresa empresa = empresaService.buscarPorRuc(ruc);
            return ResponseEntity.ok(new ApiResponse(true, "Empresa", empresa));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody EmpresaDto dto) {
        try {
            Empresa empresa = empresaService.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Empresa creada", empresa));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{ruc}")
    public ResponseEntity<?> actualizar(@PathVariable String ruc, @Valid @RequestBody EmpresaDto dto) {
        try {
            Empresa empresa = empresaService.actualizar(ruc, dto);
            return ResponseEntity.ok(new ApiResponse(true, "Empresa actualizada", empresa));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{ruc}")
    public ResponseEntity<?> eliminar(@PathVariable String ruc) {
        try {
            empresaService.eliminar(ruc);
            return ResponseEntity.ok(new ApiResponse(true, "Empresa eliminada", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}
