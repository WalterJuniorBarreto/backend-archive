package com.ecommerce.api_geek_store.service.notification;

import com.ecommerce.api_geek_store.domain.model.Order;
import com.ecommerce.api_geek_store.domain.model.OrderItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Value("${mail.from.address}")
    private String fromEmail;

    @Value("${application.frontend.url}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void send(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom(fromEmail);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Fallo al enviar email simple a {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendOrderConfirmation(String to, Order order) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setText(buildEmailContent(order), true);
            helper.setTo(to);
            helper.setSubject("Confirmación de Compra - Pedido #" + order.getId() + " | ARCHIVE.");
            helper.setFrom(fromEmail);

            mailSender.send(mimeMessage);
            log.info("Confirmación de orden enviada a: {}", to);

        } catch (MessagingException e) {
            log.error("Error al enviar confirmación de orden: {}", e.getMessage());
        }
    }

    // --- LÓGICA DE CORREO DE ESTADO (ENVIADO/ENTREGADO) ---
    @Async
    public void sendOrderStatusUpdate(String to, Order order) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String estado = order.getEstado().toUpperCase();
            String titulo = "Actualización de Pedido";
            String mensaje = "El estado de tu pedido ha cambiado.";
            String colorEstado = "#000000";

            switch (estado) {
                case "ENVIADO" -> {
                    titulo = "¡Tu pedido está en camino! 🚚";
                    mensaje = "Tu paquete ha salido de nuestro almacén.";
                    colorEstado = "#2563eb"; // Azul
                }
                case "ENTREGADO" -> {
                    titulo = "¡Paquete Entregado! 🎉";
                    mensaje = "Tu pedido ha sido entregado. ¡Disfruta tu compra!";
                    colorEstado = "#16a34a"; // Verde
                }
                case "CANCELADO" -> {
                    titulo = "Pedido Cancelado ✕";
                    mensaje = "Tu pedido ha sido cancelado.";
                    colorEstado = "#dc2626"; // Rojo
                }
            }

            // Tracking HTML
            String trackingHtml = "";
            if ("ENVIADO".equals(estado) && order.getTrackingNumber() != null) {
                trackingHtml = String.format("""
                    <div style="margin: 20px 0; padding: 15px; border: 1px dashed #000; text-align: center;">
                        <p style="font-size: 10px; font-weight: bold; text-transform: uppercase; color: #666; margin: 0;">Courier</p>
                        <p style="margin: 5px 0 10px; font-weight: bold;">%s</p>
                        <p style="font-size: 10px; font-weight: bold; text-transform: uppercase; color: #666; margin: 0;">Código de Seguimiento</p>
                        <p style="font-family: monospace; font-size: 16px; font-weight: bold; margin: 5px 0 0;">%s</p>
                    </div>
                """, order.getCourierName() != null ? order.getCourierName() : "Agencia", order.getTrackingNumber());
            }

            String enlaceWeb = frontendUrl + "/profile/orders";

            String htmlContent = String.format("""
                <div style="font-family: 'Helvetica', Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 2px solid #000; color: #000;">
                   <div style="padding: 30px; background-color: #fff;">
                        <h1 style="margin: 0 0 20px; font-size: 24px; letter-spacing: -1px; text-transform: uppercase;">ARCHIVE.</h1>
                        
                        <div style="border-left: 4px solid %s; padding-left: 15px; margin-bottom: 20px;">
                            <h2 style="margin: 0; font-size: 20px; text-transform: uppercase;">%s</h2>
                            <p style="margin: 5px 0 0; color: #666;">%s</p>
                        </div>
                        
                        %s
                        
                        <div style="text-align: center; margin-top: 40px;">
                            <a href="%s" style="display: inline-block; background-color: #000; color: #fff; padding: 15px 30px; text-decoration: none; font-weight: bold; text-transform: uppercase; font-size: 12px; letter-spacing: 1px;">
                                Rastrear en la Web
                            </a>
                        </div>
                   </div>
                </div>
                """, colorEstado, titulo, mensaje, trackingHtml, enlaceWeb);

            helper.setText(htmlContent, true);
            helper.setTo(to);
            helper.setSubject(titulo + " | Orden #" + order.getId());
            helper.setFrom(fromEmail);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("Error enviando correo de estado: {}", e.getMessage());
        }
    }

    @Async
    public void sendVerificationEmail(String to, String name, String token) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String link = frontendUrl + "/auth/confirm-account?token=" + token;

            String htmlContent = String.format("""
            <div style="font-family: 'Helvetica', sans-serif; max-width: 600px; margin: 0 auto; border: 2px solid #000; text-align: center; padding: 40px 20px;">
                <h1 style="font-size: 28px; margin-bottom: 10px; letter-spacing: -1px;">ARCHIVE.</h1>
                <p style="font-size: 14px; text-transform: uppercase; font-weight: bold; margin-bottom: 30px; color: #666;">Verificación de Cuenta</p>
                
                <p style="margin-bottom: 30px;">Hola <strong>%s</strong>, para acceder a tu cuenta, confirma tu correo.</p>
                
                <a href="%s" style="display: inline-block; background-color: #000; color: #fff; padding: 15px 30px; text-decoration: none; font-weight: bold; text-transform: uppercase; font-size: 12px;">
                    Confirmar Cuenta
                </a>
            </div>
            """, name, link);

            helper.setText(htmlContent, true);
            helper.setTo(to);
            helper.setSubject("Activa tu cuenta | ARCHIVE.");
            helper.setFrom(fromEmail);
            mailSender.send(mimeMessage);

        } catch (Exception e) {
            log.error("Error enviando verificación: {}", e.getMessage());
        }
    }
    @Async
    public void sendRecoveryCodeEmail(String to, String name, String code) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlContent = String.format("""
                <div style="font-family: 'Helvetica', Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 2px solid #000; text-align: center; padding: 40px 20px;">
                    <h1 style="font-size: 24px; text-transform: uppercase; margin-bottom: 20px; letter-spacing: 1px;">Recuperar Contraseña</h1>
                    <p style="color: #666; margin-bottom: 30px; font-size: 14px;">Hola <strong>%s</strong>, usa el siguiente código para restablecer tu acceso:</p>
                    
                    <div style="background-color: #f4f4f5; border: 2px dashed #000; padding: 15px 30px; display: inline-block; margin-bottom: 30px;">
                        <span style="font-family: monospace; font-size: 36px; font-weight: 900; letter-spacing: 8px; color: #000;">%s</span>
                    </div>
                    
                    <p style="font-size: 11px; color: #999; text-transform: uppercase; font-weight: bold;">Este código expira en 15 minutos</p>
                    <p style="font-size: 11px; color: #999;">Si no solicitaste esto, ignora este mensaje.</p>
                </div>
                """, name, code);

            helper.setText(htmlContent, true);
            helper.setTo(to);
            helper.setSubject("Código de Recuperación: " + code + " | ARCHIVE.");
            helper.setFrom(fromEmail);

            mailSender.send(mimeMessage);
            log.info("Código de recuperación enviado a {}", to);

        } catch (Exception e) {
            log.error("Error enviando código de recuperación: {}", e.getMessage());
        }
    }
    // --- MÉTODO PRIVADO CORREGIDO PARA GENERAR EL HTML REAL ---
    private String buildEmailContent(Order order) {
        StringBuilder itemsHtml = new StringBuilder();

        // 1. Construir las filas de la tabla con los productos
        for (OrderItem item : order.getItems()) {
            BigDecimal subtotal = item.getPrecioUnitario().multiply(new BigDecimal(item.getCantidad()));

            // Inyectamos fila por fila
            itemsHtml.append(String.format("""
                <tr>
                    <td style="padding: 12px 0; border-bottom: 1px solid #e5e5e5; font-size: 13px;">
                        <span style="display: block; font-weight: bold; text-transform: uppercase; color: #000;">%s</span>
                        <span style="font-size: 11px; color: #666; text-transform: uppercase;">
                            %s | Talla: %s | Cant: %d
                        </span>
                    </td>
                    <td style="padding: 12px 0; border-bottom: 1px solid #e5e5e5; text-align: right; font-weight: bold; font-size: 13px; color: #000;">
                        S/ %.2f
                    </td>
                </tr>
            """,
                    item.getProduct(),
                    item.getColor() != null ? item.getColor() : "-",
                    item.getTalla() != null ? item.getTalla() : "-",
                    item.getCantidad(),
                    subtotal));
        }

        // 2. Información de pago (Yape vs Tarjeta)
        String metodoPagoInfo = "Tarjeta de Crédito/Débito";
        if ("YAPE_QR".equals(order.getMetodoPago())) {
            metodoPagoInfo = "Yape / Plin (Validación Manual)";
        }

        // 3. Estructura completa del correo (HTML Brutalista)
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f4f4f5; -webkit-font-smoothing: antialiased;">
                <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                    <tr>
                        <td align="center" style="padding: 40px 0;">
                            <div style="max-width: 600px; width: 100%%; background-color: #ffffff; border: 2px solid #000000; text-align: left;">
                                
                                <div style="background-color: #000000; color: #ffffff; padding: 25px; text-align: center;">
                                    <h1 style="margin: 0; font-size: 28px; letter-spacing: 2px; text-transform: uppercase; font-weight: 900;">ARCHIVE.</h1>
                                </div>

                                <div style="padding: 40px 30px;">
                                    <p style="font-size: 16px; font-weight: bold; text-transform: uppercase; margin-top: 0; color: #000;">
                                        Hola, %s
                                    </p>
                                    <p style="color: #666; font-size: 14px; line-height: 1.6; margin-bottom: 30px;">
                                        Hemos recibido tu pedido correctamente. Aquí tienes el resumen de tu compra.
                                    </p>

                                    <div style="background-color: #f9fafb; padding: 20px; border: 1px solid #e5e5e5; margin-bottom: 30px; display: flex; justify-content: space-between;">
                                        <div>
                                            <p style="margin: 0; font-size: 10px; text-transform: uppercase; color: #999; font-weight: bold; letter-spacing: 1px;">Orden</p>
                                            <p style="margin: 5px 0 0; font-size: 18px; font-weight: bold; font-family: monospace; color: #000;">#%d</p>
                                        </div>
                                        <div style="text-align: right;">
                                            <p style="margin: 0; font-size: 10px; text-transform: uppercase; color: #999; font-weight: bold; letter-spacing: 1px;">Método</p>
                                            <p style="margin: 5px 0 0; font-size: 12px; font-weight: bold; color: #000;">%s</p>
                                        </div>
                                    </div>

                                    <table style="width: 100%%; border-collapse: collapse; margin-bottom: 30px;">
                                        <thead>
                                            <tr>
                                                <th style="text-align: left; padding-bottom: 10px; border-bottom: 2px solid #000; text-transform: uppercase; font-size: 11px; color: #999; letter-spacing: 1px;">Producto</th>
                                                <th style="text-align: right; padding-bottom: 10px; border-bottom: 2px solid #000; text-transform: uppercase; font-size: 11px; color: #999; letter-spacing: 1px;">Subtotal</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            %s  </tbody>
                                        <tfoot>
                                            <tr>
                                                <td style="padding-top: 20px; font-weight: bold; text-transform: uppercase; text-align: right; font-size: 14px;">Total Pagado</td>
                                                <td style="padding-top: 20px; font-weight: 900; font-size: 22px; text-align: right; letter-spacing: -1px;">S/ %.2f</td>
                                            </tr>
                                        </tfoot>
                                    </table>

                                    <div style="text-align: center; margin-top: 40px; padding-top: 20px; border-top: 1px dashed #e5e5e5;">
                                        <a href="%s/profile/orders" style="display: inline-block; background-color: #000000; color: #ffffff; padding: 16px 35px; text-decoration: none; font-weight: bold; text-transform: uppercase; font-size: 12px; letter-spacing: 2px; transition: background 0.3s;">
                                            Ver Estado del Pedido
                                        </a>
                                    </div>
                                </div>

                                <div style="background-color: #f4f4f5; color: #999; text-align: center; padding: 20px; font-size: 10px; border-top: 1px solid #e5e5e5; text-transform: uppercase; letter-spacing: 1px;">
                                    &copy; 2026 ARCHIVE PERU.
                                </div>
                            </div>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
        """,
                order.getUser().getNombre(),
                order.getId(),
                metodoPagoInfo,
                itemsHtml.toString(),
                order.getTotal(),
                frontendUrl);
    }
}