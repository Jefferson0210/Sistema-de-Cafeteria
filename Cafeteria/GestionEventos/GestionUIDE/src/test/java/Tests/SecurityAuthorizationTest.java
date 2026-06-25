package Tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.cafeteria.app.dto.*;
import com.cafeteria.app.enums.EstadoPedido;
import com.cafeteria.app.enums.MetodoPago;
import com.cafeteria.app.enums.Rol;
import com.cafeteria.app.model.Factura;
import com.cafeteria.app.model.Pedido;
import com.cafeteria.app.model.Producto;
import com.cafeteria.app.model.Usuario;
import com.cafeteria.app.service.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de AUTORIZACIÓN a nivel HTTP, pasando por el JWTAuthorizationFilter real
 * (token emitido por /api/auth/login). Blindan los arreglos de seguridad:
 * control de acceso por rol, IDOR (self-or-staff) y anti auto-asignación de roles.
 */
@SpringBootTest(classes = com.cafeteria.app.CafeteriaApp.class)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// Este test hace muchos logins HTTP en el seed; desactivamos el rate limiting para no toparlo.
@org.springframework.test.context.TestPropertySource(properties = "app.ratelimit.enabled=false")
public class SecurityAuthorizationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UsuarioService usuarioService;
    @Autowired private RolService rolService;
    @Autowired private CategoryService categoryService;
    @Autowired private ProductoService productoService;
    @Autowired private MesaService mesaService;
    @Autowired private PedidoService pedidoService;
    @Autowired private FacturaService facturaService;

    private static final String SUF = UUID.randomUUID().toString().substring(0, 6);
    private static final String PWD = "Uide2024*";

    private Long cajeroId, meseroId, cliente1Id, cliente2Id;
    private String tokAdmin, tokCajero, tokCliente1, tokCliente2;
    private Long facturaCliente1Id;   // factura cuyo cliente es cliente1 (IDOR de recurso)

    @BeforeAll
    void seed() throws Exception {
        registrar("admin", Rol.ADMIN);
        cajeroId   = registrar("cajero", Rol.CAJERO);
        meseroId   = registrar("mesero", Rol.MESERO);
        cliente1Id = registrar("cli1", null);   // solo CLIENTE
        cliente2Id = registrar("cli2", null);

        tokAdmin    = login("admin"  + SUF);
        tokCajero   = login("cajero" + SUF);
        tokCliente1 = login("cli1"   + SUF);
        tokCliente2 = login("cli2"   + SUF);

        // Datos para el IDOR de recurso: producto + mesa + pedido de cliente1 + factura
        Long catId = categoryService.crear(CategoryDto.builder()
                .name("Cat" + SUF).descripcion("x").activo(true).build()).getCategoryId();
        Producto prod = productoService.crear(ProductoDto.builder()
                .nombre("Prod" + SUF).descripcion("x").precio(BigDecimal.valueOf(5.00))
                .stock(100).disponible(true).categoryId(catId).build());
        Long mesaId = mesaService.crear(MesaDto.builder()
                .numeroMesa(700 + Math.abs(SUF.hashCode() % 200)).capacidad(4).ubicacion("x").activo(true).build()).getId();
        Pedido pedido = pedidoService.crear(CrearPedidoDto.builder()
                .mesaId(mesaId).clienteId(cliente1Id).meseroId(meseroId)
                .items(List.of(ItemPedidoDto.builder().productoId(prod.getId()).cantidad(2).build()))
                .build());
        // Transición válida antes de facturar: PENDIENTE -> EN_PREPARACION -> SERVIDO -> (PAGADO al facturar)
        pedidoService.cambiarEstado(pedido.getId(), EstadoPedido.EN_PREPARACION);
        pedidoService.cambiarEstado(pedido.getId(), EstadoPedido.SERVIDO);
        Factura f = facturaService.crearDesdePedido(pedido.getId(), cajeroId);
        facturaCliente1Id = f.getId();
    }

    private Long registrar(String base, Rol rolExtra) {
        Usuario u = usuarioService.registrar(UsuarioRegistroDto.builder()
                .username(base + SUF).email(base + SUF + "@uide.edu.ec").password(PWD).confirmPassword(PWD)
                .nombre(base).apellido("Test").telefono("0990000000").build());
        // Con el doble opt-in, login exige email verificado; estos usuarios de prueba se marcan verificados.
        usuarioService.marcarEmailVerificado(u.getId());
        if (rolExtra != null) rolService.agregarRol(u, rolExtra);
        return u.getId();
    }

    private String login(String username) throws Exception {
        String body = objectMapper.writeValueAsString(
                LoginDto.builder().usernameOrEmail(username).password(PWD).build());
        MvcResult res = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn();
        return res.getResponse().getHeader("Authorization");   // "Bearer xxx"
    }

    // ===== Endpoints públicos (sin token) =====

    @Test
    void productos_publico_sinToken() throws Exception {
        mvc.perform(get("/api/productos")).andExpect(status().isOk());
    }

    @Test
    void categorias_publico_sinToken() throws Exception {
        mvc.perform(get("/api/categorias")).andExpect(status().isOk());
    }

    @Test
    void usuarios_sinToken_denegado() throws Exception {
        mvc.perform(get("/api/usuarios")).andExpect(status().is4xxClientError());
    }

    // ===== CLIENTE no puede acceder a endpoints de admin =====

    @Test
    void cliente_listarUsuarios_403() throws Exception {
        mvc.perform(get("/api/usuarios").header("Authorization", tokCliente1)).andExpect(status().isForbidden());
    }

    @Test
    void cliente_eliminarUsuario_403() throws Exception {
        mvc.perform(delete("/api/usuarios/" + cliente2Id).header("Authorization", tokCliente1)).andExpect(status().isForbidden());
    }

    @Test
    void cliente_auditoria_403() throws Exception {
        mvc.perform(get("/api/auditoria").header("Authorization", tokCliente1)).andExpect(status().isForbidden());
    }

    // ===== Pedido por QR (cuenta por mesa) =====

    @Test
    void cliente_pedirEnMesa_201() throws Exception {
        Long catId = categoryService.crear(CategoryDto.builder()
                .name("CatQ" + SUF).descripcion("x").activo(true).build()).getCategoryId();
        Producto prod = productoService.crear(ProductoDto.builder()
                .nombre("ProdQ" + SUF).descripcion("x").precio(BigDecimal.valueOf(3.00))
                .stock(50).disponible(true).categoryId(catId).build());
        Long mesaId = mesaService.crear(MesaDto.builder()
                .numeroMesa(9000 + Math.abs(("q" + SUF).hashCode() % 90000))
                .capacidad(4).ubicacion("x").activo(true).build()).getId();
        String body = "{\"items\":[{\"productoId\":" + prod.getId() + ",\"cantidad\":1}]}";
        mvc.perform(post("/api/pedidos/mesa/" + mesaId).header("Authorization", tokCliente1)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void cliente_agregarItemsAPedidoArbitrario_403() throws Exception {
        // /api/pedidos/{id}/items sigue ADMIN/MESERO -> CLIENTE 403 (regla de propiedad intacta)
        String body = "{\"productoId\":1,\"cantidad\":1}";
        mvc.perform(post("/api/pedidos/1/items").header("Authorization", tokCliente1)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    // ===== Anti auto-asignación de rol =====

    @Test
    void cliente_autoAsignarseAdmin_403() throws Exception {
        mvc.perform(put("/api/usuarios/" + cliente1Id + "/rol").param("rol", "ADMIN")
                .header("Authorization", tokCliente1)).andExpect(status().isForbidden());
    }

    @Test
    void admin_asignaRol_200() throws Exception {
        mvc.perform(put("/api/usuarios/" + cliente2Id + "/rol").param("rol", "MESERO")
                .header("Authorization", tokAdmin)).andExpect(status().isOk());
    }

    // ===== IDOR: usuarios =====

    @Test
    void cliente_verPropioUsuario_200() throws Exception {
        mvc.perform(get("/api/usuarios/" + cliente1Id).header("Authorization", tokCliente1)).andExpect(status().isOk());
    }

    @Test
    void cliente_verOtroUsuario_403() throws Exception {
        mvc.perform(get("/api/usuarios/" + cliente2Id).header("Authorization", tokCliente1)).andExpect(status().isForbidden());
    }

    // ===== IDOR: pedidos por cliente =====

    @Test
    void cliente_pedidosPropios_200() throws Exception {
        mvc.perform(get("/api/pedidos/cliente/" + cliente1Id).header("Authorization", tokCliente1)).andExpect(status().isOk());
    }

    @Test
    void cliente_pedidosAjenos_403() throws Exception {
        mvc.perform(get("/api/pedidos/cliente/" + cliente2Id).header("Authorization", tokCliente1)).andExpect(status().isForbidden());
    }

    // ===== Facturas / Pagos: staff vs cliente =====

    @Test
    void cajero_listarFacturas_200() throws Exception {
        mvc.perform(get("/api/facturas").header("Authorization", tokCajero)).andExpect(status().isOk());
    }

    @Test
    void cliente_listarFacturas_403() throws Exception {
        mvc.perform(get("/api/facturas").header("Authorization", tokCliente1)).andExpect(status().isForbidden());
    }

    @Test
    void cliente_registrarPago_403() throws Exception {
        String body = objectMapper.writeValueAsString(PagoDto.builder()
                .facturaId(facturaCliente1Id).metodoPago(MetodoPago.EFECTIVO).monto(BigDecimal.valueOf(1)).build());
        mvc.perform(post("/api/pagos").contentType(MediaType.APPLICATION_JSON).content(body)
                .header("Authorization", tokCliente1)).andExpect(status().isForbidden());
    }

    // ===== IDOR: facturas por cliente =====

    @Test
    void cliente_facturasPropias_200() throws Exception {
        mvc.perform(get("/api/facturas/cliente/" + cliente1Id).header("Authorization", tokCliente1)).andExpect(status().isOk());
    }

    @Test
    void cliente_facturasAjenas_403() throws Exception {
        mvc.perform(get("/api/facturas/cliente/" + cliente2Id).header("Authorization", tokCliente1)).andExpect(status().isForbidden());
    }

    // ===== IDOR de recurso: lectura de una factura concreta (puedeLeerFactura) =====

    @Test
    void cliente_leerFacturaPropia_200() throws Exception {
        mvc.perform(get("/api/facturas/" + facturaCliente1Id).header("Authorization", tokCliente1)).andExpect(status().isOk());
    }

    @Test
    void cliente_leerFacturaAjena_403() throws Exception {
        mvc.perform(get("/api/facturas/" + facturaCliente1Id).header("Authorization", tokCliente2)).andExpect(status().isForbidden());
    }
}
