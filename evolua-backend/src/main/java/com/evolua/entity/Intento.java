package com.evolua.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Vez que un estudiante realiza una evaluación o una practia.
 * <p>Tabla: <code>intentos</code>.</p>
 */
@Entity
@Table(name = "intentos")
public class Intento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Usuario estudiante;

    /** Solo si el tipo es EVALUACION. */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "evaluacion_id")
    private Evaluacion evaluacion;

    /** Solo si el tipo es PRACTICA. */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "tema_id")
    private Tema tema;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 15)
    private TipoIntento tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoIntento estado = EstadoIntento.EN_CURSO;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio = LocalDateTime.now();

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    /** Porcentaje 0 a 100 calculado en el servidor. */
    @Column(name = "puntuacion", precision = 5, scale = 2)
    private BigDecimal puntuacion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Usuario estudiante) {
        this.estudiante = estudiante;
    }

    public Evaluacion getEvaluacion() {
        return evaluacion;
    }

    public void setEvaluacion(Evaluacion evaluacion) {
        this.evaluacion = evaluacion;
    }

    public Tema getTema() {
        return tema;
    }

    public void setTema(Tema tema) {
        this.tema = tema;
    }

    public TipoIntento getTipo() {
        return tipo;
    }

    public void setTipo(TipoIntento tipo) {
        this.tipo = tipo;
    }

    public EstadoIntento getEstado() {
        return estado;
    }

    public void setEstado(EstadoIntento estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public BigDecimal getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(BigDecimal puntuacion) {
        this.puntuacion = puntuacion;
    }
}
