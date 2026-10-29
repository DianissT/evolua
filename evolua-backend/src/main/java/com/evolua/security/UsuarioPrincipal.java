package com.evolua.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.evolua.entity.Rol;
import com.evolua.entity.Usuario;

/**
 * Usuario autenticado. Adapta la entidad {@link Usuario} al contrato de Spring Security;
 * el rol se expone como la autoridad <code>ROLE_</code> seguida del nombre del rol.
 */
public class UsuarioPrincipal implements UserDetails {

    private final Long id;
    private final String nombre;
    private final String correo;
    private final String passwordHash;
    private final Rol rol;
    private final boolean activo;

    public UsuarioPrincipal(Long id, String nombre, String correo, String passwordHash, Rol rol, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = activo;
    }

    public UsuarioPrincipal(Usuario usuario) {
        this(usuario.getId(), usuario.getNombre(), usuario.getCorreo(), usuario.getPasswordHash(),
                usuario.getRol(), usuario.getActivo());
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Rol getRol() {
        return rol;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
