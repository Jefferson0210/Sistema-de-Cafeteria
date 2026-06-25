package tics.uide.gestionuide.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import tics.uide.gestionuide.dto.CrearPedidoDto;
import tics.uide.gestionuide.dto.ItemPedidoDto;
import tics.uide.gestionuide.dto.PedidoEventoDto;
import tics.uide.gestionuide.dto.ItemEventoDto;
import tics.uide.gestionuide.event.PedidoEvento;
import tics.uide.gestionuide.enums.EstadoMesa;
import tics.uide.gestionuide.enums.EstadoPedido;
import tics.uide.gestionuide.enums.ModoCuenta;
import org.springframework.beans.factory.annotation.Value;
import java.util.concurrent.ConcurrentHashMap;
import tics.uide.gestionuide.exception.BadRequestException;
import tics.uide.gestionuide.exception.NotFoundException;
import tics.uide.gestionuide.model.*;
import tics.uide.gestionuide.repository.PedidoRepository;
import tics.uide.gestionuide.util.Money;

@Service
@Transactional
public class PedidoService {

    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private IvaService ivaService;
    @Autowired private MesaService mesaService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private ProductoService productoService;
    @Autowired private DetallePedidoService detallePedidoService;
    @Autowired private ApplicationEventPublisher eventPublisher;

    @Value("${app.mesa.modo-por-defecto:SEPARADA}")
    private ModoCuenta modoPorDefecto;

    /** Estados que cuentan como cerrados (no abiertos) de un pedido. */
    private static final List<EstadoPedido> ESTADOS_CERRADOS = List.of(EstadoPedido.PAGADO, EstadoPedido.CANCELADO);

    /** Locks por mesa para serializar "resolver-o-crear" (una instancia). */
    private final ConcurrentHashMap<Long, Object> locksMesa = new ConcurrentHashMap<>();

    public Pedido crear(CrearPedidoDto dto) {
        Usuario mesero = dto.getMeseroId() != null ? usuarioService.buscarPorId(dto.getMeseroId()) : null;
        Usuario cliente = dto.getClienteId() != null ? usuarioService.buscarPorId(dto.getClienteId()) : null;

        Mesa mesa = null;
        if (dto.getMesaId() != null) {
            mesa = mesaService.buscarPorId(dto.getMesaId());
            if (!Boolean.TRUE.equals(mesa.getActivo())) {
                throw new BadRequestException("Mesa no disponible");
            }
            if (mesa.getEstado() != EstadoMesa.LIBRE && mesa.getEstado() != EstadoMesa.RESERVADA) {
                throw new BadRequestException("La mesa no está disponible");
            }
            mesaService.cambiarEstado(mesa.getId(), EstadoMesa.OCUPADA);
        }
        return ensamblar(mesa, cliente, mesero, dto.getItems(), dto.getNotas());
    }

    /**
     * Núcleo de creación de pedido (chequeo stock + persistir + detalles + reducir stock + totales + evento).
     * NO toca el estado de la mesa; lo gestiona el llamador. Reusado por crear() y pedirEnMesa().
     */
    private Pedido ensamblar(Mesa mesa, Usuario cliente, Usuario mesero, List<ItemPedidoDto> items, String notas) {
        for (ItemPedidoDto item : items) {
            if (!productoService.hayStock(item.getProductoId(), item.getCantidad())) {
                Producto p = productoService.buscarPorId(item.getProductoId());
                throw new BadRequestException("Stock insuficiente para " + p.getNombre() + ". Disponible: " + p.getStock());
            }
        }

        Pedido pedido = Pedido.builder()
                .mesa(mesa).cliente(cliente).mesero(mesero)
                .estado(EstadoPedido.PENDIENTE).notas(notas)
                .subtotal(Money.zero()).iva(Money.zero()).total(Money.zero()).build();
        pedido = pedidoRepository.save(pedido);

        for (ItemPedidoDto item : items) {
            Producto producto = productoService.buscarPorId(item.getProductoId());
            detallePedidoService.crear(pedido, producto, item.getCantidad(), item.getNotas());
            productoService.reducirStock(producto.getId(), item.getCantidad());
        }

        recalcularTotales(pedido.getId());
        publicarEventoCocina(pedido.getId(), PedidoEventoDto.Tipo.NUEVO);
        return pedido;
    }

