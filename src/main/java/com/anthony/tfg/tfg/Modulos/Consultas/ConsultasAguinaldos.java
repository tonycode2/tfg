package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Aguinaldos;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.AguinaldosRepositorio;

@Service
public class ConsultasAguinaldos implements  ConsultaInterface<Aguinaldos>{

    private final AguinaldosRepositorio repo;

    public ConsultasAguinaldos(AguinaldosRepositorio repo) {
        this.repo = repo;
    }

    public Aguinaldos obtenerPorId(Long id) {
        Optional<Aguinaldos> aguinaldo = repo.findById(id);
        return aguinaldo.orElse(null);
    }

    public List<Aguinaldos> obtenerTodos() {
        return repo.findAll();
    }
}
