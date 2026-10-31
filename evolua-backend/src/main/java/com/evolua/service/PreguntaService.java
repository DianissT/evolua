package com.evolua.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evolua.dto.OpcionRequest;
import com.evolua.dto.PreguntaRequest;
import com.evolua.dto.PreguntaResponse;
import com.evolua.entity.OpcionPregunta;
import com.evolua.entity.Pregunta;
import com.evolua.entity.Tema;
import com.evolua.entity.TipoPregunta;
import com.evolua.exception.ConflictoException;
import com.evolua.exception.RecursoNoEncontradoException;
import com.evolua.exception.SolicitudInvalidaException;
import com.evolua.repository.OpcionPreguntaRepository;
import com.evolua.repository.PreguntaRepository;
import com.evolua.repository.RespuestaRepository;

/**
 * Gestión del banco de preguntas y sus opciones. Una pregunta con respuestas registradas no cambia su contenido:
 * se desactiva y se crea una nueva. Las preguntas no se eliminan: se desactivan.
 */
@Service
public class PreguntaService {

    private static final int MAXIMO_OPCIONES = 6;

    private final PreguntaRepository preguntas;
    private final OpcionPreguntaRepository opciones;
    private final RespuestaRepository respuestas;
    private final TemaService temaService;
    private final ContextoAuditoria contexto;
    private final AuditoriaService auditoria;

    public PreguntaService(PreguntaRepository preguntas, OpcionPreguntaRepository opciones,
            RespuestaRepository respuestas, TemaService temaService, ContextoAuditoria contexto,
            AuditoriaService auditoria) {
        this.preguntas = preguntas;
        this.opciones = opciones;
        this.respuestas = respuestas;
        this.temaService = temaService;
        this.contexto = contexto;
        this.auditoria = auditoria;
    }

    @Transactional
    public PreguntaResponse crear(PreguntaRequest solicitud, Long docenteId) {
        Tema tema = temaService.temaPropio(solicitud.temaId(), docenteId);
        validarOpciones(solicitud.tipo(), solicitud.opciones());
        Pregunta pregunta = new Pregunta();
        pregunta.setTema(tema);
        pregunta.setEnunciado(solicitud.enunciado().trim());
        pregunta.setDificultad(solicitud.dificultad());
        pregunta.setTipo(solicitud.tipo());
        pregunta.setRetroalimentacion(solicitud.retroalimentacion());
        if (solicitud.activa() != null) {
            pregunta.setActiva(solicitud.activa());
        }
        Pregunta guardada = preguntas.save(pregunta);
        List<OpcionPregunta> guardadas = guardarOpciones(guardada, solicitud.opciones());
        auditoria.registrar(docenteId, "CREAR", "preguntas", guardada.getId(),
                "pregunta creada en el tema " + tema.getNombre());
        return PreguntaResponse.desde(guardada, guardadas);
    }

    @Transactional(readOnly = true)
    public PreguntaResponse obtener(Long id, Long docenteId) {
        Pregunta pregunta = preguntaPropia(id, docenteId);
        return PreguntaResponse.desde(pregunta, opciones.findByPreguntaIdOrderByOrden(id));
    }

    @Transactional(readOnly = true)
    public List<PreguntaResponse> listarPorTema(Long temaId, Long docenteId) {
        temaService.temaPropio(temaId, docenteId);
        return preguntas.findByTemaIdOrderById(temaId).stream()
                .map(p -> PreguntaResponse.desde(p, opciones.findByPreguntaIdOrderByOrden(p.getId())))
                .toList();
    }

