package com.anthony.tfg.tfg.Repositorios;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.anthony.tfg.tfg.DTOs.Respuesta.EmpleadoEvaluacionResumenDTO;

@SpringBootTest
@ActiveProfiles("test")
public class EvaluacionDeDesempenoRepositorioTest {

    @Autowired
    EvaluacionDeDesempenoRepositorio repositorio;

    @Test
    public void queryReturnsListAndDoesNotFail() {
        assertNotNull(repositorio);
        List<EmpleadoEvaluacionResumenDTO> resumen = repositorio.findResumenPorDepartamento(-1L);
        assertNotNull(resumen);
        assertTrue(resumen.isEmpty());
    }
}
