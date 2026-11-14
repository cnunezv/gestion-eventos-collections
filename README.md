# Sistema de gestión de eventos — Java Collections Framework

**Actividad:** Diseño de un sistema de gestión utilizando Java Collections Framework (Protocolo Individual, Unidad III)
**Asignatura:** Estructuras de Datos — Ingeniería de Software, Universidad de Cartagena
**Caso asignado:** 14. Sistema de gestión de eventos
**Autor:** Carlos Andrés Núñez Vargas

## Descripción del problema

Una oficina de bienestar y extensión universitaria organiza eventos (congresos, torneos, talleres, conciertos...). El sistema permite:

- registrar **eventos** con código único, nombre, fecha, categoría, prioridad y cupo;
- registrar **participantes** (identificados por su correo) e **inscribirlos** en eventos, sin duplicados;
- administrar **categorías** únicas;
- consultar la **agenda ordenada por fecha**;
- enviar eventos a una **cola de preparación logística** que se atiende **por prioridad** (no por orden de llegada);
- modificar y eliminar eventos manteniendo **todas las colecciones sincronizadas**.

## Colecciones utilizadas

| Necesidad del sistema | Interfaz | Implementación | Por qué |
| --- | --- | --- | --- |
| Registro general de eventos | `List<Evento>` | `ArrayList` | Conserva el orden de registro; se recorre completo y se accede por posición. Los duplicados los impide el Map. |
| Buscar evento por código | `Map<String, Evento>` | `HashMap` | Acceso directo por clave en O(1) promedio, sin recorrer la lista. |
| Participantes únicos por correo | `Map<String, Participante>` | `HashMap` | El correo es la clave: `containsKey` impide registrar dos veces a la misma persona. |
| Inscritos de cada evento sin repetir | `Set<Participante>` | `LinkedHashSet` | No admite al mismo participante dos veces (`equals`/`hashCode` por correo) y conserva el orden de inscripción. |
| Categorías únicas y ordenadas | `NavigableSet<String>` | `TreeSet` (`CASE_INSENSITIVE_ORDER`) | "Cultural" y "cultural" son la misma categoría; se listan alfabéticamente. |
| Agenda por fecha | `NavigableMap<LocalDate, List<Evento>>` | `TreeMap` | Fechas siempre en orden cronológico; permite rangos (`subMap`) y "próxima fecha" (`ceilingKey`). |
| Preparación logística por prioridad | `Queue<Evento>` | `PriorityQueue` + `Comparator` | Sale primero la prioridad ALTA; si empatan, la fecha más cercana; luego el código. |

## Estructura del proyecto

```
src/eventos/
├── Main.java                       Punto de entrada
├── modelo/                         Entidades del dominio
│   ├── Evento.java                 Comparable (por código), equals/hashCode por código, Set de inscritos
│   ├── Participante.java           equals/hashCode por correo
│   ├── Prioridad.java              ALTA, MEDIA, BAJA
│   └── EstadoEvento.java           PROGRAMADO, PREPARADO
├── servicio/                       Reglas de negocio y colecciones
│   ├── GestorEventos.java          Única clase que modifica las colecciones
│   └── ComparadoresEvento.java     Comparator de la cola de preparación
├── datos/DatosPrueba.java          16 eventos, 12 participantes, 5 categorías
├── ui/                             Interacción con el usuario
│   ├── Consola.java                Lectura y validación de entradas
│   └── MenuEventos.java            Menú de consola
└── pruebas/PruebasSistema.java     52 pruebas automáticas
```

## Requisitos y ejecución

- **Java 17 o superior** (desarrollado y probado con Java 21).

```bash
# Compilar
javac -encoding UTF-8 -d out $(find src -name "*.java")          # Linux / macOS / Git Bash
# En Windows (CMD):
#   dir /s /b src\*.java > fuentes.txt && javac -encoding UTF-8 -d out @fuentes.txt

# Ejecutar el sistema
java -cp out eventos.Main

# Ejecutar las pruebas
java -cp out eventos.pruebas.PruebasSistema
```

En VS Code (con *Extension Pack for Java*): abrir la carpeta y ejecutar `Main.java` con **Run**.

## Menú

```
 1. Registrar evento
 2. Ver todos los eventos (orden de registro)
 3. Buscar evento por código
 4. Participantes y categorías (datos únicos)
 5. Ver información ordenada (agenda, códigos, categorías)
 6. Enviar evento a la cola de preparación logística
 7. Consultar próximo evento a preparar
 8. Preparar siguiente evento (por prioridad)
 9. Modificar evento
10. Eliminar evento
11. Ver estado de las colecciones
 0. Salir
```

## Consistencia entre colecciones

Un mismo objeto `Evento` está en la lista, en el mapa, en la agenda y, a veces, en la cola. `GestorEventos` actualiza cada colección afectada:

- **Cambiar código:** se elimina la clave anterior del `HashMap` y se registra la nueva; si estaba en la cola, se saca antes del cambio y se vuelve a insertar.
- **Cambiar fecha:** se mueve de día en el `TreeMap` (si un día queda vacío se elimina la clave) y se reubica en la cola.
- **Cambiar prioridad:** la `PriorityQueue` no se reordena sola, por eso se saca y se vuelve a insertar.
- **Eliminar:** se quita de la lista, el mapa, la agenda y la cola.

La opción 11 del menú verifica la consistencia y muestra el contenido de cada colección.

## Documentación

La guía de la actividad está en el aula virtual (SIMA-PESAD) de la asignatura. El informe en PDF se entrega por ese medio.
