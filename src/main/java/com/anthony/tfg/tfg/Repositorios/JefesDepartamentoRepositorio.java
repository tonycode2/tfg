package com.anthony.tfg.tfg.Repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.anthony.tfg.tfg.Entidades.JefesDepartamento;

@Repository
public interface JefesDepartamentoRepositorio extends JpaRepository<JefesDepartamento, Long> {
}
