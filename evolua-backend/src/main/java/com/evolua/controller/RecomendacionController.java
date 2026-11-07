package com.evolua.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evolua.dto.RecomendacionResponse;
import com.evolua.security.UsuarioPrincipal;
import com.evolua.service.RecomendacionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Recomendaciones del motor adaptativo para el estudiante autenticado.
 */
@RestController
@RequestMapping("/api/v1/recommendations")
@Tag(name = "Recomendaciones")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class RecomendacionController {

    private final RecomendacionService recomendacionService;

    public RecomendacionController(RecomendacionService recomendacionService) {
        this.recomendacionService = recomendacionService;
    }

    @GetMapping("/me")
    @Operation(summary = "Recomendaciones vigentes, con su motivo y el rango de dificultad sugerido")
    public List<RecomendacionResponse> misRecomendaciones(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return recomendacionService.listarMias(usuario.getId());
    }
}
