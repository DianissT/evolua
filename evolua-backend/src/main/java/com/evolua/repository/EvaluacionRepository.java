package com.evolua.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.evolua.entity.Evaluacion;

/**
 * Acceso a datos de evaluaciones (Spring Data JPA).
 * <p>Hereda guardar, buscar, listar y eliminar.</p>
 */
public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {

    List<Evaluacion> findByCursoId(Long cursoId);
}
