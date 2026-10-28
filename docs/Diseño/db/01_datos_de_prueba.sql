--Datos de prueba 
--5 usuarios (1 ADMIN, 1 DOCENTE y 3 Estudiantes)
--36 preguntas con opciones, 12 por tema y difreretens dificultades (1, 2 y 3)
--Evaluacion inicial con 15 preguntas

TRUNCATE TABLE
    auditoria, configuracion, recomendaciones, progreso_tema, respuestas,
    intentos, evaluacion_preguntas, evaluaciones, opciones_pregunta,
    preguntas, temas, matriculas, cursos, usuarios
RESTART IDENTITY CASCADE;

--usuarios (Claves en BCrypt)
INSERT INTO usuarios (nombre, correo, password_hash, rol) VALUES
    ('Administrador Evolua', 'admin@uni.edu',        '$2a$10$/vssaHIiWjhOTWykW7TijeCHGTQFYdccxMWPnM5OjAuqe42yVB8qW', 'ADMIN'),
    ('Laura Gómez',          'laura.gomez@uni.edu',  '$2a$10$HkoJgQ/WGTCcQ2pMPk2JUePkczjbu8WG0f6Jvxi77/NcMKlO3OJhi', 'DOCENTE'),
    ('Ana Pérez',            'ana.perez@uni.edu',    '$2a$10$oMBb7HhNWusdsBryDkKt1ePAZVx2CN2Ei63NTWoBKKdALv.0UGzt2', 'ESTUDIANTE'),
    ('Pedro Soto',           'pedro.soto@uni.edu',   '$2a$10$fOGRESZaC96mpTnbcOzUMu/.iVcW750g.Ad4hyt/0RmvkN4HwAru2', 'ESTUDIANTE'),
    ('Camila Rojas',         'camila.rojas@uni.edu', '$2a$10$L8/R3isNSbGMnzkfd8t8euLZEuFqJjgU9GmoHfUkGw82f/AWQp2Ey', 'ESTUDIANTE');

--cursos, matriculas y temas
INSERT INTO cursos (nombre, descripcion, docente_id)
SELECT 'Fundamentos de Programación',
       'Curso introductorio: variables, condicionales y ciclos.',
       id
FROM usuarios WHERE correo = 'laura.gomez@uni.edu';
 
INSERT INTO matriculas (estudiante_id, curso_id)
SELECT u.id, c.id
FROM usuarios u, cursos c
WHERE u.rol = 'ESTUDIANTE' AND c.nombre = 'Fundamentos de Programación';
 
INSERT INTO temas (curso_id, nombre, orden)
SELECT c.id, t.nombre, t.orden
FROM cursos c,
     (VALUES ('Variables', 1), ('Condicionales', 2), ('Ciclos', 3)) AS t (nombre, orden)
WHERE c.nombre = 'Fundamentos de Programación';

--tabla configuracion; parametros de motor adaptativo
INSERT INTO configuracion (clave, valor, descripcion) VALUES
    ('umbral_refuerzo',        '60', 'Por debajo de este porcentaje el tema requiere refuerzo'),
    ('umbral_dominio',         '80', 'Desde este porcentaje el tema se considera avanzado'),
    ('min_respuestas_tema',    '5',  'Respuestas mínimas para clasificar un tema'),
    ('intentos_para_dominio',  '2',  'Resultados satisfactorios necesarios para declarar dominio'),
    ('preguntas_por_practica', '5',  'Cantidad de preguntas de una práctica automática');

--banco de preguntas
CREATE OR REPLACE FUNCTION pg_temp.pq(
    p_tema text, p_dif int, p_enun text, p_retro text,
    p_correcta text, p_malas text[], p_pos int DEFAULT 1)
RETURNS void
LANGUAGE plpgsql
AS $$
DECLARE
    v_preg bigint;
    v_ops  text[];
BEGIN
    INSERT INTO preguntas (tema_id, enunciado, dificultad, tipo, retroalimentacion)
    SELECT id, p_enun, p_dif, 'SELECCION_MULTIPLE', p_retro FROM temas WHERE nombre = p_tema
    RETURNING id INTO v_preg;
 
    v_ops := p_malas[1:p_pos - 1] || p_correcta || p_malas[p_pos:3];
    FOR i IN 1..4 LOOP
        INSERT INTO opciones_pregunta (pregunta_id, texto, es_correcta, orden)
        VALUES (v_preg, v_ops[i], i = p_pos, i);
    END LOOP;
END;
$$;
 
CREATE OR REPLACE FUNCTION pg_temp.pvf(
    p_tema text, p_dif int, p_enun text, p_retro text, p_verdadera boolean)
RETURNS void
LANGUAGE plpgsql
AS $$
DECLARE
    v_preg bigint;
