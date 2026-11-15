package com.evolua.service;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evolua.dto.UsuarioAdminRequest;
import com.evolua.dto.UsuarioAdminResponse;
import com.evolua.entity.Rol;
import com.evolua.entity.Usuario;
import com.evolua.exception.ConflictoException;
import com.evolua.exception.RecursoNoEncontradoException;
import com.evolua.exception.SolicitudInvalidaException;
import com.evolua.repository.CursoRepository;
import com.evolua.repository.MatriculaRepository;
import com.evolua.repository.UsuarioRepository;

/**
 * Gestión de usuarios y roles por el administrador. Los usuarios no se eliminan.
 * <p>La auditoría es de dos niveles: la creación y el cambio de estado los registra {@link AuditoriaService}; el
 * cambio de rol lo registra el trigger de PostgreSQL, al que se informa el administrador con
 * {@link ContextoAuditoria}.</p>
 */
@Service
public class UsuarioAdminService {

    private static final int MAXIMO_BYTES_BCRYPT = 72;

    private final UsuarioRepository usuarios;
    private final CursoRepository cursos;
    private final MatriculaRepository matriculas;
    private final PasswordEncoder codificador;
    private final AuditoriaService auditoria;
    private final ContextoAuditoria contexto;

    public UsuarioAdminService(UsuarioRepository usuarios, CursoRepository cursos, MatriculaRepository matriculas,
            PasswordEncoder codificador, AuditoriaService auditoria, ContextoAuditoria contexto) {
        this.usuarios = usuarios;
        this.cursos = cursos;
        this.matriculas = matriculas;
        this.codificador = codificador;
        this.auditoria = auditoria;
        this.contexto = contexto;
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdminResponse> listar() {
        return usuarios.findAll(Sort.by("nombre")).stream().map(UsuarioAdminResponse::desde).toList();
    }

    @Transactional
    public UsuarioAdminResponse crear(UsuarioAdminRequest solicitud, Long adminId) {
        String correo = solicitud.correo().trim().toLowerCase();
        if (usuarios.existsByCorreo(correo)) {
            throw new ConflictoException("Ya existe un usuario con ese correo");
        }
        if (solicitud.password().getBytes(StandardCharsets.UTF_8).length > MAXIMO_BYTES_BCRYPT) {
            throw new SolicitudInvalidaException("La contraseña no puede superar los 72 bytes");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(solicitud.nombre().trim());
        usuario.setCorreo(correo);
        usuario.setPasswordHash(codificador.encode(solicitud.password()));
        usuario.setRol(solicitud.rol());
        Usuario guardado = usuarios.save(usuario);
        auditoria.registrar(adminId, "CREAR", "usuarios", guardado.getId(),
                "usuario creado: " + correo + " (" + guardado.getRol() + ")");
        return UsuarioAdminResponse.desde(guardado);
    }

    @Transactional
    public UsuarioAdminResponse cambiarRol(Long id, Rol nuevoRol, Long adminId) {
        Usuario usuario = buscar(id);
        if (usuario.getId().equals(adminId)) {
            throw new SolicitudInvalidaException("No puedes cambiar tu propio rol");
        }
        if (usuario.getRol() == nuevoRol) {
            return UsuarioAdminResponse.desde(usuario);
        }
        if (usuario.getRol() == Rol.DOCENTE && !cursos.findByDocenteId(id).isEmpty()) {
            throw new ConflictoException("El docente tiene cursos a su cargo; su rol no se puede cambiar");
        }
        if (usuario.getRol() == Rol.ESTUDIANTE && !matriculas.findByEstudianteId(id).isEmpty()) {
            throw new ConflictoException("El estudiante tiene matrículas; su rol no se puede cambiar");
        }
        // El trigger fn_auditar_rol_usuario lee este valor para registrar quién hizo el cambio.
        contexto.registrarUsuario(adminId);
        usuario.setRol(nuevoRol);
        return UsuarioAdminResponse.desde(usuario);
    }

    @Transactional
    public UsuarioAdminResponse cambiarEstado(Long id, boolean activo, Long adminId) {
        Usuario usuario = buscar(id);
        if (!activo && usuario.getId().equals(adminId)) {
            throw new SolicitudInvalidaException("No puedes desactivar tu propia cuenta");
        }
        if (Boolean.valueOf(activo).equals(usuario.getActivo())) {
            return UsuarioAdminResponse.desde(usuario);
        }
        usuario.setActivo(activo);
        auditoria.registrar(adminId, activo ? "ACTIVAR" : "DESACTIVAR", "usuarios", id,
                "usuario " + (activo ? "activado" : "desactivado") + ": " + usuario.getCorreo());
        return UsuarioAdminResponse.desde(usuario);
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}
