package com.anthony.tfg.tfg.Modulos.Mantenimientos;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Asistencia;
import com.anthony.tfg.tfg.Modulos.Interfaces.MantenimientoInterface;
import com.anthony.tfg.tfg.Repositorios.AsistenciaRepositorio;

@Service
public class MantenimientosAsistencia implements MantenimientoInterface<Asistencia>{

    private final AsistenciaRepositorio repo;

    public MantenimientosAsistencia(AsistenciaRepositorio repo) {
        this.repo = repo;
    }

    public Asistencia crear(Asistencia entidad) {
        return repo.save(entidad);
    }

    public Asistencia actualizar(Asistencia entidad) {
        return repo.save(entidad);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

}
