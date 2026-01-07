package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Permisos;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.PermisosRepositorio;

@Service
public class ConsultasPermisos implements ConsultaInterface<Permisos>{

    private final PermisosRepositorio repo;

    public ConsultasPermisos(PermisosRepositorio repo) {
        this.repo = repo;
    }

    public Permisos obtenerPorId(Long id) {
        Optional<Permisos> permiso = repo.findById(id);
        return permiso.orElse(null);
    }

    public List<Permisos> obtenerTodos() {
        return repo.findAll();
    }
    
}
