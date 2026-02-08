package com.anthony.tfg.tfg.Modulos.Empleados.Integracion;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;

@SpringBootTest
@ActiveProfiles("test")
class EmpleadosRepositorioIntegrationTest {

    @Autowired
    private EmpleadosRepositorio repositorio;

    @Test
    void findByEstaActivoTrue_respondeLista() {
        assertNotNull(repositorio);
        assertNotNull(repositorio.findByEstaActivoTrue());
    }
}
