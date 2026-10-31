package com.evolua.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evolua.dto.UsuarioResponse;
import com.evolua.security.UsuarioPrincipal;
import com.evolua.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Consulta del usuario autenticado.
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UsuarioService usuarioService;

    public UserController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    @Operation(summary = "Devuelve los datos del usuario autenticado")
    public UsuarioResponse perfil(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return usuarioService.obtenerPorId(usuario.getId());
    }
}
