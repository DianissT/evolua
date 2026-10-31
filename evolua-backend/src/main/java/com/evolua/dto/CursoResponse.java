package com.evolua.dto;

import java.time.LocalDateTime;

import com.evolua.entity.Curso;

/**
 * Datos de un curso.
 */
public record CursoResponse(Long id, String nombre, String descripcion, Long docenteId, String docenteNombre,
        Boolean activo, LocalDateTime fechaCreacion) {

    public static CursoResponse desde(Curso curso) {
        return new CursoResponse(curso.getId(), curso.getNombre(), curso.getDescripcion(),
                curso.getDocente().getId(), curso.getDocente().getNombre(), curso.getActivo(),
                curso.getFechaCreacion());
    }
}
