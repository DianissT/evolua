package com.evolua.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evolua.adaptation.EstadisticaTema;
import com.evolua.adaptation.MotorAdaptativoService;
import com.evolua.dto.IntentoIniciado;
import com.evolua.dto.IntentoResponse;
import com.evolua.dto.IntentoResumenResponse;
import com.evolua.dto.OpcionIntentoResponse;
import com.evolua.dto.OpcionRevisionResponse;
import com.evolua.dto.PreguntaIntentoResponse;
import com.evolua.dto.RecomendacionResponse;
import com.evolua.dto.RespuestaRequest;
import com.evolua.dto.RespuestaResponse;
import com.evolua.dto.ResultadoIntentoResponse;
import com.evolua.dto.ResultadoTemaResponse;
import com.evolua.dto.RevisionPreguntaResponse;
import com.evolua.entity.EstadoIntento;
import com.evolua.entity.Evaluacion;
import com.evolua.entity.EvaluacionPregunta;
import com.evolua.entity.Intento;
import com.evolua.entity.OpcionPregunta;
import com.evolua.entity.Pregunta;
import com.evolua.entity.Respuesta;
import com.evolua.entity.Tema;
import com.evolua.entity.TipoIntento;
import com.evolua.exception.ConflictoException;
import com.evolua.exception.RecursoNoEncontradoException;
import com.evolua.exception.SolicitudInvalidaException;
import com.evolua.repository.EvaluacionPreguntaRepository;
import com.evolua.repository.EvaluacionRepository;
import com.evolua.repository.IntentoRepository;
import com.evolua.repository.MatriculaRepository;
import com.evolua.repository.OpcionPreguntaRepository;
import com.evolua.repository.RespuestaRepository;
import com.evolua.repository.UsuarioRepository;

/**
 * Intentos de evaluaciones fijas. Al iniciar un intento se crean sus filas de respuestas sin opción, que fijan
 * las preguntas asignadas. La calificación y la puntuación se calculan siempre en el servidor, al finalizar.
 */
@Service
public class IntentoService {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final IntentoRepository intentos;
    private final RespuestaRepository respuestas;
    private final EvaluacionRepository evaluaciones;
    private final EvaluacionPreguntaRepository vinculos;
    private final OpcionPreguntaRepository opciones;
    private final MatriculaRepository matriculas;
    private final UsuarioRepository usuarios;
    private final MotorAdaptativoService motor;
    private final RecomendacionService recomendaciones;

    public IntentoService(IntentoRepository intentos, RespuestaRepository respuestas,
            EvaluacionRepository evaluaciones, EvaluacionPreguntaRepository vinculos,
            OpcionPreguntaRepository opciones, MatriculaRepository matriculas, UsuarioRepository usuarios,
            MotorAdaptativoService motor, RecomendacionService recomendaciones) {
        this.intentos = intentos;
        this.respuestas = respuestas;
        this.evaluaciones = evaluaciones;
        this.vinculos = vinculos;
        this.opciones = opciones;
        this.matriculas = matriculas;
        this.usuarios = usuarios;
        this.motor = motor;
        this.recomendaciones = recomendaciones;
    }

