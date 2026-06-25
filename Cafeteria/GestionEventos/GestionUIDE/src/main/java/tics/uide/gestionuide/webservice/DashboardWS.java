package tics.uide.gestionuide.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tics.uide.gestionuide.dto.ApiResponse;
import tics.uide.gestionuide.service.DashboardService;
import java.util.Date;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardWS {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/resumen")
    public ResponseEntity<?> resumen() {
        return ResponseEntity.ok(new ApiResponse(true, "Resumen", dashboardService.obtenerResumen()));
    }

    @GetMapping("/ventas")
    public ResponseEntity<?> ventas(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date desde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date hasta) {
        if (desde != null && hasta != null) {
            return ResponseEntity.ok(new ApiResponse(true, "Ventas filtradas", dashboardService.obtenerVentasPorFecha(desde, hasta)));
        }
        return ResponseEntity.ok(new ApiResponse(true, "Ventas", dashboardService.obtenerVentas()));
    }

    @GetMapping("/top-productos")
    public ResponseEntity<?> topProductos(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(new ApiResponse(true, "Top productos", dashboardService.obtenerTopProductos(limit)));
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<?> stockBajo(@RequestParam(defaultValue = "5") int umbral) {
        return ResponseEntity.ok(new ApiResponse(true, "Stock bajo", dashboardService.obtenerStockBajo(umbral)));
    }

    @GetMapping("/ventas-por-mesero")
    public ResponseEntity<?> ventasPorMesero() {
        return ResponseEntity.ok(new ApiResponse(true, "Ventas por mesero", dashboardService.obtenerVentasPorMesero()));
    }
}
