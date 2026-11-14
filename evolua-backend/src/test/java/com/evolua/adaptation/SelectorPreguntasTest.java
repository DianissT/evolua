package com.evolua.adaptation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.evolua.adaptation.SelectorPreguntas.Candidata;

/**
 * Pruebas del selector de preguntas de la práctica: dificultad primero, recencia después y desempate por id.
 */
class SelectorPreguntasTest {

    private static final LocalDateTime HOY = LocalDateTime.of(2026, 11, 12, 10, 0);

    private final SelectorPreguntas selector = new SelectorPreguntas();

    /** 1: básica nunca vista, 2: media vista hoy, 3: media vista hace 5 días, 4: media nunca vista, 5: alta. */
    private final List<Candidata> banco = List.of(
            new Candidata(1L, 1, null),
            new Candidata(2L, 2, HOY),
            new Candidata(3L, 2, HOY.minusDays(5)),
            new Candidata(4L, 2, null),
            new Candidata(5L, 3, null));

    @Test
    void dentroDelRangoVanPrimeroLasNuncaVistasYLuegoLasMasAntiguas() {
        assertEquals(List.of(4L, 3L, 2L), selector.seleccionar(banco, new RangoDificultad(2, 2), 3, false));
    }

    @Test
    void siFaltanSeCompletaConLasDificultadesMasCercanas() {
        assertEquals(List.of(4L, 3L, 2L, 1L, 5L), selector.seleccionar(banco, new RangoDificultad(2, 2), 5, false));
        assertEquals(List.of(5L, 4L, 3L), selector.seleccionar(banco, new RangoDificultad(3, 3), 3, false));
    }

    @Test
    void elModoEstrictoDescartaLasPreguntasFueraDelRango() {
        assertEquals(List.of(5L), selector.seleccionar(banco, new RangoDificultad(3, 3), 5, true));
        assertEquals(List.of(4L, 5L, 3L, 2L), selector.seleccionar(banco, new RangoDificultad(2, 3), 5, true));
        assertTrue(selector.seleccionar(List.of(new Candidata(1L, 1, null)), new RangoDificultad(3, 3), 5, true).isEmpty());
    }

    @Test
    void conTodasLasDificultadesSoloDecideLaRecencia() {
        assertEquals(List.of(1L, 4L), selector.seleccionar(banco, new RangoDificultad(1, 3), 2, false));
    }

    @Test
    void sinCandidatasNoDevuelveNada() {
        assertTrue(selector.seleccionar(List.of(), new RangoDificultad(1, 3), 5, false).isEmpty());
    }

    @Test
    void laCantidadDebeSerAlMenosUno() {
        assertThrows(IllegalArgumentException.class,
                () -> selector.seleccionar(banco, new RangoDificultad(1, 3), 0, false));
    }

    @Test
    void laDistanciaEsCeroDentroDelRango() {
        RangoDificultad rango = new RangoDificultad(2, 3);
        assertEquals(1, SelectorPreguntas.distancia(new Candidata(1L, 1, null), rango));
        assertEquals(0, SelectorPreguntas.distancia(new Candidata(1L, 2, null), rango));
        assertEquals(0, SelectorPreguntas.distancia(new Candidata(1L, 3, null), rango));
    }
}
