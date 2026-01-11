package com.anthony.tfg.tfg.Entidades.Enums;

public enum EstadoSolicitud {
    PENDIENTE,              // Esperando aprobación del jefe
    PENDIENTE_RH,          // Esperando RH (sin jefe o solicitante es jefe)
    APROBADA_POR_JEFE,     // Jefe aprobó, esperando RH
    RECHAZADA_POR_JEFE,    // Rechazada por jefe
    RECHAZADA_POR_RH,      // Rechazada por RH
    APROBADA,              // Aprobación final
    CANCELADA              // Cancelada por RH
}
