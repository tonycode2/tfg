package com.anthony.tfg.tfg.Modulos.Consultas;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.DiasFeriados;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.DiasFeriadosRepositorio;

/**
 * Servicio de consultas para días feriados.
 */
@Service
public class ConsultasDiasFeriados implements ConsultaInterface<DiasFeriados> {
    
    private final DiasFeriadosRepositorio repo;
    
    public ConsultasDiasFeriados(DiasFeriadosRepositorio repo) {
        this.repo = repo;
    }
    
    @Override
    public DiasFeriados obtenerPorId(Long id) {
        Optional<DiasFeriados> feriado = repo.findById(id);
        return feriado.orElse(null);
    }
    
    @Override
    public List<DiasFeriados> obtenerTodos() {
        return repo.findAllByOrderByFechaDesc();
    }
    
    /**
     * Verifica si una fecha específica es un día feriado.
     * @param fecha La fecha a verificar
     * @return El feriado si existe, null si no
     */
    public DiasFeriados obtenerPorFecha(LocalDate fecha) {
        return repo.findByFecha(fecha).orElse(null);
    }
    
    /**
     * Obtiene todos los feriados dentro de un rango de fechas.
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de feriados en el rango
     */
    public List<DiasFeriados> obtenerPorRango(LocalDate fechaInicio, LocalDate fechaFin) {
        return repo.findByFechaBetween(fechaInicio, fechaFin);
    }
    
    /**
     * Verifica si existe un feriado en la fecha especificada.
     * @param fecha La fecha a verificar
     * @return true si la fecha es un feriado
     */
    public boolean esFeriado(LocalDate fecha) {
        return repo.existsByFecha(fecha);
    }
    
    /**
     * Obtiene todos los feriados de un año específico.
     * @param anio El año a consultar
     * @return Lista de feriados del año
     */
    public List<DiasFeriados> obtenerPorAnio(int anio) {
        return repo.findByAnio(anio);
    }
}
