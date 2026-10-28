package com.evolua.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Parámetro del sistema editable por el administrador.
 * <p>Tabla: <code>configuracion</code>.</p>
 */
@Entity
@Table(name = "configuracion")
public class Configuracion {

    @Id
    @Column(name = "clave", length = 60)
    private String clave;

    @Column(name = "valor", nullable = false, length = 200)
    private String valor;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
