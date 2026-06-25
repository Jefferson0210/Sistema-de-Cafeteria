package Tests;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;

import tics.uide.gestionuide.service.EmailService;

import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * El correo de bienvenida debe usar la frontendUrl CONFIGURABLE (no el localhost:3000 hardcodeado).
 * Inyecta una frontendUrl de prueba vía @TestPropertySource y verifica el HTML del correo.
 */
@SpringBootTest(classes = tics.uide.gestionuide.GestionUIDE.class)
@TestPropertySource(properties = "app.frontend.url=https://cafe.example.com")
public class BienvenidaEmailTest {

    @Autowired private EmailService emailService;
    @MockBean private JavaMailSender mailSender;

    @Test
    void correoBienvenida_usaFrontendUrlConfigurable_noLocalhost() throws Exception {
        Mockito.when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        emailService.enviarBienvenida("nuevo@uide.edu.ec", "Juan");

        ArgumentCaptor<MimeMessage> cap = ArgumentCaptor.forClass(MimeMessage.class);
        Mockito.verify(mailSender).send(cap.capture());
        String html = extraerTexto(cap.getValue().getContent());

        assertTrue(html.contains("https://cafe.example.com"),
                "el correo de bienvenida debe usar la frontendUrl configurada");
        assertFalse(html.contains("localhost:3000"),
                "el correo no debe contener el localhost:3000 hardcodeado");
    }

    /** Extrae el texto del contenido, recorriendo multiparts anidados (el helper usa multipart). */
    private String extraerTexto(Object content) throws Exception {
        if (content instanceof String) return (String) content;
        if (content instanceof Multipart) {
            Multipart mp = (Multipart) content;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mp.getCount(); i++) {
                sb.append(extraerTexto(mp.getBodyPart(i).getContent()));
            }
            return sb.toString();
        }
        return String.valueOf(content);
    }
}
