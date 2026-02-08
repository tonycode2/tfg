package com.anthony.tfg.tfg.Modulos.Aguinaldo.Integracion;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.anthony.tfg.tfg.Repositorios.AguinaldosRepositorio;

@SpringBootTest
@ActiveProfiles("test")
class AguinaldosRepositorioIntegrationTest {

    @Autowired
    private AguinaldosRepositorio repositorio;

    @Test
    void findAll_retornaListaVacia() {
        assertNotNull(repositorio);
        assertTrue(repositorio.findAll().isEmpty());
    }
}
