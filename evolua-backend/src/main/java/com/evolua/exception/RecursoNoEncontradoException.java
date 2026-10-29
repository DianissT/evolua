package com.evolua.exception;

/**
 * Se lanza cuando un recurso solicitado no existe
 */
public class RecursoNoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
