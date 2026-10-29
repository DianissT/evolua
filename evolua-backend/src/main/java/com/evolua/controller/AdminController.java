package com.evolua.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evolua.dto.AuditoriaResponse;
import com.evolua.service.AuditoriaService;

/**
 * Operaciones solo para administradores
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AuditoriaService auditoriaService;

    public AdminController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditoriaResponse> auditoria() {
        return auditoriaService.listar();
    }
}
