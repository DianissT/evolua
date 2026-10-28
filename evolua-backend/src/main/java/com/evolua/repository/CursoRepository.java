package com.evolua.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.evolua.entity.Curso;

/**
 * Acceso a datos de cursos (Spring Data JPA).
 * <p>Hereda guardar, buscar, listar y eliminar.</p>
 */
public interface CursoRepository extends JpaRepository<Curso, Long> {

    List<Curso> findByDocenteId(Long docenteId);

    List<Curso> findByActivoTrue();
}
