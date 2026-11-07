package com.evolua.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evolua.dto.ProgresoTemaResponse;
import com.evolua.security.UsuarioPrincipal;
import com.evolua.service.ProgresoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Progreso del estudiante autenticado por tema.
 */
@RestController
@RequestMapping("/api/v1/progress")
@Tag(name = "Progreso")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class ProgresoController {

    private final ProgresoService progresoService;

    public ProgresoController(ProgresoService progresoService) {
        this.progresoService = progresoService;
    }

    @GetMapping("/me")
    @Operation(summary = "Progreso propio: aciertos, nivel estimado y resultados satisfactorios de cada tema")
    public List<ProgresoTemaResponse> miProgreso(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return progresoService.listarMio(usuario.getId());
    }
}
