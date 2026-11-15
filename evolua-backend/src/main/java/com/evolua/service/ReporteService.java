package com.evolua.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evolua.adaptation.EstadisticaTema;
import com.evolua.dto.PreguntaDificilResponse;
import com.evolua.dto.RendimientoCursoResponse;
import com.evolua.dto.RendimientoEstudianteResponse;
import com.evolua.dto.RendimientoTemaResponse;
import com.evolua.entity.Curso;
import com.evolua.entity.Matricula;
import com.evolua.entity.Tema;
import com.evolua.exception.SolicitudInvalidaException;
import com.evolua.repository.ConteoPorClave;
import com.evolua.repository.MatriculaRepository;
import com.evolua.repository.ReporteRepository;
import com.evolua.repository.ResumenEstudianteCurso;
import com.evolua.repository.ResumenTema;
import com.evolua.repository.TemaRepository;

/**
 * Reportes de rendimiento para el docente. Un docente solo consulta los cursos de los que es responsable. Los
 * porcentajes se calculan con {@link EstadisticaTema}, la misma clase que usa el motor adaptativo, para que los
 * números del reporte coincidan con los del progreso del estudiante.
 */
@Service
public class ReporteService {

    /** Respuestas mínimas que debe tener una pregunta para aparecer en el reporte de preguntas difíciles. */
    public static final int MINIMO_RESPUESTAS_POR_DEFECTO = 3;
    private static final int MAXIMO_PREGUNTAS_DIFICILES = 10;

    private final CursoService cursos;
    private final TemaRepository temas;
    private final MatriculaRepository matriculas;
    private final ReporteRepository reportes;

    public ReporteService(CursoService cursos, TemaRepository temas, MatriculaRepository matriculas,
            ReporteRepository reportes) {
        this.cursos = cursos;
        this.temas = temas;
        this.matriculas = matriculas;
        this.reportes = reportes;
    }

    @Transactional(readOnly = true)
    public RendimientoCursoResponse rendimiento(Long cursoId, Long docenteId) {
        Curso curso = cursos.cursoPropio(cursoId, docenteId);

        Map<Long, ResumenTema> porTema = reportes.resumenPorTema(cursoId).stream()
                .collect(Collectors.toMap(ResumenTema::getTemaId, Function.identity()));
        Map<Long, Long> refuerzoPorTema = conteos(reportes.estudiantesEnRefuerzoPorTema(cursoId));
        List<RendimientoTemaResponse> filasTema = temas.findByCursoIdAndActivoTrueOrderByOrden(cursoId).stream()
                .map(tema -> filaTema(tema, porTema.get(tema.getId()), refuerzoPorTema.getOrDefault(tema.getId(), 0L)))
                .toList();

        Map<Long, ResumenEstudianteCurso> porEstudiante = reportes.resumenPorEstudiante(cursoId).stream()
                .collect(Collectors.toMap(ResumenEstudianteCurso::getEstudianteId, Function.identity()));
        Map<Long, Long> refuerzoPorEstudiante = conteos(reportes.temasEnRefuerzoPorEstudiante(cursoId));
        List<RendimientoEstudianteResponse> filasEstudiante = matriculas.findByCursoIdOrderByFechaMatricula(cursoId)
                .stream()
                .map(matricula -> filaEstudiante(matricula, porEstudiante.get(matricula.getEstudiante().getId()),
                        refuerzoPorEstudiante.getOrDefault(matricula.getEstudiante().getId(), 0L)))
                .toList();

        long intentos = filasEstudiante.stream().mapToLong(RendimientoEstudianteResponse::intentosFinalizados).sum();
        BigDecimal suma = filasEstudiante.stream()
                .map(fila -> porEstudiante.get(fila.estudianteId()))
                .filter(resumen -> resumen != null && resumen.getSuma() != null)
                .map(ResumenEstudianteCurso::getSuma)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int conActividad = (int) filasEstudiante.stream().filter(fila -> fila.intentosFinalizados() > 0).count();
        return new RendimientoCursoResponse(curso.getId(), curso.getNombre(), filasEstudiante.size(), conActividad,
                intentos, promedio(suma, intentos), filasTema, filasEstudiante);
    }

    /**
     * Preguntas con menor porcentaje de aciertos (top 10)). Solo se consideran las que recibieron al
     * menos {@code minimoRespuestas} respuestas, para no sacar conclusiones con muy pocos datos. Si {@code temaId} no
     * es nulo, solo se consideran las preguntas de ese tema.
     */
    @Transactional(readOnly = true)
    public List<PreguntaDificilResponse> preguntasDificiles(Long cursoId, Long docenteId, int minimoRespuestas,
            Long temaId) {
        if (minimoRespuestas < 1) {
            throw new SolicitudInvalidaException("El mínimo de respuestas debe ser al menos 1");
        }
        cursos.cursoPropio(cursoId, docenteId);
        return reportes.resumenPorPregunta(cursoId, minimoRespuestas).stream()
                .filter(resumen -> temaId == null || temaId.equals(resumen.getTemaId()))
                .map(resumen -> new PreguntaDificilResponse(resumen.getPreguntaId(), resumen.getEnunciado(),
                        resumen.getTemaId(), resumen.getTemaNombre(), resumen.getDificultad(), resumen.getTotal(),
                        resumen.getCorrectas(), porcentaje(resumen.getCorrectas(), resumen.getTotal())))
                .sorted(Comparator.comparing(PreguntaDificilResponse::porcentajeAciertos)
                        .thenComparing(Comparator.comparingLong(PreguntaDificilResponse::totalRespuestas).reversed()))
                .limit(MAXIMO_PREGUNTAS_DIFICILES)
                .toList();
    }

    private RendimientoTemaResponse filaTema(Tema tema, ResumenTema resumen, long enRefuerzo) {
        long total = resumen != null ? resumen.getTotal() : 0;
        long correctas = resumen != null && resumen.getCorrectas() != null ? resumen.getCorrectas() : 0;
        return new RendimientoTemaResponse(tema.getId(), tema.getNombre(), total, correctas,
                porcentaje(correctas, total), enRefuerzo);
    }

    private RendimientoEstudianteResponse filaEstudiante(Matricula matricula, ResumenEstudianteCurso resumen,
            long enRefuerzo) {
        var estudiante = matricula.getEstudiante();
        long intentos = resumen != null ? resumen.getIntentos() : 0;
        BigDecimal promedio = resumen != null && resumen.getSuma() != null ? promedio(resumen.getSuma(), intentos) : null;
        return new RendimientoEstudianteResponse(estudiante.getId(), estudiante.getNombre(), estudiante.getCorreo(),
                intentos, promedio, resumen != null ? resumen.getUltimaActividad() : null, enRefuerzo);
    }

    private static Map<Long, Long> conteos(List<ConteoPorClave> lista) {
        return lista.stream().collect(Collectors.toMap(ConteoPorClave::getClave, ConteoPorClave::getTotal));
    }

    private static BigDecimal porcentaje(long correctas, long total) {
        return EstadisticaTema.calcularPorcentaje((int) correctas, (int) total);
    }

    /** Promedio con dos decimales; null si no hay intentos. */
    private static BigDecimal promedio(BigDecimal suma, long intentos) {
        if (intentos == 0) {
            return null;
        }
        return suma.divide(BigDecimal.valueOf(intentos), 2, RoundingMode.HALF_UP);
    }
}
