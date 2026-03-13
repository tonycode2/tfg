package com.anthony.tfg.tfg.Modulos.Mantenimientos;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Departamento;
import com.anthony.tfg.tfg.Modulos.Interfaces.MantenimientoInterface;
import com.anthony.tfg.tfg.Repositorios.DepartamentoRepositorio;

@Service
public class MantenimientosDepartamentos implements MantenimientoInterface<Departamento>{

    private final DepartamentoRepositorio departamentoRepositorio;

    public MantenimientosDepartamentos(DepartamentoRepositorio departamentoRepositorio) {
        this.departamentoRepositorio = departamentoRepositorio;
    }

    /** 
     * @param entidad
     * @return Departamento
     */
    public Departamento crear(Departamento entidad) {
        return departamentoRepositorio.save(entidad);
    }

    /** 
     * @param entidad
     * @return Departamento
     */
    public Departamento actualizar(Departamento entidad) {
        return departamentoRepositorio.save(entidad);
    }

    /** 
     * @param id
     */
    public void eliminar(Long id) {
        departamentoRepositorio.deleteById(id);
    }

}
