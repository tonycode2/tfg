package com.anthony.tfg.tfg.Modulos.Consultas;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Incapacidades;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.IncapacidadesRepositorio;

@Service
public class ConsultasIncapacidades implements ConsultaInterface<Incapacidades> {

    private final IncapacidadesRepositorio repo;

    public ConsultasIncapacidades(IncapacidadesRepositorio repo) {
        this.repo = repo;
    }

    @Override
    public Incapacidades obtenerPorId(Long id) {
        Optional<Incapacidades> incapacidad = repo.findById(id);
        return incapacidad.orElse(null);
    }

    @Override
    public List<Incapacidades> obtenerTodos() {
        return repo.findAll();
    }

    /**
     * Obtiene incapacidades por empleado ordenadas por fecha de solicitud DESC
     */
    public List<Incapacidades> obtenerPorEmpleadoId(Long idEmpleado) {
        return repo.findByEmpleadoIdOrderByFechaSolicitudDesc(idEmpleado);
    }
    
    /**
     * Obtiene incapacidades pendientes de un departamento
     */
    public List<Incapacidades> obtenerIncapacidadesPendientesByDepartamento(Long idDepartamento) {
        return repo.findIncapacidadesPendientesByDepartamento(idDepartamento);
    }
    
    /**
     * Obtiene incapacidades que necesitan aprobación de RH
     */
    public List<Incapacidades> obtenerIncapacidadesParaRH() {
        return repo.findIncapacidadesParaRH();
    }
    
    /**
     * Obtiene todas las incapacidades ordenadas por fecha (para auditoría)
     */
    public List<Incapacidades> obtenerTodosOrdenados() {
        return repo.findAllByOrderByFechaSolicitudDesc();
    }
    
    /**
     * Obtiene incapacidades activas (aprobadas y en curso)
     */
    public List<Incapacidades> obtenerIncapacidadesActivas(LocalDate fecha) {
        return repo.findIncapacidadesActivas(fecha);
    }
    
    /**
     * Obtiene incapacidades activas de un departamento específico
     */
    public List<Incapacidades> obtenerIncapacidadesActivasByDepartamento(Long idDepartamento, LocalDate fecha) {
        return repo.findIncapacidadesActivasByDepartamento(idDepartamento, fecha);
    }
}
