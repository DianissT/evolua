package com.evolua.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Respuesta del estudiante a una pregunta de un intento, guarda todas las preguntas de un intento.
 * <p>Tabla: <code>respuestas</code>.</p>
 */
@Entity
@Table(name = "respuestas")
public class Respuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intento_id", nullable = false)
    private Intento intento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private Pregunta pregunta;

    /** Opción elegida; null mientras no responda. */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "opcion_id")
    private OpcionPregunta opcion;

    @Column(name = "es_correcta")
    private Boolean esCorrecta;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Intento getIntento() {
        return intento;
    }

    public void setIntento(Intento intento) {
        this.intento = intento;
    }

    public Pregunta getPregunta() {
        return pregunta;
    }

    public void setPregunta(Pregunta pregunta) {
        this.pregunta = pregunta;
    }

    public OpcionPregunta getOpcion() {
        return opcion;
    }

    public void setOpcion(OpcionPregunta opcion) {
        this.opcion = opcion;
    }

    public Boolean getEsCorrecta() {
        return esCorrecta;
    }

    public void setEsCorrecta(Boolean esCorrecta) {
        this.esCorrecta = esCorrecta;
    }
}
