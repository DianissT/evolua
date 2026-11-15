package com.evolua.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evolua.dto.ConfiguracionRequest;
import com.evolua.dto.ConfiguracionResponse;
import com.evolua.security.UsuarioPrincipal;
import com.evolua.service.ConfiguracionAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Consulta y edición de los parámetros del sistema solo para los administradores.
 */
@RestController
@RequestMapping("/api/v1/admin/config")
@Tag(name = "Administración")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfiguracionController {

    private final ConfiguracionAdminService configuracionService;

    public AdminConfiguracionController(ConfiguracionAdminService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping
    @Operation(summary = "Lista los parámetros del sistema")
    public List<ConfiguracionResponse> listar() {
        return configuracionService.listar();
    }

    @PutMapping("/{clave}")
    @Operation(summary = "Cambia el valor de un parámetro; los umbrales se validan y el cambio se audita")
    public ConfiguracionResponse actualizar(@PathVariable("clave") String clave,
            @Valid @RequestBody ConfiguracionRequest solicitud,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal admin) {
        return configuracionService.actualizar(clave, solicitud.valor(), admin.getId());
    }
}
