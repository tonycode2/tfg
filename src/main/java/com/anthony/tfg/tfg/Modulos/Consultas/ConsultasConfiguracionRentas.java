package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<ConfiguracionRenta> obtenerTodos(Pageable pageable) {
        return repo.findAll(pageable);
    }

}
