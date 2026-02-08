package com.anthony.tfg.tfg.Modulos.JornadaDiaria.Integracion;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.anthony.tfg.tfg.Repositorios.JornadaDiariaRepositorio;

@SpringBootTest
@ActiveProfiles("test")
class JornadaDiariaRepositorioIntegrationTest {

    @Autowired
    private JornadaDiariaRepositorio repositorio;

    @Test
    void findAll_respondeLista() {
        assertNotNull(repositorio);
        assertNotNull(repositorio.findAll());
    }
}
