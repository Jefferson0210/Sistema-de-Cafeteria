package com.cafeteria.app.service;

import java.io.File;
import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:cafeteria@uide.edu.ec}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    // Email simple (texto)
    public void enviarTexto(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar email: " + e.getMessage());
        }
    }

    // Email HTML
    public void enviarHtml(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar email: " + e.getMessage());
        }
    }

    // Email con archivo adjunto (PDF)
    public void enviarConAdjunto(String to, String subject, String body, File adjunto, String nombreAdjunto) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.addAttachment(nombreAdjunto, new FileSystemResource(adjunto));
            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar email con adjunto: " + e.getMessage());
        }
    }

    // ========== PLANTILLAS ==========

    public void enviarBienvenida(String email, String nombre) {
        String html = "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;background:#f9f9f9;padding:40px;'>"
            + "<div style='max-width:500px;margin:0 auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,0.1);'>"
            + "<div style='background:linear-gradient(135deg,#910048,#002D72);padding:30px;text-align:center;'>"
            + "<h1 style='color:#EAAA00;margin:0;font-size:24px;'>☕ Cafetería UIDE</h1></div>"
            + "<div style='padding:30px;'>"
            + "<h2 style='color:#333;'>¡Bienvenido, " + nombre + "!</h2>"
            + "<p style='color:#666;line-height:1.6;'>Tu cuenta ha sido creada exitosamente. Ahora puedes:</p>"
            + "<ul style='color:#666;line-height:2;'>"
            + "<li>Ver nuestro menú de platos ecuatorianos</li>"
            + "<li>Realizar pedidos desde tu celular</li>"
            + "<li>Reservar mesas</li>"
            + "<li>Guardar tus platos favoritos</li></ul>"
            + "<a href='" + frontendUrl + "' style='display:inline-block;background:#EAAA00;color:#333;text-decoration:none;padding:12px 30px;border-radius:8px;font-weight:bold;margin-top:15px;'>Ir al Menú</a>"
            + "</div>"
            + "<div style='background:#f5f5f5;padding:15px;text-align:center;font-size:12px;color:#999;'>"
            + "© 2026 Cafetería UIDE — Universidad Internacional del Ecuador</div>"
            + "</div></body></html>";
        enviarHtml(email, "¡Bienvenido a Cafetería UIDE!", html);
    }

    public void enviarEnlaceRecuperacion(String email, String nombre, String enlace) {
        String html = "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;background:#f9f9f9;padding:40px;'>"
            + "<div style='max-width:500px;margin:0 auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,0.1);'>"
            + "<div style='background:linear-gradient(135deg,#910048,#002D72);padding:30px;text-align:center;'>"
            + "<h1 style='color:#EAAA00;margin:0;font-size:24px;'>🔐 Restablecer Contraseña</h1></div>"
            + "<div style='padding:30px;text-align:center;'>"
            + "<p style='color:#666;'>Hola <strong>" + nombre + "</strong>, recibimos una solicitud para restablecer tu contraseña.</p>"
            + "<a href='" + enlace + "' style='display:inline-block;background:#EAAA00;color:#333;text-decoration:none;padding:12px 30px;border-radius:8px;font-weight:bold;margin:20px 0;'>Restablecer contraseña</a>"
            + "<p style='color:#999;font-size:13px;'>Este enlace expira en 1 hora y solo puede usarse una vez.<br>Si no solicitaste esto, ignora este email.</p>"
            + "</div></div></body></html>";
        enviarHtml(email, "Restablecer contraseña — Cafetería UIDE", html);
    }

    public void enviarEnlaceVerificacion(String email, String nombre, String enlace) {
        String html = "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;background:#f9f9f9;padding:40px;'>"
            + "<div style='max-width:500px;margin:0 auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,0.1);'>"
            + "<div style='background:linear-gradient(135deg,#910048,#002D72);padding:30px;text-align:center;'>"
            + "<h1 style='color:#EAAA00;margin:0;font-size:24px;'>✉️ Verifica tu correo</h1></div>"
            + "<div style='padding:30px;text-align:center;'>"
            + "<p style='color:#666;'>Hola <strong>" + nombre + "</strong>, confirma tu correo para activar tu cuenta de Cafetería UIDE.</p>"
            + "<a href='" + enlace + "' style='display:inline-block;background:#EAAA00;color:#333;text-decoration:none;padding:12px 30px;border-radius:8px;font-weight:bold;margin:20px 0;'>Verificar mi correo</a>"
            + "<p style='color:#999;font-size:13px;'>Este enlace expira en 24 horas.<br>Si no creaste esta cuenta, ignora este email.</p>"
            + "</div></div></body></html>";
        enviarHtml(email, "Verifica tu correo — Cafetería UIDE", html);
    }

    public void enviarFactura(String email, String nombre, String numFactura, File pdfFile) {
        String html = "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;background:#f9f9f9;padding:40px;'>"
            + "<div style='max-width:500px;margin:0 auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,0.1);'>"
            + "<div style='background:linear-gradient(135deg,#910048,#002D72);padding:30px;text-align:center;'>"
            + "<h1 style='color:#EAAA00;margin:0;font-size:24px;'>📄 Factura " + numFactura + "</h1></div>"
            + "<div style='padding:30px;'>"
            + "<p style='color:#666;'>Hola <strong>" + nombre + "</strong>,</p>"
            + "<p style='color:#666;line-height:1.6;'>Adjunto encontrarás tu factura <strong>" + numFactura + "</strong> de Cafetería UIDE.</p>"
            + "<p style='color:#666;'>¡Gracias por tu compra!</p>"
            + "</div>"
            + "<div style='background:#f5f5f5;padding:15px;text-align:center;font-size:12px;color:#999;'>"
            + "© 2026 Cafetería UIDE</div>"
            + "</div></body></html>";
        enviarConAdjunto(email, "Factura " + numFactura + " — Cafetería UIDE", html, pdfFile, numFactura + ".pdf");
    }
}
