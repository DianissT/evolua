package com.evolua.adaptation;

import java.math.BigDecimal;
import java.util.Locale;

import com.evolua.entity.NivelDominio;
import com.evolua.entity.TipoActividad;

/**
 * Reglas del motor adaptativo: clasifica el desempeño de un estudiante en un tema y redacta el motivo de la
 * recomendación. No depende de la base de datos: todas las decisiones salen de las estadísticas y de los
 * parámetros recibidos.
 *
 * <table>
 * <caption>Reglas de clasificación</caption>
 * <tr><th>Nivel</th><th>Condición</th><th>Actividad</th></tr>
 * <tr><td>SIN_DATOS</td><td>menos de las respuestas mínimas</td><td>PRACTICA_ADICIONAL</td></tr>
 * <tr><td>REFUERZO</td><td>porcentaje menor al umbral de refuerzo</td><td>REFUERZO</td></tr>
 * <tr><td>INTERMEDIO</td><td>entre el umbral de refuerzo y el de dominio</td><td>PRACTICA_ADICIONAL</td></tr>
 * <tr><td>AVANZADO</td><td>desde el umbral de dominio, sin confirmar</td><td>CONFIRMACION</td></tr>
 * <tr><td>DOMINADO</td><td>desde el umbral de dominio, con los resultados satisfactorios exigidos</td>
 * <td>AVANZADO</td></tr>
 * </table>
 */
public final class ClasificadorTema {

    private static final Locale ES = Locale.forLanguageTag("es");

    private final ParametrosMotor parametros;

    public ClasificadorTema(ParametrosMotor parametros) {
        this.parametros = parametros;
    }

    public Clasificacion clasificar(EstadisticaTema estadistica) {
        BigDecimal porcentaje = estadistica.porcentaje();
        int total = estadistica.totalRespuestas();
        int correctas = estadistica.respuestasCorrectas();

        if (total < parametros.minRespuestasTema()) {
            return new Clasificacion(NivelDominio.SIN_DATOS, TipoActividad.PRACTICA_ADICIONAL, porcentaje);
        }
        if (!EstadisticaTema.alcanza(correctas, total, parametros.umbralRefuerzo())) {
            return new Clasificacion(NivelDominio.REFUERZO, TipoActividad.REFUERZO, porcentaje);
        }
        if (!EstadisticaTema.alcanza(correctas, total, parametros.umbralDominio())) {
            return new Clasificacion(NivelDominio.INTERMEDIO, TipoActividad.PRACTICA_ADICIONAL, porcentaje);
        }
        if (estadistica.intentosSatisfactorios() >= parametros.intentosParaDominio()) {
            return new Clasificacion(NivelDominio.DOMINADO, TipoActividad.AVANZADO, porcentaje);
        }
        return new Clasificacion(NivelDominio.AVANZADO, TipoActividad.CONFIRMACION, porcentaje);
    }

    /**
     * Redacta el motivo de la recomendación, con los datos que la justifican.
     */
    public String motivo(String tema, EstadisticaTema estadistica, Clasificacion clasificacion) {
        String porcentaje = String.format(ES, "%.1f", clasificacion.porcentaje());
        int total = estadistica.totalRespuestas();
        int correctas = estadistica.respuestasCorrectas();
        return switch (clasificacion.nivel()) {
            case SIN_DATOS -> String.format(ES,
                    "Aún no hay suficientes respuestas en %s (%d de %d necesarias) para clasificar tu nivel. "
                            + "Practica con preguntas de dificultad media.",
                    tema, total, parametros.minRespuestasTema());
            case REFUERZO -> String.format(ES,
                    "Acertaste %d de %d preguntas de %s (%s %%), por debajo del %d %% definido para el refuerzo. "
                            + "Practica con preguntas básicas y revisa la retroalimentación.",
                    correctas, total, tema, porcentaje, parametros.umbralRefuerzo());
            case INTERMEDIO -> String.format(ES,
                    "Acertaste %d de %d preguntas de %s (%s %%), entre el %d %% y el %d %%. "
                            + "Continúa con una práctica adicional del mismo nivel.",
                    correctas, total, tema, porcentaje, parametros.umbralRefuerzo(), parametros.umbralDominio());
            case AVANZADO -> String.format(ES,
                    "Acertaste %d de %d preguntas de %s (%s %%), por encima del %d %%, pero aún falta confirmar el "
                            + "dominio: llevas %d de %d resultados satisfactorios. Realiza una evaluación de confirmación.",
                    correctas, total, tema, porcentaje, parametros.umbralDominio(),
                    estadistica.intentosSatisfactorios(), parametros.intentosParaDominio());
            case DOMINADO -> String.format(ES,
                    "Dominas %s: %s %% de aciertos y %d resultados satisfactorios. "
                            + "Practica con ejercicios de mayor dificultad.",
                    tema, porcentaje, estadistica.intentosSatisfactorios());
        };
    }
}
