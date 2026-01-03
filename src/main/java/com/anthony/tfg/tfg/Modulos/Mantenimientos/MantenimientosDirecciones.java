package com.anthony.tfg.tfg.Modulos.Mantenimientos;

import com.anthony.tfg.tfg.Entidades.Direccion;
import com.anthony.tfg.tfg.Modulos.Interfaces.MantenimientoInterface;
import com.anthony.tfg.tfg.Repositorios.DireccionRepositorio;

public class MantenimientosDirecciones implements MantenimientoInterface<Direccion>{

    private final DireccionRepositorio repo;

    public MantenimientosDirecciones(DireccionRepositorio repo) {
        this.repo = repo;
    }

    public Direccion crear(Direccion entidad) {
        return repo.save(entidad);
    }

    public Direccion actualizar(Direccion entidad) {
        return repo.save(entidad);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

}
