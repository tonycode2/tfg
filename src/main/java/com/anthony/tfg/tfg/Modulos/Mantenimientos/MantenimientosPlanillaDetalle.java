package com.anthony.tfg.tfg.Modulos.Mantenimientos;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.PlanillaDetalle;
import com.anthony.tfg.tfg.Modulos.Interfaces.MantenimientoInterface;
import com.anthony.tfg.tfg.Repositorios.PlanillaDetalleRepositorio;

@Service
public class MantenimientosPlanillaDetalle implements MantenimientoInterface<PlanillaDetalle>{

    private final PlanillaDetalleRepositorio repo;

    public MantenimientosPlanillaDetalle(PlanillaDetalleRepositorio repo) {
        this.repo = repo;
    }

    /** 
     * @param entidad
     * @return PlanillaDetalle
     */
    public PlanillaDetalle crear(PlanillaDetalle entidad) {
        return repo.save(entidad);
    }

    /** 
     * @param entidad
     * @return PlanillaDetalle
     */
    public PlanillaDetalle actualizar(PlanillaDetalle entidad) {
        return repo.save(entidad);
    }

    /** 
     * @param id
     */
    public void eliminar(Long id) {
        repo.deleteById(id);
    }

}
