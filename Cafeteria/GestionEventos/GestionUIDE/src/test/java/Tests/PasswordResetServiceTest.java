package Tests;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import tics.uide.gestionuide.dto.LoginDto;
import tics.uide.gestionuide.dto.UsuarioRegistroDto;
import tics.uide.gestionuide.exception.BadRequestException;
import tics.uide.gestionuide.model.PasswordResetToken;
import tics.uide.gestionuide.model.Usuario;
import tics.uide.gestionuide.repository.PasswordResetTokenRepository;
import tics.uide.gestionuide.service.EmailService;
import tics.uide.gestionuide.service.PasswordResetService;
import tics.uide.gestionuide.service.UsuarioService;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración del flujo de recuperación de contraseña.
 * EmailService se mockea para (a) no enviar SMTP real y (b) capturar el token crudo del enlace.
 */
@SpringBootTest(classes = tics.uide.gestionuide.GestionUIDE.class)
public class PasswordResetServiceTest {

    @Autowired private PasswordResetService passwordResetService;
    @Autowired private PasswordResetTokenRepository tokenRepo;
    @Autowired private UsuarioService usuarioService;

    @MockBean private EmailService emailService;

    private static final String SUF = UUID.randomUUID().toString().substring(0, 6);
    private static final String PWD_INICIAL = "Uide2024*";

    private Usuario nuevoUsuario(String tag) {
        String s = tag + UUID.randomUUID().toString().substring(0, 6);
        Usuario u = usuarioService.registrar(UsuarioRegistroDto.builder()
                .username(s).email(s + "@uide.edu.ec").password(PWD_INICIAL).confirmPassword(PWD_INICIAL)
                .nombre("Reset").apellido("Test").telefono("0990000000").build());
        // El reset no verifica el email; estos usuarios se marcan verificados para poder loguearse.
        usuarioService.marcarEmailVerificado(u.getId());
        return u;
    }

    /** Llama solicitar() y devuelve el token CRUDO capturado del enlace del email. */
    private String solicitarYToken(String correo) {
        Mockito.clearInvocations(emailService);
        passwordResetService.solicitar(correo);
        ArgumentCaptor<String> enlaceCap = ArgumentCaptor.forClass(String.class);
        Mockito.verify(emailService).enviarEnlaceRecuperacion(Mockito.anyString(), Mockito.anyString(), enlaceCap.capture());
        String enlace = enlaceCap.getValue();
        return enlace.substring(enlace.indexOf("token=") + "token=".length());
    }

    @Test
    void tokenValido_cambiaClave_loginNuevaOk_viejaFalla_tokenUsado() {
        Usuario u = nuevoUsuario("ok");
        String raw = solicitarYToken(u.getEmail());
        String nueva = "NuevaClave123";

        passwordResetService.restablecer(raw, nueva);

        // login con la nueva funciona
        assertNotNull(usuarioService.autenticar(
                LoginDto.builder().usernameOrEmail(u.getEmail()).password(nueva).build()));
        // login con la vieja falla
        assertThrows(BadRequestException.class, () -> usuarioService.autenticar(
                LoginDto.builder().usernameOrEmail(u.getEmail()).password(PWD_INICIAL).build()));
        // el token quedó usado -> no hay tokens sin usar del usuario
        assertTrue(tokenRepo.findByUsuario_IdAndUsadoFalse(u.getId()).isEmpty());
    }

    @Test
    void tokenExpirado_rechazado() {
        Usuario u = nuevoUsuario("exp");
        String raw = solicitarYToken(u.getEmail());

        // forzar expiración en BD
        PasswordResetToken t = tokenRepo.findByUsuario_IdAndUsadoFalse(u.getId()).get(0);
        t.setExpiraEn(new Date(System.currentTimeMillis() - 1000));
        tokenRepo.save(t);

        assertThrows(BadRequestException.class, () -> passwordResetService.restablecer(raw, "OtraClave123"));
    }

    @Test
    void tokenReusado_rechazado() {
        Usuario u = nuevoUsuario("reuse");
        String raw = solicitarYToken(u.getEmail());

        passwordResetService.restablecer(raw, "PrimeraClave123");   // 1er uso: ok
        assertThrows(BadRequestException.class,
                () -> passwordResetService.restablecer(raw, "SegundaClave123"));   // 2do uso: rechazado
    }

    @Test
    void emailInexistente_silencioso_noCreaToken() {
        Mockito.clearInvocations(emailService);
        long antes = tokenRepo.count();

        assertDoesNotThrow(() -> passwordResetService.solicitar("noexiste" + SUF + "@uide.edu.ec"));

        Mockito.verify(emailService, Mockito.never())
                .enviarEnlaceRecuperacion(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        assertEquals(antes, tokenRepo.count());   // no se creó ningún token
    }

    @Test
    void pedirNuevoToken_invalidaPrevios() {
        Usuario u = nuevoUsuario("inval");
        solicitarYToken(u.getEmail());   // token 1
        solicitarYToken(u.getEmail());   // token 2 -> invalida el 1

        // solo queda 1 token sin usar (el último)
        assertEquals(1, tokenRepo.findByUsuario_IdAndUsadoFalse(u.getId()).size());
    }
}
