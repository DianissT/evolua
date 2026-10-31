package com.evolua.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evolua.entity.Respuesta;

/**
 * Repositorio de acceso a datos de la entidad Respuesta.
 */
public interface RespuestaRepository extends JpaRepository<Respuesta, Long> {

    List<Respuesta> findByIntentoId(Long intentoId);

    boolean existsByPreguntaId(Long preguntaId);
}
