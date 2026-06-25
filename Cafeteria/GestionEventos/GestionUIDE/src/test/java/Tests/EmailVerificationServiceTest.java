package Tests;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.cafeteria.app.dto.LoginDto;
import com.cafeteria.app.dto.UsuarioRegistroDto;
import com.cafeteria.app.exception.BadRequestException;
import com.cafeteria.app.model.EmailVerificationToken;
import com.cafeteria.app.model.Usuario;
import com.cafeteria.app.repository.EmailVerificationTokenRepository;
import com.cafeteria.app.service.EmailService;
import com.cafeteria.app.service.EmailVerificationService;
import com.cafeteria.app.service.UsuarioService;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración de la verificación de correo (doble opt-in).
 * EmailService se mockea para no enviar SMTP real y capturar el token crudo del enlace.
 */
@SpringBootTest(classes = com.cafeteria.app.CafeteriaApp.class)
public class EmailVerificationServiceTest {

    @Autowired private EmailVerificationService emailVerificationService;
    @Autowired private EmailVerificationTokenRepository tokenRepo;
    @Autowired private UsuarioService usuarioService;

    @MockBean private EmailService emailService;

    private static final String PWD = "Uide2024*";

    private Usuario nuevoUsuario(String tag) {
        String s = tag + UUID.randomUUID().toString().substring(0, 6);
        return usuarioService.registrar(UsuarioRegistroDto.builder()
                .username(s).email(s + "@uide.edu.ec").password(PWD).confirmPassword(PWD)
                .nombre("Verif").apellido("Test").telefono("0990000000").build());
    }

    /** Dispara enviarVerificacion() y devuelve el token CRUDO capturado del enlace del email. */
    private String enviarYToken(Usuario u) {
        Mockito.clearInvocations(emailService);
        emailVerificationService.enviarVerificacion(u);
        ArgumentCaptor<String> enlaceCap = ArgumentCaptor.forClass(String.class);
        Mockito.verify(emailService).enviarEnlaceVerificacion(Mockito.anyString(), Mockito.anyString(), enlaceCap.capture());
        String enlace = enlaceCap.getValue();
        return enlace.substring(enlace.indexOf("token=") + "token=".length());
    }

    @Test
    void tokenValido_verifica_loginOk_tokenUsado() {
        Usuario u = nuevoUsuario("ok");
        // antes de verificar, el login está bloqueado
        assertThrows(BadRequestException.class, () -> usuarioService.autenticar(
                LoginDto.builder().usernameOrEmail(u.getEmail()).password(PWD).build()));

        String raw = enviarYToken(u);
        emailVerificationService.verificar(raw);

        // queda verificado en BD
        assertTrue(usuarioService.buscarPorId(u.getId()).getEmailVerificado());
        // ahora el login funciona
        assertNotNull(usuarioService.autenticar(
                LoginDto.builder().usernameOrEmail(u.getEmail()).password(PWD).build()));
        // el token quedó usado
        assertTrue(tokenRepo.findByUsuario_IdAndUsadoFalse(u.getId()).isEmpty());
    }

    @Test
    void loginNoVerificado_bloqueado() {
        Usuario u = nuevoUsuario("block");
        assertThrows(BadRequestException.class, () -> usuarioService.autenticar(
                LoginDto.builder().usernameOrEmail(u.getEmail()).password(PWD).build()));
    }

    @Test
    void loginUsuarioExistenteMigrado_ok() {
        // Simula un usuario YA existente al que la migración V3 marcó como verificado.
        Usuario u = nuevoUsuario("legacy");
        usuarioService.marcarEmailVerificado(u.getId());

        assertNotNull(usuarioService.autenticar(
                LoginDto.builder().usernameOrEmail(u.getEmail()).password(PWD).build()));
    }

    @Test
    void tokenExpirado_rechazado() {
        Usuario u = nuevoUsuario("exp");
        String raw = enviarYToken(u);

        EmailVerificationToken t = tokenRepo.findByUsuario_IdAndUsadoFalse(u.getId()).get(0);
        t.setExpiraEn(new Date(System.currentTimeMillis() - 1000));
        tokenRepo.save(t);

        assertThrows(BadRequestException.class, () -> emailVerificationService.verificar(raw));
    }

    @Test
    void tokenReusado_rechazado() {
        Usuario u = nuevoUsuario("reuse");
        String raw = enviarYToken(u);

        emailVerificationService.verificar(raw);   // 1er uso: ok
        assertThrows(BadRequestException.class, () -> emailVerificationService.verificar(raw));   // 2do uso: rechazado
    }

    @Test
    void reenviar_invalidaTokensPrevios() {
        Usuario u = nuevoUsuario("inval");
        enviarYToken(u);                              // token 1
        emailVerificationService.reenviar(u.getEmail());   // token 2 -> invalida el 1

        assertEquals(1, tokenRepo.findByUsuario_IdAndUsadoFalse(u.getId()).size());
    }
}