    @Transactional
    public PreguntaResponse actualizar(Long id, PreguntaRequest solicitud, Long docenteId) {
        Pregunta pregunta = preguntaPropia(id, docenteId);
        Tema tema = solicitud.temaId().equals(pregunta.getTema().getId())
                ? pregunta.getTema()
                : temaService.temaPropio(solicitud.temaId(), docenteId);
        validarOpciones(solicitud.tipo(), solicitud.opciones());

        List<OpcionPregunta> actuales = opciones.findByPreguntaIdOrderByOrden(id);
        boolean mismasOpciones = coinciden(actuales, solicitud.opciones());
        boolean mismoContenido = mismasOpciones
                && pregunta.getEnunciado().equals(solicitud.enunciado().trim())
                && pregunta.getDificultad().equals(solicitud.dificultad())
                && pregunta.getTipo() == solicitud.tipo()
                && pregunta.getTema().getId().equals(tema.getId());
        if (!mismoContenido && respuestas.existsByPreguntaId(id)) {
            throw new ConflictoException("La pregunta ya tiene respuestas registradas: desactívala y crea una nueva");
        }

        contexto.registrarUsuario(docenteId);
        pregunta.setTema(tema);
        pregunta.setEnunciado(solicitud.enunciado().trim());
        pregunta.setDificultad(solicitud.dificultad());
        pregunta.setTipo(solicitud.tipo());
        pregunta.setRetroalimentacion(solicitud.retroalimentacion());
        if (solicitud.activa() != null) {
            pregunta.setActiva(solicitud.activa());
        }
        List<OpcionPregunta> resultado = actuales;
        if (!mismasOpciones) {
            opciones.eliminarPorPregunta(id);
            resultado = guardarOpciones(pregunta, solicitud.opciones());
        }
        return PreguntaResponse.desde(pregunta, resultado);
    }

    @Transactional
    public void desactivar(Long id, Long docenteId) {
        Pregunta pregunta = preguntaPropia(id, docenteId);
        contexto.registrarUsuario(docenteId);
        pregunta.setActiva(false);
    }

    private Pregunta preguntaPropia(Long id, Long docenteId) {
        Pregunta pregunta = preguntas.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pregunta no encontrada"));
        temaService.temaPropio(pregunta.getTema().getId(), docenteId);
        return pregunta;
    }

    private void validarOpciones(TipoPregunta tipo, List<OpcionRequest> solicitadas) {
        if (tipo == TipoPregunta.VERDADERO_FALSO && solicitadas.size() != 2) {
            throw new SolicitudInvalidaException("Una pregunta de verdadero o falso debe tener exactamente 2 opciones");
        }
        if (tipo == TipoPregunta.SELECCION_MULTIPLE && (solicitadas.size() < 2 || solicitadas.size() > MAXIMO_OPCIONES)) {
            throw new SolicitudInvalidaException(
                    "Una pregunta de selección múltiple debe tener entre 2 y " + MAXIMO_OPCIONES + " opciones");
        }
        long correctas = solicitadas.stream().filter(OpcionRequest::esCorrecta).count();
        if (correctas != 1) {
            throw new SolicitudInvalidaException("La pregunta debe tener exactamente una opción correcta");
        }
    }

    private boolean coinciden(List<OpcionPregunta> actuales, List<OpcionRequest> solicitadas) {
        if (actuales.size() != solicitadas.size()) {
            return false;
        }
        for (int i = 0; i < actuales.size(); i++) {
            if (!actuales.get(i).getTexto().equals(solicitadas.get(i).texto().trim())
                    || !actuales.get(i).getEsCorrecta().equals(solicitadas.get(i).esCorrecta())) {
                return false;
            }
        }
        return true;
    }

    private List<OpcionPregunta> guardarOpciones(Pregunta pregunta, List<OpcionRequest> solicitadas) {
        List<OpcionPregunta> nuevas = new ArrayList<>();
        for (int i = 0; i < solicitadas.size(); i++) {
            OpcionPregunta opcion = new OpcionPregunta();
            opcion.setPregunta(pregunta);
            opcion.setTexto(solicitadas.get(i).texto().trim());
            opcion.setEsCorrecta(solicitadas.get(i).esCorrecta());
            opcion.setOrden(i + 1);
            nuevas.add(opcion);
        }
        return opciones.saveAll(nuevas);
    }
}
