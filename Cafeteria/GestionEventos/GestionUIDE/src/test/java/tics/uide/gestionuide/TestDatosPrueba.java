package Tests;
/**
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import tics.uide.gestionuide.GestionUIDE;
import tics.uide.gestionuide.dto.CedulaDatoAdd;
import tics.uide.gestionuide.enums.Rol;
import static tics.uide.gestionuide.enums.Rol.ADMINISTRADOR;
import tics.uide.gestionuide.enumsproducto.Categoria;
import tics.uide.gestionuide.enumsproducto.IVA;
import tics.uide.gestionuide.model.DetalleFactura;
import tics.uide.gestionuide.model.Emails;
import tics.uide.gestionuide.model.Favoritos;
import tics.uide.gestionuide.model.Persona;
import tics.uide.gestionuide.model.Producto;
import tics.uide.gestionuide.model.Telefono;
import tics.uide.gestionuide.model.Usuario;
import tics.uide.gestionuide.service.DetalleService;
import tics.uide.gestionuide.service.EmpresaService;
import tics.uide.gestionuide.service.FacturaService;
import tics.uide.gestionuide.service.FavoritoService;
import tics.uide.gestionuide.service.PersonaService;
import tics.uide.gestionuide.service.ProductosService;
import tics.uide.gestionuide.service.RolService;
import tics.uide.gestionuide.service.TelefonoService;
import tics.uide.gestionuide.service.UsuarioService;
import tics.uide.gestionuide.webservice.DetalleFacturaWs;
import tics.uide.gestionuide.webservice.FacturaWs;
import tics.uide.gestionuide.webservice.ProductoWs;
import tics.uide.gestionuide.webservice.TelefonoWS;


 *
 * @author Usuario Jefferson

@SpringBootTest
@ContextConfiguration(classes = GestionUIDE.class)
public class Tests {

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private ProductosService productosService;

    @Autowired
    private DetalleService detalleService;

    @Autowired
    private FacturaService facturaService;

    @Autowired
    private ProductoWs productoWs;

    @Autowired
    private FacturaWs facturaWs;

    @Autowired
    private DetalleFacturaWs detalleWs;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolService rolService;

    @Autowired
    private TelefonoService telefonoService;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private TelefonoWS telefono;

    @Autowired
    private FavoritoService favoritoService;

    @Test
    public void testing() throws Exception {
        testearCrear();
        testearObtener();

        Calendar fecha = Calendar.getInstance();
        fecha.set(1981, 1, 1);

        productosService.crear("Arroz Relleno", "https://www.tqma.com.ec/images/com_yoorecipe/banner_superior/14688_1.jpg", "Consomé de pollo, Arroz relleno con pollo y platanitos maduros, jugo del día", 1.75, IVA.SI, 1.0, Categoria.DESAYUNOS_SOSTENIDO);

        productosService.crear("Guata", "https://recetas123.net/wp-content/uploads/guatita1.jpg", " Un plato típico ecuatoriano preparado con mondongo y papas en un guiso o salsa con maní", 1.75, IVA.SI, 1.75, Categoria.DESAYUNOS_SOSTENIDO);

        productosService.crear("Mixta GUATA-CARNE", "https://www.cocina-ecuatoriana.com/base/stock/Recipe/35-image/35-image_web.jpg", " Estofado hecho a base de trozos de mondongo. Se distingue por ser un plato muy calórico, con carne de res", 1.75, IVA.SI, 1.75, Categoria.DESAYUNOS_SOSTENIDO);

        productosService.crear("Seco de Pollo", "https://thumbs.dreamstime.com/b/pollo-frito-con-arroz-4712289.jpg", "Seco de pollo cocinado a fuego lento en una salsa de cerveza, jugo de naranjilla, cebolla, pimiento, tomate, hierbitas y condimentos", 1.75, IVA.SI, 1.0, Categoria.DESAYUNOS_SOSTENIDO);

        productosService.crear("Mote Pillo", "https://www.laylita.com/recetas/wp-content/uploads/Mote-pillo-receta.jpg", " Plato tradicional comúnmente de la sierra que se prepara mote con cebolla blanca, ajo, achiote, huevos, leche, cebolletas, y cilantro o perejil. ", 1.75, IVA.SI, 1.0, Categoria.DESAYUNOS_SOSTENIDO);

        productosService.crear("Tigrillo", "https://i0.wp.com/www.cafesetentaysiete.com/wp-content/uploads/2021/11/Cafe-77-Tigrillo-de-queso.png?fit=570%2C480&ssl=1", " Plato exquisito que tiene una base de plátano verde, queso y huevo ", 1.75, IVA.SI, 1.0, Categoria.DESAYUNOS_SOSTENIDO);

        productosService.crear("Desayuno Continental", "https://img.freepik.com/fotos-premium/desayuno-continental-mesa-madera_137441-1113.jpg?w=2000", "Cafe o Leche, Pan o Tostada \n"
                + "Jugo o Fruta \n"
                + "Mantequilla y Mermelada \n"
                + "Huevos y queso", 1.75, IVA.SI, 1.0, Categoria.DESAYUNOS);

        productosService.crear("Doritos", "https://yaperito.com/shop/wp-content/uploads/2020/09/doritos-52g-600x659.jpg", "Pollo Frito", 1.75, IVA.SI, 1.0, Categoria.SNACKS);

        productosService.crear("Chitos", "https://www.fybeca.com/dw/image/v2/BDPM_PRD/on/demandware.static/-/Sites-masterCatalog_FybecaEcuador/default/dwa1af907a/images/large/100182750-SNACK-K-CHITOS-ORIGINAL-76-G-UNIDAD.jpg?sw=1000&sh=1000", "Satisface tu hambre con un rico snack", 1.75, IVA.SI, 1.75, Categoria.SNACKS);

        productosService.crear("Tigrillo", "https://imagenes.extra.ec/files/image_full/uploads/2020/07/26/5f1de63a556b1.jpeg", "Pollo Frito", 1.75, IVA.SI, 1.0, Categoria.SNACKS);

        productosService.crear("Gaseosa", "https://www.coca-cola.com.co/content/dam/one/co/es/products/fanta/fanta_Colombia.png", "Refresca tu cuerpo con una bebida muy deliciosa", 1.75, IVA.SI, 1.0, Categoria.BEBIDA);

        productosService.crear("Cafe", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ9ZVIjZ-_bITeqJbeQnjdw4CzKgytsXtiwvP_oSA64R8x2vz0EO-55kkl_K1SXOfDmglE&usqp=CAU", "Tomate un exquisito café perfecto artesanal", 1.75, IVA.SI, 1.0, Categoria.BEBIDA);

        productosService.crear("Agua Aromatica", "https://static.wixstatic.com/media/4f9b21_b64b35ce94a548838f62cba8ea665398~mv2.jpg/v1/fill/w_498,h_332,al_c,q_85,usm_0.66_1.00_0.01/4f9b21_b64b35ce94a548838f62cba8ea665398~mv2.jpg", " Bebidas elaboradas a base de agua, frutas, verduras y hierbas aromáticas que le aportan color, sabor y sus principios activos. ", 1.75, IVA.SI, 1.0, Categoria.TES);

        productosService.crear("Cafe con Leche", "https://cdn.kiwilimon.com/recetaimagen/15262/th5-640x640-7456.jpg", "Una taza de café caliente con leche", 1.75, IVA.SI, 1.0, Categoria.BEBIDA);

        productosService.crear("Cafe con Leche", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSehpoYo6bgfELH7lwoJ7MwcZTmdj9H3Y6P5hsG3ayIbDWVKpCG8p9hR4_GZvaqqhU5ZkE&usqp=CAU", " Una taza de café caliente con leche ", 1.75, IVA.SI, 1.0, Categoria.BEBIDA);

        productosService.crear("Yogurt", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTJiXLGPNXaC3Nw_hSKaIY6gyQHokBAkRXcEw&usqp=CAU", " Yogurt natural fortalece el sistema inmunológico", 1.75, IVA.SI, 1.0, Categoria.BEBIDA);

        productosService.crear("Bebida Energizante", "https://laboladeoroquito.com/wp-content/uploads/2020/05/V220-ORIGINAL-600ML-600x600.jpg", " Aliviar la fatiga, mantener la vigilia, mejorar el rendimiento físico y estimular las capacidades cognitivas ante situaciones de estrés ", 1.75, IVA.SI, 1.75, Categoria.BEBIDA);

        productosService.crear("Torta del día", "https://dam.cocinafacil.com.mx/wp-content/uploads/2019/01/Pastel-chocolate-crema-frambuesas.jpg", " torta de chocolate francesa es super fácil de preparar y lleva chocolate oscuro, mantequilla, azúcar, huevos, y un poquito de harina. Se sirve decorada con menta o albahaca fresa y moras/frambuesas, helado", 1.75, IVA.SI, 1.75, Categoria.ANTOJITOS);

        productosService.crear("Tamal", "https://imgmedia.buenazo.pe/650x358/buenazo/original/2020/11/26/5fc06dec0616b9765d22eed6.jpg", " Preparado con una masa de maíz rellena con guiso de carne. Se envuelven en hojas de plátano y se cocinan al vapor. ", 1.75, IVA.SI, 1.75, Categoria.ANTOJITOS);

        productosService.crear("Bolón", "https://www.lahora.com.ec/wp-content/uploads/2022/04/PRODUCTO-1.jpeg", "Platillo típico de la costa ecuatoriana, por lo general se lo considera un plato ideal para el desayuno ", 1.75, IVA.SI, 1.75, Categoria.ANTOJITOS);

        productosService.crear("Empanada de verde", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQDwa1TEzhTULfeiNsqBnVEpZQmwCvlSJHXrEkDRtKT7GOIIc_Q-2U7MjXg5iVtMvsuG8M&usqp=CAU", " Receta ecuatoriana se preparada con masa de plátano verde y rellenas con queso y carne", 1.75, IVA.SI, 1.75, Categoria.ANTOJITOS);

        productosService.crear("Empanada de viento", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ2hERGvx7QvNyOhdDdagFaoZwN9-jH372NRA&usqp=CAU", "Deliciosas empanadas fritas llevan un relleno de quesillo o queso y se sirven espolvoreadas con azúcar. ", 1.75, IVA.SI, 1.75, Categoria.ANTOJITOS);

        productosService.crear("Sanduches frios/Prensado", "https://dam.cocinafacil.com.mx/wp-content/uploads/2018/08/sandwich-de-coles-de-bruselasas-con-pesto-y-gruyere.jpg", " El pan frito crujiente o frybread es una receta india perfecta para un picoteo saludable de los que no se olvidan", 1.75, IVA.SI, 1.75, Categoria.ANTOJITOS);

        productosService.crear("Pan de yuca", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTQP0u2wpqXPE75wgkZi_jymvZLTzYejFf4ZQ&usqp=CAU", "Preparado con almidón de yuca, queso, huevos, leche y mantequilla", 1.75, IVA.SI, 1.75, Categoria.ANTOJITOS);
        Producto producto = productosService.crear("Pan de yuca", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTQP0u2wpqXPE75wgkZi_jymvZLTzYejFf4ZQ&usqp=CAU", "Guata", 1.75, IVA.SI, 1.75, Categoria.ANTOJITOS);
        Persona persona = personaService.buscarPersona("0801904392");

        List<Telefono> telefono = new ArrayList<>();
        telefono.add(Telefono.builder().telefono("0987654321").build());

        List<Emails> emails = new ArrayList<>();
        emails.add(Emails.builder().email("jeferalo-0210@gmail.com").build());

        usuarioService.crearUsuario("Jefferson", "jefferson", "1104772197", "Jefferson Fernando Ramirez Lozada", null, ADMINISTRADOR);

        empresaService.crear("1104772195", "Jefferson Fernando Ramirez Lozada", 12.0, "UIDE", "0986636499", "jeferalo@gmail.com");

        List<DetalleFactura> listaa = new ArrayList<>();
        listaa.add(DetalleFactura.builder().nombre("Sanduches frios/Prensado").cantidad(1.0).costoUnitario(1.75).total(1.0 * 1.75).build());
        listaa.add(DetalleFactura.builder().nombre("Pan de yuca").cantidad(1.0).costoUnitario(1.75).build());

        System.out.println(listaa);

        facturaService.crear("1104772195", "1104772197", fecha.getTime(), listaa, "1111100000");



    }

    public void testearObtener() {

        Persona personaNormal = personaService.buscarPersona("0801904392");

        Persona personaOptimizado = personaService.buscarPersonaUnaConsulta("0801904392");
        Persona personaConEmails = personaService.buscarPersonaConEmails("0801904392");
        Persona personaConTelefonos = personaService.buscarPersonaConTelefonos("0801904392");
        System.out.println("--------------------Persona-----------------");
        System.out.println(personaNormal.getNombres());
        System.out.println(personaNormal.getUrlFoto());
        for (Telefono telefono : personaConTelefonos.getTelefonos()) {
            System.out.println("Telefono: " + telefono.getTelefono());
        }
        System.out.println("----------------- fin persona ---------------");

    }

    public void testearCrear() {
        usuarioService.crearUsuario("pruebauno", "pruebaclave", "1112345678", "los nombres y apellidos", null, null);
        if (usuarioService.comprobarDisponibilidadUsuario("pruebados")) {
            Usuario usuario = usuarioService.crearUsuario("pruebados", "pruebaclave", "0801904392", "Dante Casella", null, "0983514333", "dacasellamo@uide.edu.ec", ADMINISTRADOR);
            rolService.agregarRol(usuario, Rol.CLIENTE);
            rolService.agregarRol(usuario, Rol.USUARIO);
            rolService.agregarRol(usuario, Rol.OTRO);
        }
        telefonoService.agregarTelefono(personaService.buscarPersona("0801904392"), "072727600");
        telefono.agregar(CedulaDatoAdd.builder().cedula("0801904392").dato("0983514333").build());

    }
}
 */