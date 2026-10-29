package com.evolua.service;

import org.springframework.stereotype.Service;

import com.evolua.dto.UsuarioResponse;
import com.evolua.exception.RecursoNoEncontradoException;
import com.evolua.repository.UsuarioRepository;

/**
 * Consulta de usuarios
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;

    public UsuarioService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public UsuarioResponse obtenerPorId(Long id) {
        return usuarios.findById(id)
                .map(UsuarioResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}
