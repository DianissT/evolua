package com.evolua.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evolua.entity.EstadoIntento;
import com.evolua.entity.Intento;

/**
 * Repositorio de acceso a datos de la entidad Intento.
 */
public interface IntentoRepository extends JpaRepository<Intento, Long> {

    List<Intento> findByEstudianteId(Long estudianteId);

    List<Intento> findByEstudianteIdOrderByFechaInicioDesc(Long estudianteId);

    Optional<Intento> findByEstudianteIdAndEvaluacionIdAndEstado(Long estudianteId, Long evaluacionId,
            EstadoIntento estado);

    long countByEstudianteIdAndEvaluacionId(Long estudianteId, Long evaluacionId);

    boolean existsByEvaluacionId(Long evaluacionId);
}
