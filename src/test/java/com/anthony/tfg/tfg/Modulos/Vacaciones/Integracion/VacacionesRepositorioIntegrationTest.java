package com.anthony.tfg.tfg.Modulos.Vacaciones.Integracion;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;

@SpringBootTest
@ActiveProfiles("test")
class VacacionesRepositorioIntegrationTest {

    @Autowired
    private EmpleadosRepositorio empleadosRepositorio;

    @Test
    void findByEstaActivoTrue_respondeLista() {
        assertNotNull(empleadosRepositorio);
        assertNotNull(empleadosRepositorio.findByEstaActivoTrue());
    }
}
