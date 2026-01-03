package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.PlanillaEncabezado;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.PlanillaEncabezadoRepositorio;

@Service
public class ConsultasPlanillaEncabezado implements ConsultaInterface<PlanillaEncabezado>{

    private final PlanillaEncabezadoRepositorio repo;

    public ConsultasPlanillaEncabezado(PlanillaEncabezadoRepositorio repo) {
        this.repo = repo;
    }

    public PlanillaEncabezado obtenerPorId(Long id) {
        Optional<PlanillaEncabezado> resultado = repo.findById(id);
        return resultado.orElse(null);
    }

    public List<PlanillaEncabezado> obtenerTodos() {
        return repo.findAll();
    }

}
