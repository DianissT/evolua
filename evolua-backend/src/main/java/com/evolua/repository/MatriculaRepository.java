package com.evolua.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evolua.entity.Matricula;

/**
 * Repositorio de acceso a datos de la entidad Matricula.
 */
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    List<Matricula> findByEstudianteId(Long estudianteId);

    List<Matricula> findByCursoIdOrderByFechaMatricula(Long cursoId);

    Optional<Matricula> findByCursoIdAndEstudianteId(Long cursoId, Long estudianteId);

    boolean existsByEstudianteIdAndCursoId(Long estudianteId, Long cursoId);
}
