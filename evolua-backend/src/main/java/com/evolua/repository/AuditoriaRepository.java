package com.evolua.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.evolua.entity.Auditoria;

/**
 * Acceso a datos de auditoria (Spring Data JPA).
 * <p>Hereda guardar, buscar, listar y eliminar.</p>
 */
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findAllByOrderByFechaDesc();
}
