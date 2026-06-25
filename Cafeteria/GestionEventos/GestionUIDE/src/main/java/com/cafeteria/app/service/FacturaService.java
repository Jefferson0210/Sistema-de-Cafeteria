package com.cafeteria.app.service;

import java.math.BigDecimal;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cafeteria.app.dto.CrearFacturaManualDto;
import com.cafeteria.app.dto.ItemFacturaDto;
import com.cafeteria.app.enums.EstadoFactura;
import com.cafeteria.app.enums.EstadoPedido;
import com.cafeteria.app.exception.BadRequestException;
import com.cafeteria.app.exception.NotFoundException;
import com.cafeteria.app.model.*;
import com.cafeteria.app.repository.FacturaRepository;
import com.cafeteria.app.util.Money;

@Service
@Transactional
public class FacturaService {

    @Autowired private FacturaRepository facturaRepository;
    @Autowired private PedidoService pedidoService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private EmpresaService empresaService;
    @Autowired private ProductoService productoService;
    @Autowired private DetalleFacturaService detalleFacturaService;
    @Autowired private IvaService ivaService;
    @Autowired private AuditService auditService;

    public Factura crearDesdePedido(Long pedidoId, Long cajeroId) {
        Pedido pedido = pedidoService.buscarPorId(pedidoId);
        if (pedido.getEstado() == EstadoPedido.PAGADO) throw new BadRequestException("Este pedido ya fue facturado");
        Usuario cajero = usuarioService.buscarPorId(cajeroId);
        BigDecimal subtotal = Money.nz(pedido.getSubtotal());
        BigDecimal total = Money.nz(pedido.getTotal());
        BigDecimal iva = Money.maxZero(Money.subtract(total, subtotal));

        Factura factura = Factura.builder()
                .numeroFactura(generarNumeroFactura()).pedido(pedido).cliente(pedido.getCliente()).cajero(cajero)
                .subtotal(subtotal).iva(iva).descuento(Money.zero()).total(total).estado(EstadoFactura.PENDIENTE).build();
        factura = facturaRepository.save(factura);
        auditService.registrar("FACTURA_CREADA", "Factura", factura.getId(),
                "desde pedido " + pedidoId + ", total=" + factura.getTotal());

        if (pedido.getDetalles() != null) {
            for (DetallePedido dp : pedido.getDetalles()) {
                detalleFacturaService.crear(factura, dp.getProducto(), dp.getCantidad().doubleValue());
            }
        }
        pedidoService.cambiarEstado(pedidoId, EstadoPedido.PAGADO);
        return factura;
    }

    public Factura crearManual(CrearFacturaManualDto dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) throw new BadRequestException("Debe enviar al menos 1 item");
        Usuario cajero = usuarioService.buscarPorId(dto.getCajeroId());
        Usuario cliente = dto.getClienteId() != null ? usuarioService.buscarPorId(dto.getClienteId()) : null;
        Empresa empresa = (dto.getEmpresaRuc() != null && !dto.getEmpresaRuc().trim().isEmpty()) ? empresaService.buscarPorRuc(dto.getEmpresaRuc()) : null;
        BigDecimal descuento = Money.nz(dto.getDescuento());

        BigDecimal subtotal = Money.zero();
        for (ItemFacturaDto item : dto.getItems()) {
            Producto p = productoService.buscarPorId(item.getProductoId());
            subtotal = Money.add(subtotal, Money.multiply(p.getPrecio(), BigDecimal.valueOf(item.getCantidad())));
        }
        BigDecimal porcentajeIva = empresa != null && empresa.getIva() != null
                ? empresa.getIva() : ivaService.porcentajePorDefecto();
        BigDecimal ivaVal = ivaService.calcular(subtotal, porcentajeIva);
        BigDecimal total = Money.maxZero(Money.subtract(Money.add(subtotal, ivaVal), descuento));

        Factura factura = Factura.builder()
                .numeroFactura(generarNumeroFactura()).cliente(cliente).cajero(cajero).empresa(empresa)
                .estado(EstadoFactura.PENDIENTE).descuento(descuento).subtotal(subtotal).iva(ivaVal).total(total).build();
        factura = facturaRepository.save(factura);
        auditService.registrar("FACTURA_CREADA", "Factura", factura.getId(),
                "manual, total=" + factura.getTotal());

        // Crear detalles Y reducir stock
        for (ItemFacturaDto item : dto.getItems()) {
            Producto p = productoService.buscarPorId(item.getProductoId());
            detalleFacturaService.crear(factura, p, item.getCantidad());
            productoService.reducirStock(p.getId(), item.getCantidad().intValue());
        }
        return factura;
    }

    public synchronized String generarNumeroFactura() {
        Long maxId = facturaRepository.count();
        String numero;
        do { maxId++; numero = String.format("FAC-%06d", maxId); } while (facturaRepository.findByNumeroFactura(numero).isPresent());
        return numero;
    }

    public Factura cambiarEstado(Long id, EstadoFactura nuevoEstado) {
        Factura f = buscarPorId(id); f.setEstado(nuevoEstado); f = facturaRepository.save(f);
        auditService.registrar("FACTURA_ESTADO", "Factura", id, "nuevoEstado=" + nuevoEstado);
        return f;
    }
    public Factura buscarPorId(Long id) { return facturaRepository.findById(id).orElseThrow(() -> new NotFoundException("Factura no encontrada con ID: " + id)); }
    public Factura buscarPorNumero(String n) { return facturaRepository.findByNumeroFactura(n).orElseThrow(() -> new NotFoundException("Factura no encontrada: " + n)); }
    public List<Factura> listarTodas() { return facturaRepository.findAll(); }
    public org.springframework.data.domain.Page<Factura> listarTodas(org.springframework.data.domain.Pageable pageable) { return facturaRepository.findAll(pageable); }
    public List<Factura> listarPorCliente(Long id) { return facturaRepository.findByCliente(usuarioService.buscarPorId(id)); }
    public List<Factura> listarPorCajero(Long id) { return facturaRepository.findByCajero(usuarioService.buscarPorId(id)); }
    public List<Factura> listarPorEstado(EstadoFactura e) { return facturaRepository.findByEstado(e); }
}
