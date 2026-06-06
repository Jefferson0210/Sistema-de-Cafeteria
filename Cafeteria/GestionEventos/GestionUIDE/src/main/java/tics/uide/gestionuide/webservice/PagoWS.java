package tics.uide.gestionuide.webservice;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tics.uide.gestionuide.dto.ApiResponse;
import tics.uide.gestionuide.dto.PagoDto;
import tics.uide.gestionuide.enums.MetodoPago;
import tics.uide.gestionuide.model.Pago;
import tics.uide.gestionuide.service.PagoService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/pagos")
public class PagoWS {

    @Autowired
    private PagoService pagoService;

    /**
     * POST /api/pagos
     * Registrar pago usando DTO (genérico)
     */
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody PagoDto dto) {
        try {
            Pago pago = pagoService.registrar(dto);
            return ResponseEntity.ok(new ApiResponse(true, "Pago registrado", pago));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * POST /api/pagos/efectivo?facturaId=1&monto=10
     */
    @PostMapping("/efectivo")
    public ResponseEntity<?> registrarEfectivo(
            @RequestParam Long facturaId,
            @RequestParam Double monto) {
        try {
            Pago pago = pagoService.registrarEfectivo(facturaId, monto);
            return ResponseEntity.ok(new ApiResponse(true, "Pago en efectivo registrado", pago));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * POST /api/pagos/tarjeta?facturaId=1&monto=10&referencia=ABC123
     */
    @PostMapping("/tarjeta")
    public ResponseEntity<?> registrarTarjeta(
            @RequestParam Long facturaId,
            @RequestParam Double monto,
            @RequestParam(required = false) String referencia) {
        try {
            Pago pago = pagoService.registrarTarjeta(facturaId, monto, referencia);
            return ResponseEntity.ok(new ApiResponse(true, "Pago con tarjeta registrado", pago));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * GET /api/pagos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            Pago pago = pagoService.buscarPorId(id);
            return ResponseEntity.ok(new ApiResponse(true, "Pago", pago));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * GET /api/pagos/factura/{facturaId}
     * Lista pagos de una factura
     */
    @GetMapping("/factura/{facturaId}")
    public ResponseEntity<?> listarPorFactura(@PathVariable Long facturaId) {
        try {
            List<Pago> pagos = pagoService.listarPorFactura(facturaId);
            return ResponseEntity.ok(new ApiResponse(true, "Pagos de la factura", pagos));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * GET /api/pagos/metodo/{metodo}
     * Ej: /api/pagos/metodo/EFECTIVO
     */
    @GetMapping("/metodo/{metodo}")
    public ResponseEntity<?> listarPorMetodo(@PathVariable MetodoPago metodo) {
        try {
            List<Pago> pagos = pagoService.listarPorMetodo(metodo);
            return ResponseEntity.ok(new ApiResponse(true, "Pagos por método", pagos));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * GET /api/pagos/factura/{facturaId}/total
     * Total pagado en una factura
     */
    @GetMapping("/factura/{facturaId}/total")
    public ResponseEntity<?> totalPagado(@PathVariable Long facturaId) {
        try {
            Double total = pagoService.calcularTotalPagado(facturaId);
            return ResponseEntity.ok(new ApiResponse(true, "Total pagado", total));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * GET /api/pagos/factura/{facturaId}/pendiente
     * Cuánto falta por pagar
     */
    @GetMapping("/factura/{facturaId}/pendiente")
    public ResponseEntity<?> pendiente(@PathVariable Long facturaId) {
        try {
            Double pendiente = pagoService.calcularPendiente(facturaId);
            return ResponseEntity.ok(new ApiResponse(true, "Pendiente", pendiente));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * GET /api/pagos/factura/{facturaId}/pagada
     * true/false si ya está pagada
     */
    @GetMapping("/factura/{facturaId}/pagada")
    public ResponseEntity<?> estaPagada(@PathVariable Long facturaId) {
        try {
            boolean pagada = pagoService.facturaEstaPagada(facturaId);
            return ResponseEntity.ok(new ApiResponse(true, "Estado de pago", pagada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}
