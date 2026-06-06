/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Tests;
/**
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import tics.uide.gestionuide.GestionUIDE;
import static tics.uide.gestionuide.JWTAuthorizationFilter.AUTORIZATION;

import tics.uide.gestionuide.dto.UsuarioDto;


 *
 * @author Dante

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = GestionUIDE.class)
public class WSTests {

    private final int port = 8080;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void probarServicio() throws JsonProcessingException {

        final String prefixAutenticacion = "http://localhost:" + port + "/autentication/";
        final String prefixTelefono = "http://localhost:" + port + "/telefono/";
        final String urlLogin = prefixAutenticacion + "login";
        final String urlRegistrar = prefixAutenticacion + "registrar";
        final String urlCheck = prefixAutenticacion + "disponible";
        final String urlAgregarTelefono = prefixTelefono + "agregar";
        String jwt = verificateResponse(urlLogin, UsuarioClaveDto.builder().usuario("pruebados")
                .clave("pruebaclave").build());
        System.out.println(jwt);
        System.out.println(urlCheck);
        verificateResponse(urlCheck, UsuarioClaveDto.builder().usuario("prueba").build());
        verificateResponse(urlRegistrar, UsuarioDto.builder().usuario("juanitoperez3")
                .clave("juanitoperez2").cedula("1187654321").nombres("juanito perezaS").email("juanitoperez2@uide.edu.ec")
                .telefono("0983514333").build());
        verificateResponse(urlAgregarTelefono, CedulaDatoAdd.builder().cedula("0801904392").dato("072727600").build(), jwt);
    }

    private String verificateResponse(String url, UsuarioClaveDto usuario) throws JsonProcessingException {
        System.out.println("---------------------" + url + "---------------------");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(usuario), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        System.out.println("Respuesta:" + response.getBody());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        System.out.println(response.getHeaders());
        return response.getHeaders().getFirst(AUTORIZATION);
    }

    private void verificateResponse(String url, CedulaDatoAdd usuario, String jwt) throws JsonProcessingException {
        System.out.println("---------------------" + url + "---------------------");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json");
        headers.add(AUTORIZATION, jwt);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(usuario), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        System.out.println("Respuesta:" + response.getBody());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    private void verificateResponse(String url, UsuarioDto usuario) throws JsonProcessingException {
        System.out.println("---------------------" + url + "---------------------");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(usuario), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        System.out.println("Respuesta:" + response.getBody());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }
}
 */