package com.anthony.tfg.tfg.Modulos.Empleados.Servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
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

    /** 
     * @param destinatario
     * @param nombreCompleto
     * @param puntuacionFinal
     * @param periodoEvaluado
     * @param observaciones
     * @param planDeMejora
     */
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

    /** 
     * @param nombreCompleto
     * @param puntuacionFinal
     * @param periodoEvaluado
     * @param observaciones
     * @param planDeMejora
     * @return String
     */
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

    /**
     * Envia colilla de pago con PDF adjunto.
     * @param destinatario parametro de entrada de la operacion.
     * @param nombreCompleto parametro de entrada de la operacion.
     * @param pdfBytes parametro de entrada de la operacion.
     * @param nombreArchivo parametro de entrada de la operacion.
     */
    public void enviarColillaPago(String destinatario, String nombreCompleto, byte[] pdfBytes, String nombreArchivo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject("Colilla de Pago - Sistema de Gestión de RH");
            
            String contenidoHtml = construirEmailColillaHtml(nombreCompleto);
            helper.setText(contenidoHtml, true);
            
            // Adjuntar el PDF
            helper.addAttachment(nombreArchivo, new ByteArrayResource(pdfBytes));
            
            mailSender.send(message);
            log.info("Colilla de pago enviada exitosamente a: {}", destinatario);
            
        } catch (Exception e) {
            log.error("Error al enviar colilla de pago a: {}", destinatario, e);
            throw new RuntimeException("No se pudo enviar la colilla de pago: " + e.getMessage());
        }
    }

    /**
     * Ejecuta la logica principal de construirEmailColillaHtml.
     * @param nombreCompleto parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private String construirEmailColillaHtml(String nombreCompleto) {
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
                    .info-box { background-color: white; padding: 15px; border-left: 4px solid #3b82f6; margin: 15px 0; }
                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Colilla de Pago</h1>
                        <p>Sastrería Gerson Andre</p>
                    </div>
                    
                    <div class="content">
                        <p>Hola <strong>%s</strong>,</p>
                        
                        <p>Adjuntamos tu colilla de pago correspondiente a la planilla de este período.</p>
                        
                        <div class="info-box">
                            <p><strong>📎 Archivo adjunto:</strong> colilla-pago.pdf</p>
                            <p>Por favor, descarga y guarda el archivo para tu registro.</p>
                        </div>
                        
                        <p>Si tienes dudas sobre tu colilla o encuentras alguna discrepancia, contacta al departamento de Recursos Humanos.</p>
                    </div>
                    
                    <div class="footer">
                        <p><strong>Sastrería Gerson Andre</strong></p>
                        <p>Este es un correo automático, por favor no respondas a este mensaje.</p>
                        <p>&copy; 2026 Sistema de Gestión de RH - Todos los derechos reservados</p>
                    </div>
                </div>
            </body>
            </html>
            """, nombreCompleto);
    }

    /**
     * Envía notificación de respuesta de incapacidad (aprobada o rechazada por RH).
     * @param destinatario parametro de entrada de la operacion.
     * @param nombreCompleto parametro de entrada de la operacion.
     * @param tipoIncapacidad parametro de entrada de la operacion.
     * @param aprobado parametro de entrada de la operacion.
     * @param comentarios parametro de entrada de la operacion.
     * @param diasTotales parametro de entrada de la operacion.
     * @param fechaInicio parametro de entrada de la operacion.
     * @param fechaFin parametro de entrada de la operacion.
     */
    public void enviarNotificacionIncapacidad(String destinatario, String nombreCompleto, String tipoIncapacidad, 
            Boolean aprobado, String comentarios, Integer diasTotales, String fechaInicio, String fechaFin) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            
            String asunto = aprobado 
                ? "Respuesta de Recursos Humanos - Incapacidad APROBADA" 
                : "Respuesta de Recursos Humanos - Incapacidad RECHAZADA";
            helper.setSubject(asunto);

            String contenidoHtml = construirEmailIncapacidadHtml(nombreCompleto, tipoIncapacidad, aprobado, 
                    comentarios, diasTotales, fechaInicio, fechaFin);
            helper.setText(contenidoHtml, true);

            mailSender.send(message);
            log.info("Email de incapacidad enviado exitosamente a: {}", destinatario);

        } catch (Exception e) {
            log.error("Error al enviar email de incapacidad a: {}", destinatario, e);
            throw new RuntimeException("No se pudo enviar el email de incapacidad: " + e.getMessage());
        }
    }

    /**
     * Construye el HTML del correo de notificación de incapacidad.
     * @param nombreCompleto parametro de entrada de la operacion.
     * @param tipoIncapacidad parametro de entrada de la operacion.
     * @param aprobado parametro de entrada de la operacion.
     * @param comentarios parametro de entrada de la operacion.
     * @param diasTotales parametro de entrada de la operacion.
     * @param fechaInicio parametro de entrada de la operacion.
     * @param fechaFin parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private String construirEmailIncapacidadHtml(String nombreCompleto, String tipoIncapacidad, Boolean aprobado,
            String comentarios, Integer diasTotales, String fechaInicio, String fechaFin) {
        String estado = aprobado ? "APROBADA" : "RECHAZADA";
        String colorEstado = aprobado ? "#10b981" : "#ef4444";
        String iconoEstado = aprobado ? "✓" : "✗";
        
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
                    .estado-box { background-color: %s; color: white; padding: 20px; text-align: center; margin: 20px 0; border-radius: 8px; }
                    .estado-icono { font-size: 48px; margin-bottom: 10px; }
                    .estado-titulo { font-size: 24px; font-weight: bold; }
                    .detalles { background-color: white; padding: 15px; border-left: 4px solid #3b82f6; margin: 15px 0; }
                    .detalle-item { margin: 10px 0; }
                    .detalle-label { font-weight: bold; color: #666; }
                    .detalle-valor { color: #1e40af; }
                    .comentarios-box { background-color: white; padding: 15px; border-left: 4px solid #f59e0b; margin: 15px 0; }
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
                        
                        <div class="estado-box">
                            <div class="estado-icono">%s</div>
                            <div class="estado-titulo">Incapacidad %s</div>
                        </div>
                        
                        <p>Te informamos que tu solicitud de incapacidad ha sido revisada por el departamento de Recursos Humanos. A continuación se detalla la respuesta:</p>
                        
                        <div class="detalles">
                            <div class="detalle-item">
                                <span class="detalle-label">Tipo de incapacidad:</span>
                                <span class="detalle-valor">%s</span>
                            </div>
                            <div class="detalle-item">
                                <span class="detalle-label">Fecha de inicio:</span>
                                <span class="detalle-valor">%s</span>
                            </div>
                            <div class="detalle-item">
                                <span class="detalle-label">Fecha de finalización:</span>
                                <span class="detalle-valor">%s</span>
                            </div>
                            <div class="detalle-item">
                                <span class="detalle-label">Días totales:</span>
                                <span class="detalle-valor">%d</span>
                            </div>
                        </div>
                        
                        %s
                        
                        <p>Si tienes alguna pregunta sobre esta resolución, por favor contacta al departamento de Recursos Humanos.</p>
                    </div>
                    
                    <div class="footer">
                        <p><strong>Sastrería Gerson Andre</strong></p>
                        <p>Este es un correo automático, por favor no respondas a este mensaje.</p>
                        <p>&copy; 2026 Sistema de Gestión de RH - Todos los derechos reservados</p>
                    </div>
                </div>
            </body>
            </html>
            """, colorEstado, nombreCompleto, iconoEstado, estado, tipoIncapacidad, fechaInicio, fechaFin, 
            diasTotales != null ? diasTotales : 0,
            comentarios != null && !comentarios.isEmpty() 
                ? String.format("<div class=\"comentarios-box\"><strong>Comentarios:</strong><br/>%s</div>", comentarios)
                : "");
    }

    /**
     * Envía notificación de respuesta de permiso o vacaciones (aprobado o rechazado).
     * @param destinatario parametro de entrada de la operacion.
     * @param nombreCompleto parametro de entrada de la operacion.
     * @param tipoPermiso parametro de entrada de la operacion.
     * @param aprobado parametro de entrada de la operacion.
     * @param comentarios parametro de entrada de la operacion.
     * @param diasTotales parametro de entrada de la operacion.
     * @param fechaInicio parametro de entrada de la operacion.
     * @param fechaFin parametro de entrada de la operacion.
     */
    public void enviarNotificacionPermiso(String destinatario, String nombreCompleto, String tipoPermiso, 
            Boolean aprobado, String comentarios, Integer diasTotales, String fechaInicio, String fechaFin) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            
            String asunto = aprobado 
                ? "Respuesta de Recursos Humanos - Permiso APROBADO" 
                : "Respuesta de Recursos Humanos - Permiso RECHAZADO";
            helper.setSubject(asunto);

            String contenidoHtml = construirEmailPermisoHtml(nombreCompleto, tipoPermiso, aprobado, 
                    comentarios, diasTotales, fechaInicio, fechaFin);
            helper.setText(contenidoHtml, true);

            mailSender.send(message);
            log.info("Email de permiso enviado exitosamente a: {}", destinatario);

        } catch (Exception e) {
            log.error("Error al enviar email de permiso a: {}", destinatario, e);
            throw new RuntimeException("No se pudo enviar el email de permiso: " + e.getMessage());
        }
    }

    /**
     * Construye el HTML del correo de notificación de permiso o vacaciones.
     * @param nombreCompleto parametro de entrada de la operacion.
     * @param tipoPermiso parametro de entrada de la operacion.
     * @param aprobado parametro de entrada de la operacion.
     * @param comentarios parametro de entrada de la operacion.
     * @param diasTotales parametro de entrada de la operacion.
     * @param fechaInicio parametro de entrada de la operacion.
     * @param fechaFin parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private String construirEmailPermisoHtml(String nombreCompleto, String tipoPermiso, Boolean aprobado,
            String comentarios, Integer diasTotales, String fechaInicio, String fechaFin) {
        String estado = aprobado ? "APROBADO" : "RECHAZADO";
        String colorEstado = aprobado ? "#10b981" : "#ef4444";
        String iconoEstado = aprobado ? "✓" : "✗";
        
        // Determinar si es vacaciones para mensajes específicos
        boolean esVacaciones = tipoPermiso != null && tipoPermiso.equalsIgnoreCase("VACACIONES");
        String tipoSolicitud = esVacaciones ? "Vacaciones" : "Permiso";
        
        // Construir secciones opcionales fuera del String.format para evitar conflictos
        String seccionComentarios = (comentarios != null && !comentarios.isEmpty()) 
            ? "<div class=\"comentarios-box\"><strong>Comentarios de RH:</strong><br/>" + comentarios + "</div>"
            : "";
        
        String seccionVacaciones = (aprobado && esVacaciones)
            ? "<div class=\"info-box\"><strong>ℹ️ Importante:</strong> El saldo de vacaciones ha sido descontado y actualizado en el sistema.</div>"
            : "";
        
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
                    .estado-box { background-color: %s; color: white; padding: 20px; text-align: center; margin: 20px 0; border-radius: 8px; }
                    .estado-icono { font-size: 48px; margin-bottom: 10px; }
                    .estado-titulo { font-size: 24px; font-weight: bold; }
                    .detalles { background-color: white; padding: 15px; border-left: 4px solid #3b82f6; margin: 15px 0; }
                    .detalle-item { margin: 10px 0; }
                    .detalle-label { font-weight: bold; color: #666; }
                    .detalle-valor { color: #1e40af; }
                    .comentarios-box { background-color: white; padding: 15px; border-left: 4px solid #f59e0b; margin: 15px 0; }
                    .info-box { background-color: #dbeafe; padding: 15px; border-left: 4px solid #3b82f6; margin: 15px 0; border-radius: 4px; }
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
                        
                        <div class="estado-box">
                            <div class="estado-icono">%s</div>
                            <div class="estado-titulo">%s</div>
                        </div>
                        
                        <p>Te informamos que tu solicitud de %s ha sido revisada por el departamento de Recursos Humanos. A continuación se detalla la respuesta:</p>
                        
                        <div class="detalles">
                            <div class="detalle-item">
                                <span class="detalle-label">Tipo de %s:</span>
                                <span class="detalle-valor">%s</span>
                            </div>
                            <div class="detalle-item">
                                <span class="detalle-label">Fecha de inicio:</span>
                                <span class="detalle-valor">%s</span>
                            </div>
                            <div class="detalle-item">
                                <span class="detalle-label">Fecha de finalización:</span>
                                <span class="detalle-valor">%s</span>
                            </div>
                            <div class="detalle-item">
                                <span class="detalle-label">Días totales:</span>
                                <span class="detalle-valor">%s</span>
                            </div>
                        </div>
                        
                        %s
                        
                        %s
                        
                        <p>Si tienes alguna pregunta sobre esta resolución, por favor contacta al departamento de Recursos Humanos.</p>
                    </div>
                    
                    <div class="footer">
                        <p><strong>Sastrería Gerson Andre</strong></p>
                        <p>Este es un correo automático, por favor no respondas a este mensaje.</p>
                        <p>&copy; 2026 Sistema de Gestión de RH - Todos los derechos reservados</p>
                    </div>
                </div>
            </body>
            </html>
            """, 
            colorEstado, 
            nombreCompleto, 
            iconoEstado, 
            estado + " " + tipoSolicitud,
            tipoSolicitud,
            tipoSolicitud.toLowerCase(),
            tipoPermiso != null ? tipoPermiso : "N/A",
            fechaInicio,
            fechaFin, 
            diasTotales != null ? String.valueOf(diasTotales) : "0",
            seccionComentarios,
            seccionVacaciones);
    }

    /**
     * Envía notificación de aprobación o rechazo de solicitud de horas extra.
     * @param destinatario parametro de entrada de la operacion.
     * @param nombreCompleto parametro de entrada de la operacion.
     * @param aprobado parametro de entrada de la operacion.
     * @param comentarios parametro de entrada de la operacion.
     * @param cantidadHoras parametro de entrada de la operacion.
     * @param fechaSolicitud parametro de entrada de la operacion.
     */
    public void enviarNotificacionHorasExtra(String destinatario, String nombreCompleto, Boolean aprobado, 
            String comentarios, Integer cantidadHoras, String fechaSolicitud) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            
            String asunto = aprobado 
                ? "Respuesta de Recursos Humanos - Horas Extra APROBADAS" 
                : "Respuesta de Recursos Humanos - Horas Extra RECHAZADAS";
            helper.setSubject(asunto);

            String contenidoHtml = construirEmailHorasExtraHtml(nombreCompleto, aprobado, 
                    comentarios, cantidadHoras, fechaSolicitud);
            helper.setText(contenidoHtml, true);

            mailSender.send(message);
            log.info("Email de horas extra enviado exitosamente a: {}", destinatario);

        } catch (Exception e) {
            log.error("Error al enviar email de horas extra a: {}", destinatario, e);
            throw new RuntimeException("No se pudo enviar el email de horas extra: " + e.getMessage());
        }
    }

    /**
     * Construye el HTML del correo de notificación de horas extra.
     * @param nombreCompleto parametro de entrada de la operacion.
     * @param aprobado parametro de entrada de la operacion.
     * @param comentarios parametro de entrada de la operacion.
     * @param cantidadHoras parametro de entrada de la operacion.
     * @param fechaSolicitud parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private String construirEmailHorasExtraHtml(String nombreCompleto, Boolean aprobado,
            String comentarios, Integer cantidadHoras, String fechaSolicitud) {
        String estado = aprobado ? "APROBADAS" : "RECHAZADAS";
        String colorEstado = aprobado ? "#10b981" : "#ef4444";
        String iconoEstado = aprobado ? "✓" : "✗";
        
        // Construir sección de comentarios si existen
        String seccionComentarios = (comentarios != null && !comentarios.isEmpty()) 
            ? "<div class=\"comentarios-box\"><strong>Comentarios de RH/Jefe:</strong><br/>" + comentarios + "</div>"
            : "";
        
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
                    .estado-box { background-color: %s; color: white; padding: 20px; text-align: center; margin: 20px 0; border-radius: 8px; }
                    .estado-icono { font-size: 48px; margin-bottom: 10px; }
                    .estado-titulo { font-size: 24px; font-weight: bold; }
                    .detalles { background-color: white; padding: 15px; border-left: 4px solid #3b82f6; margin: 15px 0; }
                    .detalle-item { margin: 10px 0; }
                    .detalle-label { font-weight: bold; color: #666; }
                    .detalle-valor { color: #1e40af; }
                    .comentarios-box { background-color: white; padding: 15px; border-left: 4px solid #f59e0b; margin: 15px 0; }
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
                        
                        <div class="estado-box">
                            <div class="estado-icono">%s</div>
                            <div class="estado-titulo">Horas Extra %s</div>
                        </div>
                        
                        <p>Te informamos que tu solicitud de horas extra ha sido revisada. A continuación se detalla la respuesta:</p>
                        
                        <div class="detalles">
                            <div class="detalle-item">
                                <span class="detalle-label">Cantidad de horas:</span>
                                <span class="detalle-valor">%d horas</span>
                            </div>
                            <div class="detalle-item">
                                <span class="detalle-label">Fecha de solicitud:</span>
                                <span class="detalle-valor">%s</span>
                            </div>
                            <div class="detalle-item">
                                <span class="detalle-label">Estado:</span>
                                <span class="detalle-valor"><strong>%s</strong></span>
                            </div>
                        </div>
                        
                        %s
                        
                        <p>Si tienes alguna pregunta sobre esta resolución, por favor contacta al departamento de Recursos Humanos.</p>
                    </div>
                    
                    <div class="footer">
                        <p><strong>Sastrería Gerson Andre</strong></p>
                        <p>Este es un correo automático, por favor no respondas a este mensaje.</p>
                        <p>&copy; 2026 Sistema de Gestión de RH - Todos los derechos reservados</p>
                    </div>
                </div>
            </body>
            </html>
            """, colorEstado, nombreCompleto, iconoEstado, estado, 
            cantidadHoras != null ? cantidadHoras : 0, 
            fechaSolicitud != null ? fechaSolicitud : "N/A",
            estado,
            seccionComentarios);
    }
}
