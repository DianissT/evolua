package com.evolua.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.evolua.entity.OpcionPregunta;

/**
 * Acceso a datos de opciones_pregunta (Spring Data JPA).
 * <p>Hereda guardar, buscar, listar y eliminar.</p>
 */
public interface OpcionPreguntaRepository extends JpaRepository<OpcionPregunta, Long> {

    List<OpcionPregunta> findByPreguntaIdOrderByOrden(Long preguntaId);
}
