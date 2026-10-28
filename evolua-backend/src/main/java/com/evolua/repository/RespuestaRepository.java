package com.evolua.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.evolua.entity.Respuesta;

/**
 * Acceso a datos de respuestas (Spring Data JPA).
 * <p>Hereda guardar, buscar, listar y eliminar.</p>
 */
public interface RespuestaRepository extends JpaRepository<Respuesta, Long> {

    List<Respuesta> findByIntentoId(Long intentoId);
}
