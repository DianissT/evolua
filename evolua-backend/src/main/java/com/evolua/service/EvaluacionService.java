package com.evolua.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evolua.dto.QuizRequest;
import com.evolua.dto.QuizResponse;
import com.evolua.dto.QuizResumenResponse;
import com.evolua.entity.Curso;
import com.evolua.entity.Evaluacion;
import com.evolua.entity.EvaluacionPregunta;
import com.evolua.entity.Pregunta;
import com.evolua.entity.Rol;
import com.evolua.entity.TipoEvaluacion;
import com.evolua.exception.ConflictoException;
import com.evolua.exception.RecursoNoEncontradoException;
import com.evolua.exception.SolicitudInvalidaException;
import com.evolua.repository.EvaluacionPreguntaRepository;
import com.evolua.repository.EvaluacionRepository;
import com.evolua.repository.IntentoRepository;
import com.evolua.repository.PreguntaRepository;
import com.evolua.security.UsuarioPrincipal;

/**
 * Gestión de evaluaciones fijas. Solo incluyen preguntas activas de temas de su propio curso, sin repetirlas.
 * Si la evaluación ya tiene intentos, sus preguntas no se pueden cambiar. Las evaluaciones no se eliminan:
 * se desactivan.
 */
@Service
public class EvaluacionService {

    private final EvaluacionRepository evaluaciones;
    private final EvaluacionPreguntaRepository vinculos;
    private final PreguntaRepository preguntas;
    private final IntentoRepository intentos;
    private final CursoService cursoService;
    private final AuditoriaService auditoria;

    public EvaluacionService(EvaluacionRepository evaluaciones, EvaluacionPreguntaRepository vinculos,
            PreguntaRepository preguntas, IntentoRepository intentos, CursoService cursoService,
            AuditoriaService auditoria) {
        this.evaluaciones = evaluaciones;
        this.vinculos = vinculos;
        this.preguntas = preguntas;
        this.intentos = intentos;
        this.cursoService = cursoService;
        this.auditoria = auditoria;
    }

    @Transactional
    public QuizResponse crear(QuizRequest solicitud, Long docenteId) {
        Curso curso = cursoService.cursoPropio(solicitud.cursoId(), docenteId);
        List<Pregunta> seleccion = validarPreguntas(curso.getId(), solicitud.preguntaIds());
        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setCurso(curso);
        evaluacion.setNombre(solicitud.nombre().trim());
        evaluacion.setTipo(solicitud.tipo() != null ? solicitud.tipo() : TipoEvaluacion.FORMATIVA);
        evaluacion.setIntentosPermitidos(solicitud.intentosPermitidos());
        Evaluacion guardada = evaluaciones.save(evaluacion);
        guardarVinculos(guardada, seleccion);
        auditoria.registrar(docenteId, "CREAR", "evaluaciones", guardada.getId(),
                "evaluación creada: " + guardada.getNombre());
        return QuizResponse.desde(guardada, vinculos.findByEvaluacionIdOrderByOrden(guardada.getId()));
    }

    @Transactional(readOnly = true)
    public QuizResponse obtener(Long id, Long docenteId) {
        Evaluacion evaluacion = evaluacionPropia(id, docenteId);
        return QuizResponse.desde(evaluacion, vinculos.findByEvaluacionIdOrderByOrden(id));
    }

    @Transactional(readOnly = true)
    public List<QuizResumenResponse> listarPorCurso(Long cursoId, UsuarioPrincipal usuario) {
        cursoService.cursoVisible(cursoId, usuario);
        boolean docente = usuario.getRol() == Rol.DOCENTE;
        List<Evaluacion> lista = docente
                ? evaluaciones.findByCursoId(cursoId)
                : evaluaciones.findByCursoIdAndActivaTrue(cursoId);
        return lista.stream()
                .map(e -> QuizResumenResponse.desde(e, vinculos.countByEvaluacionId(e.getId()),
                        docente ? null : intentos.countByEstudianteIdAndEvaluacionId(usuario.getId(), e.getId())))
                .toList();
    }

