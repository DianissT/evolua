package com.evolua.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evolua.entity.Evaluacion;

/**
 * Repositorio de acceso a datos de la entidad Evaluacion.
 */
public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {

    List<Evaluacion> findByCursoId(Long cursoId);

    List<Evaluacion> findByCursoIdAndActivaTrue(Long cursoId);
}
