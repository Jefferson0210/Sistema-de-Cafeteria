package tics.uide.gestionuide.service;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tics.uide.gestionuide.dto.CrearPedidoDto;
import tics.uide.gestionuide.dto.ItemPedidoDto;
import tics.uide.gestionuide.enums.EstadoMesa;
import tics.uide.gestionuide.enums.EstadoPedido;
import tics.uide.gestionuide.exception.BadRequestException;
import tics.uide.gestionuide.exception.NotFoundException;
import tics.uide.gestionuide.model.*;
import tics.uide.gestionuide.repository.PedidoRepository;

@Service
@Transactional
public class PedidoService {

    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private MesaService mesaService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private ProductoService productoService;
    @Autowired private DetallePedidoService detallePedidoService;

    public Pedido crear(CrearPedidoDto dto) {
        Usuario mesero = dto.getMeseroId() != null ? usuarioService.buscarPorId(dto.getMeseroId()) : null;
        Usuario cliente = dto.getClienteId() != null ? usuarioService.buscarPorId(dto.getClienteId()) : null;

        Mesa mesa = null;
        if (dto.getMesaId() != null) {
            mesa = mesaService.buscarPorId(dto.getMesaId());
            if (mesa.getEstado() != EstadoMesa.LIBRE && mesa.getEstado() != EstadoMesa.RESERVADA) {
                throw new BadRequestException("La mesa no está disponible");
            }
            mesaService.cambiarEstado(mesa.getId(), EstadoMesa.OCUPADA);
        }

        // Validar stock ANTES de crear pedido
        for (ItemPedidoDto item : dto.getItems()) {
            if (!productoService.hayStock(item.getProductoId(), item.getCantidad())) {
                Producto p = productoService.buscarPorId(item.getProductoId());
                throw new BadRequestException("Stock insuficiente para " + p.getNombre() + ". Disponible: " + p.getStock());
            }
        }

        Pedido pedido = Pedido.builder()
                .mesa(mesa).cliente(cliente).mesero(mesero)
                .estado(EstadoPedido.PENDIENTE).notas(dto.getNotas())
                .subtotal(0.0).iva(0.0).total(0.0).build();
        pedido = pedidoRepository.save(pedido);

        for (ItemPedidoDto item : dto.getItems()) {
            Producto producto = productoService.buscarPorId(item.getProductoId());
            detallePedidoService.crear(pedido, producto, item.getCantidad(), item.getNotas());
            productoService.reducirStock(producto.getId(), item.getCantidad());
        }

        recalcularTotales(pedido.getId());
        return pedido;
    }

    public Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = buscarPorId(id);

        // Validar transiciones permitidas
        validarTransicion(pedido.getEstado(), nuevoEstado);

        // Si se cancela, restaurar stock y liberar mesa
        if (nuevoEstado == EstadoPedido.CANCELADO) {
            restaurarStock(pedido);
            if (pedido.getMesa() != null) mesaService.cambiarEstado(pedido.getMesa().getId(), EstadoMesa.LIBRE);
        }

        // Si se paga, liberar mesa
        if (nuevoEstado == EstadoPedido.PAGADO && pedido.getMesa() != null) {
            mesaService.cambiarEstado(pedido.getMesa().getId(), EstadoMesa.LIBRE);
        }

        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    private void validarTransicion(EstadoPedido actual, EstadoPedido nuevo) {
        // CANCELADO se permite desde cualquier estado excepto PAGADO
        if (nuevo == EstadoPedido.CANCELADO && actual == EstadoPedido.PAGADO) {
            throw new BadRequestException("No se puede cancelar un pedido ya pagado");
        }
        if (nuevo == EstadoPedido.CANCELADO) return;

        // Transiciones normales
        boolean valida = false;
        switch (actual) {
            case PENDIENTE: valida = (nuevo == EstadoPedido.EN_PREPARACION); break;
            case EN_PREPARACION: valida = (nuevo == EstadoPedido.SERVIDO); break;
            case SERVIDO: valida = (nuevo == EstadoPedido.PAGADO); break;
            default: break;
        }
        if (!valida) {
            throw new BadRequestException("Transición no permitida: " + actual + " → " + nuevo);
        }
    }

    // Restaurar stock de todos los items del pedido
    private void restaurarStock(Pedido pedido) {
        List<DetallePedido> detalles = detallePedidoService.listarPorPedido(pedido.getId());
        for (DetallePedido d : detalles) {
            if (d.getProducto() != null && d.getCantidad() != null) {
                productoService.actualizarStock(d.getProducto().getId(), d.getCantidad());
            }
        }
    }

    public Pedido agregarItem(Long pedidoId, ItemPedidoDto item) {
        Pedido pedido = buscarPorId(pedidoId);
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) throw new BadRequestException("Solo se pueden agregar items a pedidos pendientes");
        Producto producto = productoService.buscarPorId(item.getProductoId());
        detallePedidoService.crear(pedido, producto, item.getCantidad(), item.getNotas());
        productoService.reducirStock(producto.getId(), item.getCantidad());
        recalcularTotales(pedidoId);
        return buscarPorId(pedidoId);
    }

    // CORREGIDO: restaurar stock al eliminar item
    public Pedido eliminarItem(Long pedidoId, Long detalleId) {
        Pedido pedido = buscarPorId(pedidoId);
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) throw new BadRequestException("Solo se pueden eliminar items de pedidos pendientes");
        DetallePedido detalle = detallePedidoService.buscarPorId(detalleId);
        if (detalle.getProducto() != null && detalle.getCantidad() != null) {
            productoService.actualizarStock(detalle.getProducto().getId(), detalle.getCantidad());
        }
        detallePedidoService.eliminar(detalleId);
        recalcularTotales(pedidoId);
        return buscarPorId(pedidoId);
    }

    public void recalcularTotales(Long pedidoId) {
        Pedido pedido = buscarPorId(pedidoId);
        List<DetallePedido> detalles = detallePedidoService.listarPorPedido(pedidoId);
        double subtotal = detalles.stream().mapToDouble(DetallePedido::getSubtotal).sum();
        pedido.setSubtotal(subtotal);
        pedido.setIva(subtotal * 0.15);
        pedido.setTotal(subtotal + pedido.getIva());
        pedidoRepository.save(pedido);
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id).orElseThrow(() -> new NotFoundException("Pedido no encontrado con ID: " + id));
    }

    public List<Pedido> listarTodos() { return pedidoRepository.findAll(); }
    public List<Pedido> listarPorMesa(Long id) { return pedidoRepository.findByMesa(mesaService.buscarPorId(id)); }
    public List<Pedido> listarPorCliente(Long id) { return pedidoRepository.findByCliente(usuarioService.buscarPorId(id)); }
    public List<Pedido> listarPorMesero(Long id) { return pedidoRepository.findByMesero(usuarioService.buscarPorId(id)); }
    public List<Pedido> listarPorEstado(EstadoPedido e) { return pedidoRepository.findByEstado(e); }
    public List<Pedido> listarPendientes() { return pedidoRepository.findByEstado(EstadoPedido.PENDIENTE); }
    public List<Pedido> listarEnPreparacion() { return pedidoRepository.findByEstado(EstadoPedido.EN_PREPARACION); }
}
