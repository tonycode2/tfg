package com.anthony.tfg.tfg.Modulos.Mantenimientos;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.JefesDepartamento;
import com.anthony.tfg.tfg.Modulos.Interfaces.MantenimientoInterface;
import com.anthony.tfg.tfg.Repositorios.JefesDepartamentoRepositorio;

@Service
public class MantenimientosJefesDepartamento implements MantenimientoInterface<JefesDepartamento>{

    private final JefesDepartamentoRepositorio repo;

    public MantenimientosJefesDepartamento(JefesDepartamentoRepositorio repo) {
        this.repo = repo;
    }

    /** 
     * @param entidad
     * @return JefesDepartamento
     */
    public JefesDepartamento crear(JefesDepartamento entidad) {
        return repo.save(entidad);
    }

    /** 
     * @param entidad
     * @return JefesDepartamento
     */
    public JefesDepartamento actualizar(JefesDepartamento entidad) {
        return repo.save(entidad);
    }

    /** 
     * @param id
     */
    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}
