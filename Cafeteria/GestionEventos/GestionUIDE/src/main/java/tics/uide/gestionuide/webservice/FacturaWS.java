package tics.uide.gestionuide.webservice;

import java.io.File;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tics.uide.gestionuide.dto.ApiResponse;
import tics.uide.gestionuide.dto.PageMeta;
import tics.uide.gestionuide.util.PageUtils;
import org.springframework.data.domain.Page;
import tics.uide.gestionuide.dto.CrearFacturaManualDto;
import tics.uide.gestionuide.enums.EstadoFactura;
import tics.uide.gestionuide.model.Factura;
import tics.uide.gestionuide.service.*;

@RestController
@RequestMapping("/api/facturas")
public class FacturaWS {

    @Autowired private FacturaService facturaService;
    @Autowired private DetalleFacturaService detalleFacturaService;
    @Autowired private PagoService pagoService;
    @Autowired private FacturaPdfService facturaPdfService;
    @Autowired private EmailService emailService;

    @PostMapping("/desde-pedido/{pedidoId}")
    public ResponseEntity<?> crearDesdePedido(@PathVariable Long pedidoId, @RequestParam Long cajeroId) {
        try { return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Factura creada", facturaService.crearDesdePedido(pedidoId, cajeroId)));
        } catch (Exception e) { return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null)); }
    }

    @PostMapping("/manual")
    public ResponseEntity<?> crearManual(@Valid @RequestBody CrearFacturaManualDto dto) {
        try { return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Factura manual creada", facturaService.crearManual(dto)));
        } catch (Exception e) { return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null)); }
    }

    @GetMapping
    public ResponseEntity<?> listarTodas(
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {
        if (page == null) {
            return ResponseEntity.ok(new ApiResponse(true, "Facturas", facturaService.listarTodas()));
        }
        Page<Factura> p = facturaService.listarTodas(PageUtils.of(page, size, sort));
        return ResponseEntity.ok(new ApiResponse(true, "Facturas", p.getContent(), new PageMeta(p)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@seguridad.puedeLeerFactura(#id, authentication)")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try { return ResponseEntity.ok(new ApiResponse(true, "Factura", facturaService.buscarPorId(id)));
        } catch (Exception e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, e.getMessage(), null)); }
    }

    @GetMapping("/{id}/detalles")
    public ResponseEntity<?> obtenerDetalles(@PathVariable Long id) {
        try { return ResponseEntity.ok(new ApiResponse(true, "Detalles", detalleFacturaService.listarPorFactura(id)));
        } catch (Exception e) { return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null)); }
    }

    @GetMapping("/{id}/pagos")
    public ResponseEntity<?> obtenerPagos(@PathVariable Long id) {
        try { return ResponseEntity.ok(new ApiResponse(true, "Pagos", pagoService.listarPorFactura(id)));
        } catch (Exception e) { return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null)); }
    }

    @GetMapping("/numero/{numero}")
    public ResponseEntity<?> obtenerPorNumero(@PathVariable String numero) {
        try { return ResponseEntity.ok(new ApiResponse(true, "Factura", facturaService.buscarPorNumero(numero)));
        } catch (Exception e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, e.getMessage(), null)); }
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("@seguridad.esDuenioOStaff(#clienteId, authentication, 'ADMIN', 'CAJERO')")
    public ResponseEntity<?> listarPorCliente(@PathVariable Long clienteId) {
        try { return ResponseEntity.ok(new ApiResponse(true, "Facturas del cliente", facturaService.listarPorCliente(clienteId)));
        } catch (Exception e) { return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null)); }
    }

    @GetMapping("/cajero/{cajeroId}")
    public ResponseEntity<?> listarPorCajero(@PathVariable Long cajeroId) {
        try { return ResponseEntity.ok(new ApiResponse(true, "Facturas del cajero", facturaService.listarPorCajero(cajeroId)));
        } catch (Exception e) { return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null)); }
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<?> listarPorEstado(@PathVariable EstadoFactura estado) {
        return ResponseEntity.ok(new ApiResponse(true, "Facturas " + estado, facturaService.listarPorEstado(estado)));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam EstadoFactura nuevoEstado) {
        try { return ResponseEntity.ok(new ApiResponse(true, "Estado actualizado", facturaService.cambiarEstado(id, nuevoEstado)));
        } catch (Exception e) { return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null)); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> anular(@PathVariable Long id) {
        try { return ResponseEntity.ok(new ApiResponse(true, "Factura anulada", facturaService.cambiarEstado(id, EstadoFactura.CANCELADA)));
        } catch (Exception e) { return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null)); }
    }

    // ========== NUEVOS: PDF + EMAIL ==========

    // GET /api/facturas/{id}/pdf — descarga PDF
    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> descargarPdf(@PathVariable Long id) {
        try {
            byte[] pdf = facturaPdfService.generarPdf(id);
            Factura f = facturaService.buscarPorId(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename(f.getNumeroFactura() + ".pdf").build());
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Error al generar PDF: " + e.getMessage(), null));
        }
    }

    // POST /api/facturas/{id}/enviar-email?email=cliente@mail.com
    @PostMapping("/{id}/enviar-email")
    public ResponseEntity<?> enviarPorEmail(@PathVariable Long id, @RequestParam(required = false) String email) {
        try {
            Factura f = facturaService.buscarPorId(id);
            String destino = email;
            if (destino == null || destino.isBlank()) {
                if (f.getCliente() != null && f.getCliente().getEmail() != null) {
                    destino = f.getCliente().getEmail();
                } else {
                    return ResponseEntity.badRequest().body(new ApiResponse(false, "No hay email destino. Envía ?email=correo@mail.com", null));
                }
            }
            File pdfFile = facturaPdfService.generarPdfFile(id);
            String nombre = f.getCliente() != null ? f.getCliente().getNombre() : "Cliente";
            emailService.enviarFactura(destino, nombre, f.getNumeroFactura(), pdfFile);
            pdfFile.delete();
            return ResponseEntity.ok(new ApiResponse(true, "Factura enviada a " + destino, Map.of("email", destino)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Error al enviar: " + e.getMessage(), null));
        }
    }
}
