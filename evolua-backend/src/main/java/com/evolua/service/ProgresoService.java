package com.evolua.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evolua.dto.ProgresoTemaResponse;
import com.evolua.entity.Curso;
import com.evolua.entity.Matricula;
import com.evolua.entity.ProgresoTema;
import com.evolua.entity.Tema;
import com.evolua.repository.MatriculaRepository;
import com.evolua.repository.ProgresoTemaRepository;
import com.evolua.repository.TemaRepository;

/**
 * Consulta del progreso de un estudiante por tema.
 */
@Service
public class ProgresoService {

    private final ProgresoTemaRepository progresos;
    private final MatriculaRepository matriculas;
    private final TemaRepository temas;

    public ProgresoService(ProgresoTemaRepository progresos, MatriculaRepository matriculas, TemaRepository temas) {
        this.progresos = progresos;
        this.matriculas = matriculas;
        this.temas = temas;
    }

    /**
     * Progreso del estudiante en todos los temas activos de sus cursos activos; los temas sin intentos
     * aparecen con nivel SIN_DATOS.
     */
    @Transactional(readOnly = true)
    public List<ProgresoTemaResponse> listarMio(Long estudianteId) {
        Map<Long, ProgresoTema> guardado = progresos.findByEstudianteId(estudianteId).stream()
                .collect(Collectors.toMap(progreso -> progreso.getTema().getId(), Function.identity()));
        List<ProgresoTemaResponse> lista = new ArrayList<>();
        for (Matricula matricula : matriculas.findByEstudianteId(estudianteId)) {
            Curso curso = matricula.getCurso();
            if (!curso.getActivo()) {
                continue;
            }
            for (Tema tema : temas.findByCursoIdAndActivoTrueOrderByOrden(curso.getId())) {
                ProgresoTema progreso = guardado.get(tema.getId());
                lista.add(progreso != null ? ProgresoTemaResponse.desde(progreso) : ProgresoTemaResponse.sinDatos(tema));
            }
        }
        return lista;
    }
}
