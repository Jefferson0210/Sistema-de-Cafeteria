package Tests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cafeteria.app.dto.*;
import com.cafeteria.app.enums.EstadoMesa;
import com.cafeteria.app.enums.EstadoPedido;
import com.cafeteria.app.enums.ModoCuenta;
import com.cafeteria.app.enums.Rol;
import com.cafeteria.app.exception.BadRequestException;
import com.cafeteria.app.model.Mesa;
import com.cafeteria.app.model.Pedido;
import com.cafeteria.app.model.Usuario;
import com.cafeteria.app.service.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cuenta por mesa (QR) — Etapa 1: modo SEPARADA + liberación consciente del modo.
 * Incluye la REGRESIÓN del pago normal (mesero) y la liberación parcial en SEPARADA.
 */
@SpringBootTest(classes = com.cafeteria.app.CafeteriaApp.class)
public class CuentaMesaTest {

    @Autowired private PedidoService pedidoService;
    @Autowired private FacturaService facturaService;
    @Autowired private MesaService mesaService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private RolService rolService;
    @Autowired private CategoryService categoryService;
    @Autowired private ProductoService productoService;
    @Autowired private DetallePedidoService detallePedidoService;

    private static final String PWD = "Uide2024*";

    private Usuario nuevoUsuario(String tag, Rol rolExtra) {
        String s = tag + UUID.randomUUID().toString().substring(0, 6);
        Usuario u = usuarioService.registrar(UsuarioRegistroDto.builder()
                .username(s).email(s + "@uide.edu.ec").password(PWD).confirmPassword(PWD)
                .nombre(tag).apellido("Test").telefono("0990000000").build());
        usuarioService.marcarEmailVerificado(u.getId());
        if (rolExtra != null) rolService.agregarRol(u, rolExtra);
        return u;
    }

    private Long nuevoProducto() {
        String tag = UUID.randomUUID().toString().substring(0, 6);
        Long catId = categoryService.crear(CategoryDto.builder()
                .name("Cat" + tag).descripcion("x").activo(true).build()).getCategoryId();
        return productoService.crear(ProductoDto.builder()
                .nombre("Prod" + tag).descripcion("x").precio(BigDecimal.valueOf(3.00))
                .stock(100).disponible(true).categoryId(catId).build()).getId();
    }

    private Long nuevaMesa(boolean activo) {
        return mesaService.crear(MesaDto.builder()
                .numeroMesa(9000 + Math.abs(UUID.randomUUID().hashCode() % 800000))
                .capacidad(4).ubicacion("x").activo(activo).build()).getId();
    }

    private ItemPedidoDto item(Long prodId, int cant) {
        return ItemPedidoDto.builder().productoId(prodId).cantidad(cant).build();
    }

    private void servir(Long pedidoId) {
        pedidoService.cambiarEstado(pedidoId, EstadoPedido.EN_PREPARACION);
        pedidoService.cambiarEstado(pedidoId, EstadoPedido.SERVIDO);
    }

    // ---- REGRESIÓN: el pago normal (mesero) sigue liberando la mesa igual que hoy ----
    @Test
    void regresion_pagoNormal_liberaMesa() {
        Usuario mesero = nuevoUsuario("mesero", Rol.MESERO);
        Usuario cajero = nuevoUsuario("cajero", Rol.CAJERO);
        Long prod = nuevoProducto();
        Long mesaId = nuevaMesa(true);

        Pedido p = pedidoService.crear(CrearPedidoDto.builder()
                .mesaId(mesaId).meseroId(mesero.getId())
                .items(List.of(item(prod, 1))).build());
        assertEquals(EstadoMesa.OCUPADA, mesaService.buscarPorId(mesaId).getEstado());

        servir(p.getId());
        facturaService.crearDesdePedido(p.getId(), cajero.getId());   // pago real (estilo mesero)

        Mesa mesa = mesaService.buscarPorId(mesaId);
        assertEquals(EstadoMesa.LIBRE, mesa.getEstado(), "el pago normal debe liberar la mesa");
        assertNull(mesa.getModoCuenta());
    }

