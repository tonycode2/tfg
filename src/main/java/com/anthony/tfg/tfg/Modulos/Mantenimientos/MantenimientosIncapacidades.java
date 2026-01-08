package com.anthony.tfg.tfg.Modulos.Mantenimientos;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Incapacidades;
import com.anthony.tfg.tfg.Modulos.Interfaces.MantenimientoInterface;
import com.anthony.tfg.tfg.Repositorios.IncapacidadesRepositorio;

@Service
public class MantenimientosIncapacidades implements MantenimientoInterface<Incapacidades> {

    private final IncapacidadesRepositorio repo;

    public MantenimientosIncapacidades(IncapacidadesRepositorio repo) {
        this.repo = repo;
    }

    public Incapacidades crear(Incapacidades entidad) {
        return repo.save(entidad);
    }

    public Incapacidades actualizar(Incapacidades entidad) {
        return repo.save(entidad);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}
