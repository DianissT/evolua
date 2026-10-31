package com.evolua.exception;

/**
 * Se lanza cuando la solicitud incumple una regla de negocio.
 */
public class SolicitudInvalidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
