package com.evolua.adaptation;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Selector de preguntas de las prácticas por tema, este no depende de la base de datos sino que, recibe las preguntas activas del
 * tema con la última vez que se le presentó cada una al estudiante y decide cuáles se usan.
 *
 * <p>Criterios, en orden de prioridad:</p>
 * <ol>
 *   <li><b>Dificultad</b>: primero las preguntas dentro del rango pedido; si no alcanzan, las de dificultad más
 *       cercana al rango (salvo en modo estricto, que descarta las que quedan fuera).</li>
 *   <li><b>Recencia</b>: entre preguntas igual de adecuadas, primero las que el estudiante nunca ha recibido y
 *       luego las que hace más tiempo no ve, para no repetir lo que acaba de practicar.</li>
 *   <li><b>Identificador</b>: desempate fijo, para que el resultado sea reproducible.</li>
 * </ol>
 */
public final class SelectorPreguntas {

    /**
     * Pregunta activa del tema.
     *
     * @param ultimaVez inicio del último intento que la incluyó, o {@code null} si el estudiante nunca la recibió
     */
    public record Candidata(Long preguntaId, int dificultad, LocalDateTime ultimaVez) {
    }

    private static final Comparator<Candidata> MENOS_RECIENTE_PRIMERO = Comparator.comparing(Candidata::ultimaVez,
            Comparator.nullsFirst(Comparator.<LocalDateTime>naturalOrder()));

    /**
     * Elige hasta {@code cantidad} preguntas, ya ordenadas por prioridad. Devuelve menos si no hay suficientes
     * candidatas y una lista vacía si ninguna cumple (modo estricto o tema sin preguntas).
     *
     * @param estricto si es {@code true}, solo se admiten preguntas dentro del rango de dificultad
     */
    public List<Long> seleccionar(List<Candidata> candidatas, RangoDificultad rango, int cantidad, boolean estricto) {
        if (cantidad < 1) {
            throw new IllegalArgumentException("La cantidad de preguntas debe ser al menos 1");
        }
        return candidatas.stream()
                .filter(candidata -> !estricto || distancia(candidata, rango) == 0)
                .sorted(Comparator.comparingInt((Candidata candidata) -> distancia(candidata, rango))
                        .thenComparing(MENOS_RECIENTE_PRIMERO)
                        .thenComparing(Candidata::preguntaId))
                .limit(cantidad)
                .map(Candidata::preguntaId)
                .toList();
    }

    /** Niveles de dificultad que separan a la pregunta del rango: 0 si está dentro de él. */
    static int distancia(Candidata candidata, RangoDificultad rango) {
        if (candidata.dificultad() < rango.minima()) {
            return rango.minima() - candidata.dificultad();
        }
        return Math.max(0, candidata.dificultad() - rango.maxima());
    }
}
