package com.evolua.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evolua.dto.AuditoriaResponse;
import com.evolua.service.AuditoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Operaciones reservadas a los administradores.
 */
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Administración")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AuditoriaService auditoriaService;

    public AdminController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista los registros de auditoría, del más reciente al más antiguo")
    public List<AuditoriaResponse> auditoria() {
        return auditoriaService.listar();
    }
}
