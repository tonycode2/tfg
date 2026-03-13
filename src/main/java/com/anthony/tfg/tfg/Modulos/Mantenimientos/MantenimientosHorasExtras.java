package com.anthony.tfg.tfg.Modulos.Mantenimientos;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.HorasExtra;
import com.anthony.tfg.tfg.Modulos.Interfaces.MantenimientoInterface;
import com.anthony.tfg.tfg.Repositorios.HorasExtraRepositorio;

@Service
public class MantenimientosHorasExtras implements MantenimientoInterface<HorasExtra>{

    private final HorasExtraRepositorio repo;

    public MantenimientosHorasExtras(HorasExtraRepositorio repo) {
        this.repo = repo;
    }

    /** 
     * @param entidad
     * @return HorasExtra
     */
    public HorasExtra crear(HorasExtra entidad) {
        return repo.save(entidad);
    }

    /** 
     * @param entidad
     * @return HorasExtra
     */
    public HorasExtra actualizar(HorasExtra entidad) {
        return repo.save(entidad);
    }

    /** 
     * @param id
     */
    public void eliminar(Long id) {
        repo.deleteById(id);
    }

}
