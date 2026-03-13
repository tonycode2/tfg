package com.anthony.tfg.tfg.Modulos.Mantenimientos;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.ConfiguracionRenta;
import com.anthony.tfg.tfg.Modulos.Interfaces.MantenimientoInterface;
import com.anthony.tfg.tfg.Repositorios.ConfiguracionRentaRepositorio;

@Service
public class MantenimientosConfiguracionRenta implements MantenimientoInterface<ConfiguracionRenta>{

    private final ConfiguracionRentaRepositorio repo;

    public MantenimientosConfiguracionRenta(ConfiguracionRentaRepositorio repo) {
        this.repo = repo;
    }

    /** 
     * @param entidad
     * @return ConfiguracionRenta
     */
    public ConfiguracionRenta crear(ConfiguracionRenta entidad) {
        return repo.save(entidad);
    }

    /** 
     * @param entidad
     * @return ConfiguracionRenta
     */
    public ConfiguracionRenta actualizar(ConfiguracionRenta entidad) {
        return repo.save(entidad);
    }

    /** 
     * @param id
     */
    public void eliminar(Long id) {
        repo.deleteById(id);
    }

}
