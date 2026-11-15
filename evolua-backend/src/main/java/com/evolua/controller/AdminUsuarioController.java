package com.evolua.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.evolua.dto.CambioEstadoRequest;
import com.evolua.dto.CambioRolRequest;
import com.evolua.dto.UsuarioAdminRequest;
import com.evolua.dto.UsuarioAdminResponse;
import com.evolua.security.UsuarioPrincipal;
import com.evolua.service.UsuarioAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Gestión de usuarios y roles solo para los administradores.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Administración")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioController {

    private final UsuarioAdminService usuarioService;

    public AdminUsuarioController(UsuarioAdminService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Lista todos los usuarios, sin contraseñas")
    public List<UsuarioAdminResponse> listar() {
        return usuarioService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea un usuario con su rol; la contraseña se guarda con BCrypt")
    public UsuarioAdminResponse crear(@Valid @RequestBody UsuarioAdminRequest solicitud,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal admin) {
        return usuarioService.crear(solicitud, admin.getId());
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Cambia el rol de un usuario; lo registra el trigger de auditoría")
    public UsuarioAdminResponse cambiarRol(@PathVariable("id") Long id,
            @Valid @RequestBody CambioRolRequest solicitud,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal admin) {
        return usuarioService.cambiarRol(id, solicitud.rol(), admin.getId());
    }

    @PutMapping("/{id}/active")
    @Operation(summary = "Activa o desactiva un usuario; un usuario inactivo no puede iniciar sesión")
    public UsuarioAdminResponse cambiarEstado(@PathVariable("id") Long id,
            @Valid @RequestBody CambioEstadoRequest solicitud,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal admin) {
        return usuarioService.cambiarEstado(id, solicitud.activo(), admin.getId());
    }
}
