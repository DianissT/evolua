package com.evolua.repository;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.evolua.entity.Auditoria;

/**
 * Acceso a datos de auditoria (Spring Data JPA). Hereda guardar, buscar, listar y eliminar.
 */
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    /** Carga el usuario junto con cada registro para evitar una consulta adicional por fila. */
    @EntityGraph(attributePaths = "usuario")
    List<Auditoria> findAllByOrderByFechaDesc();
}
