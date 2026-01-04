# Solución para Error de Autenticación de Email (Outlook)

## Problema
El sistema está intentando enviar correos electrónicos a través de Outlook, pero está fallando con el siguiente error:

```
535 5.7.139 Authentication unsuccessful, basic authentication is disabled
```

## Causa
Microsoft/Outlook ha **deshabilitado la autenticación básica** (username/password) para mejorar la seguridad. Ya no se puede usar la contraseña normal de la cuenta para aplicaciones de terceros.

## Soluciones

### Opción 1: Usar Contraseña de Aplicación (Recomendado para Outlook)

1. **Ir a la cuenta de Microsoft**:
   - Visita: https://account.microsoft.com/security
   - Inicia sesión con tu cuenta de Outlook

2. **Habilitar verificación en dos pasos** (si no está habilitado):
   - Ve a "Seguridad avanzada"
   - Activa "Verificación en dos pasos"

3. **Generar contraseña de aplicación**:
   - Ve a "Seguridad avanzada" → "Contraseñas de aplicación"
   - Crea una nueva contraseña de aplicación
   - Dale un nombre descriptivo (ej: "Sistema RH")
   - **COPIA LA CONTRASEÑA** (se muestra una sola vez)

4. **Actualizar application.properties**:
   ```properties
   # Reemplazar tu contraseña actual con la contraseña de aplicación generada
   spring.mail.password=xxxx-xxxx-xxxx-xxxx
   ```

5. **Reiniciar el backend**

### Opción 2: Usar Gmail (Alternativa más confiable)

Gmail es más consistente para SMTP. Si tienes una cuenta de Gmail:

1. **Habilitar verificación en dos pasos en Gmail**:
   - Ve a: https://myaccount.google.com/security
   - Activa "Verificación en dos pasos"

2. **Generar contraseña de aplicación**:
   - Ve a: https://myaccount.google.com/apppasswords
   - Selecciona "Correo" y "Otro"
   - Dale un nombre y genera
   - **COPIA LA CONTRASEÑA DE 16 CARACTERES**

3. **Actualizar application.properties**:
   ```properties
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=tucorreo@gmail.com
   spring.mail.password=xxxx xxxx xxxx xxxx
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   spring.mail.properties.mail.smtp.starttls.required=true
   spring.mail.properties.mail.smtp.connectiontimeout=10000
   spring.mail.properties.mail.smtp.timeout=10000
   spring.mail.properties.mail.smtp.writetimeout=10000
   ```

4. **Reiniciar el backend**

### Opción 3: Usar OAuth2 (Más complejo pero más seguro)

Esta opción requiere:
- Registrar la aplicación en Azure AD
- Obtener Client ID y Client Secret
- Implementar flujo OAuth2 en Spring Boot

**No recomendado para desarrollo inicial** - usar después en producción.

## Verificación

Después de aplicar cualquiera de las soluciones:

1. Reinicia el backend
2. Crea un empleado
3. Genera el usuario
4. Verifica que llegue el correo con las credenciales

## Estado Actual del Sistema

✅ **Usuario se crea correctamente** en base de datos  
✅ **Flag `passwordChangeRequired`** se establece correctamente  
✅ **Cambio de contraseña obligatorio** ya implementado en frontend/backend  
❌ **Email no se envía** (problema de autenticación)

## Próximos Pasos

1. Elegir Opción 1 o 2 arriba
2. Configurar la contraseña de aplicación
3. Actualizar `application.properties`
4. Reiniciar backend
5. Probar el flujo completo:
   - Login con usuario generado
   - Sistema debe redirigir a `/change-password`
   - Cambiar contraseña
   - Login nuevamente
   - Debe ir directo al dashboard

## Notas Importantes

- **NUNCA** uses tu contraseña real en `application.properties`
- Usa **contraseñas de aplicación** específicas que puedes revocar
- En producción, considera usar **variables de entorno** para las credenciales
- Outlook/Microsoft es menos confiable para SMTP que Gmail
