package com.anthony.tfg.tfg.Modulos.Mantenimientos;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Modulos.Interfaces.MantenimientoInterface;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;

import jakarta.transaction.Transactional;

@Service
public class MantenimientosEmpleados implements MantenimientoInterface<Empleados>{

    private final EmpleadosRepositorio repo;

    public MantenimientosEmpleados(EmpleadosRepositorio repo) {
        this.repo = repo;
    }

    /** 
     * @param entidad
     * @return Empleados
     */
    public Empleados crear(Empleados entidad) {
        return repo.save(entidad);
    }

    /** 
     * @param entidad
     * @return Empleados
     */
    public Empleados actualizar(Empleados entidad) {
        return repo.save(entidad);
    }

    /** 
     * @param id
     */
    @Transactional
    public void eliminar(Long id) {
        // Obtener IDs de usuario y dirección antes de eliminar
        Long idUsuario = repo.getUsuarioIdByEmpleadoId(id);
        Long idDireccion = repo.getDireccionIdByEmpleadoId(id);
        
        // 1. Eliminar todos los registros relacionados
        repo.deletePermisosByEmpleadoId(id);
        repo.deleteAsistenciasByEmpleadoId(id);
        repo.deleteHorasExtraByEmpleadoId(id);
        repo.deleteAguinaldosByEmpleadoId(id);
        repo.deleteEvaluacionesByEmpleadoId(id);
        repo.deleteLiquidacionesByEmpleadoId(id);
        repo.deletePlanillaDetallesByEmpleadoId(id);
        
        // 2. Eliminar el empleado
        repo.deleteEmpleadoById(id);
        
        // 3. Eliminar el usuario si existe
        if(idUsuario != null) {
            repo.deleteUsuarioById(idUsuario);
        }
        
        // 4. Eliminar la dirección si existe
        if(idDireccion != null) {
            repo.deleteDireccionById(idDireccion);
        }
    }

}
