package com.anthony.tfg.tfg.Modulos.Mantenimientos;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.JornadaDiaria;
import com.anthony.tfg.tfg.Modulos.Interfaces.MantenimientoInterface;
import com.anthony.tfg.tfg.Repositorios.JornadaDiariaRepositorio;

@Service
public class MantenimientosJornadaDiaria implements MantenimientoInterface<JornadaDiaria>{

    private final JornadaDiariaRepositorio repo;

    public MantenimientosJornadaDiaria(JornadaDiariaRepositorio repo) {
        this.repo = repo;
    }

    public JornadaDiaria crear(JornadaDiaria entidad) {
        return repo.save(entidad);
    }

    public JornadaDiaria actualizar(JornadaDiaria entidad) {
        return repo.save(entidad);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}