    @Transactional
    public QuizResponse actualizar(Long id, QuizRequest solicitud, Long docenteId) {
        Evaluacion evaluacion = evaluacionPropia(id, docenteId);
        if (!evaluacion.getCurso().getId().equals(solicitud.cursoId())) {
            throw new SolicitudInvalidaException("No se puede cambiar el curso de una evaluación");
        }
        List<Long> actuales = vinculos.findByEvaluacionIdOrderByOrden(id).stream()
                .map(vinculo -> vinculo.getPregunta().getId())
                .toList();
        boolean mismasPreguntas = actuales.equals(solicitud.preguntaIds());
        List<Pregunta> seleccion = null;
        if (!mismasPreguntas) {
            seleccion = validarPreguntas(evaluacion.getCurso().getId(), solicitud.preguntaIds());
            if (intentos.existsByEvaluacionId(id)) {
                throw new ConflictoException(
                        "La evaluación ya tiene intentos registrados: no se pueden cambiar sus preguntas");
            }
        }
        evaluacion.setNombre(solicitud.nombre().trim());
        if (solicitud.tipo() != null) {
            evaluacion.setTipo(solicitud.tipo());
        }
        evaluacion.setIntentosPermitidos(solicitud.intentosPermitidos());
        if (solicitud.activa() != null) {
            evaluacion.setActiva(solicitud.activa());
        }
        if (seleccion != null) {
            vinculos.eliminarPorEvaluacion(id);
            guardarVinculos(evaluacion, seleccion);
        }
        auditoria.registrar(docenteId, "ACTUALIZAR", "evaluaciones", id,
                "evaluación actualizada: " + evaluacion.getNombre());
        return QuizResponse.desde(evaluacion, vinculos.findByEvaluacionIdOrderByOrden(id));
    }

    @Transactional
    public void desactivar(Long id, Long docenteId) {
        Evaluacion evaluacion = evaluacionPropia(id, docenteId);
        evaluacion.setActiva(false);
        auditoria.registrar(docenteId, "DESACTIVAR", "evaluaciones", id,
                "evaluación desactivada: " + evaluacion.getNombre());
    }

    private Evaluacion evaluacionPropia(Long id, Long docenteId) {
        Evaluacion evaluacion = evaluaciones.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evaluación no encontrada"));
        cursoService.cursoPropio(evaluacion.getCurso().getId(), docenteId);
        return evaluacion;
    }

    private List<Pregunta> validarPreguntas(Long cursoId, List<Long> ids) {
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new SolicitudInvalidaException("No se pueden repetir preguntas en una evaluación");
        }
        Map<Long, Pregunta> porId = preguntas.findAllById(ids).stream()
                .collect(Collectors.toMap(Pregunta::getId, Function.identity()));
        return ids.stream().map(id -> {
            Pregunta pregunta = porId.get(id);
            if (pregunta == null) {
                throw new RecursoNoEncontradoException("Pregunta no encontrada: " + id);
            }
            if (!pregunta.getActiva()) {
                throw new SolicitudInvalidaException("La pregunta " + id + " está desactivada");
            }
            if (!pregunta.getTema().getCurso().getId().equals(cursoId)) {
                throw new SolicitudInvalidaException("La pregunta " + id + " no pertenece al curso de la evaluación");
            }
            return pregunta;
        }).toList();
    }

    private void guardarVinculos(Evaluacion evaluacion, List<Pregunta> seleccion) {
        for (int i = 0; i < seleccion.size(); i++) {
            EvaluacionPregunta vinculo = new EvaluacionPregunta();
            vinculo.setEvaluacion(evaluacion);
            vinculo.setPregunta(seleccion.get(i));
            vinculo.setOrden(i + 1);
            vinculos.save(vinculo);
        }
    }
}
