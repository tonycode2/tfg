package com.anthony.tfg.tfg.Modulos.Seguridad.jwt;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import com.anthony.tfg.tfg.Modulos.Seguridad.user.Role;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.User;

class JwtServiceTest {

    /** 
     * @throws Exception
     */
    @Test
    void getToken_generaJwtFirmado() throws Exception {
        JwtService service = new JwtService();
        Field field = JwtService.class.getDeclaredField("SECRET_KEY");
        field.setAccessible(true);
        field.set(service, "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=");

        User user = User.builder()
                .id(1L)
                .username("usuario")
                .role(Role.EMPLEADO)
                .build();

        String token = service.getToken(user);

        assertNotNull(token);
        assertTrue(token.contains("."));
    }
}
