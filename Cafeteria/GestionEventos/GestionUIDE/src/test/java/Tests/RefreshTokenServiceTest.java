package Tests;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import tics.uide.gestionuide.dto.UsuarioRegistroDto;
import tics.uide.gestionuide.exception.BadRequestException;
import tics.uide.gestionuide.model.RefreshToken;
import tics.uide.gestionuide.model.Usuario;
import tics.uide.gestionuide.repository.RefreshTokenRepository;
import tics.uide.gestionuide.service.EmailService;
import tics.uide.gestionuide.service.PasswordResetService;
import tics.uide.gestionuide.service.RefreshTokenService;
import tics.uide.gestionuide.service.UsuarioService;
import tics.uide.gestionuide.util.Tokens;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración de refresh tokens: rotación, revocación, detección de reuso,
 * y revocación de todas las sesiones tras un reset de contraseña.
 */
@SpringBootTest(classes = tics.uide.gestionuide.GestionUIDE.class)
public class RefreshTokenServiceTest {

    @Autowired private RefreshTokenService refreshTokenService;
    @Autowired private RefreshTokenRepository tokenRepo;
    @Autowired private UsuarioService usuarioService;
    @Autowired private PasswordResetService passwordResetService;

    @MockBean private EmailService emailService;   // para el flujo de reset (captura del enlace) y evitar SMTP

    private static final String PWD = "Uide2024*";

    private Usuario nuevoUsuario(String tag) {
        String s = tag + UUID.randomUUID().toString().substring(0, 6);
        return usuarioService.registrar(UsuarioRegistroDto.builder()
                .username(s).email(s + "@uide.edu.ec").password(PWD).confirmPassword(PWD)
                .nombre("Refresh").apellido("Test").telefono("0990000000").build());
    }

    private boolean estaRevocado(String raw) {
        return tokenRepo.findByTokenHash(Tokens.sha256(raw)).orElseThrow().isRevocado();
    }

    @Test
    void rotar_viejoRevocado_nuevoValido() {
        Usuario u = nuevoUsuario("rot");
        String raw1 = refreshTokenService.emitir(u);

        RefreshTokenService.Rotacion r = refreshTokenService.rotar(raw1);

        assertTrue(estaRevocado(raw1), "el refresh viejo debe quedar revocado");
        assertFalse(estaRevocado(r.nuevoRefresh), "el refresh nuevo debe ser válido");
    }

    @Test
    void refreshExpirado_rechazado() {
        Usuario u = nuevoUsuario("exp");
        String raw = refreshTokenService.emitir(u);

        RefreshToken t = tokenRepo.findByTokenHash(Tokens.sha256(raw)).orElseThrow();
        t.setExpiraEn(new Date(System.currentTimeMillis() - 1000));
        tokenRepo.save(t);

        assertThrows(BadRequestException.class, () -> refreshTokenService.rotar(raw));
    }

    @Test
    void refreshRevocadoPorLogout_rechazado() {
        Usuario u = nuevoUsuario("logout");
        String raw = refreshTokenService.emitir(u);

        refreshTokenService.revocar(raw);   // logout

        assertThrows(BadRequestException.class, () -> refreshTokenService.rotar(raw));
    }

    @Test
    void reusoDeRefreshRevocado_revocaTodos() {
        Usuario u = nuevoUsuario("reuse");
        String raw1 = refreshTokenService.emitir(u);
        refreshTokenService.emitir(u);                 // segunda sesión (raw2)
        refreshTokenService.rotar(raw1);               // raw1 -> revocado, emite raw3

        // reuso de raw1 (ya revocado) => detección de robo => revoca TODOS
        assertThrows(BadRequestException.class, () -> refreshTokenService.rotar(raw1));
        assertTrue(tokenRepo.findByUsuario_IdAndRevocadoFalse(u.getId()).isEmpty(),
                "el reuso debe revocar todas las sesiones del usuario");
    }

    @Test
    void revocarTodos_invalidaTodasLasSesiones() {
        Usuario u = nuevoUsuario("all");
        String raw1 = refreshTokenService.emitir(u);
        refreshTokenService.emitir(u);

        refreshTokenService.revocarTodos(u.getId());

        assertTrue(tokenRepo.findByUsuario_IdAndRevocadoFalse(u.getId()).isEmpty());
        assertThrows(BadRequestException.class, () -> refreshTokenService.rotar(raw1));
    }

    @Test
    void trasResetPassword_refreshPreviosRevocados() {
        Usuario u = nuevoUsuario("reset");
        refreshTokenService.emitir(u);   // sesión activa

        // Flujo de reset: solicitar (captura el token del email mockeado) + restablecer
        Mockito.clearInvocations(emailService);
        passwordResetService.solicitar(u.getEmail());
        ArgumentCaptor<String> enlaceCap = ArgumentCaptor.forClass(String.class);
        Mockito.verify(emailService).enviarEnlaceRecuperacion(Mockito.anyString(), Mockito.anyString(), enlaceCap.capture());
        String enlace = enlaceCap.getValue();
        String resetToken = enlace.substring(enlace.indexOf("token=") + "token=".length());

        passwordResetService.restablecer(resetToken, "NuevaClave123");

        // tras el reset, las sesiones (refresh tokens) previas del usuario quedan revocadas
        assertTrue(tokenRepo.findByUsuario_IdAndRevocadoFalse(u.getId()).isEmpty());
    }
}
