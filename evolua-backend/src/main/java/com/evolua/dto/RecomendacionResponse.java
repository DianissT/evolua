package com.evolua.dto;

import java.time.LocalDateTime;

import com.evolua.adaptation.RangoDificultad;
import com.evolua.entity.Recomendacion;
import com.evolua.entity.TipoActividad;

/**
 * Actividad recomendada a un estudiante en un tema, con el motivo y el rango de dificultad sugerido.
 */
public record RecomendacionResponse(Long id, Long temaId, String temaNombre, Long cursoId, String cursoNombre,
        TipoActividad tipoActividad, int dificultadMinima, int dificultadMaxima, String motivo,
        LocalDateTime fechaCreacion) {

    public static RecomendacionResponse desde(Recomendacion recomendacion) {
        RangoDificultad rango = RangoDificultad.de(recomendacion.getTipoActividad());
        return new RecomendacionResponse(recomendacion.getId(), recomendacion.getTema().getId(),
                recomendacion.getTema().getNombre(), recomendacion.getTema().getCurso().getId(),
                recomendacion.getTema().getCurso().getNombre(), recomendacion.getTipoActividad(), rango.minima(),
                rango.maxima(), recomendacion.getMotivo(), recomendacion.getFechaCreacion());
    }
}
