package com.evolua.dto;

import com.evolua.entity.Configuracion;

public record ConfiguracionResponse(String clave, String valor, String descripcion) {

    public static ConfiguracionResponse desde(Configuracion fila) {
        return new ConfiguracionResponse(fila.getClave(), fila.getValor(), fila.getDescripcion());
    }
}
