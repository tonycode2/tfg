package com.anthony.tfg.tfg.Modulos.JefesDepartamento.Integracion;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.anthony.tfg.tfg.Repositorios.JefesDepartamentoRepositorio;

@SpringBootTest
@ActiveProfiles("test")
class JefesDepartamentoRepositorioIntegrationTest {

    @Autowired
    private JefesDepartamentoRepositorio repositorio;

    @Test
    void findAll_respondeLista() {
        assertNotNull(repositorio);
        assertNotNull(repositorio.findAll());
    }
}
