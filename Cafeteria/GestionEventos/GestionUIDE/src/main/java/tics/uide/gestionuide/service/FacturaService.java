package tics.uide.gestionuide.service;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tics.uide.gestionuide.dto.CrearFacturaManualDto;
import tics.uide.gestionuide.dto.ItemFacturaDto;
import tics.uide.gestionuide.enums.EstadoFactura;
import tics.uide.gestionuide.enums.EstadoPedido;
import tics.uide.gestionuide.exception.BadRequestException;
import tics.uide.gestionuide.exception.NotFoundException;
import tics.uide.gestionuide.model.*;
import tics.uide.gestionuide.repository.FacturaRepository;

@Service
@Transactional
public class FacturaService {

    @Autowired private FacturaRepository facturaRepository;
    @Autowired private PedidoService pedidoService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private EmpresaService empresaService;
    @Autowired private ProductoService productoService;
    @Autowired private DetalleFacturaService detalleFacturaService;

    public Factura crearDesdePedido(Long pedidoId, Long cajeroId) {
        Pedido pedido = pedidoService.buscarPorId(pedidoId);
        if (pedido.getEstado() == EstadoPedido.PAGADO) throw new BadRequestException("Este pedido ya fue facturado");
        Usuario cajero = usuarioService.buscarPorId(cajeroId);
        Double subtotal = pedido.getSubtotal() != null ? pedido.getSubtotal() : 0.0;
        Double total = pedido.getTotal() != null ? pedido.getTotal() : 0.0;
        Double iva = Math.max(0.0, total - subtotal);

        Factura factura = Factura.builder()
                .numeroFactura(generarNumeroFactura()).pedido(pedido).cliente(pedido.getCliente()).cajero(cajero)
                .subtotal(subtotal).iva(iva).descuento(0.0).total(total).estado(EstadoFactura.PENDIENTE).build();
        factura = facturaRepository.save(factura);

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
        Double descuento = dto.getDescuento() != null ? dto.getDescuento() : 0.0;

        double subtotal = 0.0;
        for (ItemFacturaDto item : dto.getItems()) {
            Producto p = productoService.buscarPorId(item.getProductoId());
            subtotal += p.getPrecio() * item.getCantidad();
        }
        double pctIva = (empresa != null && empresa.getIva() != null ? empresa.getIva() : 15.0) / 100.0;
        double ivaVal = subtotal * pctIva;
        double total = Math.max(0, subtotal + ivaVal - descuento);

        Factura factura = Factura.builder()
                .numeroFactura(generarNumeroFactura()).cliente(cliente).cajero(cajero).empresa(empresa)
                .estado(EstadoFactura.PENDIENTE).descuento(descuento).subtotal(subtotal).iva(ivaVal).total(total).build();
        factura = facturaRepository.save(factura);

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
        Factura f = buscarPorId(id); f.setEstado(nuevoEstado); return facturaRepository.save(f);
    }
    public Factura buscarPorId(Long id) { return facturaRepository.findById(id).orElseThrow(() -> new NotFoundException("Factura no encontrada con ID: " + id)); }
    public Factura buscarPorNumero(String n) { return facturaRepository.findByNumeroFactura(n).orElseThrow(() -> new NotFoundException("Factura no encontrada: " + n)); }
    public List<Factura> listarTodas() { return facturaRepository.findAll(); }
    public List<Factura> listarPorCliente(Long id) { return facturaRepository.findByCliente(usuarioService.buscarPorId(id)); }
    public List<Factura> listarPorCajero(Long id) { return facturaRepository.findByCajero(usuarioService.buscarPorId(id)); }
    public List<Factura> listarPorEstado(EstadoFactura e) { return facturaRepository.findByEstado(e); }
}
