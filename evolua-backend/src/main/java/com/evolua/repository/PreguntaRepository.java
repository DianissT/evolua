package com.evolua.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.evolua.entity.Pregunta;

/**
 * Acceso a datos de preguntas (Spring Data JPA).
 * <p>Hereda guardar, buscar, listar y eliminar.</p>
 */
public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {

    List<Pregunta> findByTemaIdAndActivaTrue(Long temaId);
}
