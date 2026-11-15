package com.evolua.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evolua.dto.ConfiguracionResponse;
import com.evolua.entity.Configuracion;
import com.evolua.exception.RecursoNoEncontradoException;
import com.evolua.exception.SolicitudInvalidaException;
import com.evolua.repository.ConfiguracionRepository;

/**
 * Consulta y edición de los parámetros del sistema por el administrador. Cada cambio se valida con {@link ValidadorConfiguracion} y se registra en la auditoría.
 * {@link ConfiguracionService} lee la tabla en cada uso, por lo que el cambio rige de inmediato.
 */
@Service
public class ConfiguracionAdminService {

    private final ConfiguracionRepository configuracion;
    private final AuditoriaService auditoria;

    public ConfiguracionAdminService(ConfiguracionRepository configuracion, AuditoriaService auditoria) {
        this.configuracion = configuracion;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<ConfiguracionResponse> listar() {
        return configuracion.findAll(Sort.by("clave")).stream().map(ConfiguracionResponse::desde).toList();
    }

    @Transactional
    public ConfiguracionResponse actualizar(String clave, String valor, Long adminId) {
        Configuracion fila = configuracion.findById(clave)
                .orElseThrow(() -> new RecursoNoEncontradoException("Parámetro no encontrado"));
        String nuevo = valor.trim();

        Map<String, String> resultante = new HashMap<>();
        configuracion.findAll().forEach(otra -> resultante.put(otra.getClave(), otra.getValor()));
        resultante.put(clave, nuevo);
        try {
            ValidadorConfiguracion.validar(resultante);
        } catch (IllegalArgumentException e) {
            throw new SolicitudInvalidaException(e.getMessage());
        }

        String anterior = fila.getValor();
        if (!anterior.equals(nuevo)) {
            fila.setValor(nuevo);
            auditoria.registrar(adminId, "ACTUALIZAR", "configuracion", null, clave + ": " + anterior + " -> " + nuevo);
        }
        return ConfiguracionResponse.desde(fila);
    }
}
