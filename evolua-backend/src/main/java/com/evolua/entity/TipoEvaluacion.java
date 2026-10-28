package com.evolua.entity;

/**
 * Proposito de una evaluación fija.
 * <p>Se guarda en la base de datos como texto (@Enumerated STRING), sin tildes y en mayúsculas.</p>
 */
public enum TipoEvaluacion {
    INICIAL,
    FORMATIVA,
    CONFIRMACION
}
