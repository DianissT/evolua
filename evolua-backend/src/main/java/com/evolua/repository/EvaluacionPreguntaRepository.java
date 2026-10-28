package com.evolua.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.evolua.entity.EvaluacionPregunta;

/**
 * Acceso a datos de evaluacion_preguntas (Spring Data JPA).
 * <p>Hereda guardar, buscar, listar y eliminar.</p>
 */
public interface EvaluacionPreguntaRepository extends JpaRepository<EvaluacionPregunta, Long> {

    List<EvaluacionPregunta> findByEvaluacionIdOrderByOrden(Long evaluacionId);
}
