package com.evolua.adaptation;

import com.evolua.entity.TipoActividad;

/**
 * Rango de dificultad (1 básica, 2 media, 3 alta) de las preguntas que corresponden a una actividad.
 */
public record RangoDificultad(int minima, int maxima) {

    public static RangoDificultad de(TipoActividad actividad) {
        return switch (actividad) {
            case REFUERZO -> new RangoDificultad(1, 1);
            case PRACTICA_ADICIONAL -> new RangoDificultad(2, 2);
            case CONFIRMACION -> new RangoDificultad(2, 3);
            case AVANZADO -> new RangoDificultad(3, 3);
        };
    }
}
