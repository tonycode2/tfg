package com.anthony.tfg.tfg.Modulos.Reportes.Integracion;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.anthony.tfg.tfg.Modulos.Reportes.Servicio.ServicioReportes;

@SpringBootTest
@ActiveProfiles("test")
class ServicioReportesIntegrationTest {

    @Autowired
    private ServicioReportes servicioReportes;

    @Test
    void generarReporteVacaciones_retornaReporteVacio() {
        var reporte = servicioReportes.generarReporteVacaciones();
        assertNotNull(reporte);
    }
}
