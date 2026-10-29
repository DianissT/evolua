package com.evolua.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.evolua.dto.LoginRequest;
import com.evolua.dto.LoginResponse;
import com.evolua.dto.UsuarioResponse;
import com.evolua.security.JwtService;
import com.evolua.security.UsuarioPrincipal;

/**
 *Inicio de sesión: verifica las credenciales y emite el token de acceso.
 */
@Service
public class AuthService {

    private static final String TIPO_TOKEN = "Bearer";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest solicitud) {
        String correo = solicitud.correo().trim().toLowerCase();
        Authentication autenticacion = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(correo, solicitud.password()));
        UsuarioPrincipal usuario = (UsuarioPrincipal) autenticacion.getPrincipal();
        return new LoginResponse(jwtService.generarToken(usuario), TIPO_TOKEN, jwtService.getExpiracionSegundos(),
                UsuarioResponse.desde(usuario));
    }
}
