package Tests;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import tics.uide.gestionuide.dto.*;
import tics.uide.gestionuide.enums.Rol;
import tics.uide.gestionuide.enums.ModoCuenta;
import tics.uide.gestionuide.model.Pedido;
import tics.uide.gestionuide.model.Producto;
import tics.uide.gestionuide.model.Usuario;
import tics.uide.gestionuide.service.*;
import tics.uide.gestionuide.event.PedidoEvento;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cableado evento→registro del canal de cocina (SSE). No prueba el streaming HTTP de punta a punta,
 * sino la garantía que importa: el canal NUNCA puede tumbar la creación de un pedido, y los emitters
 * que fallan se purgan del registro.
 */
@SpringBootTest(classes = tics.uide.gestionuide.GestionUIDE.class)
@Import(CocinaNotificationTest.CaptureConfig.class)
public class CocinaNotificationTest {

    @Autowired private CocinaNotificationService cocina;
    @Autowired private PedidoService pedidoService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private RolService rolService;
    @Autowired private CategoryService categoryService;
    @Autowired private ProductoService productoService;
    @Autowired private MesaService mesaService;
    @Autowired private CapturaCocina capturaCocina;

    private static final String PWD = "Uide2024*";

    /** Captura el payload del evento DESPUÉS del commit (réplica fiel del escenario lazy). */
    @TestConfiguration
    static class CaptureConfig {
        @Bean
        CapturaCocina capturaCocina() { return new CapturaCocina(); }
    }

    static class CapturaCocina {
        volatile PedidoEventoDto ultimo;
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void on(PedidoEvento e) { this.ultimo = e.getPayload(); }
    }

    private PedidoEventoDto dummy() {
        return PedidoEventoDto.builder()
                .tipo(PedidoEventoDto.Tipo.NUEVO).pedidoId(1L).estado("PENDIENTE")
                .total(BigDecimal.ONE).numItems(1).build();
    }

    private Pedido crearPedido() {
        String tag = UUID.randomUUID().toString().substring(0, 6);
        Usuario mesero = usuarioService.registrar(UsuarioRegistroDto.builder()
                .username("mes" + tag).email("mes" + tag + "@uide.edu.ec").password(PWD).confirmPassword(PWD)
                .nombre("Mesero").apellido("Test").telefono("0990000000").build());
        rolService.agregarRol(mesero, Rol.MESERO);
        Long catId = categoryService.crear(CategoryDto.builder()
                .name("Cat" + tag).descripcion("x").activo(true).build()).getCategoryId();
        Producto prod = productoService.crear(ProductoDto.builder()
                .nombre("Prod" + tag).descripcion("x").precio(BigDecimal.valueOf(5.00))
                .stock(100).disponible(true).categoryId(catId).build());
        Long mesaId = mesaService.crear(MesaDto.builder()
                .numeroMesa(100000 + Math.abs(UUID.randomUUID().hashCode() % 800000))
                .capacidad(4).ubicacion("x").activo(true).build()).getId();
        return pedidoService.crear(CrearPedidoDto.builder()
                .mesaId(mesaId).meseroId(mesero.getId())
                .items(List.of(ItemPedidoDto.builder().productoId(prod.getId()).cantidad(2).build()))
                .build());
    }

    @Test
    void sinSuscriptores_crearNoLanza() {
        // Sin nadie escuchando, la creación debe completarse normalmente: el canal jamás la tumba.
        Pedido pedido = assertDoesNotThrow(this::crearPedido);
        assertNotNull(pedido.getId());
    }

    @Test
    void emitterQueFalla_seEliminaDelRegistro() throws IOException {
        int base = cocina.suscriptores();
        SseEmitter malo = Mockito.mock(SseEmitter.class);
        Mockito.doThrow(new IOException("conexión caída"))
                .when(malo).send(Mockito.any(SseEmitter.SseEventBuilder.class));
        cocina.registrar(malo);
        assertEquals(base + 1, cocina.suscriptores());

        cocina.emitir(dummy());   // al fallar el envío, se purga

        assertEquals(base, cocina.suscriptores(), "el emitter que falla debe quitarse del registro");
    }

