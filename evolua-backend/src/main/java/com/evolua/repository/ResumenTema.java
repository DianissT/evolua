package com.evolua.repository;

/**
 * Respuestas totales y correctas de un estudiante en un tema.
 */
public interface ResumenTema {

    Long getTemaId();

    Long getTotal();

    Long getCorrectas();
}
