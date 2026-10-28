--pruebas de trigger
-- 1. Cambiar la dificultad de una pregunta genera un registro de auditoría
DO $$
DECLARE v_n int;
BEGIN
    UPDATE preguntas SET dificultad = 2 WHERE id = (SELECT min(id) FROM preguntas WHERE dificultad = 1);
    SELECT count(*) INTO v_n FROM auditoria WHERE entidad = 'preguntas' AND accion = 'UPDATE' AND detalle LIKE 'dificultad: 1 -> 2%';
    IF v_n = 1 THEN RAISE NOTICE 'OK    RN-31: cambiar una pregunta generó 1 registro de auditoría';
    ELSE RAISE NOTICE 'FALLO RN-31: se esperaba 1 registro y hay %', v_n; END IF;
END $$;
 
-- 2. Un UPDATE que no cambia nada no genera auditoría
DO $$
DECLARE v_antes int; v_despues int;
BEGIN
    SELECT count(*) INTO v_antes FROM auditoria;
    UPDATE preguntas SET dificultad = dificultad WHERE id = (SELECT min(id) FROM preguntas);
    SELECT count(*) INTO v_despues FROM auditoria;
    IF v_antes = v_despues THEN RAISE NOTICE 'OK    RN-31: un UPDATE sin cambios no genera auditoría';
    ELSE RAISE NOTICE 'FALLO RN-31: se auditó un UPDATE sin cambios'; END IF;
END $$;
 
-- 3. El trigger toma el usuario que configura el servidor (app.usuario_id)
DO $$
DECLARE v_docente bigint; v_registrado bigint;
BEGIN
    SELECT id INTO v_docente FROM usuarios WHERE rol = 'DOCENTE';
    PERFORM set_config('app.usuario_id', v_docente::text, true);
    UPDATE preguntas SET activa = FALSE WHERE id = (SELECT min(id) FROM preguntas);
    SELECT usuario_id INTO v_registrado FROM auditoria WHERE detalle LIKE 'activa: true -> false%';
    PERFORM set_config('app.usuario_id', '', true);
    IF v_registrado = v_docente THEN RAISE NOTICE 'OK    la auditoría registró al docente que hizo el cambio';
    ELSE RAISE NOTICE 'FALLO: usuario_id registrado = %', v_registrado; END IF;
END $$;
 
-- 4. Cambiar el rol de un usuario genera auditoría y no guarda la contraseña (RN-32)
DO $$
DECLARE v_n int; v_hash text; v_fuga int;
BEGIN
    SELECT password_hash INTO v_hash FROM usuarios WHERE correo = 'pedro.soto@uni.edu';
    UPDATE usuarios SET rol = 'DOCENTE' WHERE correo = 'pedro.soto@uni.edu';
    SELECT count(*) INTO v_n FROM auditoria WHERE accion = 'CAMBIO_ROL' AND detalle = 'rol: ESTUDIANTE -> DOCENTE';
    SELECT count(*) INTO v_fuga FROM auditoria WHERE detalle LIKE '%' || v_hash || '%';
    IF v_n = 1 THEN RAISE NOTICE 'OK    RN-31: el cambio de rol quedó auditado';
    ELSE RAISE NOTICE 'FALLO RN-31: cambio de rol no auditado'; END IF;
    IF v_fuga = 0 THEN RAISE NOTICE 'OK    RN-32: la auditoría no contiene la contraseña';
    ELSE RAISE NOTICE 'FALLO RN-32: la auditoría contiene la contraseña'; END IF;
END $$;
 
-- 5. Cambiar solo el nombre NO genera auditoría de rol
DO $$
DECLARE v_antes int; v_despues int;
BEGIN
    SELECT count(*) INTO v_antes FROM auditoria WHERE accion = 'CAMBIO_ROL';
    UPDATE usuarios SET nombre = 'Ana María Pérez' WHERE correo = 'ana.perez@uni.edu';
    SELECT count(*) INTO v_despues FROM auditoria WHERE accion = 'CAMBIO_ROL';
    IF v_antes = v_despues THEN RAISE NOTICE 'OK    cambiar el nombre no genera auditoría de rol';
    ELSE RAISE NOTICE 'FALLO: se auditó un cambio que no era de rol'; END IF;
END $$;
 
-- Se crea una evaluación completa: intento con 15 respuestas, calificado y finalizado
INSERT INTO intentos (estudiante_id, evaluacion_id, tipo)
    SELECT u.id, e.id, 'EVALUACION' FROM usuarios u, evaluaciones e
    WHERE u.correo = 'ana.perez@uni.edu' AND e.nombre = 'Evaluación inicial';
 
INSERT INTO respuestas (intento_id, pregunta_id)
    SELECT (SELECT max(id) FROM intentos), pregunta_id FROM evaluacion_preguntas;
 
UPDATE respuestas r SET
    opcion_id   = (SELECT o.id FROM opciones_pregunta o WHERE o.pregunta_id = r.pregunta_id AND o.es_correcta),
    es_correcta = TRUE;
 
-- 6. Finalizar el intento (el flujo normal) SÍ está permitido
DO $$ BEGIN
    UPDATE intentos SET estado = 'FINALIZADO', puntuacion = 100, fecha_fin = now() WHERE id = (SELECT max(id) FROM intentos);
    RAISE NOTICE 'OK    se puede finalizar un intento en curso';
EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'FALLO: no permitió finalizar el intento (%)', SQLERRM;
END $$;
 
-- 7. Modificar un intento FINALIZADO se rechaza
DO $$ BEGIN
    UPDATE intentos SET puntuacion = 0 WHERE id = (SELECT max(id) FROM intentos);
    RAISE NOTICE 'FALLO RN-19: permitió modificar un intento finalizado';
EXCEPTION WHEN raise_exception THEN
    RAISE NOTICE 'OK    RN-19: rechazó modificar un intento FINALIZADO';
END $$;
 
-- 8. Eliminar un intento FINALIZADO se rechaza
DO $$ BEGIN
    DELETE FROM intentos WHERE id = (SELECT max(id) FROM intentos);
    RAISE NOTICE 'FALLO RN-19: permitió eliminar un intento finalizado';
EXCEPTION WHEN raise_exception THEN
    RAISE NOTICE 'OK    RN-19: rechazó eliminar un intento FINALIZADO';
END $$;
 
-- 9. Cambiar una respuesta de un intento FINALIZADO se rechaza
DO $$ BEGIN
    UPDATE respuestas SET es_correcta = FALSE WHERE intento_id = (SELECT max(id) FROM intentos);
    RAISE NOTICE 'FALLO RN-19: permitió alterar la calificación';
EXCEPTION WHEN raise_exception THEN
    RAISE NOTICE 'OK    RN-19: rechazó alterar la calificación de un intento FINALIZADO';
END $$;
 
-- 10. Agregar una respuesta a un intento FINALIZADO se rechaza
DO $$ BEGIN
    INSERT INTO respuestas (intento_id, pregunta_id)
        SELECT (SELECT max(id) FROM intentos),
               (SELECT id FROM preguntas WHERE id NOT IN (SELECT pregunta_id FROM evaluacion_preguntas) LIMIT 1);
    RAISE NOTICE 'FALLO RN-19: permitió agregar una respuesta';
EXCEPTION WHEN raise_exception THEN
    RAISE NOTICE 'OK    RN-19: rechazó agregar respuestas a un intento FINALIZADO';
END $$;
 
ROLLBACK;   -- descarta TODO lo anterior