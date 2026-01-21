package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SolicitudHorasExtraDTO {
    public Long id;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaSolicitud;

    @NotNull
    @Positive
    public Integer cantidadDeHoras;

    @NotBlank
    @Size(min = 5, max = 200)
    public String motivo;

    @NotNull
    public Boolean aprobado;

    @NotNull
    public Boolean procesado;

    @NotBlank
    public String estadoSolicitud;

    @NotBlank
    public String tipoTarifa;

    @NotNull
    public Long idEmpleado;
}
