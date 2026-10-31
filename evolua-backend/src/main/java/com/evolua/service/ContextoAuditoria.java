package com.evolua.service;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Informa a la base de datos qué usuario realiza la operación en curso, mediante el parámetro de sesión
 * <code>app.usuario_id</code>, que leen los triggers de auditoría. Debe invocarse dentro de la transacción.
 */
@Component
public class ContextoAuditoria {

    @PersistenceContext
    private EntityManager entityManager;

    public void registrarUsuario(Long usuarioId) {
        entityManager.createNativeQuery("SELECT set_config('app.usuario_id', :usuario, true)")
                .setParameter("usuario", String.valueOf(usuarioId))
                .getSingleResult();
    }
}
