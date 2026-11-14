package com.evolua.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evolua.dto.IntentoIniciado;
import com.evolua.dto.IntentoResponse;
import com.evolua.dto.IntentoResumenResponse;
import com.evolua.dto.PracticaRequest;
import com.evolua.dto.RespuestaRequest;
import com.evolua.dto.RespuestaResponse;
import com.evolua.dto.ResultadoIntentoResponse;
import com.evolua.security.UsuarioPrincipal;
import com.evolua.service.IntentoService;
import com.evolua.service.PracticaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Intentos de evaluaciones: iniciar, responder, finalizar y consultar resultados. Reservado a los estudiantes;
 * cada estudiante solo accede a sus propios intentos.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Intentos")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class IntentoController {

    private final IntentoService intentoService;
    private final PracticaService practicaService;

    public IntentoController(IntentoService intentoService, PracticaService practicaService) {
        this.intentoService = intentoService;
        this.practicaService = practicaService;
    }

    @PostMapping("/quizzes/{id}/attempts")
    @Operation(summary = "Inicia un intento de la evaluación (201) o retoma el que está en curso (200)")
    public ResponseEntity<IntentoResponse> iniciar(@PathVariable("id") Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        IntentoIniciado iniciado = intentoService.iniciar(id, usuario.getId());
        return ResponseEntity.status(iniciado.nuevo() ? HttpStatus.CREATED : HttpStatus.OK).body(iniciado.intento());
    }

    @PostMapping("/topics/{topicId}/practice")
    @Operation(summary = "Inicia una práctica del tema (201) o retoma la que está en curso (200). "
            + "AUTOMATICA elige las preguntas según la recomendación vigente; LIBRE, según la dificultad y cantidad "
            + "que pida el estudiante. Sin cuerpo equivale a AUTOMATICA")
    public ResponseEntity<IntentoResponse> iniciarPractica(@PathVariable("topicId") Long topicId,
            @Valid @RequestBody(required = false) PracticaRequest solicitud,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        IntentoIniciado iniciado = practicaService.iniciar(topicId, solicitud, usuario.getId());
        return ResponseEntity.status(iniciado.nuevo() ? HttpStatus.CREATED : HttpStatus.OK).body(iniciado.intento());
    }

    @GetMapping("/attempts")
    @Operation(summary = "Historial de intentos del estudiante autenticado")
    public List<IntentoResumenResponse> historial(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return intentoService.historial(usuario.getId());
    }

    @GetMapping("/attempts/{id}")
    @Operation(summary = "Consulta un intento en curso, sin las respuestas correctas")
    public IntentoResponse obtener(@PathVariable("id") Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return intentoService.obtener(id, usuario.getId());
    }

    @PostMapping("/attempts/{id}/answers")
    @Operation(summary = "Registra o cambia la opción elegida para una pregunta del intento")
    public RespuestaResponse responder(@PathVariable("id") Long id, @Valid @RequestBody RespuestaRequest solicitud,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return intentoService.registrarRespuesta(id, solicitud, usuario.getId());
    }

    @PostMapping("/attempts/{id}/submit")
    @Operation(summary = "Finaliza el intento y calcula la puntuación en el servidor")
    public ResultadoIntentoResponse finalizar(@PathVariable("id") Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return intentoService.finalizar(id, usuario.getId());
    }

    @GetMapping("/attempts/{id}/result")
    @Operation(summary = "Consulta el resultado de un intento finalizado, con la revisión de cada pregunta")
    public ResultadoIntentoResponse resultado(@PathVariable("id") Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return intentoService.resultado(id, usuario.getId());
    }
}
