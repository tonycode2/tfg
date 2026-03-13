package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;

@Service
public class ConsultasEmpleados implements ConsultaInterface<Empleados>{

    private final EmpleadosRepositorio repo;

    public ConsultasEmpleados(EmpleadosRepositorio repo) {
        this.repo = repo;
    }

    /** 
     * @param id
     * @return Empleados
     */
    public Empleados obtenerPorId(Long id) {
        Optional<Empleados> empleado = repo.findById(id);
        return empleado.orElse(null);
    }

    /** 
     * @return List<Empleados>
     */
    public List<Empleados> obtenerTodos() {
        return repo.findAll();
    }

}