BEGIN
    INSERT INTO preguntas (tema_id, enunciado, dificultad, tipo, retroalimentacion)
    SELECT id, p_enun, p_dif, 'VERDADERO_FALSO', p_retro FROM temas WHERE nombre = p_tema
    RETURNING id INTO v_preg;
 
    INSERT INTO opciones_pregunta (pregunta_id, texto, es_correcta, orden) VALUES
        (v_preg, 'Verdadero', p_verdadera,     1),
        (v_preg, 'Falso',     NOT p_verdadera, 2);
END;
$$;
 
 
-- Tema Variables
-- Dificultad 1 (básica)
SELECT pg_temp.pq('Variables', 1, '¿Cuál es el tipo de dato más adecuado para guardar la edad de una persona (un número entero)?',
    'Los números enteros sin parte decimal se almacenan en variables de tipo int.',
    'int', ARRAY['String', 'boolean', 'double'], 2);
SELECT pg_temp.pq('Variables', 1, '¿Cuál de estos es un nombre de variable válido en Java?',
    'Un nombre no puede empezar con un número, no puede tener espacios ni ser una palabra reservada como class.',
    'total_ventas', ARRAY['2total', 'mi variable', 'class'], 3);
SELECT pg_temp.pq('Variables', 1, '¿Qué hace la instrucción int x = 5; ?',
    'Declara la variable x de tipo int y le asigna el valor 5.',
    'Declara la variable x y le asigna el valor 5', ARRAY['Compara x con 5', 'Imprime el valor 5 en pantalla', 'Declara una constante llamada 5'], 1);
SELECT pg_temp.pvf('Variables', 1, 'Una variable de tipo boolean solo puede almacenar los valores true o false.',
    'Correcto: boolean representa únicamente verdadero o falso.', true);
SELECT pg_temp.pq('Variables', 1, '¿Qué valor tiene x después de ejecutar estas instrucciones?
int x = 7;
x = x + 3;',
    'x vale 7 y se reasigna con 7 + 3, por lo que queda en 10.',
    '10', ARRAY['7', '3', '73'], 4);
-- Dificultad 2 (media)
SELECT pg_temp.pq('Variables', 2, '¿Cuál es el resultado de 10 / 4 cuando ambos operandos son de tipo int?',
    'La división entre enteros descarta la parte decimal: 10 / 4 da 2.',
    '2', ARRAY['2.5', '3', '2.0'], 1);
SELECT pg_temp.pq('Variables', 2, '¿Cuál es el resultado de 10 % 3?',
    'El operador % devuelve el residuo de la división: 10 entre 3 da 3 y sobra 1.',
    '1', ARRAY['3', '0', '3.33'], 3);
SELECT pg_temp.pq('Variables', 2, '¿Qué tipo de dato conviene para guardar el precio de un producto, por ejemplo 19.99?',
    'Los valores con parte decimal se guardan en double (o float).',
    'double', ARRAY['int', 'char', 'boolean'], 2);
SELECT pg_temp.pvf('Variables', 2, 'Una variable declarada con la palabra final puede cambiar su valor durante la ejecución del programa.',
    'Falso: final convierte la variable en una constante, que no se puede reasignar.', false);
-- Dificultad 3 (alta)
SELECT pg_temp.pq('Variables', 3, '¿Qué valor tiene r al final?
int a = 5;
int b = 2;
double r = a / b;',
    'a / b es una división entre enteros (da 2) y ese 2 se convierte después a double: 2.0.',
    '2.0', ARRAY['2.5', '2', '3.0'], 2);
SELECT pg_temp.pq('Variables', 3, '¿Qué valor tiene x al final?
int x = 3;
x += 2;
x *= 4;',
    'x += 2 deja x en 5 y x *= 4 lo multiplica por 4: queda en 20.',
    '20', ARRAY['11', '24', '9'], 4);
SELECT pg_temp.pq('Variables', 3, '¿Qué valores tienen i y j al final?
int i = 5;
int j = i++ + 2;',
    'i++ usa el valor actual (5) y después incrementa: j = 5 + 2 = 7 e i queda en 6.',
    'i = 6 y j = 7', ARRAY['i = 5 y j = 7', 'i = 6 y j = 8', 'i = 7 y j = 6'], 1);
 
-- Tema condicionales
-- Dificultad 1 (básica)
SELECT pg_temp.pq('Condicionales', 1, '¿Qué estructura permite ejecutar un bloque de código solo si se cumple una condición?',
    'La instrucción if ejecuta su bloque únicamente cuando la condición es verdadera.',
    'if', ARRAY['for', 'import', 'class'], 1);
SELECT pg_temp.pq('Condicionales', 1, '¿Qué operador se usa en Java para comprobar si dos valores numéricos son iguales?',
    'El operador == compara igualdad; un solo = es una asignación.',
    '==', ARRAY['=', '=>', '!='], 3);
