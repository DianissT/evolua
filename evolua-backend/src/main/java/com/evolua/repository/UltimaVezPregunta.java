package com.evolua.repository;

import java.time.LocalDateTime;

/**
 * Última vez que se le presentó una pregunta a un estudiante (inicio del ultimo intento que la incluyó).
 */
public interface UltimaVezPregunta {
    Long getPreguntaId();

    LocalDateTime getUltimaVez();
}
