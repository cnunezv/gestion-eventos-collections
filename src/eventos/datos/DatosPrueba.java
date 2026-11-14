package eventos.datos;

import eventos.modelo.Prioridad;
import eventos.servicio.GestorEventos;

import java.time.LocalDate;

/**
 * Datos de prueba preparados a propósito para demostrar cada colección:
 *  - 16 eventos (entidad principal) registrados en un orden que NO coincide con el código,
 *    ni con la fecha, ni con la prioridad -> se notan los distintos órdenes;
 *  - varias fechas con más de un evento -> la agenda (TreeMap) agrupa por día;
 *  - prioridades mezcladas -> la PriorityQueue no sigue el orden de llegada;
 *  - 12 participantes con inscripciones en varios eventos (relación Evento - Participante);
 *  - 5 categorías (las pruebas intentan duplicarlas con otras mayúsculas).
 */
public final class DatosPrueba {

    private DatosPrueba() {
    }

    public static void cargar(GestorEventos g) {
        // Categorías (TreeSet): se registran desordenadas a propósito
        g.registrarCategoria("Tecnología");
        g.registrarCategoria("Cultural");
        g.registrarCategoria("Académico");
        g.registrarCategoria("Salud");
        g.registrarCategoria("Deportivo");

        // 16 eventos (ArrayList + HashMap + TreeMap)
        g.registrarEvento("EV-108", "Hackatón Caribe Digital", LocalDate.of(2026, 12, 5), "Tecnología", Prioridad.ALTA, 120);
        g.registrarEvento("EV-102", "Festival de Poesía del Caribe", LocalDate.of(2026, 11, 28), "Cultural", Prioridad.BAJA, 200);
        g.registrarEvento("EV-115", "Jornada de Salud Mental", LocalDate.of(2026, 11, 20), "Salud", Prioridad.MEDIA, 80);
        g.registrarEvento("EV-101", "Congreso de Matemáticas Aplicadas", LocalDate.of(2026, 12, 5), "Académico", Prioridad.ALTA, 300);
        g.registrarEvento("EV-110", "Torneo Interfacultades de Fútbol", LocalDate.of(2026, 11, 22), "Deportivo", Prioridad.MEDIA, 160);
        g.registrarEvento("EV-104", "Taller de Ciencia de Datos con Python", LocalDate.of(2026, 11, 20), "Tecnología", Prioridad.MEDIA, 40);
        g.registrarEvento("EV-112", "Concierto Sinfónico de Fin de Año", LocalDate.of(2026, 12, 12), "Cultural", Prioridad.ALTA, 500);
        g.registrarEvento("EV-106", "Seminario de Ingeniería de Software", LocalDate.of(2026, 11, 26), "Académico", Prioridad.BAJA, 150);
        g.registrarEvento("EV-114", "Brigada de Vacunación Universitaria", LocalDate.of(2026, 11, 18), "Salud", Prioridad.ALTA, 400);
        g.registrarEvento("EV-103", "Muestra de Cine Colombiano", LocalDate.of(2026, 11, 30), "Cultural", Prioridad.BAJA, 90);
        g.registrarEvento("EV-109", "Maratón Recreativa 5K Cartagena", LocalDate.of(2026, 12, 6), "Deportivo", Prioridad.MEDIA, 600);
        g.registrarEvento("EV-105", "Feria de Proyectos de Ingeniería", LocalDate.of(2026, 12, 3), "Académico", Prioridad.MEDIA, 250);
        g.registrarEvento("EV-111", "Charla de Ciberseguridad", LocalDate.of(2026, 11, 26), "Tecnología", Prioridad.ALTA, 60);
        g.registrarEvento("EV-107", "Torneo de Ajedrez Universitario", LocalDate.of(2026, 12, 10), "Deportivo", Prioridad.BAJA, 32);
        g.registrarEvento("EV-113", "Simposio de Investigación Estudiantil", LocalDate.of(2026, 12, 1), "Académico", Prioridad.MEDIA, 180);
        g.registrarEvento("EV-116", "Taller de Primeros Auxilios", LocalDate.of(2026, 11, 24), "Salud", Prioridad.BAJA, 3);

        // 12 participantes (HashMap por correo)
        g.registrarParticipante("valentina.rios@unicartagena.edu.co", "Valentina Ríos", "1047382910");
        g.registrarParticipante("jorge.perez@unicartagena.edu.co", "Jorge Pérez", "73145882");
        g.registrarParticipante("camila.herrera@unicartagena.edu.co", "Camila Herrera", "1050963271");
        g.registrarParticipante("andres.salgado@unicartagena.edu.co", "Andrés Salgado", "45528190");
        g.registrarParticipante("mariana.torres@gmail.com", "Mariana Torres", "1143367025");
        g.registrarParticipante("luis.martinez@hotmail.com", "Luis Martínez", "9876543");
        g.registrarParticipante("daniela.gomez@unicartagena.edu.co", "Daniela Gómez", "1001234567");
        g.registrarParticipante("beatriz.castro@gmail.com", "Beatriz Castro", "30812456");
        g.registrarParticipante("sebastian.mejia@unicartagena.edu.co", "Sebastián Mejía", "1128456903");
        g.registrarParticipante("alvaro.nunez@gmail.com", "Álvaro Núñez", "73001928");
        g.registrarParticipante("paola.villa@unicartagena.edu.co", "Paola Villa", "22001345");
        g.registrarParticipante("ernesto.lara@gmail.com", "Ernesto Lara", "22002678");

        // Inscripciones (LinkedHashSet en cada evento)
        g.inscribir("EV-101", "valentina.rios@unicartagena.edu.co");
        g.inscribir("EV-101", "camila.herrera@unicartagena.edu.co");
        g.inscribir("EV-101", "andres.salgado@unicartagena.edu.co");
        g.inscribir("EV-101", "alvaro.nunez@gmail.com");
        g.inscribir("EV-108", "jorge.perez@unicartagena.edu.co");
        g.inscribir("EV-108", "sebastian.mejia@unicartagena.edu.co");
        g.inscribir("EV-108", "daniela.gomez@unicartagena.edu.co");
        g.inscribir("EV-104", "valentina.rios@unicartagena.edu.co");
        g.inscribir("EV-104", "daniela.gomez@unicartagena.edu.co");
        g.inscribir("EV-110", "luis.martinez@hotmail.com");
        g.inscribir("EV-110", "ernesto.lara@gmail.com");
        g.inscribir("EV-112", "mariana.torres@gmail.com");
        g.inscribir("EV-112", "beatriz.castro@gmail.com");
        g.inscribir("EV-112", "paola.villa@unicartagena.edu.co");
        g.inscribir("EV-115", "paola.villa@unicartagena.edu.co");
        // EV-116 tiene cupo 3: se llena para probar la validación de cupo
        g.inscribir("EV-116", "camila.herrera@unicartagena.edu.co");
        g.inscribir("EV-116", "luis.martinez@hotmail.com");
        g.inscribir("EV-116", "ernesto.lara@gmail.com");
    }
}
