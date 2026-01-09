package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.util.List;

/**
 * DTO representing attendance summary for an entire department
 */
public class ResumenDepartamentoDTO {
    
    public Long departamentoId;
    public String departamentoNombre;
    
    /**
     * Total number of employees in the department
     */
    public Integer totalEmpleados;
    
    /**
     * Number of employees currently clocked in
     */
    public Integer empleadosLaborando;
    
    /**
     * Number of employees not clocked in
     */
    public Integer empleadosFuera;
    
    /**
     * List of all employees with their current attendance status
     */
    public List<EstadoAsistenciaDTO> empleados;
}
