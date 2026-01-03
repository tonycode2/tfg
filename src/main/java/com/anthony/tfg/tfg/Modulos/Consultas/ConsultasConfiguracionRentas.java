package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.ConfiguracionRenta;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.ConfiguracionRentaRepositorio;

@Service
public class ConsultasConfiguracionRentas implements ConsultaInterface<ConfiguracionRenta>{

    private final ConfiguracionRentaRepositorio repo;

    public ConsultasConfiguracionRentas(ConfiguracionRentaRepositorio repo) {
        this.repo = repo;
    }

    public ConfiguracionRenta obtenerPorId(Long id) {
        Optional<ConfiguracionRenta> resultado = repo.findById(id);
        return resultado.orElse(null);
    }

    public List<ConfiguracionRenta> obtenerTodos() {
        return repo.findAll();
    }

}
