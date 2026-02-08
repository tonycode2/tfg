package com.anthony.tfg.tfg.Modulos.Incapacidad.Integracion;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.anthony.tfg.tfg.Repositorios.IncapacidadesRepositorio;

@SpringBootTest
@ActiveProfiles("test")
class IncapacidadesRepositorioIntegrationTest {

    @Autowired
    private IncapacidadesRepositorio repositorio;

    @Test
    void findAll_respondeLista() {
        assertNotNull(repositorio);
        assertNotNull(repositorio.findAll());
    }
}
