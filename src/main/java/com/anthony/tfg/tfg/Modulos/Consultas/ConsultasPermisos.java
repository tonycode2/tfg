package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<Permisos> obtenerTodos(Pageable pageable) {
        return repo.findAll(pageable);
    }
    
}