SELECT pg_temp.pq('Condicionales', 1, '¿Qué imprime este código?
int n = 8;
if (n > 10) {
    System.out.println("A");
} else {
    System.out.println("B");
}',
    'Como 8 no es mayor que 10, se ejecuta el bloque else y se imprime B.',
    'B', ARRAY['A', 'A y B', 'No imprime nada'], 2);
SELECT pg_temp.pvf('Condicionales', 1, 'El bloque else se ejecuta cuando la condición del if es falsa.',
    'Verdadero: else cubre el caso en que la condición del if no se cumple.', true);
SELECT pg_temp.pq('Condicionales', 1, '¿Qué operador lógico significa "y" (deben cumplirse ambas condiciones)?',
    'El operador && es verdadero solo si las dos condiciones lo son.',
    '&&', ARRAY['||', '!', '=='], 1);
-- Dificultad 2 (media)
SELECT pg_temp.pq('Condicionales', 2, '¿Qué imprime este código?
int nota = 60;
if (nota >= 60) {
    System.out.println("Aprobado");
} else {
    System.out.println("Reprobado");
}',
    'La condición >= incluye el 60, por eso se imprime Aprobado.',
    'Aprobado', ARRAY['Reprobado', 'Aprobado y Reprobado', 'No imprime nada'], 3);
SELECT pg_temp.pq('Condicionales', 2, 'Si edad = 20 y tieneDocumento = false, ¿cuál es el valor de la expresión edad >= 18 && tieneDocumento?',
    'Con && ambas partes deben ser verdaderas; tieneDocumento es falsa, así que el resultado es false.',
    'false', ARRAY['true', '20', 'Genera un error'], 2);
SELECT pg_temp.pq('Condicionales', 2, '¿Qué condición es verdadera cuando x está entre 1 y 10, ambos incluidos?',
    'Se necesitan las dos comparaciones unidas con &&: x >= 1 && x <= 10.',
    'x >= 1 && x <= 10', ARRAY['x >= 1 || x <= 10', 'x > 1 && x < 10', 'x >= 1 && x >= 10'], 4);
SELECT pg_temp.pvf('Condicionales', 2, 'En una cadena if / else if / else se pueden ejecutar varios bloques en la misma pasada.',
    'Falso: en cuanto una condición se cumple se ejecuta ese bloque y se omiten los demás.', false);
-- Dificultad 3 (alta)
SELECT pg_temp.pq('Condicionales', 3, '¿Qué imprime este código?
int a = 4, b = 9;
if (a > 5 || b > 5 && a > b) {
    System.out.println("X");
} else {
    System.out.println("Y");
}',
    'El operador && se evalúa antes que ||: (b > 5 && a > b) es falso y a > 5 también, por lo tanto se imprime Y.',
    'Y', ARRAY['X', 'X y Y', 'Genera un error'], 1);
SELECT pg_temp.pq('Condicionales', 3, '¿Qué imprime este código?
int x = 7;
if (x % 2 == 0) System.out.println("par");
else if (x > 5) System.out.println("impar grande");
else System.out.println("impar pequeño");',
    '7 % 2 vale 1, así que no es par; como 7 > 5, se imprime impar grande.',
    'impar grande', ARRAY['par', 'impar pequeño', 'par e impar grande'], 2);
SELECT pg_temp.pq('Condicionales', 3, '¿Cuál es el resultado de !(5 > 3 && 2 > 4)?',
    '5 > 3 es true y 2 > 4 es false; true && false da false, y el operador ! lo invierte a true.',
    'true', ARRAY['false', '5', 'Genera un error'], 3);
 
-- Tema ciclos
-- Dificultad 1 (básica)
SELECT pg_temp.pq('Ciclos', 1, '¿Qué estructura repite un bloque de instrucciones mientras una condición sea verdadera?',
    'El ciclo while evalúa la condición antes de cada repetición.',
    'while', ARRAY['if', 'switch', 'return'], 2);
SELECT pg_temp.pq('Ciclos', 1, '¿Cuántas veces se ejecuta el ciclo for (int i = 1; i <= 5; i++)?',
    'i toma los valores 1, 2, 3, 4 y 5: son 5 repeticiones.',
    '5 veces', ARRAY['4 veces', '6 veces', 'Ninguna'], 1);
SELECT pg_temp.pq('Ciclos', 1, '¿Qué imprime este código?
for (int i = 1; i <= 3; i++) {
    System.out.println(i);
}',
    'Imprime 1, 2 y 3, cada número en su propia línea.',
    'Los números 1, 2 y 3', ARRAY['Los números 0, 1 y 2', 'Los números 1, 2, 3 y 4', 'Los números 3, 2 y 1'], 3);
