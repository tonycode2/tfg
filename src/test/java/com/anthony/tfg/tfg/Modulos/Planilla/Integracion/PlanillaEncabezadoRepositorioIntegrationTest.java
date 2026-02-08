package com.anthony.tfg.tfg.Modulos.Planilla.Integracion;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.anthony.tfg.tfg.Repositorios.PlanillaEncabezadoRepositorio;

@SpringBootTest
@ActiveProfiles("test")
class PlanillaEncabezadoRepositorioIntegrationTest {

    @Autowired
    private PlanillaEncabezadoRepositorio repositorio;

    @Test
    void findAll_respondeLista() {
        assertNotNull(repositorio);
        assertNotNull(repositorio.findAll());
    }
}
