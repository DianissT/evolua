package com.evolua.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.evolua.entity.Curso;
import com.evolua.entity.Rol;
import com.evolua.entity.Tema;
import com.evolua.entity.Usuario;

/**
 * Comprueba que las entidades JPA se conectan a 
 * PostgreSQL y leen los datos de prueba
 */
@SpringBootTest
@Transactional
class RepositoriosTest {

    @Autowired private UsuarioRepository usuarios;
    @Autowired private CursoRepository cursos;
    @Autowired private TemaRepository temas;
    @Autowired private PreguntaRepository preguntas;
    @Autowired private OpcionPreguntaRepository opciones;
    @Autowired private EvaluacionPreguntaRepository evaluacionPreguntas;
    @Autowired private ConfiguracionRepository configuracion;

    @Test
    void elSeedEstaCargado() {
        assertEquals(5, usuarios.count());
        assertEquals(1, cursos.count());
        assertEquals(3, temas.count());
        assertEquals(36, preguntas.count());
        assertEquals(132, opciones.count());
        assertEquals(15, evaluacionPreguntas.count());
        assertEquals(5, configuracion.count());
    }

    @Test
    void seBuscaUnUsuarioPorCorreo() {
        Usuario ana = usuarios.findByCorreo("ana.perez@uni.edu").orElseThrow();
        assertEquals("Ana Pérez", ana.getNombre());
        assertEquals(Rol.ESTUDIANTE, ana.getRol());
        assertTrue(ana.getActivo());
        assertTrue(usuarios.existsByCorreo("laura.gomez@uni.edu"));
        assertFalse(usuarios.existsByCorreo("nadie@uni.edu"));
    }

    @Test
    void seNavegaDeUnCursoASuDocente() {
        Curso curso = cursos.findByActivoTrue().get(0);
        assertEquals("Fundamentos de Programación", curso.getNombre());
        assertEquals("Laura Gómez", curso.getDocente().getNombre());   // relación @ManyToOne LAZY
        assertEquals(1, cursos.findByDocenteId(curso.getDocente().getId()).size());
    }

    @Test
    void lasConsultasDerivadasFuncionan() {
        Curso curso = cursos.findAll().get(0);
        List<Tema> lista = temas.findByCursoIdOrderByOrden(curso.getId());
        assertEquals(List.of("Variables", "Condicionales", "Ciclos"),
                lista.stream().map(Tema::getNombre).toList());

        Long idTema = lista.get(0).getId();
        assertEquals(12, preguntas.findByTemaIdAndActivaTrue(idTema).size());

        Long idPregunta = preguntas.findByTemaIdAndActivaTrue(idTema).get(0).getId();
        assertEquals(4, opciones.findByPreguntaIdOrderByOrden(idPregunta).size());
    }

    @Test
    void laConfiguracionSeLeePorClave() {
        assertEquals("60", configuracion.findById("umbral_refuerzo").orElseThrow().getValor());
    }
}
