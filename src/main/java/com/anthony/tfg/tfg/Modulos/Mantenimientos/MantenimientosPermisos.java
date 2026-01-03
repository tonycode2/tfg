package com.anthony.tfg.tfg.Modulos.Mantenimientos;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Permisos;
import com.anthony.tfg.tfg.Modulos.Interfaces.MantenimientoInterface;
import com.anthony.tfg.tfg.Repositorios.PermisosRepositorio;

@Service
public class MantenimientosPermisos implements MantenimientoInterface<Permisos>{

    private final PermisosRepositorio repo;

    public MantenimientosPermisos(PermisosRepositorio repo) {
        this.repo = repo;
    }

    public Permisos crear(Permisos entidad) {
        return repo.save(entidad);
    }

    public Permisos actualizar(Permisos entidad) {
        return repo.save(entidad);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

}