    /**
     * Pedido desde el QR de una mesa (Etapa 1: modo SEPARADA). El clienteId viene del token.
     * Primer escaneo abre la sesión (fija modo, mesa OCUPADA). En SEPARADA, cada cliente tiene su
     * propio pedido: se añade al suyo si existe, o se crea uno nuevo. Bajo lock por mesa.
     */
    public Pedido pedirEnMesa(Long mesaId, List<ItemPedidoDto> items, Long clienteId, ModoCuenta modoSolicitado) {
        Object lock = locksMesa.computeIfAbsent(mesaId, k -> new Object());
        synchronized (lock) {
            Mesa mesa = mesaService.buscarPorId(mesaId);
            if (!Boolean.TRUE.equals(mesa.getActivo())) {
                throw new BadRequestException("Mesa no disponible");
            }
            Usuario cliente = clienteId != null ? usuarioService.buscarPorId(clienteId) : null;
            List<Pedido> abiertos = pedidoRepository.findByMesa_IdAndEstadoNotIn(mesaId, ESTADOS_CERRADOS);

            if (abiertos.isEmpty()) {
                // Primer escaneo: abre sesión y fija el modo (COMUN o SEPARADA)
                ModoCuenta modo = modoSolicitado != null ? modoSolicitado : modoPorDefecto;
                if (mesa.getEstado() != EstadoMesa.LIBRE && mesa.getEstado() != EstadoMesa.RESERVADA) {
                    throw new BadRequestException("La mesa no está disponible");
                }
                mesaService.cambiarEstado(mesaId, EstadoMesa.OCUPADA);
                mesaService.fijarModo(mesaId, modo);
                return ensamblar(mesa, cliente, null, items, null);
            }

            // Sesión abierta: el modo ya está fijado
            if (mesa.getModoCuenta() == ModoCuenta.COMUN) {
                // TAB COMPARTIDO: hay un único pedido abierto -> todos suman a ESE (resuelto por mesaId, nunca por pedidoId)
                return anadirItems(abiertos.get(0).getId(), items);
            }
            // SEPARADA: añadir al pedido propio si existe; si no, crear el propio (mesa ya OCUPADA, sin re-verja)
            List<Pedido> mios = clienteId == null ? List.of()
                    : pedidoRepository.findByMesa_IdAndCliente_IdAndEstadoNotIn(mesaId, clienteId, ESTADOS_CERRADOS);
            if (!mios.isEmpty()) {
                return anadirItems(mios.get(0).getId(), items);
            }
            return ensamblar(mesa, cliente, null, items, null);
        }
    }

    public Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = buscarPorId(id);
        validarTransicion(pedido.getEstado(), nuevoEstado);

        if (nuevoEstado == EstadoPedido.CANCELADO) {
            restaurarStock(pedido);
        }

        pedido.setEstado(nuevoEstado);
        Pedido guardado = pedidoRepository.save(pedido);   // este pedido ya cuenta como cerrado

        // Liberación CONSCIENTE DEL MODO: liberar la mesa SOLO si ya no quedan pedidos abiertos.
        // Pedido único (mesero/COMÚN) -> al pagar/cancelar no quedan abiertos -> libera igual que antes.
        // SEPARADA -> la mesa se libera al cerrar el ÚLTIMO pedido abierto.
        if ((nuevoEstado == EstadoPedido.PAGADO || nuevoEstado == EstadoPedido.CANCELADO)
                && pedido.getMesa() != null) {
            liberarMesaSiSinPedidosAbiertos(pedido.getMesa().getId());
        }

