package com.evolua.adaptation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.evolua.entity.NivelDominio;
import com.evolua.entity.TipoActividad;

/**
 * Pruebas del motor adaptativo con varios escenarios: desempeño bajo, intermedio y alto, un único
 * resultado alto y un estudiante que mejora después de practicar.
 */
class ClasificadorTemaTest {

    private final ClasificadorTema clasificador = new ClasificadorTema(new ParametrosMotor(60, 80, 5, 2));

    @Test
    void unDesempenoBajoRequiereRefuerzoConPreguntasBasicas() {
        Clasificacion resultado = clasificador.clasificar(new EstadisticaTema(6, 2, 0));

        assertEquals(NivelDominio.REFUERZO, resultado.nivel());
        assertEquals(TipoActividad.REFUERZO, resultado.actividad());
        assertEquals(1, RangoDificultad.de(resultado.actividad()).maxima());
    }

    @Test
    void unDesempenoIntermedioRecomiendaPracticaDelMismoNivel() {
        Clasificacion resultado = clasificador.clasificar(new EstadisticaTema(10, 7, 0));

        assertEquals(NivelDominio.INTERMEDIO, resultado.nivel());
        assertEquals(TipoActividad.PRACTICA_ADICIONAL, resultado.actividad());
        assertEquals(2, RangoDificultad.de(resultado.actividad()).minima());
    }

    @Test
    void unDesempenoAltoRecomiendaConfirmarOEjerciciosAvanzados() {
        Clasificacion sinConfirmar = clasificador.clasificar(new EstadisticaTema(10, 9, 1));
        Clasificacion confirmado = clasificador.clasificar(new EstadisticaTema(10, 9, 2));

        assertEquals(NivelDominio.AVANZADO, sinConfirmar.nivel());
        assertEquals(TipoActividad.CONFIRMACION, sinConfirmar.actividad());
        assertEquals(NivelDominio.DOMINADO, confirmado.nivel());
        assertEquals(TipoActividad.AVANZADO, confirmado.actividad());
    }

    @Test
    void unUnicoResultadoAltoNoDeclaraElTemaComoDominado() {
        Clasificacion resultado = clasificador.clasificar(new EstadisticaTema(5, 5, 1));

        assertEquals(NivelDominio.AVANZADO, resultado.nivel());
        assertEquals(TipoActividad.CONFIRMACION, resultado.actividad());
        assertEquals(2, RangoDificultad.de(resultado.actividad()).minima());
        assertEquals(3, RangoDificultad.de(resultado.actividad()).maxima());
    }

    @Test
    void unEstudianteQueMejoraDespuesDePracticarAvanzaDeNivel() {
        Clasificacion inicial = clasificador.clasificar(new EstadisticaTema(6, 3, 0));
        Clasificacion trasElRefuerzo = clasificador.clasificar(new EstadisticaTema(11, 8, 1));
        Clasificacion trasLaPractica = clasificador.clasificar(new EstadisticaTema(16, 13, 2));

        assertEquals(NivelDominio.REFUERZO, inicial.nivel());
        assertEquals(NivelDominio.INTERMEDIO, trasElRefuerzo.nivel());
        assertEquals(NivelDominio.DOMINADO, trasLaPractica.nivel());
        assertEquals("72.73", trasElRefuerzo.porcentaje().toPlainString());
        assertEquals("81.25", trasLaPractica.porcentaje().toPlainString());
    }

    @Test
    void conPocasRespuestasElTemaNoSeClasifica() {
        Clasificacion resultado = clasificador.clasificar(new EstadisticaTema(3, 3, 1));

        assertEquals(NivelDominio.SIN_DATOS, resultado.nivel());
        assertEquals(TipoActividad.PRACTICA_ADICIONAL, resultado.actividad());
    }

