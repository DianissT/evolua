package com.evolua.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.evolua.entity.OpcionPregunta;

/**
 * Repositorio de acceso a datos de la entidad OpcionPregunta.
 */
public interface OpcionPreguntaRepository extends JpaRepository<OpcionPregunta, Long> {

    List<OpcionPregunta> findByPreguntaIdOrderByOrden(Long preguntaId);

    @Modifying(flushAutomatically = true)
    @Query("delete from OpcionPregunta o where o.pregunta.id = :preguntaId")
    void eliminarPorPregunta(@Param("preguntaId") Long preguntaId);
}