    // ---- SEPARADA: dos clientes, dos pedidos, mesa OCUPADA en modo SEPARADA ----
    @Test
    void separada_dosClientes_dosPedidos() {
        Usuario a = nuevoUsuario("cliA", null);
        Usuario b = nuevoUsuario("cliB", null);
        Long prod = nuevoProducto();
        Long mesaId = nuevaMesa(true);

        Pedido pa = pedidoService.pedirEnMesa(mesaId, List.of(item(prod, 1)), a.getId(), null);
        Pedido pb = pedidoService.pedirEnMesa(mesaId, List.of(item(prod, 2)), b.getId(), null);

        assertNotEquals(pa.getId(), pb.getId(), "cada cliente tiene su propio pedido");
        Mesa mesa = mesaService.buscarPorId(mesaId);
        assertEquals(EstadoMesa.OCUPADA, mesa.getEstado());
        assertEquals(ModoCuenta.SEPARADA, mesa.getModoCuenta());
    }

    // ---- SEPARADA: liberación PARCIAL — pagar A deja OCUPADA; pagar B libera ----
    @Test
    void separada_liberacionParcial() {
        Usuario a = nuevoUsuario("cliA", null);
        Usuario b = nuevoUsuario("cliB", null);
        Long prod = nuevoProducto();
        Long mesaId = nuevaMesa(true);

        Pedido pa = pedidoService.pedirEnMesa(mesaId, List.of(item(prod, 1)), a.getId(), null);
        Pedido pb = pedidoService.pedirEnMesa(mesaId, List.of(item(prod, 1)), b.getId(), null);

        // Pagar A
        servir(pa.getId());
        pedidoService.cambiarEstado(pa.getId(), EstadoPedido.PAGADO);
        assertEquals(EstadoMesa.OCUPADA, mesaService.buscarPorId(mesaId).getEstado(),
                "con B aún abierto, la mesa sigue ocupada");

        // Pagar B
        servir(pb.getId());
        pedidoService.cambiarEstado(pb.getId(), EstadoPedido.PAGADO);
        Mesa mesa = mesaService.buscarPorId(mesaId);
        assertEquals(EstadoMesa.LIBRE, mesa.getEstado(), "al cerrar el último, la mesa se libera");
        assertNull(mesa.getModoCuenta());
    }

    // ---- SEPARADA: el mismo cliente reescanea → añade a SU pedido (no crea otro) ----
    @Test
    void separada_mismoCliente_anadeASuPedido() {
        Usuario a = nuevoUsuario("cliA", null);
        Long prod = nuevoProducto();
        Long mesaId = nuevaMesa(true);

        Pedido p1 = pedidoService.pedirEnMesa(mesaId, List.of(item(prod, 1)), a.getId(), null);
        Pedido p2 = pedidoService.pedirEnMesa(mesaId, List.of(item(prod, 2)), a.getId(), null);

        assertEquals(p1.getId(), p2.getId(), "el mismo cliente suma a su propio pedido");
        assertEquals(2, detallePedidoService.listarPorPedido(p1.getId()).size(), "dos líneas en su pedido");
    }

    // ---- COMÚN: dos clientes distintos → UN solo pedido (tab compartido) con los items de ambos ----
    @Test
    void comun_dosClientes_unSoloPedido() {
        Usuario a = nuevoUsuario("cliA", null);
        Usuario b = nuevoUsuario("cliB", null);
        Long prod = nuevoProducto();
        Long mesaId = nuevaMesa(true);

        Pedido pa = pedidoService.pedirEnMesa(mesaId, List.of(item(prod, 1)), a.getId(), ModoCuenta.COMUN);
        Pedido pb = pedidoService.pedirEnMesa(mesaId, List.of(item(prod, 2)), b.getId(), null);

        assertEquals(pa.getId(), pb.getId(), "ambos comensales suman al MISMO pedido (tab compartido)");
        Mesa mesa = mesaService.buscarPorId(mesaId);
        assertEquals(ModoCuenta.COMUN, mesa.getModoCuenta());
        assertEquals(EstadoMesa.OCUPADA, mesa.getEstado());
        assertEquals(2, detallePedidoService.listarPorPedido(pa.getId()).size(), "el tab tiene los items de ambos");
    }

    // ---- mesa.activo: pedir en una mesa dada de baja → rechazado ----
    @Test
    void mesaInactiva_rechazada() {
        Usuario a = nuevoUsuario("cliA", null);
        Long prod = nuevoProducto();
        Long mesaId = nuevaMesa(false);   // inactiva

        assertThrows(BadRequestException.class,
                () -> pedidoService.pedirEnMesa(mesaId, List.of(item(prod, 1)), a.getId(), null));
    }
}
