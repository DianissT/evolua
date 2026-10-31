package com.evolua.exception;

/**
 * Se lanza cuando la operación entra en conflicto con el estado actual de los datos.
 */
public class ConflictoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
