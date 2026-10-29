package com.evolua.dto;

import java.util.List;

/**
 * Formato de errores de la API.
 */
public record ErrorResponse(int estado, String mensaje, List<String> detalles) {
}
