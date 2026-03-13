package com.anthony.tfg.tfg.Modulos.Empleados.Servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioEmail {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    /**
     * Envia la informacion solicitada.
     * @param destinatario parametro de entrada de la operacion.
     * @param nombreCompleto parametro de entrada de la operacion.
     * @param username parametro de entrada de la operacion.
     * @param password parametro de entrada de la operacion.
     */
    public void enviarCredenciales(String destinatario, String nombreCompleto, String username, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject("Credenciales de acceso al Sistema de Planillas");
            
            String contenidoHtml = construirEmailHtml(nombreCompleto, username, password);
            helper.setText(contenidoHtml, true);
            
            mailSender.send(message);
            log.info("Email enviado exitosamente a: {}", destinatario);
            
        } catch (Exception e) {
            log.error("Error al enviar email a: {}", destinatario, e);
            throw new RuntimeException("No se pudo enviar el email: " + e.getMessage());
        }
    }
    
    /**
     * Ejecuta la logica principal de construirEmailHtml.
     * @param nombreCompleto parametro de entrada de la operacion.
     * @param username parametro de entrada de la operacion.
     * @param password parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private String construirEmailHtml(String nombreCompleto, String username, String password) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #3b82f6; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 20px; margin: 20px 0; }
                    .credentials { background-color: white; padding: 15px; border-left: 4px solid #3b82f6; margin: 15px 0; }
                    .credential-label { font-weight: bold; color: #666; }
                    .credential-value { font-size: 18px; color: #1e40af; font-family: monospace; }
                    .warning { background-color: #fef3c7; padding: 15px; border-left: 4px solid #f59e0b; margin: 15px 0; }
                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Sistema de Gestión de RH</h1>
                        <p>Sastrería Gerson Andre</p>
                    </div>
                    
                    <div class="content">
                        <p>Hola <strong>%s</strong>,</p>
                        
                        <p>Se ha creado una cuenta de usuario para ti en el Sistema de Gestión de Recursos Humanos. A continuación encontrarás tus credenciales de acceso:</p>
                        
                        <div class="credentials">
                            <div style="margin-bottom: 10px;">
                                <span class="credential-label">Usuario:</span><br>
                                <span class="credential-value">%s</span>
                            </div>
                            <div>
                                <span class="credential-label">Contraseña temporal:</span><br>
                                <span class="credential-value">%s</span>
                            </div>
                        </div>
                        
                        <div class="warning">
                            <strong>⚠️ Importante:</strong> Por tu seguridad, deberás cambiar esta contraseña en tu primer inicio de sesión.
                        </div>
                        
                        <p><strong>¿Cómo acceder?</strong></p>
                        <ol>
                            <li>Ingresa al sistema con las credenciales proporcionadas</li>
                            <li>El sistema te redirigirá automáticamente para establecer una nueva contraseña</li>
                            <li>Elige una contraseña segura que solo tú conozcas</li>
                        </ol>
                        
                        <p>Si tienes alguna duda o problema para acceder, contacta al departamento de Recursos Humanos.</p>
                    </div>
                    
                    <div class="footer">
                        <p><strong>Sastrería Gerson Andre</strong></p>
                        <p>Este es un correo automático, por favor no respondas a este mensaje.</p>
                        <p>&copy; 2026 Sistema de Gestión de RH - Todos los derechos reservados</p>
                    </div>
                </div>
            </body>
            </html>
            """, nombreCompleto, username, password);
    }

    public void enviarNotificacionEvaluacion(String destinatario, String nombreCompleto, Double puntuacionFinal,
            String periodoEvaluado, String observaciones, String planDeMejora) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject(String.format("Nueva evaluación de desempeño - %s", periodoEvaluado));

            String contenidoHtml = construirEmailEvaluacionHtml(nombreCompleto, puntuacionFinal, periodoEvaluado, observaciones, planDeMejora);
            helper.setText(contenidoHtml, true);

            mailSender.send(message);
            log.info("Email de evaluación enviado exitosamente a: {}", destinatario);

        } catch (Exception e) {
            log.error("Error al enviar email de evaluación a: {}", destinatario, e);
            throw new RuntimeException("No se pudo enviar el email: " + e.getMessage());
        }
    }

    private String construirEmailEvaluacionHtml(String nombreCompleto, Double puntuacionFinal, String periodoEvaluado,
            String observaciones, String planDeMejora) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #3b82f6; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 20px; margin: 20px 0; }
                    .summary { background-color: white; padding: 15px; border-left: 4px solid #3b82f6; margin: 15px 0; }
                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Sistema de Gestión de RH</h1>
                        <p>Sastrería Gerson Andre</p>
                    </div>
                    <div class="content">
                        <p>Hola <strong>%s</strong>,</p>
                        <p>Se ha registrado una nueva evaluación de desempeño para el periodo <strong>%s</strong>.</p>

                        <div class="summary">
                            <p><strong>Puntuación final:</strong> %s</p>
                            <p><strong>Observaciones:</strong><br/>%s</p>
                            <p><strong>Plan de mejora:</strong><br/>%s</p>
                        </div>

                        <p>Si considera que hay algún error, contacte a su jefe o al departamento de Recursos Humanos.</p>
                    </div>
                    <div class="footer">
                        <p><strong>Sastrería Gerson Andre</strong></p>
                        <p>Este es un correo automático, por favor no responda a este mensaje.</p>
                    </div>
                </div>
            </body>
            </html>
            """, nombreCompleto, periodoEvaluado,
                puntuacionFinal != null ? String.format("%.2f", puntuacionFinal) : "N/A",
                observaciones != null ? observaciones : "-",
                planDeMejora != null ? planDeMejora : "-");
    }
}
