package com.anthony.tfg.tfg.Modulos.Mantenimientos;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.EvaluacionDeDesempeno;
import com.anthony.tfg.tfg.Modulos.Interfaces.MantenimientoInterface;
import com.anthony.tfg.tfg.Repositorios.EvaluacionDeDesempenoRepositorio;

@Service
public class MantenimientosEvaluacionDeDesempeno implements MantenimientoInterface<EvaluacionDeDesempeno>{

    private final EvaluacionDeDesempenoRepositorio repo;

    public MantenimientosEvaluacionDeDesempeno(EvaluacionDeDesempenoRepositorio repo) {
        this.repo = repo;
    }

    /** 
     * @param entidad
     * @return EvaluacionDeDesempeno
     */
    public EvaluacionDeDesempeno crear(EvaluacionDeDesempeno entidad) {
        return repo.save(entidad);
    }

    /** 
     * @param entidad
     * @return EvaluacionDeDesempeno
     */
    public EvaluacionDeDesempeno actualizar(EvaluacionDeDesempeno entidad) {
        return repo.save(entidad);
    }

    /** 
     * @param id
     */
    public void eliminar(Long id) {
        repo.deleteById(id);
    }
    
}
