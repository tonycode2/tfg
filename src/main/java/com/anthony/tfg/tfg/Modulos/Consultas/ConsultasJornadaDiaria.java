package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.JornadaDiaria;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.JornadaDiariaRepositorio;

@Service
public class ConsultasJornadaDiaria implements ConsultaInterface<JornadaDiaria>{

    private final JornadaDiariaRepositorio repo;

    public ConsultasJornadaDiaria(JornadaDiariaRepositorio repo) {
        this.repo = repo;
    }

    public JornadaDiaria obtenerPorId(Long id) {
        Optional<JornadaDiaria> jornada = repo.findById(id);
        return jornada.orElse(null);
    }

    public List<JornadaDiaria> obtenerTodos() {
        return repo.findAll();
    }
}
