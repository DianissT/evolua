package com.evolua.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.evolua.entity.Tema;

/**
 * Acceso a datos de temas (Spring Data JPA).
 * <p>Hereda guardar, buscar, listar y eliminar.</p>
 */
public interface TemaRepository extends JpaRepository<Tema, Long> {

    List<Tema> findByCursoIdOrderByOrden(Long cursoId);
}
