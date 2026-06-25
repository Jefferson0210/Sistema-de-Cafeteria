package Tests;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import tics.uide.gestionuide.dto.*;
import tics.uide.gestionuide.enums.*;
import tics.uide.gestionuide.exception.BadRequestException;
import tics.uide.gestionuide.model.*;
import tics.uide.gestionuide.repository.UsuarioRepository;
import tics.uide.gestionuide.service.*;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 *  CAFETERÍA UIDE — TEST COMPLETO CON DATOS REALES
 *  50 platos ecuatorianos reales distribuidos en 6 categorías
 * ╠══════════════════════════════════════════════════════════════════╣
 *  USUARIOS PARA LOGIN (frontend / Postman):
 *
 *  ROL ADMIN   → username: carlos.mendoza   password: Uide2024*
 *  ROL MESERO  → username: ana.torres        password: Uide2024*
 *  ROL MESERO  → username: luis.pacheco      password: Uide2024*
 *  ROL CAJERO  → username: maria.suarez      password: Uide2024*
 *  ROL CLIENTE → username: juan.perez        password: Uide2024*
 *  ROL CLIENTE → username: sofia.diaz        password: Uide2024*
 *  ROL CLIENTE → username: pedro.vega        password: Uide2024*
 *
 *  Endpoint: POST /api/auth/login
 *  Body: { "username": "carlos.mendoza", "password": "Uide2024*" }
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@SpringBootTest(classes = tics.uide.gestionuide.GestionUIDE.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestCompletoCafeteriaFull1 {

    @Autowired private CategoryService  categoryService;
    @Autowired private ProductoService  productoService;
    @Autowired private UsuarioService   usuarioService;
    @Autowired private RolService       rolService;
    @Autowired private MesaService      mesaService;
    @Autowired private PedidoService    pedidoService;
    @Autowired private FacturaService   facturaService;
    @Autowired private UsuarioRepository usuarioRepository;

    // Sufijo único para evitar colisiones en BD si se ejecuta varias veces
    private static final String SUF = UUID.randomUUID().toString().substring(0, 6);

    // Contraseña fija para todos los usuarios de prueba
    private static final String PASSWORD = "Uide2024*";

    // IDs de categorías
    private static Long catDesayunosId;
    private static Long catAlmuerzoId;
    private static Long catSnacksId;
    private static Long catBebidasId;
    private static Long catPostreId;
    private static Long catEspecialidadId;

    // IDs de todos los productos (50 platos)
    private static final List<Long> productosIds = new ArrayList<>();

    // IDs de usuarios
    private static Long adminId;
    private static Long mesero1Id;
    private static Long mesero2Id;
    private static Long cajeroId;
    private static Long cliente1Id;
    private static Long cliente2Id;
    private static Long cliente3Id;

    // IDs de mesas
    private static Long mesa1Id;
    private static Long mesa2Id;
    private static Long mesa3Id;

    // IDs de pedidos
    private static Long pedido1Id;
    private static Long pedido2Id;

    // ─── Helper: agregar rol sin romper si ya existe ─────────────────────────
    private void agregarRolSeguro(Usuario usuario, Rol rol) {
        try {
            rolService.agregarRol(usuario, rol);
        } catch (BadRequestException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("ya")) return;
            throw e;
        }
    }

    // ─── Helper: buscar usuario existente sin importar si el repositorio devuelve Usuario u Optional<Usuario> ─────
    @SuppressWarnings("unchecked")
    private Usuario buscarUsuarioExistente(String username) {
        try {
            Object resultado = usuarioRepository.getClass()
                    .getMethod("findByUsername", String.class)
                    .invoke(usuarioRepository, username);

            if (resultado instanceof Optional) {
                return ((Optional<Usuario>) resultado).orElse(null);
            }

            if (resultado instanceof Usuario) {
                return (Usuario) resultado;
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // ─── Helper: registrar usuario con credenciales fijas ────────────────────
    private Usuario registrarUsuario(String username, String email,
                                     String nombre, String apellido,
                                     String telefono, Rol rol) {
        try {
            // Email con sufijo para evitar duplicados en BD entre ejecuciones
            String emailUnico = email.replace("@", "_" + SUF + "@");
            UsuarioRegistroDto dto = UsuarioRegistroDto.builder()
                    .username(username)
                    .email(emailUnico)
                    .password(PASSWORD)
                    .confirmPassword(PASSWORD)
                    .nombre(nombre)
                    .apellido(apellido)
                    .telefono(telefono)
                    .build();
            Usuario u = usuarioService.registrar(dto);
            agregarRolSeguro(u, rol);
            System.out.printf("  ✓ %-20s | %-8s | ID=%-4d | login: %s / %s%n",
                    nombre + " " + apellido, rol, u.getId(), username, PASSWORD);
            return u;
        } catch (Exception e) {
            System.out.println("  ⚠ Usuario ya existe o falló (" + username + "): " + e.getMessage());
            System.out.println("  → Recuperando usuario existente desde la base...");

            Usuario existente = buscarUsuarioExistente(username);

            if (existente != null) {
                agregarRolSeguro(existente, rol);
                System.out.printf("  ✓ Recuperado %-20s | %-8s | ID=%-4d | login: %s / %s%n",
                        existente.getNombre() + " " + existente.getApellido(),
                        rol,
                        existente.getId(),
                        username,
                        PASSWORD);
                return existente;
            }

            throw new RuntimeException("No se pudo crear ni recuperar el usuario: " + username, e);
        }
    }

    // ─── Helper: crear producto y registrar su ID ────────────────────────────
    private void crearProducto(String nombre, String descripcion, double precio,
                                int stock, String imagen, Long categoriaId) {
        ProductoDto dto = ProductoDto.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .precio(java.math.BigDecimal.valueOf(precio))
                .stock(stock)
                .disponible(true)
                .imagenUrl(imagen)
                .categoryId(categoriaId)
                .build();
        Producto p = productoService.crear(dto);
        Assertions.assertNotNull(p, "El producto '" + nombre + "' no debe ser null");
        productosIds.add(p.getId());
        System.out.printf("  ✓ [%02d] %-42s $%5.2f | stock=%d%n",
                productosIds.size(), nombre, precio, stock);
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  TEST 00 — Usuarios reales con roles
    // ════════════════════════════════════════════════════════════════════════════
    @Test @Order(0)
    public void test00_crearUsuariosReales() {
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("  TEST 00: USUARIOS REALES DE LA CAFETERÍA UIDE");
        System.out.println("╚══════════════════════════════════════════════╝");

        Usuario admin = registrarUsuario(
                "carlos.mendoza", "carlos.mendoza@uide.edu.ec",
                "Carlos", "Mendoza", "0998001122", Rol.ADMIN);
        if (admin != null) adminId = admin.getId();

        Usuario mesero1 = registrarUsuario(
                "ana.torres", "ana.torres@uide.edu.ec",
                "Ana", "Torres", "0991234567", Rol.MESERO);
        if (mesero1 != null) mesero1Id = mesero1.getId();

        Usuario mesero2 = registrarUsuario(
                "luis.pacheco", "luis.pacheco@uide.edu.ec",
                "Luis", "Pacheco", "0987654321", Rol.MESERO);
        if (mesero2 != null) mesero2Id = mesero2.getId();

        Usuario cajero = registrarUsuario(
                "maria.suarez", "maria.suarez@uide.edu.ec",
                "María", "Suárez", "0993456789", Rol.CAJERO);
        if (cajero != null) cajeroId = cajero.getId();

        Usuario cliente1 = registrarUsuario(
                "juan.perez", "juan.perez@gmail.com",
                "Juan", "Pérez", "0996677889", Rol.CLIENTE);
        if (cliente1 != null) cliente1Id = cliente1.getId();

        Usuario cliente2 = registrarUsuario(
                "sofia.diaz", "sofia.diaz@gmail.com",
                "Sofía", "Díaz", "0994455667", Rol.CLIENTE);
        if (cliente2 != null) cliente2Id = cliente2.getId();

        Usuario cliente3 = registrarUsuario(
                "pedro.vega", "pedro.vega@hotmail.com",
                "Pedro", "Vega", "0992233445", Rol.CLIENTE);
        if (cliente3 != null) cliente3Id = cliente3.getId();

        System.out.println("\n  ┌──────────────────────────────────────────────┐");
        System.out.println("  │  CREDENCIALES PARA LOGIN EN EL FRONTEND      │");
        System.out.println("  ├──────────────────────────────────────────────┤");
        System.out.println("  │  ADMIN   → carlos.mendoza  / " + PASSWORD + "  │");
        System.out.println("  │  MESERO  → ana.torres      / " + PASSWORD + "  │");
        System.out.println("  │  MESERO  → luis.pacheco    / " + PASSWORD + "  │");
        System.out.println("  │  CAJERO  → maria.suarez    / " + PASSWORD + "  │");
        System.out.println("  │  CLIENTE → juan.perez      / " + PASSWORD + "  │");
        System.out.println("  │  CLIENTE → sofia.diaz      / " + PASSWORD + "  │");
        System.out.println("  │  CLIENTE → pedro.vega      / " + PASSWORD + "  │");
        System.out.println("  └──────────────────────────────────────────────┘");
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  TEST 01 — 6 Categorías del menú
    // ════════════════════════════════════════════════════════════════════════════
    @Test @Order(1)
    public void test01_crearCategorias() {
        System.out.println("\n╔══════════════════════════════╗");
        System.out.println("  TEST 01: CATEGORÍAS DEL MENÚ");
        System.out.println("╚══════════════════════════════╝");

        catDesayunosId = categoryService.crear(CategoryDto.builder()
                .name("Desayunos_" + SUF)
                .descripcion("Desayunos típicos ecuatorianos — 7h00 a 11h00")
                .activo(true).build()).getCategoryId();

        catAlmuerzoId = categoryService.crear(CategoryDto.builder()
                .name("Almuerzos_" + SUF)
                .descripcion("Almuerzos ejecutivos y platos fuertes — 12h00 a 15h00")
                .activo(true).build()).getCategoryId();

        catSnacksId = categoryService.crear(CategoryDto.builder()
                .name("Snacks_" + SUF)
                .descripcion("Bocadillos, empanadas y refrigerios rápidos")
                .activo(true).build()).getCategoryId();

        catBebidasId = categoryService.crear(CategoryDto.builder()
                .name("Bebidas_" + SUF)
                .descripcion("Jugos naturales, batidos, café y bebidas calientes")
                .activo(true).build()).getCategoryId();

        catPostreId = categoryService.crear(CategoryDto.builder()
                .name("Postres_" + SUF)
                .descripcion("Postres artesanales y dulces típicos ecuatorianos")
                .activo(true).build()).getCategoryId();

        catEspecialidadId = categoryService.crear(CategoryDto.builder()
                .name("Especialidades_" + SUF)
                .descripcion("Platos especiales y recetas de la casa")
                .activo(true).build()).getCategoryId();

        System.out.println("  ✓ Desayunos       | ID=" + catDesayunosId);
        System.out.println("  ✓ Almuerzos       | ID=" + catAlmuerzoId);
        System.out.println("  ✓ Snacks          | ID=" + catSnacksId);
        System.out.println("  ✓ Bebidas         | ID=" + catBebidasId);
        System.out.println("  ✓ Postres         | ID=" + catPostreId);
        System.out.println("  ✓ Especialidades  | ID=" + catEspecialidadId);

        Assertions.assertNotNull(catDesayunosId);
        Assertions.assertNotNull(catAlmuerzoId);
        Assertions.assertNotNull(catSnacksId);
        Assertions.assertNotNull(catBebidasId);
        Assertions.assertNotNull(catPostreId);
        Assertions.assertNotNull(catEspecialidadId);
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  TEST 02 — 50 platos reales ecuatorianos
    // ════════════════════════════════════════════════════════════════════════════
    @Test @Order(2)
    public void test02_crearCincuentaPlatos() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("  TEST 02: 50 PLATOS REALES ECUATORIANOS");
        System.out.println("╚════════════════════════════════════════╝");

        Assertions.assertNotNull(catDesayunosId,    "Categoría Desayunos requerida");
        Assertions.assertNotNull(catAlmuerzoId,     "Categoría Almuerzos requerida");
        Assertions.assertNotNull(catSnacksId,       "Categoría Snacks requerida");
        Assertions.assertNotNull(catBebidasId,      "Categoría Bebidas requerida");
        Assertions.assertNotNull(catPostreId,       "Categoría Postres requerida");
        Assertions.assertNotNull(catEspecialidadId, "Categoría Especialidades requerida");

        System.out.println("\n  ── DESAYUNOS (10) ──────────────────────────────────────────");
        // índice 0
        crearProducto("Bolón de verde con queso",
                "Bola de plátano verde frito rellena de queso fresco, con café",
                2.50, 50, "bolon_queso.jpg", catDesayunosId);
        // índice 1
        crearProducto("Bolón de verde con chicharrón",
                "Bola de plátano verde frito rellena de chicharrón crujiente",
                2.75, 40, "bolon_chicharron.jpg", catDesayunosId);
        // índice 2
        crearProducto("Tigrillo serrano",
                "Plátano verde machucado con huevo, queso y mantequilla",
                3.00, 35, "tigrillo.jpg", catDesayunosId);
        // índice 3
        crearProducto("Sánduche de pernil",
                "Pan redondo con pernil de cerdo, encurtido de cebolla y tomate",
                2.25, 60, "sanduche_pernil.jpg", catDesayunosId);
        // índice 4
        crearProducto("Tostadas con mantequilla y mermelada",
                "Dos tostadas con mantequilla y mermelada de mora sierra",
                1.50, 80, "tostadas.jpg", catDesayunosId);
        // índice 5
        crearProducto("Humitas de choclo",
                "Tamal dulce de maíz fresco envuelto en hoja de choclo",
                1.75, 45, "humitas.jpg", catDesayunosId);
        // índice 6
        crearProducto("Tamales de maíz con pollo",
                "Tamal de masa de maíz relleno con pollo y pasas, en hoja de achira",
                2.00, 40, "tamales.jpg", catDesayunosId);
        // índice 7
        crearProducto("Empanadas de morocho",
                "Empanadas de maíz morocho rellenas de carne y vegetales, fritas",
                1.50, 70, "empanadas_morocho.jpg", catDesayunosId);
        // índice 8
        crearProducto("Desayuno ejecutivo completo",
                "Jugo natural + café + huevos revueltos + pan + fruta de temporada",
                4.50, 30, "desayuno_ejecutivo.jpg", catDesayunosId);
        // índice 9
        crearProducto("Colada de avena con canela",
                "Bebida caliente de avena con canela, naranjilla y panela",
                1.25, 100, "colada_avena.jpg", catDesayunosId);

        System.out.println("\n  ── ALMUERZOS (12) ──────────────────────────────────────────");
        // índice 10
        crearProducto("Seco de pollo con arroz",
                "Pollo guisado en salsa de tomate, con arroz, ensalada y maduro",
                3.50, 40, "seco_pollo.jpg", catAlmuerzoId);
        // índice 11
        crearProducto("Seco de res con menestra",
                "Res en salsa de chicha de jora, arroz, menestra de lenteja y patacón",
                4.00, 35, "seco_res.jpg", catAlmuerzoId);
        // índice 12
        crearProducto("Guatita",
                "Mondongo de res guisado en salsa de maní con papa chola y arroz",
                3.75, 25, "guatita.jpg", catAlmuerzoId);
        // índice 13
        crearProducto("Caldo de gallina criolla",
                "Sopa de gallina de campo con papa, fideos, yuca y cebolla blanca",
                3.00, 30, "caldo_gallina.jpg", catAlmuerzoId);
        // índice 14
        crearProducto("Yahuarlocro",
                "Locro cremoso de papas con sangre de borrego, aguacate y maní",
                3.50, 20, "yahuarlocro.jpg", catAlmuerzoId);
        // índice 15
        crearProducto("Locro de papa con queso",
                "Sopa espesa de papa chola con queso fresco y aguacate",
                2.75, 45, "locro_papa.jpg", catAlmuerzoId);
        // índice 16
        crearProducto("Arroz con menestra y carne asada",
                "Arroz blanco, menestra de fréjol y carne de res a la plancha",
                3.50, 50, "arroz_menestra.jpg", catAlmuerzoId);
        // índice 17
        crearProducto("Fritada con mote y maduro",
                "Cerdo frito en su jugo con mote pelado, tostado y plátano maduro",
                4.50, 30, "fritada.jpg", catAlmuerzoId);
        // índice 18
        crearProducto("Hornado estilo Riobamba",
                "Cerdo al horno de leña con llapingachos, mote y encebollado",
                5.00, 20, "hornado.jpg", catAlmuerzoId);
        // índice 19
        crearProducto("Encebollado de atún",
                "Sopa de yuca con atún, cebolla encurtida, tomate y culantro",
                3.25, 40, "encebollado.jpg", catAlmuerzoId);
        // índice 20
        crearProducto("Churrasco a la plancha",
                "Carne de res, arroz, huevo frito, papas fritas y ensalada",
                5.50, 25, "churrasco.jpg", catAlmuerzoId);
        // índice 21
        crearProducto("Almuerzo del día",
                "Sopa del día + segundo (arroz, proteína, ensalada) + jugo natural",
                3.00, 60, "almuerzo_dia.jpg", catAlmuerzoId);

        System.out.println("\n  ── SNACKS (8) ──────────────────────────────────────────────");
        // índice 22
        crearProducto("Empanadas de viento con queso",
                "Empanadas fritas con queso fresco derretido, espolvoreadas con azúcar",
                1.25, 100, "empanadas_viento.jpg", catSnacksId);
        // índice 23
        crearProducto("Ceviche de camarón",
                "Camarones en salsa de limón, tomate y cebolla, con canguil",
                4.50, 30, "ceviche_camaron.jpg", catSnacksId);
        // índice 24
        crearProducto("Patacones con hogao",
                "Plátano verde frito y aplastado con salsa de tomate y cebolla",
                2.00, 50, "patacones.jpg", catSnacksId);
        // índice 25
        crearProducto("Salchipapas especiales",
                "Papas fritas con salchichas, mayo, mostaza, kétchup y salsa rosada",
                2.75, 60, "salchipapas.jpg", catSnacksId);
        // índice 26
        crearProducto("Chifles con guacamole",
                "Chifles de verde salados con guacamole casero",
                1.75, 80, "chifles_guacamole.jpg", catSnacksId);
        // índice 27
        crearProducto("Tostado con quesillo",
                "Maíz tostado con quesillo de hoja y ají casero",
                1.50, 70, "tostado_quesillo.jpg", catSnacksId);
        // índice 28
        crearProducto("Quimbolitos",
                "Tamalitos dulces de harina de maíz con pasas, en hoja de achira",
                1.00, 90, "quimbolitos.jpg", catSnacksId);
        // índice 29
        crearProducto("Mote pillo con huevo",
                "Mote pelado salteado con huevo, mantequilla, cebolla y culantro",
                2.50, 40, "mote_pillo.jpg", catSnacksId);

        System.out.println("\n  ── BEBIDAS (10) ─────────────────────────────────────────────");
        // índice 30
        crearProducto("Jugo de naranjilla natural",
                "Jugo puro de naranjilla con agua o leche, sin azúcar o con panela",
                1.50, 120, "jugo_naranjilla.jpg", catBebidasId);
        // índice 31
        crearProducto("Jugo de tomate de árbol",
                "Jugo de tomate de árbol con limón y canela, servido frío",
                1.50, 100, "jugo_tomate.jpg", catBebidasId);
        // índice 32
        crearProducto("Batido de mora con leche",
                "Batido cremoso de mora negra con leche entera y azúcar",
                2.00, 80, "batido_mora.jpg", catBebidasId);
        // índice 33
        crearProducto("Batido de guanábana",
                "Batido refrescante de guanábana con leche y hielo",
                2.00, 60, "batido_guanabana.jpg", catBebidasId);
        // índice 34
        crearProducto("Café pasado ecuatoriano",
                "Café de altura amazónica, pasado a la taza, con leche opcional",
                1.25, 150, "cafe_pasado.jpg", catBebidasId);
        // índice 35
        crearProducto("Chocolate caliente de taza",
                "Chocolate artesanal de Esmeraldas disuelto en leche caliente",
                1.75, 90, "chocolate_caliente.jpg", catBebidasId);
        // índice 36
        crearProducto("Chicha de jora",
                "Bebida fermentada artesanal de maíz jora, receta serrana tradicional",
                1.50, 50, "chicha_jora.jpg", catBebidasId);
        // índice 37
        crearProducto("Jugo de caña con limón",
                "Jugo fresco de caña de azúcar con limón y hielo picado",
                1.00, 80, "jugo_cana.jpg", catBebidasId);
        // índice 38
        crearProducto("Agua de Jamaica",
                "Infusión fría de flor de Jamaica con panela y canela",
                1.25, 70, "agua_jamaica.jpg", catBebidasId);
        // índice 39
        crearProducto("Limonada de coco",
                "Limonada con leche de coco, menta y hielo",
                2.25, 60, "limonada_coco.jpg", catBebidasId);

        System.out.println("\n  ── POSTRES (6) ──────────────────────────────────────────────");
        // índice 40
        crearProducto("Arroz con leche y canela",
                "Arroz cremoso cocido en leche con canela, pasas y cáscara de naranja",
                1.75, 60, "arroz_leche.jpg", catPostreId);
        // índice 41
        crearProducto("Espumilla de guayaba",
                "Merengue ecuatoriano de guayaba en cono de galleta",
                1.00, 80, "espumilla.jpg", catPostreId);
        // índice 42
        crearProducto("Flan de coco artesanal",
                "Flan casero de coco con caramelo dorado y coco rallado",
                2.50, 40, "flan_coco.jpg", catPostreId);
        // índice 43
        crearProducto("Tres leches casero",
                "Bizcocho bañado en tres leches con crema batida y canela",
                2.75, 35, "tres_leches.jpg", catPostreId);
        // índice 44
        crearProducto("Pristiños con miel de panela",
                "Buñuelos de masa frita bañados en miel de panela con anís",
                1.50, 50, "pristinos.jpg", catPostreId);
        // índice 45
        crearProducto("Helado de paila de taxo",
                "Helado artesanal batido a mano en paila de bronce con taxo andino",
                2.00, 45, "helado_paila.jpg", catPostreId);

        System.out.println("\n  ── ESPECIALIDADES (4) ────────────────────────────────────────");
        // índice 46
        crearProducto("Cuy asado estilo Riobamba",
                "Cuy entero asado al carbón con papas, maní, ensalada y ají de pepa",
                12.00, 10, "cuy_asado.jpg", catEspecialidadId);
        // índice 47
        crearProducto("Bandeja paisa ecuatoriana",
                "Arroz, fréjol, chicharrón, chorizo, huevo, maduro y aguacate",
                6.50, 20, "bandeja_paisa.jpg", catEspecialidadId);
        // índice 48
        crearProducto("Cazuela de mariscos",
                "Cazuela de camarones, almejas y pescado en salsa de coco y especias",
                8.00, 15, "cazuela_mariscos.jpg", catEspecialidadId);
        // índice 49
        crearProducto("Parrillada familiar UIDE",
                "Selección de carnes a la parrilla (res, cerdo, chorizo) con guarniciones",
                15.00, 8, "parrillada.jpg", catEspecialidadId);

        Assertions.assertEquals(50, productosIds.size(),
                "Deben crearse exactamente 50 productos. Creados: " + productosIds.size());

        System.out.println("\n  ✅ Total de platos registrados: " + productosIds.size() + " / 50");
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  TEST 03 — 5 Mesas reales de la cafetería UIDE
    // ════════════════════════════════════════════════════════════════════════════
    @Test @Order(3)
    public void test03_crearMesas() {
        System.out.println("\n╔═══════════════════════════════╗");
        System.out.println("  TEST 03: MESAS DE LA CAFETERÍA");
        System.out.println("╚═══════════════════════════════╝");

        Mesa m1 = mesaService.crear(MesaDto.builder()
                .numeroMesa(101).capacidad(4).ubicacion("Zona interior — ventana norte").activo(true).build());
        mesa1Id = m1.getId();

        Mesa m2 = mesaService.crear(MesaDto.builder()
                .numeroMesa(102).capacidad(6).ubicacion("Zona interior — centro").activo(true).build());
        mesa2Id = m2.getId();

        Mesa m3 = mesaService.crear(MesaDto.builder()
                .numeroMesa(103).capacidad(2).ubicacion("Terraza exterior").activo(true).build());
        mesa3Id = m3.getId();

        mesaService.crear(MesaDto.builder()
                .numeroMesa(104).capacidad(8).ubicacion("Salón privado — reuniones").activo(true).build());

        mesaService.crear(MesaDto.builder()
                .numeroMesa(105).capacidad(4).ubicacion("Zona de barra — mostrador").activo(true).build());

        System.out.println("  ✓ Mesa 101 — Zona interior norte (4 personas) | ID=" + mesa1Id);
        System.out.println("  ✓ Mesa 102 — Zona interior centro (6 personas) | ID=" + mesa2Id);
        System.out.println("  ✓ Mesa 103 — Terraza exterior (2 personas)     | ID=" + mesa3Id);
        System.out.println("  ✓ Mesa 104 — Salón privado (8 personas)");
        System.out.println("  ✓ Mesa 105 — Zona de barra (4 personas)");

        Assertions.assertNotNull(mesa1Id);
        Assertions.assertNotNull(mesa2Id);
        Assertions.assertNotNull(mesa3Id);
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  TEST 04 — Pedido 1: Juan Pérez en mesa 101, atendido por Ana Torres
    // ════════════════════════════════════════════════════════════════════════════
    @Test @Order(4)
    public void test04_crearPedido1() {
        System.out.println("\n╔═══════════════════════════════════════════════════╗");
        System.out.println("  TEST 04: PEDIDO 1 — Juan Pérez / Mesera Ana Torres");
        System.out.println("╚═══════════════════════════════════════════════════╝");

        Assertions.assertNotNull(mesa1Id,    "mesa1Id requerida");
        Assertions.assertNotNull(cliente1Id, "cliente1Id requerido");
        Assertions.assertNotNull(mesero1Id,  "mesero1Id requerido");

        // Juan pide: Seco de pollo [10] + Locro de papa [15] + Jugo naranjilla×2 [30] + Tres leches [43]
        List<ItemPedidoDto> items = new ArrayList<>();
        items.add(ItemPedidoDto.builder().productoId(productosIds.get(10)).cantidad(1).notas("Sin picante por favor").build());
        items.add(ItemPedidoDto.builder().productoId(productosIds.get(15)).cantidad(1).notas("Con extra de queso").build());
        items.add(ItemPedidoDto.builder().productoId(productosIds.get(30)).cantidad(2).notas("Sin azúcar").build());
        items.add(ItemPedidoDto.builder().productoId(productosIds.get(43)).cantidad(1).notas("Con más crema").build());

        CrearPedidoDto dto = CrearPedidoDto.builder()
                .mesaId(mesa1Id)
                .clienteId(cliente1Id)
                .meseroId(mesero1Id)
                .notas("Mesa junto a ventana. Cliente alérgico al cilantro.")
                .items(items)
                .build();

        Pedido pedido1 = pedidoService.crear(dto);
        Assertions.assertNotNull(pedido1, "Pedido 1 no debe ser null");
        pedido1Id = pedido1.getId();
        Assertions.assertTrue(pedido1.getTotal().signum() > 0, "Total pedido 1 debe ser > 0");

        // Total esperado con IVA 15%: subtotal 12.00 × 1.15 = 13.80
        double subtotal = 3.50 + 2.75 + 2 * 1.50 + 2.75;
        double esperado = subtotal * 1.15;
        System.out.println("  ✓ Pedido 1 creado  | ID=" + pedido1Id);
        System.out.println("    Cliente  : Juan Pérez");
        System.out.println("    Mesera   : Ana Torres");
        System.out.println("    Mesa     : 101 — Zona interior norte");
        System.out.println("    Items    : Seco de pollo, Locro de papa, Jugo naranjilla×2, Tres leches");
        System.out.println("    Total    : $" + pedido1.getTotal() + "  (esperado: $" + esperado + ")");
        Assertions.assertEquals(esperado, pedido1.getTotal().doubleValue(), 0.01,
                "Total pedido 1 incorrecto");
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  TEST 05 — Pedido 2: Sofía Díaz en mesa 103, atendido por Luis Pacheco
    // ════════════════════════════════════════════════════════════════════════════
    @Test @Order(5)
    public void test05_crearPedido2() {
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("  TEST 05: PEDIDO 2 — Sofía Díaz / Mesero Luis Pacheco");
        System.out.println("╚═══════════════════════════════════════════════════════╝");

        Assertions.assertNotNull(mesa3Id,    "mesa3Id requerida");
        Assertions.assertNotNull(cliente2Id, "cliente2Id requerido");
        Assertions.assertNotNull(mesero2Id,  "mesero2Id requerido");

        // Sofía pide: Desayuno ejecutivo [8] + Café pasado [34] + Espumilla×2 [41]
        List<ItemPedidoDto> items = new ArrayList<>();
        items.add(ItemPedidoDto.builder().productoId(productosIds.get(8)).cantidad(1).notas("Huevos fritos").build());
        items.add(ItemPedidoDto.builder().productoId(productosIds.get(34)).cantidad(1).notas("Con leche al lado").build());
        items.add(ItemPedidoDto.builder().productoId(productosIds.get(41)).cantidad(2).notas("").build());

        CrearPedidoDto dto = CrearPedidoDto.builder()
                .mesaId(mesa3Id)
                .clienteId(cliente2Id)
                .meseroId(mesero2Id)
                .notas("Terraza — necesita sombrilla.")
                .items(items)
                .build();

        Pedido pedido2 = pedidoService.crear(dto);
        Assertions.assertNotNull(pedido2, "Pedido 2 no debe ser null");
        pedido2Id = pedido2.getId();
        Assertions.assertTrue(pedido2.getTotal().signum() > 0, "Total pedido 2 debe ser > 0");

        // Total esperado con IVA 15%: subtotal 7.75 × 1.15 = 8.9125
        double subtotal = 4.50 + 1.25 + 2 * 1.00;
        double esperado = subtotal * 1.15;
        System.out.println("  ✓ Pedido 2 creado  | ID=" + pedido2Id);
        System.out.println("    Cliente  : Sofía Díaz");
        System.out.println("    Mesero   : Luis Pacheco");
        System.out.println("    Mesa     : 103 — Terraza exterior");
        System.out.println("    Items    : Desayuno ejecutivo, Café pasado, Espumilla×2");
        System.out.println("    Total    : $" + pedido2.getTotal() + "  (esperado: $" + esperado + ")");
        Assertions.assertEquals(esperado, pedido2.getTotal().doubleValue(), 0.01,
                "Total pedido 2 incorrecto");
    }

    @Test @Order(7)
    public void test07_facturaManualPedroVega() {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("  TEST 07: FACTURA MANUAL — Pedro Vega (para llevar)");
        System.out.println("╚════════════════════════════════════════════════════════╝");

        Assertions.assertNotNull(cliente3Id, "cliente3Id requerido");
        Assertions.assertNotNull(cajeroId,   "cajeroId requerido");

        // Pedro compra: Fritada [17] + Bolón queso×2 [0] + Chicha de jora×2 [36]
        List<ItemFacturaDto> items = new ArrayList<>();
        items.add(ItemFacturaDto.builder().productoId(productosIds.get(17)).cantidad(1.0).build());
        items.add(ItemFacturaDto.builder().productoId(productosIds.get(0)).cantidad(2.0).build());
        items.add(ItemFacturaDto.builder().productoId(productosIds.get(36)).cantidad(2.0).build());

        CrearFacturaManualDto dto = CrearFacturaManualDto.builder()
                .clienteId(cliente3Id)
                .cajeroId(cajeroId)
                .empresaRuc(null)   // consumidor final, sin RUC
                .descuento(java.math.BigDecimal.ZERO)
                .items(items)
                .build();

        try {
            Factura f = facturaService.crearManual(dto);
            Assertions.assertNotNull(f, "Factura manual no debe ser null");
            Assertions.assertTrue(f.getTotal().signum() > 0, "Total factura manual > 0");

            // Total esperado con IVA 15%: subtotal 12.50 × 1.15 = 14.375
            double subtotal = 4.50 + 2 * 2.50 + 2 * 1.50;
            double esperado = subtotal * 1.15;
            System.out.println("  ✓ Factura manual #" + f.getId() + " para Pedro Vega");
            System.out.println("    Items  : Fritada, Bolón con queso×2, Chicha de jora×2");
            System.out.println("    Total  : $" + f.getTotal() + "  (esperado: $" + esperado + ")");
            Assertions.assertEquals(esperado, f.getTotal().doubleValue(), 0.01,
                    "Total factura manual incorrecto");
        } catch (ConstraintViolationException e) {
            System.out.println("  ⚠ Violación de validación: " + e.getMessage());
            Assertions.assertTrue(e.getMessage().toLowerCase().contains("subtotal")
                    || e.getMessage().toLowerCase().contains("total"),
                    "Se esperaba violación en subtotal/total");
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  TEST 08 — Resumen final de la cafetería
    // ════════════════════════════════════════════════════════════════════════════
    @Test @Order(8)
    public void test08_resumenFinal() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("  TEST 08: RESUMEN FINAL — CAFETERÍA UIDE");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        int cats      = categoryService.listarTodas().size();
        int productos = productoService.listarTodos().size();
        int pedidos   = pedidoService.listarTodos().size();
        int mesas     = mesaService.listarTodas().size();
        int usuarios  = usuarioService.listarTodos().size();

        System.out.println("  Categorías : " + cats      + "  (esperado ≥ 6)");
        System.out.println("  Platos     : " + productos + "  (esperado ≥ 50)");
        System.out.println("  Pedidos    : " + pedidos   + "  (esperado ≥ 2)");
        System.out.println("  Mesas      : " + mesas     + "  (esperado ≥ 5)");
        System.out.println("  Usuarios   : " + usuarios  + "  (esperado ≥ 7)");

        Assertions.assertTrue(cats      >= 6,  "Debe haber al menos 6 categorías");
        Assertions.assertTrue(productos >= 50, "Debe haber al menos 50 productos");
        Assertions.assertTrue(pedidos   >= 2,  "Debe haber al menos 2 pedidos");
        Assertions.assertTrue(mesas     >= 5,  "Debe haber al menos 5 mesas");
        Assertions.assertTrue(usuarios  >= 7,  "Debe haber al menos 7 usuarios");

        System.out.println("\n  ┌─────────────────────────────────────────────────────┐");
        System.out.println("  │          LOGINS DISPONIBLES EN EL FRONTEND          │");
        System.out.println("  ├──────────────┬──────────────────┬───────────────────┤");
        System.out.println("  │ ROL          │ USERNAME         │ PASSWORD          │");
        System.out.println("  ├──────────────┼──────────────────┼───────────────────┤");
        System.out.println("  │ ADMIN        │ carlos.mendoza   │ " + PASSWORD + "     │");
        System.out.println("  │ MESERO       │ ana.torres       │ " + PASSWORD + "     │");
        System.out.println("  │ MESERO       │ luis.pacheco     │ " + PASSWORD + "     │");
        System.out.println("  │ CAJERO       │ maria.suarez     │ " + PASSWORD + "     │");
        System.out.println("  │ CLIENTE      │ juan.perez       │ " + PASSWORD + "     │");
        System.out.println("  │ CLIENTE      │ sofia.diaz       │ " + PASSWORD + "     │");
        System.out.println("  │ CLIENTE      │ pedro.vega       │ " + PASSWORD + "     │");
        System.out.println("  └──────────────┴──────────────────┴───────────────────┘");
        System.out.println("\n  🎉 TEST COMPLETO EXITOSO — CAFETERÍA UIDE ✅");
    }
}