    /**
     * Inicia un intento de la evaluación. Si el estudiante ya tiene uno en curso, lo retoma.
     */
    @Transactional
    public IntentoIniciado iniciar(Long evaluacionId, Long estudianteId) {
        Evaluacion evaluacion = evaluaciones.findById(evaluacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evaluación no encontrada"));
        if (!matriculas.existsByEstudianteIdAndCursoId(estudianteId, evaluacion.getCurso().getId())) {
            throw new AccessDeniedException("Acceso denegado");
        }
        if (!evaluacion.getActiva() || !evaluacion.getCurso().getActivo()) {
            throw new ConflictoException("La evaluación no está disponible");
        }
        Optional<Intento> enCurso = intentos.findByEstudianteIdAndEvaluacionIdAndEstado(estudianteId, evaluacionId,
                EstadoIntento.EN_CURSO);
        if (enCurso.isPresent()) {
            return new IntentoIniciado(vista(enCurso.get()), false);
        }
        if (evaluacion.getIntentosPermitidos() != null
                && intentos.countByEstudianteIdAndEvaluacionId(estudianteId, evaluacionId)
                        >= evaluacion.getIntentosPermitidos()) {
            throw new ConflictoException("Alcanzaste el número máximo de intentos permitidos");
        }
        List<Pregunta> activas = vinculos.findByEvaluacionIdOrderByOrden(evaluacionId).stream()
                .map(EvaluacionPregunta::getPregunta)
                .filter(Pregunta::getActiva)
                .toList();
        if (activas.isEmpty()) {
            throw new ConflictoException("La evaluación no tiene preguntas activas");
        }

        Intento intento = new Intento();
        intento.setEstudiante(usuarios.getReferenceById(estudianteId));
        intento.setEvaluacion(evaluacion);
        intento.setTipo(TipoIntento.EVALUACION);
        Intento guardado = intentos.save(intento);
        for (Pregunta pregunta : activas) {
            Respuesta fila = new Respuesta();
            fila.setIntento(guardado);
            fila.setPregunta(pregunta);
            respuestas.save(fila);
        }
        return new IntentoIniciado(vista(guardado), true);
    }

    @Transactional(readOnly = true)
    public IntentoResponse obtener(Long intentoId, Long estudianteId) {
        Intento intento = intentoPropio(intentoId, estudianteId);
        if (intento.getEstado() != EstadoIntento.EN_CURSO) {
            throw new ConflictoException("El intento ya finalizó: consulta su resultado");
        }
        return vista(intento);
    }

    @Transactional
    public RespuestaResponse registrarRespuesta(Long intentoId, RespuestaRequest solicitud, Long estudianteId) {
        Intento intento = intentoPropio(intentoId, estudianteId);
        exigirEnCurso(intento);
        Respuesta fila = respuestas.findByIntentoIdAndPreguntaId(intentoId, solicitud.preguntaId())
                .orElseThrow(() -> new SolicitudInvalidaException("La pregunta no pertenece al intento"));
        OpcionPregunta opcion = opciones.findById(solicitud.opcionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Opción no encontrada"));
        if (!opcion.getPregunta().getId().equals(solicitud.preguntaId())) {
            throw new SolicitudInvalidaException("La opción no pertenece a la pregunta");
        }
        fila.setOpcion(opcion);
        return new RespuestaResponse(solicitud.preguntaId(), opcion.getId());
    }

    /**
     * Califica el intento y lo finaliza. Todo ocurre en una sola transacción: las respuestas se califican y se
     * guardan antes de cambiar el intento a FINALIZADO, y a continuación el motor adaptativo actualiza el progreso
     * y genera las recomendaciones. Las preguntas sin responder cuentan como incorrectas.
     */
    @Transactional
    public ResultadoIntentoResponse finalizar(Long intentoId, Long estudianteId) {
        Intento intento = intentoPropio(intentoId, estudianteId);
        exigirEnCurso(intento);

        List<Respuesta> filas = respuestas.findByIntentoIdOrderById(intentoId);
        int correctas = 0;
        for (Respuesta fila : filas) {
            boolean acierto = fila.getOpcion() != null && Boolean.TRUE.equals(fila.getOpcion().getEsCorrecta());
            fila.setEsCorrecta(acierto);
            if (acierto) {
                correctas++;
            }
        }
        respuestas.saveAllAndFlush(filas);

        intento.setPuntuacion(calcularPuntuacion(correctas, filas.size()));
        intento.setFechaFin(LocalDateTime.now());
        intento.setEstado(EstadoIntento.FINALIZADO);
        intentos.saveAndFlush(intento);

        List<RecomendacionResponse> nuevas = motor.actualizarTrasIntento(estudianteId, temasDe(filas)).stream()
                .map(RecomendacionResponse::desde)
                .toList();
        return construirResultado(intento, filas, nuevas);
    }

    @Transactional(readOnly = true)
    public ResultadoIntentoResponse resultado(Long intentoId, Long estudianteId) {
        Intento intento = intentoPropio(intentoId, estudianteId);
        if (intento.getEstado() == EstadoIntento.EN_CURSO) {
            throw new ConflictoException("El intento aún no ha finalizado");
        }
        List<Respuesta> filas = respuestas.findByIntentoIdOrderById(intentoId);
        return construirResultado(intento, filas, recomendaciones.vigentesEnTemas(estudianteId, temasDe(filas)));
    }

    @Transactional(readOnly = true)
    public List<IntentoResumenResponse> historial(Long estudianteId) {
        return intentos.findByEstudianteIdOrderByFechaInicioDesc(estudianteId).stream()
                .map(IntentoResumenResponse::desde)
                .toList();
    }

    private Intento intentoPropio(Long intentoId, Long estudianteId) {
        Intento intento = intentos.findById(intentoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Intento no encontrado"));
        if (!intento.getEstudiante().getId().equals(estudianteId)) {
            throw new AccessDeniedException("Acceso denegado");
        }
        return intento;
    }

    private void exigirEnCurso(Intento intento) {
        if (intento.getEstado() != EstadoIntento.EN_CURSO) {
            throw new ConflictoException("El intento ya fue finalizado");
        }
    }

    private BigDecimal calcularPuntuacion(int correctas, int total) {
        if (total == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(correctas).multiply(CIEN).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private IntentoResponse vista(Intento intento) {
        List<Respuesta> filas = respuestas.findByIntentoIdOrderById(intento.getId());
        Map<Long, List<OpcionPregunta>> opcionesPorPregunta = opcionesDe(filas);
        List<PreguntaIntentoResponse> preguntas = filas.stream()
                .map(fila -> new PreguntaIntentoResponse(fila.getPregunta().getId(), fila.getPregunta().getEnunciado(),
                        fila.getPregunta().getTipo(),
                        opcionesPorPregunta.getOrDefault(fila.getPregunta().getId(), List.of()).stream()
                                .map(opcion -> new OpcionIntentoResponse(opcion.getId(), opcion.getTexto()))
                                .toList(),
                        fila.getOpcion() != null ? fila.getOpcion().getId() : null))
                .toList();
        return new IntentoResponse(intento.getId(), intento.getEvaluacion().getId(),
                intento.getEvaluacion().getNombre(), intento.getEstado(), intento.getFechaInicio(), preguntas);
    }

    private ResultadoIntentoResponse construirResultado(Intento intento, List<Respuesta> filas,
            List<RecomendacionResponse> recomendacionesDelIntento) {
        Map<Long, List<OpcionPregunta>> opcionesPorPregunta = opcionesDe(filas);
        List<RevisionPreguntaResponse> revision = new ArrayList<>();
        int correctas = 0;
        int sinResponder = 0;
        for (Respuesta fila : filas) {
            boolean acierto = Boolean.TRUE.equals(fila.getEsCorrecta());
            if (acierto) {
                correctas++;
            }
            if (fila.getOpcion() == null) {
                sinResponder++;
            }
            Pregunta pregunta = fila.getPregunta();
            revision.add(new RevisionPreguntaResponse(pregunta.getId(), pregunta.getEnunciado(), pregunta.getTipo(),
                    opcionesPorPregunta.getOrDefault(pregunta.getId(), List.of()).stream()
                            .map(opcion -> new OpcionRevisionResponse(opcion.getId(), opcion.getTexto(),
                                    Boolean.TRUE.equals(opcion.getEsCorrecta())))
                            .toList(),
                    fila.getOpcion() != null ? fila.getOpcion().getId() : null, acierto,
                    pregunta.getRetroalimentacion()));
        }
        int total = filas.size();
        return new ResultadoIntentoResponse(intento.getId(), intento.getEvaluacion().getId(),
                intento.getEvaluacion().getNombre(), intento.getEstado(), intento.getFechaInicio(),
                intento.getFechaFin(), intento.getPuntuacion(), total, correctas, total - correctas - sinResponder,
                sinResponder, revision, resultadosPorTema(filas), recomendacionesDelIntento);
    }

    private List<Long> temasDe(List<Respuesta> filas) {
        return filas.stream().map(fila -> fila.getPregunta().getTema().getId()).distinct().sorted().toList();
    }

    private List<ResultadoTemaResponse> resultadosPorTema(List<Respuesta> filas) {
        Map<Tema, List<Respuesta>> porTema = filas.stream()
                .collect(Collectors.groupingBy(fila -> fila.getPregunta().getTema(), LinkedHashMap::new,
                        Collectors.toList()));
        return porTema.entrySet().stream().map(entrada -> {
            int total = entrada.getValue().size();
            int correctas = (int) entrada.getValue().stream()
                    .filter(fila -> Boolean.TRUE.equals(fila.getEsCorrecta()))
                    .count();
            return new ResultadoTemaResponse(entrada.getKey().getId(), entrada.getKey().getNombre(), correctas, total,
                    EstadisticaTema.calcularPorcentaje(correctas, total));
        }).toList();
    }

    private Map<Long, List<OpcionPregunta>> opcionesDe(List<Respuesta> filas) {
        List<Long> ids = filas.stream().map(fila -> fila.getPregunta().getId()).toList();
        return opciones.findByPreguntaIdInOrderByPreguntaIdAscOrdenAsc(ids).stream()
                .collect(Collectors.groupingBy(opcion -> opcion.getPregunta().getId()));
    }
}
