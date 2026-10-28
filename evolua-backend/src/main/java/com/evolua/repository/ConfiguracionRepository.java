package com.evolua.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.evolua.entity.Configuracion;

/**
 * Acceso a datos de configuracion (Spring Data JPA).
 * <p>Hereda guardar, buscar, listar y eliminar.</p>
 */
public interface ConfiguracionRepository extends JpaRepository<Configuracion, String> {
}
