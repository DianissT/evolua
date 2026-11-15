package com.evolua.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.evolua.entity.Respuesta;

/**
 * Consultas agregadas (JPQL) de los reportes del docente.
 */
public interface ReporteRepository extends Repository<Respuesta, Long> {

    /** Respuestas totales y correctas de todos los estudiantes, por tema de un curso. */
    @Query("select t.id as temaId, count(r) as total, "
            + "sum(case when r.esCorrecta = true then 1 else 0 end) as correctas "
            + "from Respuesta r join r.pregunta p join p.tema t join r.intento i "
            + "where t.curso.id = :cursoId and i.estado = com.evolua.entity.EstadoIntento.FINALIZADO "
            + "group by t.id")
    List<ResumenTema> resumenPorTema(@Param("cursoId") Long cursoId);

    /** Intentos finalizados (evaluaciones y prácticas) de cada estudiante en un curso. */
    @Query("select u.id as estudianteId, count(i) as intentos, sum(i.puntuacion) as suma, "
            + "max(i.fechaFin) as ultimaActividad "
            + "from Intento i join i.estudiante u left join i.evaluacion e left join i.tema t "
            + "where i.estado = com.evolua.entity.EstadoIntento.FINALIZADO "
            + "and (e.curso.id = :cursoId or t.curso.id = :cursoId) "
            + "group by u.id")
    List<ResumenEstudianteCurso> resumenPorEstudiante(@Param("cursoId") Long cursoId);

    /** Cuántos estudiantes están en nivel REFUERZO en cada tema del curso. */
    @Query("select pt.tema.id as clave, count(pt) as total from ProgresoTema pt "
            + "where pt.tema.curso.id = :cursoId and pt.nivel = com.evolua.entity.NivelDominio.REFUERZO "
            + "group by pt.tema.id")
    List<ConteoPorClave> estudiantesEnRefuerzoPorTema(@Param("cursoId") Long cursoId);

    /** En cuántos temas del curso está en REFUERZO cada estudiante. */
    @Query("select pt.estudiante.id as clave, count(pt) as total from ProgresoTema pt "
            + "where pt.tema.curso.id = :cursoId and pt.nivel = com.evolua.entity.NivelDominio.REFUERZO "
            + "group by pt.estudiante.id")
    List<ConteoPorClave> temasEnRefuerzoPorEstudiante(@Param("cursoId") Long cursoId);

    /** Preguntas del curso con al menos {@code minimo} respuestas, para ordenarlas por porcentaje de aciertos. */
    @Query("select p.id as preguntaId, p.enunciado as enunciado, t.id as temaId, t.nombre as temaNombre, "
            + "p.dificultad as dificultad, count(r) as total, "
            + "sum(case when r.esCorrecta = true then 1 else 0 end) as correctas "
            + "from Respuesta r join r.pregunta p join p.tema t join r.intento i "
            + "where t.curso.id = :cursoId and i.estado = com.evolua.entity.EstadoIntento.FINALIZADO "
            + "group by p.id, p.enunciado, t.id, t.nombre, p.dificultad "
            + "having count(r) >= :minimo")
    List<ResumenPregunta> resumenPorPregunta(@Param("cursoId") Long cursoId, @Param("minimo") long minimo);
}
