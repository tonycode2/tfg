package com.anthony.tfg.tfg.Entidades;

import java.sql.Date;

import com.anthony.tfg.tfg.Entidades.Enums.TipoDeJornada;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "empleados")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Empleados {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String cedula;
    String nombre;
    @Column(name = "primer_apellido")
    String primerApellido;
    @Column(name = "segundo_apellido")
    String segundoApellido;
    @Column(name = "correo_personal")
    String correoPersonal;
    Date fechaNacimiento;
    Date fechaIngreso;
    @Column(name = "salario_base")
    Double salarioBase;
    @Column(name = "cantidad_de_hijos")
    Integer cantidadDeHijos;
    @Column(name = "saldo_vacaciones")
    Integer saldoVacaciones;
    @Column(name = "cuenta_iban")
    String cuentaIban;
    @Column(name = "esta_activo")
    Boolean estaActivo;
    @Column(name = "esta_casado")
    Boolean estaCasado;
    
    @Column(name = "tipo_de_jornada")
    @Enumerated(EnumType.STRING)
    TipoDeJornada tipoDeJornada;
    
    @ManyToOne
    @JoinColumn(name = "id_puesto")
    Puestos puesto;
    
    @ManyToOne
    @JoinColumn(name = "id_direccion")
    Direccion direccion;
    
    @OneToOne
    @JoinColumn(name = "id_usuario")
    User usuario;
    
}
