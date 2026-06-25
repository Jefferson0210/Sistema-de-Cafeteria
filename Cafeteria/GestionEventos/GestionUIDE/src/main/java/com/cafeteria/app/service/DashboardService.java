package com.cafeteria.app.service;

import java.util.*;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cafeteria.app.enums.*;
import com.cafeteria.app.model.*;
import com.cafeteria.app.repository.*;
import com.cafeteria.app.util.Money;

@Service
@Transactional
public class DashboardService {

    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private FacturaRepository facturaRepository;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private MesaRepository mesaRepository;
    @Autowired private DetallePedidoRepository detallePedidoRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    public Map<String, Object> obtenerResumen() {
        Map<String, Object> r = new LinkedHashMap<>();
        Map<String, Long> pedidosPorEstado = new LinkedHashMap<>();
        for (EstadoPedido e : EstadoPedido.values()) pedidosPorEstado.put(e.name(), (long) pedidoRepository.findByEstado(e).size());
        r.put("pedidosPorEstado", pedidosPorEstado);
        Map<String, Long> facturasPorEstado = new LinkedHashMap<>();
        for (EstadoFactura e : EstadoFactura.values()) facturasPorEstado.put(e.name(), (long) facturaRepository.findByEstado(e).size());
        r.put("facturasPorEstado", facturasPorEstado);
        Map<String, Long> mesasPorEstado = new LinkedHashMap<>();
        for (EstadoMesa e : EstadoMesa.values()) mesasPorEstado.put(e.name(), (long) mesaRepository.findByEstado(e).size());
        r.put("mesasPorEstado", mesasPorEstado);
        r.put("totalProductos", productoRepository.count());
        r.put("productosDisponibles", (long) productoRepository.findByDisponibleTrue().size());
        r.put("totalPedidos", pedidoRepository.count());
        r.put("totalFacturas", facturaRepository.count());
        r.put("totalCategorias", categoryRepository.count());
        r.put("totalUsuarios", usuarioRepository.count());
        r.put("totalMesas", mesaRepository.count());
        return r;
    }

    public Map<String, Object> obtenerVentas() {
        return calcularVentas(facturaRepository.findByEstado(EstadoFactura.PAGADA), pagoRepository.findAll());
    }

    // NUEVO: ventas filtradas por rango de fecha
    public Map<String, Object> obtenerVentasPorFecha(Date desde, Date hasta) {
        // Ajustar hasta al final del día
        Calendar cal = Calendar.getInstance();
        cal.setTime(hasta);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date hastaFin = cal.getTime();

        List<Factura> pagadas = facturaRepository.findByEstado(EstadoFactura.PAGADA).stream()
                .filter(f -> f.getFechaEmision() != null && !f.getFechaEmision().before(desde) && !f.getFechaEmision().after(hastaFin))
                .collect(Collectors.toList());
        List<Pago> pagos = pagoRepository.findAll().stream()
                .filter(p -> p.getFechaPago() != null && !p.getFechaPago().before(desde) && !p.getFechaPago().after(hastaFin))
                .collect(Collectors.toList());
        Map<String, Object> v = calcularVentas(pagadas, pagos);
        v.put("desde", desde);
        v.put("hasta", hasta);
        return v;
    }

    private Map<String, Object> calcularVentas(List<Factura> facturasPagadas, List<Pago> pagos) {
        Map<String, Object> v = new LinkedHashMap<>();
        BigDecimal ingresoTotal = Money.sum(facturasPagadas.stream().map(Factura::getTotal).collect(Collectors.toList()));
        v.put("ingresoTotal", ingresoTotal);
        BigDecimal ivaTotal = Money.sum(facturasPagadas.stream().map(Factura::getIva).collect(Collectors.toList()));
        v.put("ivaTotal", ivaTotal);
        BigDecimal ticketPromedio = facturasPagadas.isEmpty() ? Money.zero()
                : ingresoTotal.divide(BigDecimal.valueOf(facturasPagadas.size()), Money.SCALE, Money.MODE);
        v.put("ticketPromedio", ticketPromedio);
        v.put("facturasPagadas", facturasPagadas.size());
        Map<String, BigDecimal> porMetodo = new LinkedHashMap<>();
        for (MetodoPago m : MetodoPago.values()) {
            BigDecimal total = Money.sum(pagos.stream().filter(p -> p.getMetodoPago() == m)
                    .map(Pago::getMonto).collect(Collectors.toList()));
            porMetodo.put(m.name(), total);
        }
        v.put("ingresosPorMetodo", porMetodo);
        List<Factura> pendientes = facturaRepository.findByEstado(EstadoFactura.PENDIENTE);
        v.put("facturasPendientes", pendientes.size());
        v.put("montoPorCobrar", Money.sum(pendientes.stream().map(Factura::getTotal).collect(Collectors.toList())));
        return v;
    }

