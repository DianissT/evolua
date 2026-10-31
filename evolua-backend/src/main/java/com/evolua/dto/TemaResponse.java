package com.evolua.dto;

import com.evolua.entity.Tema;

/**
 * Datos de un tema.
 */
public record TemaResponse(Long id, Long cursoId, String nombre, Integer orden, Boolean activo) {

    public static TemaResponse desde(Tema tema) {
        return new TemaResponse(tema.getId(), tema.getCurso().getId(), tema.getNombre(), tema.getOrden(),
                tema.getActivo());
    }
}
