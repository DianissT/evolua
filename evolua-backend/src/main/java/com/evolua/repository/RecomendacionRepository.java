package com.evolua.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.evolua.entity.Recomendacion;

/**
 * Acceso a datos de recomendaciones (Spring Data JPA).
 * <p>Hereda guardar, buscar, listar y eliminar.</p>
 */
public interface RecomendacionRepository extends JpaRepository<Recomendacion, Long> {

    List<Recomendacion> findByEstudianteIdAndVigenteTrue(Long estudianteId);
}
