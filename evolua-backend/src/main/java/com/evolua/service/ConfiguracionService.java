package com.evolua.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evolua.adaptation.ParametrosMotor;
import com.evolua.entity.Configuracion;
import com.evolua.repository.ConfiguracionRepository;

/**
 * Lectura de los parámetros del sistema guardados en la tabla de configuración.
 */
@Service
public class ConfiguracionService {

    public static final String UMBRAL_REFUERZO = "umbral_refuerzo";
    public static final String UMBRAL_DOMINIO = "umbral_dominio";
    public static final String MIN_RESPUESTAS_TEMA = "min_respuestas_tema";
    public static final String INTENTOS_PARA_DOMINIO = "intentos_para_dominio";
    public static final String PREGUNTAS_POR_PRACTICA = "preguntas_por_practica";
    
    /** Cantidad de preguntas de una práctica cuando el parámetro no está configurado. */
    public static final int PREGUNTAS_POR_PRACTICA_DEFECTO = 5;

    private final ConfiguracionRepository configuracion;

    public ConfiguracionService(ConfiguracionRepository configuracion) {
        this.configuracion = configuracion;
    }

    /**
     * Parámetros vigentes del motor adaptativo. Se leen en cada uso, por lo que un cambio en la tabla se aplica de
     * inmediato.
     */
    @Transactional(readOnly = true)
    public ParametrosMotor parametrosMotor() {
        Map<String, String> valores = configuracion
                .findAllById(List.of(UMBRAL_REFUERZO, UMBRAL_DOMINIO, MIN_RESPUESTAS_TEMA, INTENTOS_PARA_DOMINIO))
                .stream()
                .collect(Collectors.toMap(Configuracion::getClave, Configuracion::getValor));
        return new ParametrosMotor(entero(valores, UMBRAL_REFUERZO), entero(valores, UMBRAL_DOMINIO),
                entero(valores, MIN_RESPUESTAS_TEMA), entero(valores, INTENTOS_PARA_DOMINIO));
    }

    /**
     * Cantidad de preguntas de una práctica. Si el parámetro no existe se usa
     * {@value #PREGUNTAS_POR_PRACTICA_DEFECTO}.
     */
    @Transactional(readOnly = true)
    public int preguntasPorPractica() {
        int cantidad = configuracion.findById(PREGUNTAS_POR_PRACTICA)
                .map(fila -> entero(Map.of(PREGUNTAS_POR_PRACTICA, fila.getValor()), PREGUNTAS_POR_PRACTICA))
                .orElse(PREGUNTAS_POR_PRACTICA_DEFECTO);
        if (cantidad < 1) {
            throw new IllegalStateException("El parámetro " + PREGUNTAS_POR_PRACTICA + " debe ser al menos 1");
        }
        return cantidad;
    }

    private int entero(Map<String, String> valores, String clave) {
        String valor = valores.get(clave);
        if (valor == null) {
            throw new IllegalStateException("Falta el parámetro de configuración " + clave);
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("El parámetro " + clave + " no es un número entero: " + valor);
        }
    }
}
