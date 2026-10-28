package com.evolua.entity;

/**
 * Tipo de pregunta.
 * <p>Se guarda en la base de datos como texto (@Enumerated STRING), sin tildes y en mayúsculas.</p>
 */
public enum TipoPregunta {
    SELECCION_MULTIPLE,
    VERDADERO_FALSO
}
