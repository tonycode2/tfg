package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.HorasExtra;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.HorasExtraRepositorio;

@Service
public class ConsultasHorasExtras implements ConsultaInterface<HorasExtra>{

    private final HorasExtraRepositorio repo;

    public ConsultasHorasExtras(HorasExtraRepositorio repo) {
        this.repo = repo;
    } 


    public HorasExtra obtenerPorId(Long id) {
        Optional<HorasExtra> horasExtra = repo.findById(id);
        return horasExtra.orElse(null);
    }

    public List<HorasExtra> obtenerTodos() {
        return repo.findAll();
    }

}
