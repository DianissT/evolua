package com.evolua.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evolua.entity.Tema;

/**
 * Repositorio de acceso a datos de la entidad Tema.
 */
public interface TemaRepository extends JpaRepository<Tema, Long> {

    List<Tema> findByCursoIdOrderByOrden(Long cursoId);

    List<Tema> findByCursoIdAndActivoTrueOrderByOrden(Long cursoId);
}