        publicarEventoCocina(guardado.getId(), PedidoEventoDto.Tipo.CAMBIO_ESTADO);
        return guardado;
    }

    /** Libera la mesa (LIBRE + modo null) solo si no quedan pedidos abiertos en ella. */
    private void liberarMesaSiSinPedidosAbiertos(Long mesaId) {
        if (!pedidoRepository.existsByMesa_IdAndEstadoNotIn(mesaId, ESTADOS_CERRADOS)) {
            mesaService.liberar(mesaId);
        }
    }

    /**
     * Publica un evento de cocina (desacoplado vía ApplicationEventPublisher).
     * Se construye dentro de la transacción (mesa/mesero LAZY accesibles); un
     * @TransactionalEventListener(AFTER_COMMIT) lo empuja por SSE solo tras el commit.
     */
    private void publicarEventoCocina(Long pedidoId, PedidoEventoDto.Tipo tipo) {
        Pedido p = buscarPorId(pedidoId);
        // La comanda se materializa AQUÍ, dentro de la transacción, desde una consulta FRESCA de los detalles
        // (no la colección cacheada del Pedido, que puede estar stale tras insertar los DetallePedido aparte).
        // Detalles + producto (EAGER) se copian a datos planos -> el DTO viaja sin proxies -> sin
        // LazyInitializationException cuando el listener AFTER_COMMIT lo lee tras cerrarse la sesión.
        List<DetallePedido> detalles = detallePedidoService.listarPorPedido(pedidoId);
        List<ItemEventoDto> items = detalles.stream()
                .map(d -> ItemEventoDto.builder()
                        .productoNombre(d.getProducto() != null ? d.getProducto().getNombre() : null)
                        .cantidad(d.getCantidad())
                        .notas(d.getNotas())
                        .build())
                .collect(Collectors.toList());
        PedidoEventoDto dto = PedidoEventoDto.builder()
                .tipo(tipo)
                .pedidoId(p.getId())
                .estado(p.getEstado().name())
                .mesaNumero(p.getMesa() != null ? p.getMesa().getNumeroMesa() : null)
                .meseroNombre(p.getMesero() != null ? p.getMesero().getNombre() : null)
                .total(p.getTotal())
                .numItems(detalles.size())
                .fecha(p.getFechaPedido())
                .items(items)
                .build();
        eventPublisher.publishEvent(new PedidoEvento(dto));
    }

    private void validarTransicion(EstadoPedido actual, EstadoPedido nuevo) {
        if (nuevo == EstadoPedido.CANCELADO && actual == EstadoPedido.PAGADO) {
            throw new BadRequestException("No se puede cancelar un pedido ya pagado");
        }
        if (nuevo == EstadoPedido.CANCELADO) return;

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

    private void restaurarStock(Pedido pedido) {
        List<DetallePedido> detalles = detallePedidoService.listarPorPedido(pedido.getId());
        for (DetallePedido d : detalles) {
            if (d.getProducto() != null && d.getCantidad() != null) {
                productoService.actualizarStock(d.getProducto().getId(), d.getCantidad());
            }
        }
    }

    public Pedido agregarItem(Long pedidoId, ItemPedidoDto item) {
        agregarItemInterno(pedidoId, item);
        recalcularTotales(pedidoId);
        publicarEventoCocina(pedidoId, PedidoEventoDto.Tipo.ITEMS_AGREGADOS);
        return buscarPorId(pedidoId);
    }

    /** Añade varios items a un pedido y emite UN solo evento de cocina (usado por el tab de mesa). */
    private Pedido anadirItems(Long pedidoId, List<ItemPedidoDto> items) {
        for (ItemPedidoDto item : items) {
            agregarItemInterno(pedidoId, item);
        }
        recalcularTotales(pedidoId);
        publicarEventoCocina(pedidoId, PedidoEventoDto.Tipo.ITEMS_AGREGADOS);
        return buscarPorId(pedidoId);
    }

    /** Núcleo de añadir un item (guard + detalle + stock), SIN recalcular ni emitir evento. */
    private void agregarItemInterno(Long pedidoId, ItemPedidoDto item) {
        Pedido pedido = buscarPorId(pedidoId);
        if (pedido.getEstado() != EstadoPedido.PENDIENTE && pedido.getEstado() != EstadoPedido.EN_PREPARACION) {
            throw new BadRequestException("Solo se pueden modificar pedidos pendientes o en preparación");
        }
        Producto producto = productoService.buscarPorId(item.getProductoId());
        detallePedidoService.crear(pedido, producto, item.getCantidad(), item.getNotas());
        productoService.reducirStock(producto.getId(), item.getCantidad());
    }

    public Pedido eliminarItem(Long pedidoId, Long detalleId) {
        Pedido pedido = buscarPorId(pedidoId);
        if (pedido.getEstado() != EstadoPedido.PENDIENTE && pedido.getEstado() != EstadoPedido.EN_PREPARACION) {
            throw new BadRequestException("Solo se pueden modificar pedidos pendientes o en preparación");
        }
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
        BigDecimal subtotal = Money.sum(detalles.stream()
                .map(DetallePedido::getSubtotal).collect(Collectors.toList()));
        BigDecimal iva = ivaService.calcularPorDefecto(subtotal);
        pedido.setSubtotal(subtotal);
        pedido.setIva(iva);
        pedido.setTotal(Money.add(subtotal, iva));
        pedidoRepository.save(pedido);
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id).orElseThrow(() -> new NotFoundException("Pedido no encontrado con ID: " + id));
    }

    public List<Pedido> listarTodos() { return pedidoRepository.findAll(); }
    public org.springframework.data.domain.Page<Pedido> listarTodos(org.springframework.data.domain.Pageable pageable) { return pedidoRepository.findAll(pageable); }
    public List<Pedido> listarPorMesa(Long id) { return pedidoRepository.findByMesa(mesaService.buscarPorId(id)); }
    public List<Pedido> listarPorCliente(Long id) { return pedidoRepository.findByCliente(usuarioService.buscarPorId(id)); }
    public List<Pedido> listarPorMesero(Long id) { return pedidoRepository.findByMesero(usuarioService.buscarPorId(id)); }
    public List<Pedido> listarPorEstado(EstadoPedido e) { return pedidoRepository.findByEstado(e); }
    public List<Pedido> listarPendientes() { return pedidoRepository.findByEstado(EstadoPedido.PENDIENTE); }
    public List<Pedido> listarEnPreparacion() { return pedidoRepository.findByEstado(EstadoPedido.EN_PREPARACION); }
}