package com.anthony.tfg.tfg.Modulos.Auxiliares.Integracion;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.anthony.tfg.tfg.Repositorios.DepartamentoRepositorio;

@SpringBootTest
@ActiveProfiles("test")
class DepartamentoRepositorioIntegrationTest {

    @Autowired
    private DepartamentoRepositorio repositorio;

    @Test
    void findAll_respondeLista() {
        assertNotNull(repositorio);
        assertNotNull(repositorio.findAll());
    }
}
