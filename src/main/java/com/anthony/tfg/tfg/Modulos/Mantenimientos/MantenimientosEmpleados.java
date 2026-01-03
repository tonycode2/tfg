package com.anthony.tfg.tfg.Modulos.Mantenimientos;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Modulos.Interfaces.MantenimientoInterface;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;

@Service
public class MantenimientosEmpleados implements MantenimientoInterface<Empleados>{

    private final EmpleadosRepositorio repo;

    public MantenimientosEmpleados(EmpleadosRepositorio repo) {
        this.repo = repo;
    }

    public Empleados crear(Empleados entidad) {
        return repo.save(entidad);
    }

    public Empleados actualizar(Empleados entidad) {
        return repo.save(entidad);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

}
