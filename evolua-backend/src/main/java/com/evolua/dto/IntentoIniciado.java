package com.evolua.dto;

/**
 * Resultado de iniciar un intento: el intento y si se creó uno nuevo o se retomó uno que estaba en curso.
 */
public record IntentoIniciado(IntentoResponse intento, boolean nuevo) {
}
