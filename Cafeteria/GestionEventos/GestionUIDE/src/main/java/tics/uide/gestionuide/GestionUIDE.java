package tics.uide.gestionuide;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableAutoConfiguration
@org.springframework.scheduling.annotation.EnableScheduling   // heartbeat SSE de cocina
public class GestionUIDE {

    public static void main(String[] args) {
        SpringApplication.run(GestionUIDE.class, args);
    }

    // Auto-crear directorios de uploads al arrancar
    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(Paths.get("uploads/productos"));
        Files.createDirectories(Paths.get("uploads/usuarios"));
    }

    @Configuration
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    class WebSecurityConfig extends WebSecurityConfigurerAdapter {

        @Value("${app.cors.allowed-origins}")
        private String allowedOrigins;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                .csrf().disable()
                .cors().configurationSource(request -> {
                    CorsConfiguration cors = new CorsConfiguration();
                    cors.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                            .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
                    cors.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
                    cors.setAllowedHeaders(Arrays.asList("*"));
                    cors.setExposedHeaders(Arrays.asList("Authorization"));
                    cors.setAllowCredentials(true);
                    return cors;
                })
                .and()
                .addFilterAfter(new JWTAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                    .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // Públicos
                    .antMatchers("/api/auth/**").permitAll()
                    .antMatchers("/uploads/**").permitAll()
                    .antMatchers(HttpMethod.GET, "/api/productos/**").permitAll()
                    .antMatchers(HttpMethod.GET, "/api/categorias/**").permitAll()
                    // Catálogo productos/categorías: escritura solo ADMIN
                    .antMatchers(HttpMethod.POST,   "/api/productos", "/api/productos/**", "/api/categorias", "/api/categorias/**").hasAuthority("ADMIN")
                    .antMatchers(HttpMethod.PUT,    "/api/productos/**", "/api/categorias/**").hasAuthority("ADMIN")
                    .antMatchers(HttpMethod.PATCH,  "/api/productos/**", "/api/categorias/**").hasAuthority("ADMIN")
                    .antMatchers(HttpMethod.DELETE, "/api/productos/**", "/api/categorias/**").hasAuthority("ADMIN")
                    // Mesas: cambiar estado = ADMIN o MESERO; resto del CRUD solo ADMIN
                    .antMatchers(HttpMethod.PUT,    "/api/mesas/*/estado").hasAnyAuthority("ADMIN", "MESERO")
                    .antMatchers(HttpMethod.POST,   "/api/mesas", "/api/mesas/**").hasAuthority("ADMIN")
                    .antMatchers(HttpMethod.PUT,    "/api/mesas/**").hasAuthority("ADMIN")
                    .antMatchers(HttpMethod.DELETE, "/api/mesas/**").hasAuthority("ADMIN")
                    // Facturas: CLIENTE lee su factura (GET /{id}) y su listado (GET /cliente/**), propiedad en el controller.
                    //           El resto (escrituras y listados de staff) = ADMIN/CAJERO.
                    .antMatchers(HttpMethod.GET, "/api/facturas/cliente/**").hasAnyAuthority("ADMIN", "CAJERO", "CLIENTE")
                    .antMatchers(HttpMethod.GET, "/api/facturas/*").hasAnyAuthority("ADMIN", "CAJERO", "CLIENTE")
                    .antMatchers("/api/facturas", "/api/facturas/**").hasAnyAuthority("ADMIN", "CAJERO")
                    // Pagos: ADMIN o CAJERO (todo)
                    .antMatchers("/api/pagos", "/api/pagos/**").hasAnyAuthority("ADMIN", "CAJERO")
                    // Pedidos: crear = ADMIN/MESERO/CLIENTE (CLIENTE solo el suyo, validado en el controller);
                    //          GET /cliente/** = ADMIN/MESERO/CLIENTE (propiedad en el controller);
                    //          resto (listados globales + escrituras) = ADMIN/MESERO.
                    .antMatchers(HttpMethod.POST, "/api/pedidos").hasAnyAuthority("ADMIN", "MESERO", "CLIENTE")
                    .antMatchers(HttpMethod.GET, "/api/pedidos/cliente/**").hasAnyAuthority("ADMIN", "MESERO", "CLIENTE")
                    // Pedido por QR: un CLIENTE puede pedir vía contexto de MESA (no expone pedidoId; no toca esDuenioOStaff)
                    .antMatchers(HttpMethod.POST, "/api/pedidos/mesa/*").hasAnyAuthority("ADMIN", "MESERO", "CLIENTE")
                    .antMatchers("/api/pedidos", "/api/pedidos/**").hasAnyAuthority("ADMIN", "MESERO")
                    // Actuator: solo el healthcheck es público; el resto, denegado (doble capa con la whitelist de exposición)
                    .antMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                    .antMatchers("/actuator/**").denyAll()
                    // Todo lo demás requiere autenticación
                    .antMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
                .and()
                .headers()
                    .xssProtection()
                    .and().cacheControl()
                    .and().contentTypeOptions()
                    .and().httpStrictTransportSecurity()
                        .includeSubDomains(true).maxAgeInSeconds(31536000)
                    .and().frameOptions().sameOrigin()
                    .addHeaderWriter(new StaticHeadersWriter("X-Content-Security-Policy","script-src 'self'"))
                    .addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN))
                    .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN);
        }

        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            Hibernate5Module hm = new Hibernate5Module();
            hm.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, false);
            mapper.registerModule(hm);
            // Dinero en BigDecimal: serializar en notación decimal plana (sin exponente)
            mapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
            return mapper;
        }
    }

    @Configuration
    class WebConfig implements WebMvcConfigurer {

        @org.springframework.beans.factory.annotation.Autowired
        private tics.uide.gestionuide.service.RateLimitService rateLimitService;

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/uploads/**").addResourceLocations("file:uploads/");
        }

        @Override
        public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
            // Rate limiting POR IP en los endpoints sensibles de auth (anti fuerza bruta).
            registry.addInterceptor(new tics.uide.gestionuide.ratelimit.RateLimitInterceptor(rateLimitService))
                    .addPathPatterns(
                            "/api/auth/login", "/api/auth/registro", "/api/auth/recuperar-password",
                            "/api/auth/restablecer-password", "/api/auth/reenviar-verificacion",
                            "/api/auth/verificar-email", "/api/auth/refresh");
        }
    }
}
