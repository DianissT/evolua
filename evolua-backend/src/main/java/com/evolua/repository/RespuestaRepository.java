package com.evolua.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.evolua.entity.Respuesta;

/**
 * Repositorio de acceso a datos de la entidad Respuesta.
 */
public interface RespuestaRepository extends JpaRepository<Respuesta, Long> {

    List<Respuesta> findByIntentoId(Long intentoId);

    List<Respuesta> findByIntentoIdOrderById(Long intentoId);

    Optional<Respuesta> findByIntentoIdAndPreguntaId(Long intentoId, Long preguntaId);

    boolean existsByPreguntaId(Long preguntaId);

    @Query("select p.tema.id as temaId, count(r) as total, "
            + "sum(case when r.esCorrecta = true then 1 else 0 end) as correctas "
            + "from Respuesta r join r.pregunta p join r.intento i "
            + "where i.estudiante.id = :estudianteId and i.estado = com.evolua.entity.EstadoIntento.FINALIZADO "
            + "and p.tema.id in :temaIds group by p.tema.id")
    List<ResumenTema> resumenPorTema(@Param("estudianteId") Long estudianteId,
            @Param("temaIds") Collection<Long> temaIds);

    @Query("select i.id as intentoId, p.tema.id as temaId, count(r) as total, "
            + "sum(case when r.esCorrecta = true then 1 else 0 end) as correctas "
            + "from Respuesta r join r.pregunta p join r.intento i "
            + "where i.estudiante.id = :estudianteId and i.estado = com.evolua.entity.EstadoIntento.FINALIZADO "
            + "and p.tema.id in :temaIds group by i.id, p.tema.id")
    List<ResumenIntentoTema> resumenPorIntentoYTema(@Param("estudianteId") Long estudianteId,
            @Param("temaIds") Collection<Long> temaIds);

    /**
     * Última vez que el estudiante recibió cada pregunta del tema, en cualquier intento (evaluación o práctica).
     * Las preguntas que nunca ha recibido no aparecen. Sirve para priorizar las menos recientes en la práctica.
     */
    @Query("select p.id as preguntaId, max(i.fechaInicio) as ultimaVez "
            + "from Respuesta r join r.pregunta p join r.intento i "
            + "where i.estudiante.id = :estudianteId and p.tema.id = :temaId group by p.id")
    List<UltimaVezPregunta> ultimaVezPorPregunta(@Param("estudianteId") Long estudianteId,
            @Param("temaId") Long temaId);
}
