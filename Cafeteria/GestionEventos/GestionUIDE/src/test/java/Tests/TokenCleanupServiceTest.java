package Tests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.cafeteria.app.dto.UsuarioRegistroDto;
import com.cafeteria.app.model.EmailVerificationToken;
import com.cafeteria.app.model.PasswordResetToken;
import com.cafeteria.app.model.RefreshToken;
import com.cafeteria.app.model.Usuario;
import com.cafeteria.app.repository.EmailVerificationTokenRepository;
import com.cafeteria.app.repository.PasswordResetTokenRepository;
import com.cafeteria.app.repository.RefreshTokenRepository;
import com.cafeteria.app.service.TokenCleanupService;
import com.cafeteria.app.service.UsuarioService;
import com.cafeteria.app.util.Tokens;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Limpieza de tokens caducados. grace-days=1: cutoff = ahora - 1 día.
 * Verifica que se borran los caducados y permanecen los vigentes y los RECIENTES
 * (incluido el refresh REVOCADO reciente, que preserva la detección de reuso).
 */
@SpringBootTest(classes = com.cafeteria.app.CafeteriaApp.class)
@TestPropertySource(properties = {
        "app.token-cleanup.enabled=true",
        "app.token-cleanup.grace-days=1"
})
public class TokenCleanupServiceTest {

    @Autowired private TokenCleanupService tokenCleanupService;
    @Autowired private PasswordResetTokenRepository prRepo;
    @Autowired private EmailVerificationTokenRepository evRepo;
    @Autowired private RefreshTokenRepository rfRepo;
    @Autowired private UsuarioService usuarioService;

    private static final String PWD = "Uide2024*";

    private Date hace2Dias() { return new Date(System.currentTimeMillis() - 2L * 24 * 60 * 60 * 1000); }
    private Date en1Hora()  { return new Date(System.currentTimeMillis() + 60L * 60 * 1000); }
    private String hash()   { return Tokens.sha256(UUID.randomUUID().toString()); }

    private Usuario nuevoUsuario() {
        String s = "clean" + UUID.randomUUID().toString().substring(0, 6);
        return usuarioService.registrar(UsuarioRegistroDto.builder()
                .username(s).email(s + "@uide.edu.ec").password(PWD).confirmPassword(PWD)
                .nombre("Clean").apellido("Test").telefono("0990000000").build());
    }

    @Test
    void purga_borraCaducados_dejaVigentesYRecientes() {
        Usuario u = nuevoUsuario();

        // password_reset_token
        String prCaducado = hash(), prVigente = hash();
        prRepo.save(PasswordResetToken.builder().usuario(u).tokenHash(prCaducado).usado(true).expiraEn(hace2Dias()).build());
        prRepo.save(PasswordResetToken.builder().usuario(u).tokenHash(prVigente).usado(false).expiraEn(en1Hora()).build());

        // email_verification_token
        String evCaducado = hash(), evVigente = hash();
        evRepo.save(EmailVerificationToken.builder().usuario(u).tokenHash(evCaducado).usado(true).expiraEn(hace2Dias()).build());
        evRepo.save(EmailVerificationToken.builder().usuario(u).tokenHash(evVigente).usado(false).expiraEn(en1Hora()).build());

        // refresh_token: caducado, vigente, y REVOCADO RECIENTE (debe sobrevivir -> detección de reuso)
        String rfCaducado = hash(), rfVigente = hash(), rfRevocadoReciente = hash();
        rfRepo.save(RefreshToken.builder().usuario(u).tokenHash(rfCaducado).revocado(true).expiraEn(hace2Dias()).build());
        rfRepo.save(RefreshToken.builder().usuario(u).tokenHash(rfVigente).revocado(false).expiraEn(en1Hora()).build());
        rfRepo.save(RefreshToken.builder().usuario(u).tokenHash(rfRevocadoReciente).revocado(true).expiraEn(en1Hora()).build());

        tokenCleanupService.purgar();

        // Caducados -> borrados
        assertTrue(prRepo.findByTokenHash(prCaducado).isEmpty(), "reset caducado debe borrarse");
        assertTrue(evRepo.findByTokenHash(evCaducado).isEmpty(), "verificacion caducado debe borrarse");
        assertTrue(rfRepo.findByTokenHash(rfCaducado).isEmpty(), "refresh caducado debe borrarse");

        // Vigentes -> permanecen
        assertTrue(prRepo.findByTokenHash(prVigente).isPresent(), "reset vigente debe permanecer");
        assertTrue(evRepo.findByTokenHash(evVigente).isPresent(), "verificacion vigente debe permanecer");
        assertTrue(rfRepo.findByTokenHash(rfVigente).isPresent(), "refresh vigente debe permanecer");

        // CLAVE: refresh revocado pero reciente (expiraEn futuro) sobrevive -> preserva detección de reuso
        assertTrue(rfRepo.findByTokenHash(rfRevocadoReciente).isPresent(),
                "refresh REVOCADO pero reciente debe sobrevivir (detección de robo)");
    }
}
