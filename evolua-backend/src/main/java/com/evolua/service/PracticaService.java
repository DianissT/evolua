package com.evolua.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evolua.adaptation.RangoDificultad;
import com.evolua.adaptation.SelectorPreguntas;
import com.evolua.adaptation.SelectorPreguntas.Candidata;
import com.evolua.dto.IntentoIniciado;
import com.evolua.dto.ModalidadPractica;
import com.evolua.dto.PracticaRequest;
import com.evolua.dto.RecomendacionResponse;
import com.evolua.entity.EstadoIntento;
import com.evolua.entity.Intento;
import com.evolua.entity.Pregunta;
import com.evolua.entity.Respuesta;
import com.evolua.entity.Tema;
import com.evolua.entity.TipoActividad;
import com.evolua.entity.TipoIntento;
import com.evolua.exception.ConflictoException;
import com.evolua.exception.RecursoNoEncontradoException;
import com.evolua.exception.SolicitudInvalidaException;
import com.evolua.repository.IntentoRepository;
import com.evolua.repository.MatriculaRepository;
import com.evolua.repository.PreguntaRepository;
import com.evolua.repository.RespuestaRepository;
import com.evolua.repository.TemaRepository;
import com.evolua.repository.UltimaVezPregunta;
import com.evolua.repository.UsuarioRepository;

/**
 * Práctica por tema. Crea un intento de tipo PRACTICA con las preguntas que elige el
 * {@link SelectorPreguntas}, luego se responde, finaliza y consulta con los mismos servicios de
 * {@link IntentoService}, y al finalizar alimenta al motor adaptativo igual que una evaluación.
 *
 * <p>Modalidades: en la AUTOMATICA la dificultad sale de la recomendación vigente del tema (o dificultad media si
 * aún no hay ninguna) y se completa con las dificultades más cercanas, mientras que, en la LIBRE el estudiante fija la
 * dificultad, que se respeta de forma estricta, y la cantidad de preguntas.</p>
 */
@Service
public class PracticaService {

    private final IntentoRepository intentos;
    private final RespuestaRepository respuestas;
    private final PreguntaRepository preguntas;
    private final TemaRepository temas;
    private final MatriculaRepository matriculas;
    private final UsuarioRepository usuarios;
    private final RecomendacionService recomendaciones;
    private final ConfiguracionService configuracion;
    private final IntentoService intentoService;
    private final SelectorPreguntas selector = new SelectorPreguntas();

    public PracticaService(IntentoRepository intentos, RespuestaRepository respuestas, PreguntaRepository preguntas,
            TemaRepository temas, MatriculaRepository matriculas, UsuarioRepository usuarios,
            RecomendacionService recomendaciones, ConfiguracionService configuracion,
            IntentoService intentoService) {
        this.intentos = intentos;
        this.respuestas = respuestas;
        this.preguntas = preguntas;
        this.temas = temas;
        this.matriculas = matriculas;
        this.usuarios = usuarios;
        this.recomendaciones = recomendaciones;
        this.configuracion = configuracion;
        this.intentoService = intentoService;
    }

    /**
     * Inicia o retoma una práctica del tema.
     *
     * @param solicitud puede ser {@code null}: equivale a la modalidad AUTOMATICA
     */
    @Transactional
    public IntentoIniciado iniciar(Long temaId, PracticaRequest solicitud, Long estudianteId) {
        PracticaRequest pedido = solicitud != null ? solicitud : new PracticaRequest(ModalidadPractica.AUTOMATICA, null, null);
        validar(pedido);

        Tema tema = temas.findById(temaId).orElseThrow(() -> new RecursoNoEncontradoException("Tema no encontrado"));
        if (!matriculas.existsByEstudianteIdAndCursoId(estudianteId, tema.getCurso().getId())) {
            throw new AccessDeniedException("Acceso denegado");
        }
        if (!tema.getActivo() || !tema.getCurso().getActivo()) {
            throw new ConflictoException("El tema no está disponible para practicar");
        }
        Optional<Intento> enCurso = intentos.findFirstByEstudianteIdAndTemaIdAndTipoAndEstadoOrderByFechaInicioDesc(
                estudianteId, temaId, TipoIntento.PRACTICA, EstadoIntento.EN_CURSO);
        if (enCurso.isPresent()) {
            return new IntentoIniciado(intentoService.vista(enCurso.get()), false);
        }

        boolean libre = pedido.modalidad() == ModalidadPractica.LIBRE;
        boolean estricto = libre && pedido.dificultad() != null;
        RangoDificultad rango = rangoPara(pedido, estudianteId, temaId);
        int cantidad = pedido.cantidad() != null ? pedido.cantidad() : configuracion.preguntasPorPractica();

        List<Pregunta> activas = preguntas.findByTemaIdAndActivaTrue(temaId);
        Map<Long, LocalDateTime> ultimaVez = respuestas.ultimaVezPorPregunta(estudianteId, temaId).stream()
                .collect(Collectors.toMap(UltimaVezPregunta::getPreguntaId, UltimaVezPregunta::getUltimaVez));
        List<Candidata> candidatas = activas.stream()
                .map(pregunta -> new Candidata(pregunta.getId(), pregunta.getDificultad(),
                        ultimaVez.get(pregunta.getId())))
                .toList();
        List<Long> elegidas = selector.seleccionar(candidatas, rango, cantidad, estricto);
        if (elegidas.isEmpty()) {
            throw new ConflictoException(estricto
                    ? "El tema no tiene preguntas activas de la dificultad elegida"
                    : "El tema no tiene preguntas activas");
        }

        Intento intento = new Intento();
        intento.setEstudiante(usuarios.getReferenceById(estudianteId));
        intento.setTema(tema);
        intento.setTipo(TipoIntento.PRACTICA);
        Intento guardado = intentos.save(intento);
        Map<Long, Pregunta> porId = activas.stream().collect(Collectors.toMap(Pregunta::getId, Function.identity()));
        for (Long preguntaId : elegidas) {
            Respuesta fila = new Respuesta();
            fila.setIntento(guardado);
            fila.setPregunta(porId.get(preguntaId));
            respuestas.save(fila);
        }
        return new IntentoIniciado(intentoService.vista(guardado), true);
    }

    private void validar(PracticaRequest pedido) {
        if (pedido.modalidad() == ModalidadPractica.AUTOMATICA
                && (pedido.dificultad() != null || pedido.cantidad() != null)) {
            throw new SolicitudInvalidaException(
                    "La práctica automática no admite dificultad ni cantidad: usa la modalidad libre");
        }
    }

    /**
     * Rango de dificultad de la práctica. Automática: el de la recomendación vigente del tema, o dificultad media si
     * aún no hay recomendación. Libre: la dificultad elegida o, si no eligió ninguna, las tres.
     */
    private RangoDificultad rangoPara(PracticaRequest pedido, Long estudianteId, Long temaId) {
        if (pedido.modalidad() == ModalidadPractica.LIBRE) {
            return pedido.dificultad() != null
                    ? new RangoDificultad(pedido.dificultad(), pedido.dificultad())
                    : new RangoDificultad(1, 3);
        }
        List<RecomendacionResponse> vigentes = recomendaciones.vigentesEnTemas(estudianteId, List.of(temaId));
        TipoActividad actividad = vigentes.isEmpty() ? TipoActividad.PRACTICA_ADICIONAL
                : vigentes.get(0).tipoActividad();
        return RangoDificultad.de(actividad);
    }
}
