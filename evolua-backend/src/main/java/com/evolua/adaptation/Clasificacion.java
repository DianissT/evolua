package com.evolua.adaptation;

import java.math.BigDecimal;

import com.evolua.entity.NivelDominio;
import com.evolua.entity.TipoActividad;

/**
 * Resultado de clasificar un tema: nivel estimado, actividad recomendada y porcentaje de aciertos.
 */
public record Clasificacion(NivelDominio nivel, TipoActividad actividad, BigDecimal porcentaje) {
}
