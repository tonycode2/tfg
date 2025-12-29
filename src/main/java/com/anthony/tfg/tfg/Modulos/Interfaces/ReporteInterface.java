package com.anthony.tfg.tfg.Modulos.Interfaces;

import java.sql.Date;
import java.util.List;

public interface ReporteInterface <T>{
    public List<T> generarReporte(Date fechaInicio, Date fechaFin);
}
