package tics.uide.gestionuide;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
    class WebSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                .csrf().disable()
                .cors().configurationSource(request -> {
                    CorsConfiguration cors = new CorsConfiguration();
                    cors.setAllowedOriginPatterns(Arrays.asList("*"));
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
            return mapper;
        }
    }

    @Configuration
    class WebConfig implements WebMvcConfigurer {
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/uploads/**").addResourceLocations("file:uploads/");
        }
    }
}
