package com.evolua.dto;

/**
 * Modalidad con la que un estudiante pide una práctica por tema.
 * Si es AUTOMÁTICA el servidor elige la dificultad según la recomendación vigente del tema. Si es LIBRE el estudiante decide la dificultad (opcional)
 * y la cantidad de preguntas. No se guarda en la base de datos: en ambos casos el intento queda como PRACTICA.
 */
public enum ModalidadPractica {
    AUTOMATICA,
    LIBRE
}
