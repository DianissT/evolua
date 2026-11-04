package com.evolua.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evolua.dto.CursoRequest;
import com.evolua.dto.CursoResponse;
import com.evolua.entity.Curso;
import com.evolua.entity.Matricula;
import com.evolua.entity.Rol;
import com.evolua.entity.Usuario;
import com.evolua.exception.RecursoNoEncontradoException;
import com.evolua.repository.CursoRepository;
import com.evolua.repository.MatriculaRepository;
import com.evolua.repository.UsuarioRepository;
import com.evolua.security.UsuarioPrincipal;

/**
 * Gestión de cursos. Un docente solo administra los cursos de los que es responsable; un estudiante solo
 * consulta los cursos activos en los que está matriculado. Los cursos no se eliminan: se desactivan.
 */
@Service
public class CursoService {

    private final CursoRepository cursos;
    private final UsuarioRepository usuarios;
    private final MatriculaRepository matriculas;
    private final AuditoriaService auditoria;

    public CursoService(CursoRepository cursos, UsuarioRepository usuarios, MatriculaRepository matriculas,
            AuditoriaService auditoria) {
        this.cursos = cursos;
        this.usuarios = usuarios;
        this.matriculas = matriculas;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<CursoResponse> listar(UsuarioPrincipal usuario) {
        if (usuario.getRol() == Rol.DOCENTE) {
            return cursos.findByDocenteId(usuario.getId()).stream().map(CursoResponse::desde).toList();
        }
        return matriculas.findByEstudianteId(usuario.getId()).stream()
                .map(Matricula::getCurso)
                .filter(Curso::getActivo)
                .map(CursoResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public CursoResponse obtener(Long id, UsuarioPrincipal usuario) {
        return CursoResponse.desde(cursoVisible(id, usuario));
    }

    @Transactional
    public CursoResponse crear(CursoRequest solicitud, Long docenteId) {
        Usuario docente = usuarios.findById(docenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        Curso curso = new Curso();
        curso.setNombre(solicitud.nombre().trim());
        curso.setDescripcion(solicitud.descripcion());
        curso.setDocente(docente);
        Curso guardado = cursos.save(curso);
        auditoria.registrar(docenteId, "CREAR", "cursos", guardado.getId(), "curso creado: " + guardado.getNombre());
        return CursoResponse.desde(guardado);
    }

    @Transactional
    public CursoResponse actualizar(Long id, CursoRequest solicitud, Long docenteId) {
        Curso curso = cursoPropio(id, docenteId);
        curso.setNombre(solicitud.nombre().trim());
        curso.setDescripcion(solicitud.descripcion());
        if (solicitud.activo() != null) {
            curso.setActivo(solicitud.activo());
        }
        auditoria.registrar(docenteId, "ACTUALIZAR", "cursos", id, "curso actualizado: " + curso.getNombre());
        return CursoResponse.desde(curso);
    }

    @Transactional
    public void desactivar(Long id, Long docenteId) {
        Curso curso = cursoPropio(id, docenteId);
        curso.setActivo(false);
        auditoria.registrar(docenteId, "DESACTIVAR", "cursos", id, "curso desactivado: " + curso.getNombre());
    }

    @Transactional(readOnly = true)
    public Curso cursoPropio(Long id, Long docenteId) {
        Curso curso = buscar(id);
        verificarPropietario(curso, docenteId);
        return curso;
    }

    @Transactional(readOnly = true)
    public Curso cursoGestionable(Long id, UsuarioPrincipal usuario) {
        Curso curso = buscar(id);
        if (usuario.getRol() != Rol.ADMIN) {
            verificarPropietario(curso, usuario.getId());
        }
        return curso;
    }

    @Transactional(readOnly = true)
    public Curso cursoVisible(Long id, UsuarioPrincipal usuario) {
        Curso curso = buscar(id);
        if (usuario.getRol() == Rol.DOCENTE) {
            verificarPropietario(curso, usuario.getId());
        } else if (!curso.getActivo() || !matriculas.existsByEstudianteIdAndCursoId(usuario.getId(), id)) {
            throw new AccessDeniedException("Acceso denegado");
        }
        return curso;
    }

    private Curso buscar(Long id) {
        return cursos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado"));
    }

    private void verificarPropietario(Curso curso, Long docenteId) {
        if (!curso.getDocente().getId().equals(docenteId)) {
            throw new AccessDeniedException("Acceso denegado");
        }
    }
}
