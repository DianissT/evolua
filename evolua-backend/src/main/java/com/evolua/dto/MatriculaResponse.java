package com.evolua.dto;

import java.time.LocalDateTime;

import com.evolua.entity.Matricula;

/**
 * Datos de la matrícula de un estudiante en un curso.
 */
public record MatriculaResponse(Long id, Long estudianteId, String estudianteNombre, String estudianteCorreo,
        Long cursoId, LocalDateTime fechaMatricula) {

    public static MatriculaResponse desde(Matricula matricula) {
        return new MatriculaResponse(matricula.getId(), matricula.getEstudiante().getId(),
                matricula.getEstudiante().getNombre(), matricula.getEstudiante().getCorreo(),
                matricula.getCurso().getId(), matricula.getFechaMatricula());
    }
}
