package com.evolua.entity;

/**
 * Nivel estimado de un tema para un estudiante (motor adaptativo).
 * <p>Se guarda en la base de datos como texto (@Enumerated STRING), sin tildes y en mayúsculas.</p>
 */
public enum NivelDominio {
    SIN_DATOS,
    REFUERZO,
    INTERMEDIO,
    AVANZADO,
    DOMINADO
}
