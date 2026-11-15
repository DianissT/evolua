package com.evolua.repository;

/**
 * Respuestas totales y correctas que recibió una pregunta.
 */
public interface ResumenPregunta {

    Long getPreguntaId();

    String getEnunciado();

    Long getTemaId();

    String getTemaNombre();

    Integer getDificultad();

    Long getTotal();

    Long getCorrectas();
}
