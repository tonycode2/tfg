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

    public PlanillaDetalle crear(PlanillaDetalle entidad) {
        return repo.save(entidad);
    }

    public PlanillaDetalle actualizar(PlanillaDetalle entidad) {
        return repo.save(entidad);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

}
