package com.evolua.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evolua.dto.MatriculaRequest;
import com.evolua.dto.MatriculaResponse;
import com.evolua.entity.Curso;
import com.evolua.entity.Matricula;
import com.evolua.entity.Rol;
import com.evolua.entity.Usuario;
import com.evolua.exception.ConflictoException;
import com.evolua.exception.RecursoNoEncontradoException;
import com.evolua.exception.SolicitudInvalidaException;
import com.evolua.repository.MatriculaRepository;
import com.evolua.repository.UsuarioRepository;
import com.evolua.security.UsuarioPrincipal;

/**
 * Gestión de matrículas. Las realiza el docente responsable del curso o un administrador, y son únicas por
 * estudiante y curso.
 */
@Service
public class MatriculaService {

    private final MatriculaRepository matriculas;
    private final UsuarioRepository usuarios;
    private final CursoService cursoService;
    private final AuditoriaService auditoria;

    public MatriculaService(MatriculaRepository matriculas, UsuarioRepository usuarios, CursoService cursoService,
            AuditoriaService auditoria) {
        this.matriculas = matriculas;
        this.usuarios = usuarios;
        this.cursoService = cursoService;
        this.auditoria = auditoria;
    }

    @Transactional
    public MatriculaResponse matricular(Long cursoId, MatriculaRequest solicitud, UsuarioPrincipal actor) {
        Curso curso = cursoService.cursoGestionable(cursoId, actor);
        if (!curso.getActivo()) {
            throw new ConflictoException("No se puede matricular en un curso inactivo");
        }
        Usuario estudiante = usuarios.findByCorreo(solicitud.correo().trim().toLowerCase())
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante no encontrado"));
        if (estudiante.getRol() != Rol.ESTUDIANTE || !estudiante.getActivo()) {
            throw new SolicitudInvalidaException("El usuario debe ser un estudiante activo");
        }
        if (matriculas.existsByEstudianteIdAndCursoId(estudiante.getId(), cursoId)) {
            throw new ConflictoException("El estudiante ya está matriculado en el curso");
        }
        Matricula matricula = new Matricula();
        matricula.setEstudiante(estudiante);
        matricula.setCurso(curso);
        Matricula guardada = matriculas.save(matricula);
        auditoria.registrar(actor.getId(), "CREAR", "matriculas", guardada.getId(),
                "matrícula de " + estudiante.getCorreo() + " en el curso " + curso.getNombre());
        return MatriculaResponse.desde(guardada);
    }

    @Transactional(readOnly = true)
    public List<MatriculaResponse> listar(Long cursoId, UsuarioPrincipal actor) {
        cursoService.cursoGestionable(cursoId, actor);
        return matriculas.findByCursoIdOrderByFechaMatricula(cursoId).stream().map(MatriculaResponse::desde).toList();
    }

    @Transactional
    public void eliminar(Long cursoId, Long estudianteId, UsuarioPrincipal actor) {
        Curso curso = cursoService.cursoGestionable(cursoId, actor);
        Matricula matricula = matriculas.findByCursoIdAndEstudianteId(cursoId, estudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Matrícula no encontrada"));
        String correo = matricula.getEstudiante().getCorreo();
        matriculas.delete(matricula);
        auditoria.registrar(actor.getId(), "ELIMINAR", "matriculas", matricula.getId(),
                "matrícula de " + correo + " eliminada del curso " + curso.getNombre());
    }
}