SELECT pg_temp.pvf('Ciclos', 1, 'Un ciclo while puede no ejecutarse ni una sola vez.',
    'Verdadero: si la condición es falsa desde el inicio, el cuerpo nunca se ejecuta.', true);
SELECT pg_temp.pq('Ciclos', 1, '¿Para qué sirve la instrucción break dentro de un ciclo?',
    'break termina el ciclo de inmediato y el programa continúa después de él.',
    'Termina el ciclo inmediatamente', ARRAY['Salta a la siguiente repetición', 'Reinicia el ciclo desde cero', 'Declara una variable nueva'], 4);
-- Dificultad 2 (media)
SELECT pg_temp.pq('Ciclos', 2, '¿Qué valor tiene suma al terminar?
int suma = 0;
for (int i = 1; i <= 4; i++) {
    suma += i;
}',
    'Se acumulan 1 + 2 + 3 + 4, que da 10.',
    '10', ARRAY['4', '24', '15'], 2);
SELECT pg_temp.pq('Ciclos', 2, '¿Qué imprime este código?
int i = 0;
while (i < 3) {
    System.out.print(i + " ");
    i++;
}',
    'i empieza en 0 y el ciclo se repite mientras sea menor que 3: imprime 0 1 2.',
    '0 1 2', ARRAY['1 2 3', '0 1 2 3', 'Es un ciclo infinito'], 1);
SELECT pg_temp.pq('Ciclos', 2, '¿Qué diferencia hay entre while y do-while?',
    'do-while evalúa la condición al final, por eso su cuerpo se ejecuta al menos una vez.',
    'do-while ejecuta su cuerpo al menos una vez', ARRAY['while no permite usar condiciones', 'do-while nunca puede terminar', 'No hay ninguna diferencia'], 3);
SELECT pg_temp.pvf('Ciclos', 2, 'La instrucción continue termina por completo el ciclo.',
    'Falso: continue solo salta a la siguiente repetición; quien termina el ciclo es break.', false);
-- Dificultad 3 (alta)
SELECT pg_temp.pq('Ciclos', 3, '¿Cuántos asteriscos se imprimen?
for (int i = 0; i < 3; i++) {
    for (int j = 0; j < 2; j++) {
        System.out.print("*");
    }
}',
    'El ciclo interno imprime 2 asteriscos por cada una de las 3 vueltas del externo: 3 x 2 = 6.',
    '6', ARRAY['5', '3', '2'], 2);
SELECT pg_temp.pq('Ciclos', 3, '¿Qué valor tiene c al terminar?
int c = 0;
for (int i = 1; i <= 10; i++) {
    if (i % 3 == 0) continue;
    c++;
}',
    'Se omiten los múltiplos de 3 (3, 6 y 9); se cuentan las otras 7 repeticiones.',
    '7', ARRAY['3', '10', '6'], 4);
SELECT pg_temp.pq('Ciclos', 3, '¿Qué imprime este código?
int n = 5;
int f = 1;
while (n > 1) {
    f *= n;
    n--;
}
System.out.println(f);',
    'Calcula el factorial: 5 x 4 x 3 x 2 = 120.',
    '120', ARRAY['24', '60', '15'], 1);

--Evaluación incial
INSERT INTO evaluaciones (curso_id, nombre, tipo)
SELECT id, 'Evaluación inicial', 'INICIAL' FROM cursos WHERE nombre = 'Fundamentos de Programación';
 
INSERT INTO evaluacion_preguntas (evaluacion_id, pregunta_id, orden)
SELECT (SELECT id FROM evaluaciones WHERE nombre = 'Evaluación inicial'),
       q.id,
       row_number() OVER (ORDER BY t.orden, q.dificultad, q.id)
FROM (SELECT p.id, p.tema_id, p.dificultad,
             row_number() OVER (PARTITION BY p.tema_id, p.dificultad ORDER BY p.id) AS rn
      FROM preguntas p) q
JOIN temas t ON t.id = q.tema_id
WHERE (q.dificultad = 1 AND q.rn <= 2)
   OR (q.dificultad = 2 AND q.rn <= 2)
   OR (q.dificultad = 3 AND q.rn <= 1);

--verificacion
SELECT 'usuarios' AS tabla, count(*) AS filas FROM usuarios
UNION ALL SELECT 'cursos',               count(*) FROM cursos
UNION ALL SELECT 'matriculas',           count(*) FROM matriculas
UNION ALL SELECT 'temas',                count(*) FROM temas
UNION ALL SELECT 'preguntas',            count(*) FROM preguntas
UNION ALL SELECT 'opciones_pregunta',    count(*) FROM opciones_pregunta
UNION ALL SELECT 'evaluacion_preguntas', count(*) FROM evaluacion_preguntas
UNION ALL SELECT 'configuracion',        count(*) FROM configuracion;