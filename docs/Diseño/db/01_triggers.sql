--TRIGGERS
--Auditoria de preguntas que registra las modificaciones y eliminaciones de preguntas.
CREATE OR REPLACE FUNCTION fn_auditar_preguntas()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    v_detalle text := '';
    v_usuario bigint := NULLIF(current_setting('app.usuario_id', true), '')::bigint;
BEGIN
    IF TG_OP = 'UPDATE' THEN
        IF NEW.enunciado IS DISTINCT FROM OLD.enunciado THEN
            v_detalle := v_detalle || 'enunciado: [' || left(OLD.enunciado, 60) || '] -> [' || left(NEW.enunciado, 60) || ']; ';
        END IF;
        IF NEW.dificultad IS DISTINCT FROM OLD.dificultad THEN
            v_detalle := v_detalle || 'dificultad: ' || OLD.dificultad || ' -> ' || NEW.dificultad || '; ';
        END IF;
        IF NEW.tipo IS DISTINCT FROM OLD.tipo THEN
            v_detalle := v_detalle || 'tipo: ' || OLD.tipo || ' -> ' || NEW.tipo || '; ';
        END IF;
        IF NEW.tema_id IS DISTINCT FROM OLD.tema_id THEN
            v_detalle := v_detalle || 'tema_id: ' || OLD.tema_id || ' -> ' || NEW.tema_id || '; ';
        END IF;
        IF NEW.activa IS DISTINCT FROM OLD.activa THEN
            v_detalle := v_detalle || 'activa: ' || OLD.activa || ' -> ' || NEW.activa || '; ';
        END IF;
        IF NEW.retroalimentacion IS DISTINCT FROM OLD.retroalimentacion THEN
            v_detalle := v_detalle || 'retroalimentación modificada; ';
        END IF;
 
        IF v_detalle = '' THEN          -- no cambió nada relevante: no se audita
            RETURN NEW;
        END IF;
 
        INSERT INTO auditoria (usuario_id, accion, entidad, entidad_id, detalle)
        VALUES (v_usuario, 'UPDATE', 'preguntas', NEW.id, rtrim(v_detalle, '; '));
        RETURN NEW;
 
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO auditoria (usuario_id, accion, entidad, entidad_id, detalle)
        VALUES (v_usuario, 'DELETE', 'preguntas', OLD.id, 'pregunta eliminada: ' || left(OLD.enunciado, 80));
        RETURN OLD;
    END IF;
 
    RETURN NULL;
END;
$$;
 
DROP TRIGGER IF EXISTS trg_auditar_preguntas ON preguntas;
CREATE TRIGGER trg_auditar_preguntas
    AFTER UPDATE OR DELETE ON preguntas
    FOR EACH ROW
    EXECUTE FUNCTION fn_auditar_preguntas();
 
--Auditoria de cambios de roles
CREATE OR REPLACE FUNCTION fn_auditar_rol_usuario()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO auditoria (usuario_id, accion, entidad, entidad_id, detalle)
    VALUES (
        NULLIF(current_setting('app.usuario_id', true), '')::bigint,
        'CAMBIO_ROL', 'usuarios', NEW.id,
        'rol: ' || OLD.rol || ' -> ' || NEW.rol
    );
    RETURN NEW;
END;
$$;
 
DROP TRIGGER IF EXISTS trg_auditar_rol_usuario ON usuarios;
CREATE TRIGGER trg_auditar_rol_usuario
    AFTER UPDATE OF rol ON usuarios
    FOR EACH ROW
    WHEN (OLD.rol IS DISTINCT FROM NEW.rol)
    EXECUTE FUNCTION fn_auditar_rol_usuario();

--Auditoria para proteger los intentos finalizados para que no se puedan borrar ni modificar
CREATE OR REPLACE FUNCTION fn_proteger_intento_finalizado()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF OLD.estado = 'FINALIZADO' THEN
        RAISE EXCEPTION 'El intento % ya está FINALIZADO y no se puede modificar ni eliminar (RN-19)', OLD.id;
    END IF;
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$$;
 
DROP TRIGGER IF EXISTS trg_proteger_intento ON intentos;
CREATE TRIGGER trg_proteger_intento
    BEFORE UPDATE OR DELETE ON intentos
    FOR EACH ROW
    EXECUTE FUNCTION fn_proteger_intento_finalizado();
 
CREATE OR REPLACE FUNCTION fn_proteger_respuestas_finalizadas()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    v_estado text;
BEGIN
    -- Intento al que pertenece la fila antes del cambio (UPDATE y DELETE)
    IF TG_OP IN ('UPDATE', 'DELETE') THEN
        SELECT estado INTO v_estado FROM intentos WHERE id = OLD.intento_id;
        IF v_estado = 'FINALIZADO' THEN
            RAISE EXCEPTION 'No se pueden modificar ni eliminar respuestas de un intento FINALIZADO (RN-19)';
        END IF;
    END IF;
    -- Intento al que quedaría la fila después del cambio (INSERT y UPDATE)
    IF TG_OP IN ('INSERT', 'UPDATE') THEN
        SELECT estado INTO v_estado FROM intentos WHERE id = NEW.intento_id;
        IF v_estado = 'FINALIZADO' THEN
            RAISE EXCEPTION 'No se pueden agregar respuestas a un intento FINALIZADO (RN-19)';
        END IF;
    END IF;
 
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$$;
 
DROP TRIGGER IF EXISTS trg_proteger_respuestas ON respuestas;
CREATE TRIGGER trg_proteger_respuestas
    BEFORE INSERT OR UPDATE OR DELETE ON respuestas
    FOR EACH ROW
    EXECUTE FUNCTION fn_proteger_respuestas_finalizadas();

SELECT tgname, tgrelid::regclass AS tabla FROM pg_trigger WHERE NOT tgisinternal ORDER BY tgname; 