package com.evolua.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.evolua.entity.ProgresoTema;

/**
 * Acceso a datos de progreso_tema (Spring Data JPA).
 * <p>Hereda guardar, buscar, listar y eliminar.</p>
 */
public interface ProgresoTemaRepository extends JpaRepository<ProgresoTema, Long> {

    Optional<ProgresoTema> findByEstudianteIdAndTemaId(Long estudianteId, Long temaId);

    List<ProgresoTema> findByEstudianteId(Long estudianteId);
}
