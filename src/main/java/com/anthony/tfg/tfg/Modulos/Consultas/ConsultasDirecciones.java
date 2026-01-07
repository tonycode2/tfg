package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Direccion;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.DireccionRepositorio;

@Service
public class ConsultasDirecciones implements ConsultaInterface<Direccion>{
    private final DireccionRepositorio repo;

    public ConsultasDirecciones(DireccionRepositorio repo) {
        this.repo = repo;
    }
    
    public Direccion obtenerPorId(Long id) {
        Optional<Direccion> direccion = repo.findById(id);
        return direccion.orElse(null);
    }

    public Page<Direccion> obtenerTodos(Pageable pageable) {
        return repo.findAll(pageable);
    }
    
}
