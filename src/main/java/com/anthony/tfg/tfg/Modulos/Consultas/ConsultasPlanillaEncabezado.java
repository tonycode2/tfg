package com.anthony.tfg.tfg.Modulos.Consultas;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.PlanillaEncabezado;
import com.anthony.tfg.tfg.Entidades.Enums.TipoQuincena;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.PlanillaEncabezadoRepositorio;

@Service
public class ConsultasPlanillaEncabezado implements ConsultaInterface<PlanillaEncabezado>{

    private final PlanillaEncabezadoRepositorio repo;

    public ConsultasPlanillaEncabezado(PlanillaEncabezadoRepositorio repo) {
        this.repo = repo;
    }

    /** 
     * @param id
     * @return PlanillaEncabezado
     */
    public PlanillaEncabezado obtenerPorId(Long id) {
        Optional<PlanillaEncabezado> resultado = repo.findById(id);
        return resultado.orElse(null);
    }

    /** 
     * @return List<PlanillaEncabezado>
     */
    public List<PlanillaEncabezado> obtenerTodos() {
        return repo.findAll();
    }

    /** 
     * @param fechaInicioPeriodo
     * @param fechaFinPeriodo
     * @param tipoQuincena
     * @return boolean
     */
    public boolean existePlanillaParaPeriodo(LocalDate fechaInicioPeriodo,
                                            LocalDate fechaFinPeriodo,
                                            TipoQuincena tipoQuincena) {
        return repo.existsByFechaInicioPeriodoAndFechaFinPeriodoAndTipoQuincena(
            fechaInicioPeriodo,
            fechaFinPeriodo,
            tipoQuincena);
    }

}
