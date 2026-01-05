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
    
    private String construirEmailHtml(String nombreCompleto, String username, String password) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { 
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; 
                        line-height: 1.6; 
                        color: #1f2937; 
                        background-color: #f3f4f6;
                        margin: 0;
                        padding: 0;
                    }
                    .container { 
                        max-width: 600px; 
                        margin: 40px auto; 
                        background-color: white;
                        border-radius: 8px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                        overflow: hidden;
                    }
                    .header { 
                        background: linear-gradient(135deg, #1e40af 0%%, #3b82f6 100%%); 
                        color: white; 
                        padding: 40px 20px; 
                        text-align: center; 
                    }
                    .logo {
                        width: 120px;
                        height: auto;
                        margin-bottom: 20px;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                        font-weight: 600;
                    }
                    .content { 
                        padding: 40px 30px; 
                    }
                    .credentials { 
                        background: linear-gradient(to right, #eff6ff 0%%, #dbeafe 100%%);
                        padding: 20px; 
                        border-left: 4px solid #3b82f6; 
                        margin: 25px 0;
                        border-radius: 4px;
                    }
                    .credential-item {
                        margin-bottom: 15px;
                    }
                    .credential-item:last-child {
                        margin-bottom: 0;
                    }
                    .credential-label { 
                        font-weight: 600; 
                        color: #6b7280;
                        font-size: 14px;
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                    }
                    .credential-value { 
                        font-size: 20px; 
                        color: #1e40af; 
                        font-family: 'Courier New', monospace;
                        font-weight: 600;
                        display: block;
                        margin-top: 5px;
                    }
                    .warning { 
                        background-color: #fef3c7; 
                        padding: 20px; 
                        border-left: 4px solid #f59e0b; 
                        margin: 25px 0;
                        border-radius: 4px;
                    }
                    .warning strong {
                        color: #92400e;
                    }
                    .steps {
                        background-color: #f9fafb;
                        padding: 20px;
                        border-radius: 4px;
                        margin: 25px 0;
                    }
                    .steps h3 {
                        margin-top: 0;
                        color: #1e40af;
                        font-size: 16px;
                    }
                    .steps ol {
                        margin: 10px 0;
                        padding-left: 20px;
                    }
                    .steps li {
                        margin: 8px 0;
                        color: #4b5563;
                    }
                    .footer { 
                        background-color: #f9fafb;
                        text-align: center; 
                        color: #6b7280; 
                        font-size: 12px; 
                        padding: 30px 20px;
                        border-top: 1px solid #e5e7eb;
                    }
                    .footer p {
                        margin: 5px 0;
                    }
                    .company-name {
                        color: #1e40af;
                        font-weight: 600;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <img src="https://i.imgur.com/placeholder.png" alt="Logo Sastrería Gerson Andre" class="logo">
                        <h1>Sistema de Gestión de RH</h1>
                        <p style="margin: 10px 0 0 0; opacity: 0.9;">Sastrería Gerson Andre</p>
                    </div>
                    
                    <div class="content">
                        <p style="font-size: 16px;">Hola <strong>%s</strong>,</p>
                        
                        <p style="color: #4b5563;">Se ha creado una cuenta de usuario para ti en el Sistema de Gestión de Recursos Humanos. A continuación encontrarás tus credenciales de acceso:</p>
                        
                        <div class="credentials">
                            <div class="credential-item">
                                <span class="credential-label">Usuario</span>
                                <span class="credential-value">%s</span>
                            </div>
                            <div class="credential-item">
                                <span class="credential-label">Contraseña Temporal</span>
                                <span class="credential-value">%s</span>
                            </div>
                        </div>
                        
                        <div class="warning">
                            <strong>⚠️ Importante:</strong> Por tu seguridad, deberás cambiar esta contraseña en tu primer inicio de sesión.
                        </div>
                        
                        <div class="steps">
                            <h3>¿Cómo acceder?</h3>
                            <ol>
                                <li>Ingresa al sistema con las credenciales proporcionadas</li>
                                <li>El sistema te redirigirá automáticamente para establecer una nueva contraseña</li>
                                <li>Elige una contraseña segura que solo tú conozcas</li>
                                <li>¡Listo! Ya podrás acceder al sistema con tu nueva contraseña</li>
                            </ol>
                        </div>
                        
                        <p style="color: #4b5563;">Si tienes alguna duda o problema para acceder, contacta al departamento de Recursos Humanos.</p>
                    </div>
                    
                    <div class="footer">
                        <p class="company-name">Sastrería Gerson Andre</p>
                        <p>Este es un correo automático, por favor no respondas a este mensaje.</p>
                        <p>&copy; 2026 Sistema de Gestión de RH - Todos los derechos reservados</p>
                    </div>
                </div>
            </body>
            </html>
            """, nombreCompleto, username, password);
    }
}
