package com.evolua.entity;

/**
 * Actividad que recomienda el motor adaptativo.
 * <p>Se guarda en la base de datos como texto (@Enumerated STRING), sin tildes y en mayúsculas.</p>
 */
public enum TipoActividad {
    REFUERZO,
    PRACTICA_ADICIONAL,
    AVANZADO,
    CONFIRMACION
}
