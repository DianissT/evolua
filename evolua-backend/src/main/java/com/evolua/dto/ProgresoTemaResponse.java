package com.evolua.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.evolua.adaptation.EstadisticaTema;
import com.evolua.entity.NivelDominio;
import com.evolua.entity.ProgresoTema;
import com.evolua.entity.Tema;

/**
 * Desempeño acumulado de un estudiante en un tema y nivel estimado.
 */
public record ProgresoTemaResponse(Long temaId, String temaNombre, Long cursoId, String cursoNombre,
        int totalRespuestas, int respuestasCorrectas, BigDecimal porcentajeAciertos, int intentosSatisfactorios,
        NivelDominio nivel, LocalDateTime fechaActualizacion) {

    public static ProgresoTemaResponse desde(ProgresoTema progreso) {
        Tema tema = progreso.getTema();
        return new ProgresoTemaResponse(tema.getId(), tema.getNombre(), tema.getCurso().getId(),
                tema.getCurso().getNombre(), progreso.getTotalRespuestas(), progreso.getRespuestasCorrectas(),
                progreso.getPorcentajeAciertos(), progreso.getIntentosSatisfactorios(), progreso.getNivel(),
                progreso.getFechaActualizacion());
    }

    public static ProgresoTemaResponse sinDatos(Tema tema) {
        return new ProgresoTemaResponse(tema.getId(), tema.getNombre(), tema.getCurso().getId(),
                tema.getCurso().getNombre(), 0, 0, EstadisticaTema.calcularPorcentaje(0, 0), 0,
                NivelDominio.SIN_DATOS, null);
    }
}
