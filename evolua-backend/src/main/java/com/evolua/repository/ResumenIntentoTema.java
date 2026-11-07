package com.evolua.repository;

/**
 * Respuestas totales y correctas de un estudiante en un tema dentro de un intento.
 */
public interface ResumenIntentoTema {

    Long getIntentoId();

    Long getTemaId();

    Long getTotal();

    Long getCorrectas();
}
