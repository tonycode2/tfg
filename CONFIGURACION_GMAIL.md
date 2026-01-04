# Configuración de Email para Envío de Emails

## Opción 1: Outlook (Recomendado - Más simple)

### Pasos para configurar Outlook SMTP:

#### 1. Crear/Usar cuenta de Outlook
- Crea una cuenta Outlook específica para la aplicación o usa una existente
- Ejemplo: `gersonandrerh@outlook.com`
- Ve a [outlook.com](https://outlook.com) para crear una cuenta gratis

#### 2. Configurar application.properties
Edita el archivo: `src/main/resources/application.properties`

Usa la siguiente configuración para **Outlook**:
```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=gersonandrerh@outlook.com
spring.mail.password=TU_CONTRASEÑA_OUTLOOK
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Ventaja de Outlook**: Para cuentas personales de Outlook, puedes usar tu contraseña normal sin necesidad de contraseñas de aplicación.

**Si tienes 2FA habilitado**: Genera una contraseña de aplicación en [account.microsoft.com/security](https://account.microsoft.com/security)

#### 3. Reiniciar la aplicación
Detén y reinicia el backend de Spring Boot para que tome los nuevos valores.

---

## Opción 2: Gmail (Alternativa)

### Pasos para configurar Gmail SMTP:

#### 1. Crear/Usar cuenta de Gmail
- Ejemplo: `gersonandre.rh@gmail.com`

#### 2. Habilitar verificación en 2 pasos
1. Ve a [myaccount.google.com](https://myaccount.google.com)
2. Selecciona "Seguridad" → "Verificación en 2 pasos"
3. Habilita la verificación en 2 pasos

#### 3. Generar App Password
1. En "Seguridad", selecciona "Contraseñas de aplicaciones"
2. Genera una nueva contraseña para "Sistema Planillas"
3. Copia la contraseña de 16 caracteres

#### 4. Configurar application.properties
Usa esta configuración para **Gmail**:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=gersonandre.rh@gmail.com
spring.mail.password=abcd efgh ijkl mnop
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Nota Gmail**: La contraseña DEBE ser la "App Password" de 16 caracteres, NO tu contraseña normal.

---

## Probar el envío de emails
1. En el sistema, ve a Mantenimientos → Empleados
2. Selecciona un empleado que NO tenga usuario
3. Click en "Generar Usuario"
4. Selecciona un rol
5. El sistema debería:
   - Crear el usuario
   - Enviar un email al correo del empleado
   - Mostrar las credenciales en pantalla

## Solución de problemas

### Error: "Authentication failed"
- Verifica que hayas habilitado la verificación en 2 pasos
- Verifica que la contraseña sea la "App Password" y no tu contraseña normal
- Verifica que no haya espacios al copiar la contraseña

### Error: "Connection timeout"
- Verifica tu conexión a internet
- Algunos firewalls corporativos bloquean el puerto 587
- Intenta usar el puerto 465 (SSL) en lugar de 587 (TLS)

### No llega el email
- Revisa la carpeta de SPAM del destinatario
- Verifica que el correo del empleado en la base de datos sea correcto
- Revisa los logs del backend para ver si hay errores

## Estructura del Email

El email que se envía incluye:
- Asunto: "Credenciales de acceso al Sistema de Planillas"
- Username generado
- Contraseña temporal
- Instrucciones de primer login
- Aviso de que debe cambiar la contraseña

## Seguridad

- La contraseña temporal tiene 12 caracteres con mayúsculas, minúsculas, números y símbolos
- El campo `passwordChangeRequired` está en `true` para forzar cambio en primer login
- Las contraseñas se almacenan encriptadas con BCrypt
- El email se envía por conexión TLS segura
