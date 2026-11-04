package com.evolua.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evolua.entity.Respuesta;

/**
 * Repositorio de acceso a datos de la entidad Respuesta.
 */
public interface RespuestaRepository extends JpaRepository<Respuesta, Long> {

    List<Respuesta> findByIntentoId(Long intentoId);

    List<Respuesta> findByIntentoIdOrderById(Long intentoId);

    Optional<Respuesta> findByIntentoIdAndPreguntaId(Long intentoId, Long preguntaId);

    boolean existsByPreguntaId(Long preguntaId);
}
