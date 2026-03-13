package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Permisos;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.PermisosRepositorio;

@Service
public class ConsultasPermisos implements ConsultaInterface<Permisos>{

    private final PermisosRepositorio repo;

    public ConsultasPermisos(PermisosRepositorio repo) {
        this.repo = repo;
    }

    /** 
     * @param id
     * @return Permisos
     */
    public Permisos obtenerPorId(Long id) {
        Optional<Permisos> permiso = repo.findById(id);
        return permiso.orElse(null);
    }

    /** 
     * @return List<Permisos>
     */
    public List<Permisos> obtenerTodos() {
        return repo.findAll();
    }
    
    /**
     * Obtiene permisos por empleado ordenados por fecha de solicitud DESC
     */
    public List<Permisos> obtenerPorEmpleadoId(Long idEmpleado) {
        return repo.findByEmpleadoIdOrderByFechaSolicitudDesc(idEmpleado);
    }
    
    /**
     * Obtiene permisos pendientes de un departamento
     */
    public List<Permisos> obtenerPermisosPendientesByDepartamento(Long idDepartamento) {
        return repo.findPermisosPendientesByDepartamento(idDepartamento);
    }
    
    /**
     * Obtiene permisos que necesitan aprobación de RH
     */
    public List<Permisos> obtenerPermisosParaRH() {
        return repo.findPermisosParaRH();
    }
    
    /**
     * Obtiene todas las solicitudes ordenadas por fecha (para auditoría)
     */
    public List<Permisos> obtenerTodosOrdenados() {
        return repo.findAllByOrderByFechaSolicitudDesc();
    }
    
}
