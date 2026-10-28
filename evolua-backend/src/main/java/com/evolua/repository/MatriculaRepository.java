package com.evolua.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.evolua.entity.Matricula;

/**
 * Acceso a datos de matriculas (Spring Data JPA).
 * <p>Hereda guardar, buscar, listar y eliminar.</p>
 */
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    List<Matricula> findByEstudianteId(Long estudianteId);

    boolean existsByEstudianteIdAndCursoId(Long estudianteId, Long cursoId);
}
