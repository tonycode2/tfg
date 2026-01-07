package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Puestos;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.PuestosRepositorio;

@Service
public class ConsultasPuestos implements ConsultaInterface<Puestos>{

    private final PuestosRepositorio repo;

    public ConsultasPuestos(PuestosRepositorio repo) {
        this.repo = repo;
    }

    public Puestos obtenerPorId(Long id) {
        Optional<Puestos> puesto = repo.findById(id);
        return puesto.orElse(null);
    }

    public Page<Puestos> obtenerTodos(Pageable pageable) {
        return repo.findAll(pageable);
    }

}
