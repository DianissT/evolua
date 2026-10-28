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
 * Desempeño acumulado de un estudiante en un tema; lo usa el motor adaptativo.
 * <p>Tabla: <code>progreso_tema</code>.</p>
 */
@Entity
@Table(name = "progreso_tema")
public class ProgresoTema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Usuario estudiante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tema_id", nullable = false)
    private Tema tema;

    @Column(name = "total_respuestas", nullable = false)
    private Integer totalRespuestas = 0;

    @Column(name = "respuestas_correctas", nullable = false)
    private Integer respuestasCorrectas = 0;

    @Column(name = "porcentaje_aciertos", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeAciertos = BigDecimal.ZERO;

    @Column(name = "intentos_satisfactorios", nullable = false)
    private Integer intentosSatisfactorios = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel", nullable = false, length = 15)
    private NivelDominio nivel = NivelDominio.SIN_DATOS;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

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

    public Tema getTema() {
        return tema;
    }

    public void setTema(Tema tema) {
        this.tema = tema;
    }

    public Integer getTotalRespuestas() {
        return totalRespuestas;
    }

    public void setTotalRespuestas(Integer totalRespuestas) {
        this.totalRespuestas = totalRespuestas;
    }

    public Integer getRespuestasCorrectas() {
        return respuestasCorrectas;
    }

    public void setRespuestasCorrectas(Integer respuestasCorrectas) {
        this.respuestasCorrectas = respuestasCorrectas;
    }

    public BigDecimal getPorcentajeAciertos() {
        return porcentajeAciertos;
    }

    public void setPorcentajeAciertos(BigDecimal porcentajeAciertos) {
        this.porcentajeAciertos = porcentajeAciertos;
    }

    public Integer getIntentosSatisfactorios() {
        return intentosSatisfactorios;
    }

    public void setIntentosSatisfactorios(Integer intentosSatisfactorios) {
        this.intentosSatisfactorios = intentosSatisfactorios;
    }

    public NivelDominio getNivel() {
        return nivel;
    }

    public void setNivel(NivelDominio nivel) {
        this.nivel = nivel;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
