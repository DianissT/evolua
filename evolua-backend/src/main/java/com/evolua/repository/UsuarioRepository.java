package com.evolua.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.evolua.entity.Usuario;

/**
 * Acceso a datos de usuarios (Spring Data JPA).
 * <p>Hereda guardar, buscar, listar y eliminar.</p>
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}
