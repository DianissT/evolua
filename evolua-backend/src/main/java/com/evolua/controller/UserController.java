package com.evolua.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evolua.dto.UsuarioResponse;
import com.evolua.security.UsuarioPrincipal;
import com.evolua.service.UsuarioService;

/**
 * Consulta del usuario autenticado
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UsuarioService usuarioService;

    public UserController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public UsuarioResponse perfil(@AuthenticationPrincipal UsuarioPrincipal usuario) {
        return usuarioService.obtenerPorId(usuario.getId());
    }
}
