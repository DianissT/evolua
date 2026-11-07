package com.evolua.adaptation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evolua.entity.ProgresoTema;
import com.evolua.entity.Recomendacion;
import com.evolua.entity.Tema;
import com.evolua.exception.RecursoNoEncontradoException;
import com.evolua.repository.ProgresoTemaRepository;
import com.evolua.repository.RecomendacionRepository;
import com.evolua.repository.RespuestaRepository;
import com.evolua.repository.ResumenIntentoTema;
import com.evolua.repository.ResumenTema;
import com.evolua.repository.TemaRepository;
import com.evolua.repository.UsuarioRepository;
import com.evolua.service.ConfiguracionService;

/**
 * Motor adaptativo: después de finalizar un intento recalcula el desempeño del estudiante en cada tema
 * afectado, actualiza su progreso y genera una recomendación con su motivo. Participa en la transacción de
 * quien lo invoca, de modo que calificar, actualizar el progreso y recomendar se guarda todo o nada.
 *
 * <p>El desempeño se recalcula desde las respuestas de todos los intentos finalizados, en lugar de sumar al
 * valor anterior, para que el progreso siempre sea consistente con los datos registrados.</p>
 */
@Service
public class MotorAdaptativoService {

    private final RespuestaRepository respuestas;
    private final ProgresoTemaRepository progresos;
    private final RecomendacionRepository recomendaciones;
    private final TemaRepository temas;
    private final UsuarioRepository usuarios;
    private final ConfiguracionService configuracion;

    public MotorAdaptativoService(RespuestaRepository respuestas, ProgresoTemaRepository progresos,
            RecomendacionRepository recomendaciones, TemaRepository temas, UsuarioRepository usuarios,
            ConfiguracionService configuracion) {
        this.respuestas = respuestas;
        this.progresos = progresos;
        this.recomendaciones = recomendaciones;
        this.temas = temas;
        this.usuarios = usuarios;
        this.configuracion = configuracion;
    }

    /**
     * Actualiza el progreso y las recomendaciones del estudiante en los temas indicados.
     *
     * @return las recomendaciones recién creadas, una por tema
     */
    @Transactional
    public List<Recomendacion> actualizarTrasIntento(Long estudianteId, Collection<Long> temaIds) {
        if (temaIds.isEmpty()) {
            return List.of();
        }
        ParametrosMotor parametros = configuracion.parametrosMotor();
        ClasificadorTema clasificador = new ClasificadorTema(parametros);
        Map<Long, ResumenTema> acumulado = respuestas.resumenPorTema(estudianteId, temaIds).stream()
                .collect(Collectors.toMap(ResumenTema::getTemaId, Function.identity()));
        Map<Long, Integer> satisfactorios = contarSatisfactorios(estudianteId, temaIds, parametros);

        List<Recomendacion> nuevas = new ArrayList<>();
        for (Long temaId : temaIds) {
            Tema tema = temas.findById(temaId).orElseThrow(() -> new RecursoNoEncontradoException("Tema no encontrado"));
            ResumenTema resumen = acumulado.get(temaId);
            int total = resumen != null ? resumen.getTotal().intValue() : 0;
            int correctas = resumen != null ? resumen.getCorrectas().intValue() : 0;
            EstadisticaTema estadistica = new EstadisticaTema(total, correctas, satisfactorios.getOrDefault(temaId, 0));
            Clasificacion clasificacion = clasificador.clasificar(estadistica);

            guardarProgreso(estudianteId, tema, estadistica, clasificacion);
            nuevas.add(guardarRecomendacion(estudianteId, tema, clasificacion,
                    clasificador.motivo(tema.getNombre(), estadistica, clasificacion)));
        }
        return nuevas;
    }

    private Map<Long, Integer> contarSatisfactorios(Long estudianteId, Collection<Long> temaIds,
            ParametrosMotor parametros) {
        Map<Long, Integer> conteo = new HashMap<>();
        for (ResumenIntentoTema resumen : respuestas.resumenPorIntentoYTema(estudianteId, temaIds)) {
            if (EstadisticaTema.alcanza(resumen.getCorrectas().intValue(), resumen.getTotal().intValue(),
                    parametros.umbralDominio())) {
                conteo.merge(resumen.getTemaId(), 1, Integer::sum);
            }
        }
        return conteo;
    }

    private void guardarProgreso(Long estudianteId, Tema tema, EstadisticaTema estadistica,
            Clasificacion clasificacion) {
        ProgresoTema progreso = progresos.findByEstudianteIdAndTemaId(estudianteId, tema.getId()).orElseGet(() -> {
            ProgresoTema nuevo = new ProgresoTema();
            nuevo.setEstudiante(usuarios.getReferenceById(estudianteId));
            nuevo.setTema(tema);
            return nuevo;
        });
        progreso.setTotalRespuestas(estadistica.totalRespuestas());
        progreso.setRespuestasCorrectas(estadistica.respuestasCorrectas());
        progreso.setPorcentajeAciertos(clasificacion.porcentaje());
        progreso.setIntentosSatisfactorios(estadistica.intentosSatisfactorios());
        progreso.setNivel(clasificacion.nivel());
        progreso.setFechaActualizacion(LocalDateTime.now());
        progresos.save(progreso);
    }

    private Recomendacion guardarRecomendacion(Long estudianteId, Tema tema, Clasificacion clasificacion,
            String motivo) {
        recomendaciones.desactivarVigentes(estudianteId, tema.getId());
        Recomendacion recomendacion = new Recomendacion();
        recomendacion.setEstudiante(usuarios.getReferenceById(estudianteId));
        recomendacion.setTema(tema);
        recomendacion.setTipoActividad(clasificacion.actividad());
        recomendacion.setMotivo(motivo);
        return recomendaciones.save(recomendacion);
    }
}