    @Test
    void emitir_entregaEventoASuscriptor() throws IOException {
        SseEmitter ok = Mockito.mock(SseEmitter.class);
        cocina.registrar(ok);

        cocina.emitir(dummy());

        Mockito.verify(ok).send(Mockito.any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void crearPedido_notificaTrasCommit() throws IOException {
        SseEmitter ok = Mockito.mock(SseEmitter.class);
        cocina.registrar(ok);

        crearPedido();   // al volver, la transacción ya commiteó y el listener AFTER_COMMIT corrió

        Mockito.verify(ok, Mockito.atLeastOnce()).send(Mockito.any(SseEmitter.SseEventBuilder.class));
    }

    /** Crea un pedido con 2 items (distintos productos) con notas. */
    private Pedido crearPedidoConDosItems() {
        String tag = UUID.randomUUID().toString().substring(0, 6);
        Usuario mesero = usuarioService.registrar(UsuarioRegistroDto.builder()
                .username("mes" + tag).email("mes" + tag + "@uide.edu.ec").password(PWD).confirmPassword(PWD)
                .nombre("Mesero").apellido("Test").telefono("0990000000").build());
        rolService.agregarRol(mesero, Rol.MESERO);
        Long catId = categoryService.crear(CategoryDto.builder()
                .name("Cat" + tag).descripcion("x").activo(true).build()).getCategoryId();
        Producto cafe = productoService.crear(ProductoDto.builder()
                .nombre("Cafe" + tag).descripcion("x").precio(BigDecimal.valueOf(1.50))
                .stock(100).disponible(true).categoryId(catId).build());
        Producto tostada = productoService.crear(ProductoDto.builder()
                .nombre("Tostada" + tag).descripcion("x").precio(BigDecimal.valueOf(2.00))
                .stock(100).disponible(true).categoryId(catId).build());
        Long mesaId = mesaService.crear(MesaDto.builder()
                .numeroMesa(100000 + Math.abs(UUID.randomUUID().hashCode() % 800000))
                .capacidad(4).ubicacion("x").activo(true).build()).getId();
        return pedidoService.crear(CrearPedidoDto.builder()
                .mesaId(mesaId).meseroId(mesero.getId())
                .items(List.of(
                        ItemPedidoDto.builder().productoId(cafe.getId()).cantidad(2).notas("sin azucar").build(),
                        ItemPedidoDto.builder().productoId(tostada.getId()).cantidad(1).notas("bien tostada").build()))
                .build());
    }

    @Test
    void comun_anadirRonda_emiteItemsAgregados_sinLazyException() {
        String tag = UUID.randomUUID().toString().substring(0, 6);
        Usuario a = usuarioService.registrar(UsuarioRegistroDto.builder()
                .username("a" + tag).email("a" + tag + "@uide.edu.ec").password(PWD).confirmPassword(PWD)
                .nombre("Ana").apellido("Test").telefono("0990000000").build());
        Usuario b = usuarioService.registrar(UsuarioRegistroDto.builder()
                .username("b" + tag).email("b" + tag + "@uide.edu.ec").password(PWD).confirmPassword(PWD)
                .nombre("Beto").apellido("Test").telefono("0990000000").build());
        Long catId = categoryService.crear(CategoryDto.builder()
                .name("Cat" + tag).descripcion("x").activo(true).build()).getCategoryId();
        Producto cafe = productoService.crear(ProductoDto.builder()
                .nombre("Cafe" + tag).descripcion("x").precio(BigDecimal.valueOf(1.50))
                .stock(100).disponible(true).categoryId(catId).build());
        Long mesaId = mesaService.crear(MesaDto.builder()
                .numeroMesa(100000 + Math.abs(UUID.randomUUID().hashCode() % 800000))
                .capacidad(4).ubicacion("x").activo(true).build()).getId();

        // A abre el tab COMÚN; B añade la 2ª ronda al MISMO pedido
        pedidoService.pedirEnMesa(mesaId, List.of(
                ItemPedidoDto.builder().productoId(cafe.getId()).cantidad(1).build()), a.getId(), ModoCuenta.COMUN);
        pedidoService.pedirEnMesa(mesaId, List.of(
                ItemPedidoDto.builder().productoId(cafe.getId()).cantidad(3).notas("para llevar").build()), b.getId(), null);

        // Tras el commit de la 2ª ronda, el evento capturado es ITEMS_AGREGADOS con la comanda completa
        PedidoEventoDto evt = capturaCocina.ultimo;
        assertNotNull(evt, "el evento debe capturarse tras el commit");
        assertEquals(PedidoEventoDto.Tipo.ITEMS_AGREGADOS, evt.getTipo(), "añadir una ronda emite ITEMS_AGREGADOS");
        assertNotNull(evt.getItems());
        assertEquals(2, evt.getItems().size(), "el evento trae la comanda del tab (ronda 1 + ronda 2)");
        assertTrue(evt.getItems().stream().anyMatch(i ->
                        Integer.valueOf(3).equals(i.getCantidad()) && "para llevar".equals(i.getNotas())),
                "debe incluir el item de la 2ª ronda con su nota");
    }

    @Test
    void eventoTraeItems_trasCommit_sinLazyException() {
        Pedido pedido = crearPedidoConDosItems();   // tras volver, commit hecho y listeners AFTER_COMMIT corridos

        // El evento capturado DESPUÉS del commit trae la comanda completa, sin LazyInitializationException
        // (los items se materializaron dentro de la transacción, son datos planos sin proxies).
        PedidoEventoDto evt = capturaCocina.ultimo;
        assertNotNull(evt, "el evento debe haberse capturado tras el commit");
        assertEquals(pedido.getId(), evt.getPedidoId());
        assertNotNull(evt.getItems(), "el evento debe traer la lista de items");
        assertEquals(2, evt.getItems().size(), "el evento debe traer los 2 items del pedido");

        assertTrue(evt.getItems().stream().anyMatch(i ->
                        Integer.valueOf(2).equals(i.getCantidad()) && "sin azucar".equals(i.getNotas())
                                && i.getProductoNombre() != null && i.getProductoNombre().startsWith("Cafe")),
                "debe estar el item Cafe x2 con su nota");
        assertTrue(evt.getItems().stream().anyMatch(i ->
                        Integer.valueOf(1).equals(i.getCantidad()) && "bien tostada".equals(i.getNotas())
                                && i.getProductoNombre() != null && i.getProductoNombre().startsWith("Tostada")),
                "debe estar el item Tostada x1 con su nota");
    }
}
