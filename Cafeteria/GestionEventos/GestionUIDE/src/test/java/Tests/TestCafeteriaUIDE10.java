package Tests;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import tics.uide.gestionuide.dto.*;
import tics.uide.gestionuide.enums.*;
import tics.uide.gestionuide.exception.BadRequestException;
import tics.uide.gestionuide.model.*;
import tics.uide.gestionuide.service.*;

import javax.validation.ConstraintViolationException;
import java.util.*;

@SpringBootTest(classes = tics.uide.gestionuide.GestionUIDE.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestCafeteriaUIDE10 {

    @Autowired private CategoryService  categoryService;
    @Autowired private ProductoService  productoService;
    @Autowired private UsuarioService   usuarioService;
    @Autowired private RolService       rolService;
    @Autowired private MesaService      mesaService;
    @Autowired private PedidoService    pedidoService;
    @Autowired private FacturaService   facturaService;
    @Autowired private PagoService      pagoService;

    private static final String SUF = UUID.randomUUID().toString().substring(0, 6);
    private static final String PWD = "Uide2024*";

    private static Long catDesayunosId, catAlmuerzoId, catSnacksId;
    private static Long catBebidasId, catPostreId, catEspecialidadId;
    private static final List<Long> productosIds = new ArrayList<>();
    private static Long adminId, mesero1Id, mesero2Id, cajeroId;
    private static Long cliente1Id, cliente2Id, cliente3Id;
    private static Long mesa1Id, mesa2Id, mesa3Id;
    private static Long pedido1Id, pedido2Id;

    private void agregarRolSeguro(Usuario usuario, Rol rol) {
        try { rolService.agregarRol(usuario, rol); }
        catch (BadRequestException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("ya")) return;
            throw e;
        }
    }

    private Usuario registrarUsuario(String username, String email, String nombre, String apellido, String telefono, Rol rol) {
        String emailUnico = email.replace("@", "_"+ "@");
        Usuario u = usuarioService.registrar(UsuarioRegistroDto.builder()
                .username(username).email(emailUnico).password(PWD).confirmPassword(PWD)
                .nombre(nombre).apellido(apellido).telefono(telefono).build());
        agregarRolSeguro(u, rol);
        System.out.printf("  + %-20s | %-8s | ID=%-4d | login: %s / %s%n", nombre+" "+apellido, rol, u.getId(), username, PWD);
        return u;
    }

    private void crearProducto(String nombre, String desc, double precio, int stock, String img, Long catId) {
        Producto p = productoService.crear(ProductoDto.builder().nombre(nombre).descripcion(desc).precio(precio)
                .stock(stock).disponible(true).imagenUrl(img).categoryId(catId).build());
        productosIds.add(p.getId());
        System.out.printf("  + [%02d] %-42s $%5.2f | stock=%d%n", productosIds.size(), nombre, precio, stock);
    }

    @Test @Order(1)
    public void test01_crearCategorias() {
        System.out.println("\n=== TEST 01: 6 CATEGORIAS DEL MENU ===");
        catDesayunosId = categoryService.crear(CategoryDto.builder().name("Desayunos").descripcion("Desayunos tipicos").activo(true).build()).getCategoryId();
        catAlmuerzoId = categoryService.crear(CategoryDto.builder().name("Almuerzos").descripcion("Almuerzos ejecutivos").activo(true).build()).getCategoryId();
        catSnacksId = categoryService.crear(CategoryDto.builder().name("Snacks").descripcion("Bocadillos rapidos").activo(true).build()).getCategoryId();
        catBebidasId = categoryService.crear(CategoryDto.builder().name("Bebidas").descripcion("Jugos y cafe").activo(true).build()).getCategoryId();
        catPostreId = categoryService.crear(CategoryDto.builder().name("Postres").descripcion("Postres artesanales").activo(true).build()).getCategoryId();
        catEspecialidadId = categoryService.crear(CategoryDto.builder().name("Especialidades").descripcion("Platos especiales").activo(true).build()).getCategoryId();
        Assertions.assertNotNull(catDesayunosId);
        Assertions.assertNotNull(catEspecialidadId);
        System.out.println("  OK: 6 categorias creadas");
    }

    @Test @Order(2)
    public void test02_crearCincuentaPlatos() {
        crearProducto("Bolon de verde con queso", "Platano verde frito con queso", 2.50, 50, "bolon_queso.jpg", catDesayunosId);
crearProducto("Bolon con chicharron", "Platano verde frito con chicharron", 2.75, 40, "bolon_chich.jpg", catDesayunosId);
crearProducto("Tigrillo serrano", "Platano verde con huevo y queso", 3.00, 35, "tigrillo.jpg", catDesayunosId);
crearProducto("Sanduche de pernil", "Pan con pernil de cerdo", 2.25, 60, "sanduche.jpg", catDesayunosId);
crearProducto("Tostadas con mermelada", "Tostadas con mantequilla y mora", 1.50, 80, "tostadas.jpg", catDesayunosId);
crearProducto("Humitas de choclo", "Tamal dulce de maiz fresco", 1.75, 45, "humitas.jpg", catDesayunosId);
crearProducto("Tamales con pollo", "Tamal de maiz con pollo", 2.00, 40, "tamales.jpg", catDesayunosId);
crearProducto("Empanadas de morocho", "Empanadas de maiz morocho", 1.50, 70, "empanadas.jpg", catDesayunosId);
crearProducto("Desayuno ejecutivo", "Jugo+cafe+huevos+pan+fruta", 4.50, 30, "desayuno.jpg", catDesayunosId);
crearProducto("Colada de avena", "Avena con canela y panela", 1.25, 100, "colada.jpg", catDesayunosId);

System.out.println("  -- ALMUERZOS (12) --");

crearProducto("Seco de pollo", "Pollo guisado con arroz", 3.50, 40, "seco_pollo.jpg", catAlmuerzoId);
crearProducto("Seco de res", "Res en salsa con arroz", 4.00, 35, "seco_res.jpg", catAlmuerzoId);
crearProducto("Guatita", "Mondongo en salsa de mani", 3.75, 25, "guatita.jpg", catAlmuerzoId);
crearProducto("Caldo de gallina", "Sopa de gallina con papa", 3.00, 30, "caldo.jpg", catAlmuerzoId);
crearProducto("Yahuarlocro", "Locro con sangre de borrego", 3.50, 20, "yahuar.jpg", catAlmuerzoId);
crearProducto("Locro de papa", "Sopa de papa con queso", 2.75, 45, "locro.jpg", catAlmuerzoId);
crearProducto("Arroz menestra carne", "Arroz menestra y carne", 3.50, 50, "menestra.jpg", catAlmuerzoId);
crearProducto("Fritada con mote", "Cerdo frito con mote", 4.50, 30, "fritada.jpg", catAlmuerzoId);
crearProducto("Hornado Riobamba", "Cerdo al horno con llapingachos", 5.00, 20, "hornado.jpg", catAlmuerzoId);
crearProducto("Encebollado", "Sopa de yuca con atun", 3.25, 40, "encebollado.jpg", catAlmuerzoId);
crearProducto("Churrasco", "Carne arroz huevo papas", 5.50, 25, "churrasco.jpg", catAlmuerzoId);
crearProducto("Almuerzo del dia", "Sopa+segundo+jugo", 3.00, 60, "almuerzo.jpg", catAlmuerzoId);

System.out.println("  -- SNACKS (8) --");

crearProducto("Empanadas de viento", "Empanadas fritas con queso", 1.25, 100, "emp_viento.jpg", catSnacksId);
crearProducto("Ceviche de camaron", "Camarones en limon", 4.50, 30, "ceviche.jpg", catSnacksId);
crearProducto("Patacones con hogao", "Platano verde frito", 2.00, 50, "patacones.jpg", catSnacksId);
crearProducto("Salchipapas", "Papas fritas con salchichas", 2.75, 60, "salchipapas.jpg", catSnacksId);
crearProducto("Chifles con guacamole", "Chifles con guacamole", 1.75, 80, "chifles.jpg", catSnacksId);
crearProducto("Tostado con quesillo", "Maiz tostado con quesillo", 1.50, 70, "tostado.jpg", catSnacksId);
crearProducto("Quimbolitos", "Tamalitos dulces de maiz", 1.00, 90, "quimbolitos.jpg", catSnacksId);

        crearProducto("Mote pillo ", "Mote con huevo y cebolla", 2.50, 40, "mote.jpg", catSnacksId);
        System.out.println("  -- BEBIDAS (10) --");
        crearProducto("Jugo de naranjilla ", "Jugo de naranjilla", 1.50, 120, "naranjilla.jpg", catBebidasId);
        crearProducto("Jugo tomate de arbol ", "Jugo de tomate de arbol", 1.50, 100, "tomate.jpg", catBebidasId);
        crearProducto("Batido de mora ", "Batido de mora con leche", 2.00, 80, "mora.jpg", catBebidasId);
        crearProducto("Batido de guanabana ", "Batido de guanabana", 2.00, 60, "guanabana.jpg", catBebidasId);
        crearProducto("Cafe pasado ", "Cafe de altura pasado", 1.25, 150, "cafe.jpg", catBebidasId);
        crearProducto("Chocolate caliente ", "Chocolate artesanal", 1.75, 90, "chocolate.jpg", catBebidasId);
        crearProducto("Chicha de jora ", "Bebida fermentada de maiz", 1.50, 50, "chicha.jpg", catBebidasId);
        crearProducto("Jugo de cana ", "Jugo de cana con limon", 1.00, 80, "cana.jpg", catBebidasId);
        crearProducto("Agua de Jamaica ", "Infusion de flor de Jamaica", 1.25, 70, "jamaica.jpg", catBebidasId);
        crearProducto("Limonada de coco ", "Limonada con leche de coco", 2.25, 60, "limonada.jpg", catBebidasId);
        System.out.println("  -- POSTRES (6) --");
        crearProducto("Arroz con leche ", "Arroz con canela y pasas", 1.75, 60, "arroz_leche.jpg", catPostreId);
        crearProducto("Espumilla de guayaba ", "Merengue de guayaba", 1.00, 80, "espumilla.jpg", catPostreId);
        crearProducto("Flan de coco ", "Flan casero de coco", 2.50, 40, "flan.jpg", catPostreId);
        crearProducto("Tres leches ", "Bizcocho en tres leches", 2.75, 35, "tresleches.jpg", catPostreId);
        crearProducto("Pristinos ", "Bunuelos con miel de panela", 1.50, 50, "pristinos.jpg", catPostreId);
        crearProducto("Helado de paila ", "Helado artesanal en paila", 2.00, 45, "helado.jpg", catPostreId);
        System.out.println("  -- ESPECIALIDADES (4) --");
        crearProducto("Cuy asado ", "Cuy al carbon con papas", 12.00, 10, "cuy.jpg", catEspecialidadId);
        crearProducto("Bandeja paisa ", "Arroz frejol chicharron chorizo", 6.50, 20, "bandeja.jpg", catEspecialidadId);
        crearProducto("Cazuela de mariscos ", "Cazuela de camarones y pescado", 8.00, 15, "cazuela.jpg", catEspecialidadId);
        crearProducto("Parrillada familiar ", "Carnes a la parrilla", 15.00, 8, "parrillada.jpg", catEspecialidadId);
        Assertions.assertEquals(50, productosIds.size());
        System.out.println("  OK: " + productosIds.size() + " platos creados");
    }

    @Test @Order(3)
    public void test03_crearUsuarios() {
        System.out.println("\n=== TEST 03: 7 USUARIOS CON ROLES ===");
        adminId = registrarUsuario("carlos.mendoza","carlos.mendoza@uide.edu.ec","Carlos","Mendoza","0998001122",Rol.ADMIN).getId();
        mesero1Id = registrarUsuario("ana.torres","ana.torres@uide.edu.ec","Ana","Torres","0991234567",Rol.MESERO).getId();
        mesero2Id = registrarUsuario("luis.pacheco","luis.pacheco@uide.edu.ec","Luis","Pacheco","0987654321",Rol.MESERO).getId();
        cajeroId = registrarUsuario("maria.suarez","maria.suarez@uide.edu.ec","Maria","Suarez","0993456789",Rol.CAJERO).getId();
        cliente1Id = registrarUsuario("juan.perez","juan.perez@gmail.com","Juan","Perez","0996677889",Rol.CLIENTE).getId();
        cliente2Id = registrarUsuario("sofia.diaz","sofia.diaz@gmail.com","Sofia","Diaz","0994455667",Rol.CLIENTE).getId();
        cliente3Id = registrarUsuario("pedro.vega","pedro.vega@hotmail.com","Pedro","Vega","0992233445",Rol.CLIENTE).getId();
        Assertions.assertNotNull(adminId);
        Assertions.assertNotNull(cliente3Id);
        System.out.println("  OK: 7 usuarios creados");
    }

    @Test @Order(4)
    public void test04_crearMesas() {
        System.out.println("\n=== TEST 04: 5 MESAS ===");
        int base = 100 + Math.abs(SUF.hashCode() % 800);
        mesa1Id = mesaService.crear(MesaDto.builder().numeroMesa(base+1).capacidad(4).ubicacion("Interior norte").activo(true).build()).getId();
        mesa2Id = mesaService.crear(MesaDto.builder().numeroMesa(base+2).capacidad(6).ubicacion("Interior centro").activo(true).build()).getId();
        mesa3Id = mesaService.crear(MesaDto.builder().numeroMesa(base+3).capacidad(2).ubicacion("Terraza").activo(true).build()).getId();
        mesaService.crear(MesaDto.builder().numeroMesa(base+4).capacidad(8).ubicacion("Salon privado").activo(true).build());
        mesaService.crear(MesaDto.builder().numeroMesa(base+5).capacidad(4).ubicacion("Barra").activo(true).build());
        Assertions.assertNotNull(mesa1Id);
        System.out.println("  OK: 5 mesas creadas");
    }

    @Test @Order(5)
    public void test05_crearPedidos() {
        System.out.println("\n=== TEST 05: 2 PEDIDOS ===");
        Assertions.assertNotNull(mesa1Id);
        Assertions.assertTrue(productosIds.size() >= 50);

        // Pedido 1: Seco pollo[10] + Locro papa[15] + Jugo naranjilla x2[30] + Tres leches[43]
        List<ItemPedidoDto> items1 = new ArrayList<>();
        items1.add(ItemPedidoDto.builder().productoId(productosIds.get(10)).cantidad(1).notas("Sin picante").build());
        items1.add(ItemPedidoDto.builder().productoId(productosIds.get(15)).cantidad(1).notas("Extra queso").build());
        items1.add(ItemPedidoDto.builder().productoId(productosIds.get(30)).cantidad(2).notas("Sin azucar").build());
        items1.add(ItemPedidoDto.builder().productoId(productosIds.get(43)).cantidad(1).notas("Con crema").build());

        Pedido p1 = pedidoService.crear(CrearPedidoDto.builder()
                .mesaId(mesa1Id).clienteId(cliente1Id).meseroId(mesero1Id)
                .notas("Alergico al cilantro").items(items1).build());
        pedido1Id = p1.getId();
        // subtotal = 3.50+2.75+2*1.50+2.75 = 12.00, total = 12.00*1.15 = 13.80
        double sub1 = 3.50 + 2.75 + 2*1.50 + 2.75;
        double esp1 = Math.round(sub1 * 1.15 * 100.0) / 100.0;
        Assertions.assertEquals(esp1, p1.getTotal(), 0.01, "Total pedido 1");
        System.out.println("  + Pedido 1 | Total=$" + p1.getTotal());

        // Pedido 2: Desayuno ejecutivo[8] + Cafe pasado[34] + Espumilla x2[41]
        List<ItemPedidoDto> items2 = new ArrayList<>();
        items2.add(ItemPedidoDto.builder().productoId(productosIds.get(8)).cantidad(1).notas("Huevos fritos").build());
        items2.add(ItemPedidoDto.builder().productoId(productosIds.get(34)).cantidad(1).notas("Con leche").build());
        items2.add(ItemPedidoDto.builder().productoId(productosIds.get(41)).cantidad(2).notas("").build());

        Pedido p2 = pedidoService.crear(CrearPedidoDto.builder()
                .mesaId(mesa3Id).clienteId(cliente2Id).meseroId(mesero2Id)
                .notas("Terraza").items(items2).build());
        pedido2Id = p2.getId();
        // subtotal = 4.50+1.25+2*1.00 = 7.75, total = 7.75*1.15 = 8.9125
        double sub2 = 4.50 + 1.25 + 2*1.00;
        double esp2 = Math.round(sub2 * 1.15 * 100.0) / 100.0;
        Assertions.assertEquals(esp2, p2.getTotal(), 0.01, "Total pedido 2");
        System.out.println("  + Pedido 2 | Total=$" + p2.getTotal());
    }

    @Test @Order(6)
    public void test06_flujoEstadosPedido() {
        System.out.println("\n=== TEST 06: FLUJO DE ESTADOS ===");
        Assertions.assertNotNull(pedido1Id);
        pedidoService.cambiarEstado(pedido1Id, EstadoPedido.EN_PREPARACION);
        Pedido p = pedidoService.cambiarEstado(pedido1Id, EstadoPedido.SERVIDO);
        Assertions.assertEquals(EstadoPedido.SERVIDO, p.getEstado());
        System.out.println("  + Pedido " + pedido1Id + " -> SERVIDO");

        Assertions.assertNotNull(pedido2Id);
        pedidoService.agregarItem(pedido2Id, ItemPedidoDto.builder()
                .productoId(productosIds.get(30)).cantidad(1).notas("Extra").build());
        pedidoService.cambiarEstado(pedido2Id, EstadoPedido.EN_PREPARACION);
        pedidoService.cambiarEstado(pedido2Id, EstadoPedido.SERVIDO);
        System.out.println("  + Pedido " + pedido2Id + " -> SERVIDO (con item extra)");
    }

    @Test @Order(7)
    public void test07_facturaDesdePedido() {
        System.out.println("\n=== TEST 07: FACTURA DESDE PEDIDO ===");
        Assertions.assertNotNull(pedido1Id);
        Factura f = facturaService.crearDesdePedido(pedido1Id, cajeroId);
        Assertions.assertNotNull(f);
        Assertions.assertTrue(f.getTotal() > 0);
        System.out.println("  + Factura #" + f.getNumeroFactura() + " | Total=$" + f.getTotal());
    }

    @Test @Order(8)
    public void test08_facturaManual() {
        System.out.println("\n=== TEST 08: FACTURA MANUAL ===");
        // Fritada[17] + Bolon queso x2[0] + Chicha jora x2[36]
        List<ItemFacturaDto> items = new ArrayList<>();
        items.add(ItemFacturaDto.builder().productoId(productosIds.get(17)).cantidad(1.0).build());
        items.add(ItemFacturaDto.builder().productoId(productosIds.get(0)).cantidad(2.0).build());
        items.add(ItemFacturaDto.builder().productoId(productosIds.get(36)).cantidad(2.0).build());

        Factura f = facturaService.crearManual(CrearFacturaManualDto.builder()
                .clienteId(cliente3Id).cajeroId(cajeroId).empresaRuc(null).descuento(0.0).items(items).build());
        Assertions.assertNotNull(f);
        Assertions.assertTrue(f.getTotal() > 0);
        // subtotal=4.50+2*2.50+2*1.50=12.50, +15%IVA=14.375
        double sub = 4.50 + 2*2.50 + 2*1.50;
        double esp = Math.round(sub * 1.15 * 100.0) / 100.0;
        Assertions.assertEquals(esp, f.getTotal(), 0.01, "Total factura manual");
        System.out.println("  + Factura manual #" + f.getNumeroFactura() + " | Total=$" + f.getTotal());
    }

    @Test @Order(9)
    public void test09_gestionStock() {
        System.out.println("\n=== TEST 09: GESTION DE STOCK ===");
        Long pid = productosIds.get(0);
        Producto antes = productoService.buscarPorId(pid);
        int stockIni = antes.getStock();
        productoService.reducirStock(pid, 5);
        Assertions.assertEquals(stockIni - 5, productoService.buscarPorId(pid).getStock());
        productoService.actualizarStock(pid, 10);
        Assertions.assertEquals(stockIni + 5, productoService.buscarPorId(pid).getStock());
        Assertions.assertTrue(productoService.hayStock(pid, 3));
        Assertions.assertFalse(productoService.hayStock(pid, 9999));
        System.out.println("  OK: Stock verificado");
    }

    @Test @Order(10)
    public void test10_resumenFinal() {
        System.out.println("\n=== TEST 10: RESUMEN FINAL ===");
        Assertions.assertTrue(categoryService.listarTodas().size() >= 6);
        Assertions.assertTrue(productoService.listarTodos().size() >= 50);
        Assertions.assertTrue(pedidoService.listarTodos().size() >= 2);
        Assertions.assertTrue(mesaService.listarTodas().size() >= 5);
        Assertions.assertTrue(usuarioService.listarTodos().size() >= 7);
        System.out.println("  ADMIN   -> carlos.mendoza / " + PWD);
        System.out.println("  MESERO  -> ana.torres     / " + PWD);
        System.out.println("  MESERO  -> luis.pacheco   / " + PWD);
        System.out.println("  CAJERO  -> maria.suarez   / " + PWD);
        System.out.println("  CLIENTE -> juan.perez     / " + PWD);
        System.out.println("  CLIENTE -> sofia.diaz     / " + PWD);
        System.out.println("  CLIENTE -> pedro.vega     / " + PWD);
        System.out.println("\n  10 TESTS COMPLETADOS OK");
    }
}