    @Test
    void losLimitesDeLosUmbralesSeRespetan() {
        assertEquals(NivelDominio.REFUERZO, clasificador.clasificar(new EstadisticaTema(100, 59, 0)).nivel());
        assertEquals(NivelDominio.INTERMEDIO, clasificador.clasificar(new EstadisticaTema(100, 60, 0)).nivel());
        assertEquals(NivelDominio.INTERMEDIO, clasificador.clasificar(new EstadisticaTema(100, 79, 0)).nivel());
        assertEquals(NivelDominio.AVANZADO, clasificador.clasificar(new EstadisticaTema(100, 80, 0)).nivel());
        assertEquals(NivelDominio.INTERMEDIO, clasificador.clasificar(new EstadisticaTema(5, 3, 0)).nivel());
        assertEquals(NivelDominio.AVANZADO, clasificador.clasificar(new EstadisticaTema(5, 4, 0)).nivel());
    }

    @Test
    void losParametrosConfigurablesCambianLaClasificacion() {
        ClasificadorTema exigente = new ClasificadorTema(new ParametrosMotor(70, 90, 5, 3));

        assertEquals(NivelDominio.AVANZADO, clasificador.clasificar(new EstadisticaTema(10, 8, 0)).nivel());
        assertEquals(NivelDominio.INTERMEDIO, exigente.clasificar(new EstadisticaTema(10, 8, 0)).nivel());
        assertEquals(NivelDominio.AVANZADO, exigente.clasificar(new EstadisticaTema(10, 10, 2)).nivel());
        assertEquals(NivelDominio.DOMINADO, exigente.clasificar(new EstadisticaTema(10, 10, 3)).nivel());
    }

    @Test
    void elMotivoExplicaLaRecomendacionConSusDatos() {
        EstadisticaTema refuerzo = new EstadisticaTema(6, 2, 0);
        String motivo = clasificador.motivo("Ciclos", refuerzo, clasificador.clasificar(refuerzo));
        assertTrue(motivo.contains("Ciclos"));
        assertTrue(motivo.contains("2 de 6"));
        assertTrue(motivo.contains("33,3 %"));
        assertTrue(motivo.contains("60 %"));

        EstadisticaTema sinDatos = new EstadisticaTema(3, 3, 0);
        assertTrue(clasificador.motivo("Ciclos", sinDatos, clasificador.clasificar(sinDatos)).contains("3 de 5"));

        EstadisticaTema avanzado = new EstadisticaTema(10, 9, 1);
        assertTrue(clasificador.motivo("Ciclos", avanzado, clasificador.clasificar(avanzado)).contains("1 de 2"));

        EstadisticaTema dominado = new EstadisticaTema(16, 13, 2);
        assertTrue(clasificador.motivo("Ciclos", dominado, clasificador.clasificar(dominado)).startsWith("Dominas Ciclos"));
    }

    @Test
    void cadaActividadTieneSuRangoDeDificultad() {
        assertEquals(new RangoDificultad(1, 1), RangoDificultad.de(TipoActividad.REFUERZO));
        assertEquals(new RangoDificultad(2, 2), RangoDificultad.de(TipoActividad.PRACTICA_ADICIONAL));
        assertEquals(new RangoDificultad(2, 3), RangoDificultad.de(TipoActividad.CONFIRMACION));
        assertEquals(new RangoDificultad(3, 3), RangoDificultad.de(TipoActividad.AVANZADO));
    }

    @Test
    void losParametrosYLasEstadisticasInvalidosSeRechazan() {
        assertThrows(IllegalArgumentException.class, () -> new ParametrosMotor(80, 60, 5, 2));
        assertThrows(IllegalArgumentException.class, () -> new ParametrosMotor(-1, 80, 5, 2));
        assertThrows(IllegalArgumentException.class, () -> new ParametrosMotor(60, 101, 5, 2));
        assertThrows(IllegalArgumentException.class, () -> new ParametrosMotor(60, 80, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> new EstadisticaTema(5, 6, 0));
    }
}
