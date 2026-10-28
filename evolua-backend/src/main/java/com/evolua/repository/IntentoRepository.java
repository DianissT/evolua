package com.evolua.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.evolua.entity.Intento;

/**
 * Acceso a datos de intentos (Spring Data JPA).
 * <p>Hereda guardar, buscar, listar y eliminar.</p>
 */
public interface IntentoRepository extends JpaRepository<Intento, Long> {

    List<Intento> findByEstudianteId(Long estudianteId);
}
