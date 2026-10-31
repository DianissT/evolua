package com.evolua.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evolua.dto.TemaRequest;
import com.evolua.dto.TemaResponse;
import com.evolua.entity.Curso;
import com.evolua.entity.Rol;
import com.evolua.entity.Tema;
import com.evolua.exception.RecursoNoEncontradoException;
import com.evolua.exception.SolicitudInvalidaException;
import com.evolua.repository.TemaRepository;
import com.evolua.security.UsuarioPrincipal;

/**
 * Gestión de temas de un curso. Los temas no se eliminan: se desactivan.
 */
@Service
public class TemaService {

    private final TemaRepository temas;
    private final CursoService cursoService;
    private final AuditoriaService auditoria;

    public TemaService(TemaRepository temas, CursoService cursoService, AuditoriaService auditoria) {
        this.temas = temas;
        this.cursoService = cursoService;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<TemaResponse> listarPorCurso(Long cursoId, UsuarioPrincipal usuario) {
        cursoService.cursoVisible(cursoId, usuario);
        List<Tema> lista = usuario.getRol() == Rol.DOCENTE
                ? temas.findByCursoIdOrderByOrden(cursoId)
                : temas.findByCursoIdAndActivoTrueOrderByOrden(cursoId);
        return lista.stream().map(TemaResponse::desde).toList();
    }

    @Transactional
    public TemaResponse crear(TemaRequest solicitud, Long docenteId) {
        Curso curso = cursoService.cursoPropio(solicitud.cursoId(), docenteId);
        Tema tema = new Tema();
        tema.setCurso(curso);
        tema.setNombre(solicitud.nombre().trim());
        tema.setOrden(solicitud.orden() != null
                ? solicitud.orden()
                : temas.findByCursoIdOrderByOrden(curso.getId()).size() + 1);
        Tema guardado = temas.save(tema);
        auditoria.registrar(docenteId, "CREAR", "temas", guardado.getId(), "tema creado: " + guardado.getNombre());
        return TemaResponse.desde(guardado);
    }

    @Transactional
    public TemaResponse actualizar(Long id, TemaRequest solicitud, Long docenteId) {
        Tema tema = temaPropio(id, docenteId);
        if (!tema.getCurso().getId().equals(solicitud.cursoId())) {
            throw new SolicitudInvalidaException("No se puede cambiar el curso de un tema");
        }
        tema.setNombre(solicitud.nombre().trim());
        if (solicitud.orden() != null) {
            tema.setOrden(solicitud.orden());
        }
        if (solicitud.activo() != null) {
            tema.setActivo(solicitud.activo());
        }
        auditoria.registrar(docenteId, "ACTUALIZAR", "temas", id, "tema actualizado: " + tema.getNombre());
        return TemaResponse.desde(tema);
    }

    @Transactional
    public void desactivar(Long id, Long docenteId) {
        Tema tema = temaPropio(id, docenteId);
        tema.setActivo(false);
        auditoria.registrar(docenteId, "DESACTIVAR", "temas", id, "tema desactivado: " + tema.getNombre());
    }

    @Transactional(readOnly = true)
    public Tema temaPropio(Long id, Long docenteId) {
        Tema tema = temas.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Tema no encontrado"));
        cursoService.cursoPropio(tema.getCurso().getId(), docenteId);
        return tema;
    }
}