    public List<Map<String, Object>> obtenerTopProductos(int limit) {
        List<DetallePedido> todos = detallePedidoRepository.findAll();
        Map<Long, int[]> stats = new LinkedHashMap<>();
        Map<Long, String> nombres = new LinkedHashMap<>();
        Map<Long, BigDecimal> ingresos = new LinkedHashMap<>();
        for (DetallePedido d : todos) {
            if (d.getProducto() != null) {
                Long pid = d.getProducto().getId();
                stats.computeIfAbsent(pid, k -> new int[]{0, 0});
                stats.get(pid)[0] += d.getCantidad() != null ? d.getCantidad() : 0;
                stats.get(pid)[1] += 1;
                nombres.put(pid, d.getProducto().getNombre());
                ingresos.merge(pid, Money.nz(d.getSubtotal()), BigDecimal::add);
            }
        }
        return stats.entrySet().stream().sorted((a, b) -> Integer.compare(b.getValue()[0], a.getValue()[0])).limit(limit)
                .map(e -> { Map<String, Object> m = new LinkedHashMap<>(); m.put("productoId", e.getKey()); m.put("nombre", nombres.get(e.getKey()));
                    m.put("cantidadVendida", e.getValue()[0]); m.put("vecesOrdenado", e.getValue()[1]);
                    m.put("ingresoGenerado", ingresos.getOrDefault(e.getKey(), Money.zero())); return m; }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> obtenerStockBajo(int umbral) {
        return productoRepository.findAll().stream().filter(p -> p.getStock() != null && p.getStock() <= umbral)
                .sorted(Comparator.comparingInt(Producto::getStock))
                .map(p -> { Map<String, Object> m = new LinkedHashMap<>(); m.put("id", p.getId()); m.put("nombre", p.getNombre());
                    m.put("stock", p.getStock()); m.put("disponible", p.getDisponible()); m.put("precio", p.getPrecio()); return m; }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> obtenerVentasPorMesero() {
        Map<Long, String> nombres = new LinkedHashMap<>(); Map<Long, int[]> stats = new LinkedHashMap<>(); Map<Long, BigDecimal> ingresos = new LinkedHashMap<>();
        for (Pedido p : pedidoRepository.findAll()) {
            if (p.getMesero() != null) { Long mid = p.getMesero().getId();
                nombres.put(mid, p.getMesero().getNombre() + " " + (p.getMesero().getApellido() != null ? p.getMesero().getApellido() : ""));
                stats.computeIfAbsent(mid, k -> new int[]{0, 0}); stats.get(mid)[0] += 1;
                ingresos.merge(mid, Money.nz(p.getTotal()), BigDecimal::add);
            }
        }
        return stats.entrySet().stream().sorted((a, b) -> ingresos.getOrDefault(b.getKey(), Money.zero()).compareTo(ingresos.getOrDefault(a.getKey(), Money.zero())))
                .map(e -> { Map<String, Object> m = new LinkedHashMap<>(); m.put("meseroId", e.getKey()); m.put("nombre", nombres.get(e.getKey()));
                    m.put("totalPedidos", e.getValue()[0]); m.put("ingresoGenerado", ingresos.getOrDefault(e.getKey(), Money.zero())); return m; }).collect(Collectors.toList());
    }
}
