package com.evolua.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.evolua.entity.EvaluacionPregunta;

/**
 * Repositorio de acceso a datos de la entidad EvaluacionPregunta.
 */
public interface EvaluacionPreguntaRepository extends JpaRepository<EvaluacionPregunta, Long> {

    List<EvaluacionPregunta> findByEvaluacionIdOrderByOrden(Long evaluacionId);

    long countByEvaluacionId(Long evaluacionId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from EvaluacionPregunta ep where ep.evaluacion.id = :evaluacionId")
    void eliminarPorEvaluacion(@Param("evaluacionId") Long evaluacionId);
}
