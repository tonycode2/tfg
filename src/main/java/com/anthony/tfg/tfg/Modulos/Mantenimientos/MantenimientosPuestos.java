package com.anthony.tfg.tfg.Modulos.Mantenimientos;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Puestos;
import com.anthony.tfg.tfg.Modulos.Interfaces.MantenimientoInterface;
import com.anthony.tfg.tfg.Repositorios.PuestosRepositorio;

@Service
public class MantenimientosPuestos implements MantenimientoInterface<Puestos>{

    private final PuestosRepositorio repo;

    public MantenimientosPuestos(PuestosRepositorio repo) {
        this.repo = repo;
    }

    public Puestos crear(Puestos entidad) {
        return repo.save(entidad);
    }

    public Puestos actualizar(Puestos entidad) {
        return repo.save(entidad);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

}
