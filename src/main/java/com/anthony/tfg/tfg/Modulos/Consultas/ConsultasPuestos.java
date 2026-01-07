package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.List;
import java.util.Optional;

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

    public List<Puestos> obtenerTodos() {
        return repo.findAll();
    }

}
