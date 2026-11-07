package com.evolua.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.evolua.entity.Recomendacion;

/**
 * Repositorio de acceso a datos de la entidad Recomendacion.
 */
public interface RecomendacionRepository extends JpaRepository<Recomendacion, Long> {

    List<Recomendacion> findByEstudianteIdAndVigenteTrue(Long estudianteId);

    List<Recomendacion> findByEstudianteIdAndTemaIdInAndVigenteTrue(Long estudianteId, Collection<Long> temaIds);

    @Modifying(flushAutomatically = true)
    @Query("update Recomendacion r set r.vigente = false "
            + "where r.estudiante.id = :estudianteId and r.tema.id = :temaId and r.vigente = true")
    void desactivarVigentes(@Param("estudianteId") Long estudianteId, @Param("temaId") Long temaId);
}
