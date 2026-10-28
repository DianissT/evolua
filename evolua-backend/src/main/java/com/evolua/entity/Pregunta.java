package com.evolua.entity;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Pregunta del banco, clasificada por tema y dificultad.
 * <p>Tabla: <code>preguntas</code>.</p>
 */
@Entity
@Table(name = "preguntas")
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tema_id", nullable = false)
    private Tema tema;

    @Column(name = "enunciado", nullable = false, columnDefinition = "text")
    private String enunciado;

    /** 1 básica, 2 media, 3 alta (RN-10). */
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "dificultad", nullable = false)
    private Integer dificultad;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 25)
    private TipoPregunta tipo;

    /** Explicacion que ve el estudiante al finalizar el intento. */
    @Column(name = "retroalimentacion", columnDefinition = "text")
    private String retroalimentacion;

    /** Las preguntas se deactivan, no se borran */
    @Column(name = "activa", nullable = false)
    private Boolean activa = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tema getTema() {
        return tema;
    }

    public void setTema(Tema tema) {
        this.tema = tema;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public Integer getDificultad() {
        return dificultad;
    }

    public void setDificultad(Integer dificultad) {
        this.dificultad = dificultad;
    }

    public TipoPregunta getTipo() {
        return tipo;
    }

    public void setTipo(TipoPregunta tipo) {
        this.tipo = tipo;
    }

    public String getRetroalimentacion() {
        return retroalimentacion;
    }

    public void setRetroalimentacion(String retroalimentacion) {
        this.retroalimentacion = retroalimentacion;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
