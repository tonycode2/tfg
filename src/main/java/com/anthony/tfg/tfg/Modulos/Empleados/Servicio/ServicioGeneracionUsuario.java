package com.anthony.tfg.tfg.Modulos.Empleados.Servicio;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaCredencialesDTO;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosEmpleados;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.Role;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.User;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioGeneracionUsuario {
    
    @Autowired
    private ConsultasEmpleados consultasEmpleados;
    
    @Autowired
    private MantenimientosEmpleados mantenimientosEmpleados;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ServicioEmail servicioEmail;
    
    private static final String CARACTERES_PASSWORD = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
    private static final int LONGITUD_PASSWORD = 12;
    
    /**
     * Genera informacion requerida por el proceso.
     * @param idEmpleado parametro de entrada de la operacion.
     * @param role parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    @Transactional
    public RespuestaCredencialesDTO generarUsuarioParaEmpleado(Long idEmpleado, Role role) {
        log.info("Iniciando generación de usuario para empleado ID: {}", idEmpleado);
        
        // Obtener empleado
        Empleados empleado = consultasEmpleados.obtenerPorId(idEmpleado);
        
        // Validar que no tenga usuario ya
        if (empleado.getUsuario() != null) {
            throw new IllegalStateException("El empleado ya tiene un usuario asignado");
        }
        
        // Generar username único
        String username = generarUsernameUnico(empleado.getNombre(), empleado.getPrimerApellido());
        log.info("Username generado: {}", username);
        
        // Generar password aleatorio
        String password = generarPasswordAleatorio();
        
        // Crear usuario
        User nuevoUsuario = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role)
                .passwordChangeRequired(true)
                .build();
        
        User usuarioGuardado = userRepository.save(nuevoUsuario);
        log.info("Usuario creado con ID: {}", usuarioGuardado.getId());
        
        // Asignar usuario al empleado
        empleado.setUsuario(usuarioGuardado);
        mantenimientosEmpleados.actualizar(empleado);
        log.info("Usuario asignado al empleado");
        
        // Enviar email con credenciales
        String nombreCompleto = String.format("%s %s %s", 
            empleado.getNombre(), 
            empleado.getPrimerApellido(), 
            empleado.getSegundoApellido());
        
        try {
            servicioEmail.enviarCredenciales(
                empleado.getCorreoPersonal(),
                nombreCompleto,
                username,
                password
            );
            log.info("Email enviado a: {}", empleado.getCorreoPersonal());
        } catch (Exception e) {
            log.error("Error al enviar email, pero usuario creado correctamente", e);
        }
        
        return new RespuestaCredencialesDTO(
            username, 
            password, 
            empleado.getCorreoPersonal(),
            nombreCompleto
        );
    }
    
    /**
     * Genera informacion requerida por el proceso.
     * @param nombre parametro de entrada de la operacion.
     * @param apellido parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private String generarUsernameUnico(String nombre, String apellido) {
        // Formato base: primera letra nombre + apellido
        String baseUsername = (nombre.charAt(0) + apellido).toLowerCase()
            .replaceAll("[^a-z0-9]", "");
        
        String username = baseUsername;
        int contador = 1;
        
        // Verificar si existe y agregar número si es necesario
        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + contador;
            contador++;
        }
        
        return username;
    }
    
    /**
     * Genera informacion requerida por el proceso.
     * @return resultado de la operacion.
     */
    private String generarPasswordAleatorio() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(LONGITUD_PASSWORD);
        
        for (int i = 0; i < LONGITUD_PASSWORD; i++) {
            int index = random.nextInt(CARACTERES_PASSWORD.length());
            password.append(CARACTERES_PASSWORD.charAt(index));
        }
        
        return password.toString();
    }
}
