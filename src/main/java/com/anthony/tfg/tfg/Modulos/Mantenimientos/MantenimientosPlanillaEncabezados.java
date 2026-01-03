package com.anthony.tfg.tfg.Modulos.Mantenimientos;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.PlanillaEncabezado;
import com.anthony.tfg.tfg.Modulos.Interfaces.MantenimientoInterface;
import com.anthony.tfg.tfg.Repositorios.PlanillaEncabezadoRepositorio;

@Service
public class MantenimientosPlanillaEncabezados implements MantenimientoInterface<PlanillaEncabezado>{

    private final PlanillaEncabezadoRepositorio repo;

    public MantenimientosPlanillaEncabezados(PlanillaEncabezadoRepositorio repo) {
        this.repo = repo;
    }

    public PlanillaEncabezado crear(PlanillaEncabezado entidad) {
        return repo.save(entidad);
    }

    public PlanillaEncabezado actualizar(PlanillaEncabezado entidad) {
        return repo.save(entidad);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

}
