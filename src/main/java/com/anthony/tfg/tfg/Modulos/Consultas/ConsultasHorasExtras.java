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


    /** 
     * @param id
     * @return HorasExtra
     */
    public HorasExtra obtenerPorId(Long id) {
        Optional<HorasExtra> horasExtra = repo.findById(id);
        return horasExtra.orElse(null);
    }

    /** 
     * @return List<HorasExtra>
     */
    public List<HorasExtra> obtenerTodos() {
        return repo.findAll();
    }

    /** 
     * @param empleadoId
     * @param fecha
     * @return List<HorasExtra>
     */
    public List<HorasExtra> obtenerPorEmpleadoYFecha(Long empleadoId, java.time.LocalDate fecha) {
        return repo.findByEmpleadoIdAndFechaSolicitud(empleadoId, fecha);
    }

}
