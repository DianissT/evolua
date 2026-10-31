package com.evolua.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evolua.entity.Pregunta;

/**
 * Repositorio de acceso a datos de la entidad Pregunta.
 */
public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {

    List<Pregunta> findByTemaIdAndActivaTrue(Long temaId);

    List<Pregunta> findByTemaIdOrderById(Long temaId);
}
