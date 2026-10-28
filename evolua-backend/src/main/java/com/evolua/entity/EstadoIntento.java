package com.evolua.entity;

/**
 * Estado de un intento.
 * <p>Se guarda en la base de datos como texto (@Enumerated STRING), sin tildes y en mayúsculas.</p>
 */
public enum EstadoIntento {
    EN_CURSO,
    FINALIZADO
}
