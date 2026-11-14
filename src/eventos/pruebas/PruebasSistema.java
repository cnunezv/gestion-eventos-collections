package eventos.pruebas;

import eventos.datos.DatosPrueba;
import eventos.modelo.EstadoEvento;
import eventos.modelo.Evento;
import eventos.modelo.Participante;
import eventos.modelo.Prioridad;
import eventos.servicio.GestorEventos;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Pruebas del sistema sin librerías externas. Cada una imprime [OK] o [FALLA].
 * Ejecutar: java -cp out eventos.pruebas.PruebasSistema
 */
public class PruebasSistema {

    private static int aprobadas;
    private static int fallidas;

    public static void main(String[] args) {
        try {
            pruebasRegistroYMap();
            pruebasSet();
            pruebasOrden();
            pruebasPrioridad();
            pruebasModificacion();
            pruebasEliminacion();
            pruebasEqualsHashCode();
        } catch (RuntimeException e) {
            fallidas++;
            System.out.println("[FALLA] Excepción inesperada: " + e);
        }
        System.out.println("--------------------------------------------------");
        System.out.println("Resultado: " + aprobadas + " pruebas OK, " + fallidas + " fallidas.");
        System.exit(fallidas == 0 ? 0 : 1);
    }

    private static GestorEventos nuevoGestorConDatos() {
        GestorEventos g = new GestorEventos();
        DatosPrueba.cargar(g);
        return g;
    }

    private static void pruebasRegistroYMap() {
        System.out.println("=== Registro (ArrayList) y búsqueda (HashMap) ===");
        GestorEventos g = nuevoGestorConDatos();
        verificar("Hay 16 eventos de prueba (mínimo 15)", g.totalEventos() == 16);
        verificar("La lista conserva el orden de registro (primero EV-108)",
                g.listarEventos().get(0).getCodigo().equals("EV-108"));
        Evento nuevo = g.registrarEvento("ev-120", "Feria del Libro Universitaria", LocalDate.of(2026, 12, 2),
                "cultural", Prioridad.MEDIA, 150);
        verificar("Registro válido: queda al final de la lista, en el mapa y en la agenda",
                g.listarEventos().get(16) == nuevo && g.buscarEvento("EV-120") == nuevo
                        && g.agendaPorFecha().get(LocalDate.of(2026, 12, 2)).contains(nuevo));
        verificar("El código se normaliza (ev-120 -> EV-120) y la categoría se toma la registrada (Cultural)",
                nuevo.getCodigo().equals("EV-120") && nuevo.getCategoria().equals("Cultural"));
        verificar("Buscar código existente (en minúsculas)", g.buscarEvento("ev-101").getNombre().startsWith("Congreso"));
        verificar("Buscar código inexistente devuelve null", g.buscarEvento("EV-999") == null);
        verificar("containsKey: existeEvento", g.existeEvento("EV-110") && !g.existeEvento("EV-999"));
        verificar("Código duplicado -> excepción", lanza(() -> g.registrarEvento("EV-101", "X", LocalDate.now(), "Salud", Prioridad.BAJA, 10)));
        verificar("Categoría inexistente -> excepción", lanza(() -> g.registrarEvento("EV-200", "X", LocalDate.now(), "Gastronomía", Prioridad.BAJA, 10)));
        verificar("Cupo fuera de rango -> excepción", lanza(() -> g.registrarEvento("EV-201", "X", LocalDate.now(), "Salud", Prioridad.BAJA, 0)));
        verificar("Código vacío -> excepción", lanza(() -> g.registrarEvento("  ", "X", LocalDate.now(), "Salud", Prioridad.BAJA, 10)));
        verificar("Prioridad inválida -> excepción", lanza(() -> Prioridad.desdeTexto("URGENTISIMA")));
    }

    private static void pruebasSet() {
        System.out.println("=== Datos únicos (Set y Map) ===");
        GestorEventos g = nuevoGestorConDatos();
        verificar("Categoría repetida con otras mayúsculas ('cultural') no se agrega",
                !g.registrarCategoria("cultural") && g.listarCategorias().size() == 5);
        verificar("Categoría nueva sí se agrega", g.registrarCategoria("Gastronomía") && g.listarCategorias().size() == 6);
        verificar("Participante con correo repetido (otras mayúsculas) -> excepción",
                lanza(() -> g.registrarParticipante("VALENTINA.RIOS@unicartagena.edu.co", "Otra", "123456")));
        verificar("Inscripción repetida en el mismo evento -> excepción (el Set la rechaza)",
                lanza(() -> g.inscribir("EV-101", "valentina.rios@unicartagena.edu.co")));
        verificar("EV-101 sigue con 4 inscritos", g.obtenerEvento("EV-101").getInscritos().size() == 4);
        verificar("El LinkedHashSet conserva el orden de inscripción",
                g.obtenerEvento("EV-101").getInscritos().iterator().next().getNombre().equals("Valentina Ríos"));
        verificar("Evento con cupo lleno (EV-116) -> excepción",
                lanza(() -> g.inscribir("EV-116", "valentina.rios@unicartagena.edu.co")));
        verificar("Inscribir participante inexistente -> excepción",
                lanza(() -> g.inscribir("EV-101", "nadie@correo.com")));
        verificar("Correo inválido -> excepción", lanza(() -> g.registrarParticipante("correo-sin-arroba", "X", "123456")));
    }

    private static void pruebasOrden() {
        System.out.println("=== Orden (TreeMap y TreeSet) ===");
        GestorEventos g = nuevoGestorConDatos();
        verificar("Agenda: primera fecha 2026-11-18 y última 2026-12-12",
                g.agendaPorFecha().firstKey().equals(LocalDate.of(2026, 11, 18))
                        && g.agendaPorFecha().lastKey().equals(LocalDate.of(2026, 12, 12)));
        verificar("Agenda agrupa: el 2026-12-05 hay 2 eventos (EV-108 y EV-101)",
                g.agendaPorFecha().get(LocalDate.of(2026, 12, 5)).size() == 2);
        verificar("subMap: del 2026-11-20 al 2026-11-26 hay 4 fechas",
                g.agendaEntre(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 26)).size() == 4);
        verificar("ceilingKey: próxima fecha desde 2026-11-27 es 2026-11-28",
                g.proximaFechaConEventos(LocalDate.of(2026, 11, 27)).equals(LocalDate.of(2026, 11, 28)));
        Iterator<Evento> it = g.eventosOrdenadosPorCodigo().iterator();
        verificar("Eventos por código: EV-101, EV-102, EV-103...",
                it.next().getCodigo().equals("EV-101") && it.next().getCodigo().equals("EV-102")
                        && it.next().getCodigo().equals("EV-103"));
        verificar("Categorías en orden alfabético", g.listarCategorias().iterator().next().equals("Académico"));
    }

    private static void pruebasPrioridad() {
        System.out.println("=== Prioridad (PriorityQueue) ===");
        GestorEventos g = nuevoGestorConDatos();
        // se agregan en orden BAJA, MEDIA, ALTA (contrario a la prioridad)
        g.agregarAColaPreparacion("EV-106"); // BAJA
        g.agregarAColaPreparacion("EV-115"); // MEDIA
        g.agregarAColaPreparacion("EV-111"); // ALTA, 2026-11-26
        g.agregarAColaPreparacion("EV-114"); // ALTA, 2026-11-18 (fecha más cercana)
        verificar("peek: sale primero ALTA con fecha más cercana (EV-114), no el primero que llegó",
                g.consultarProximoAPreparar().getCodigo().equals("EV-114"));
        List<Evento> orden = g.colaPreparacionEnOrden();
        verificar("Orden completo: EV-114, EV-111, EV-115, EV-106",
                orden.get(0).getCodigo().equals("EV-114") && orden.get(1).getCodigo().equals("EV-111")
                        && orden.get(2).getCodigo().equals("EV-115") && orden.get(3).getCodigo().equals("EV-106"));
        Evento preparado = g.prepararSiguienteEvento();
        verificar("poll saca EV-114 y lo marca PREPARADO",
                preparado.getCodigo().equals("EV-114") && preparado.getEstado() == EstadoEvento.PREPARADO
                        && g.totalEnColaPreparacion() == 3);
        verificar("No se puede volver a encolar un evento preparado", lanza(() -> g.agregarAColaPreparacion("EV-114")));
        verificar("No se puede encolar dos veces el mismo evento", lanza(() -> g.agregarAColaPreparacion("EV-106")));
        GestorEventos vacio = new GestorEventos();
        verificar("Preparar con la cola vacía -> excepción", lanza(vacio::prepararSiguienteEvento));
        verificar("peek con la cola vacía devuelve null", vacio.consultarProximoAPreparar() == null);
    }

    private static void pruebasModificacion() {
        System.out.println("=== Modificación con consistencia ===");
        GestorEventos g = nuevoGestorConDatos();
        g.agregarAColaPreparacion("EV-106"); // BAJA
        g.agregarAColaPreparacion("EV-111"); // ALTA
        boolean reubicado = g.cambiarPrioridad("EV-106", Prioridad.ALTA); // 2026-11-26 = misma fecha que EV-111
        verificar("Cambiar prioridad reubica el evento en la cola (EV-106 pasa al frente por código)",
                reubicado && g.consultarProximoAPreparar().getCodigo().equals("EV-106"));
        g.cambiarFecha("EV-101", LocalDate.of(2026, 12, 15));
        verificar("Cambiar fecha mueve el evento en la agenda (sale del 12-05 y entra al 12-15)",
                g.agendaPorFecha().get(LocalDate.of(2026, 12, 5)).size() == 1
                        && g.agendaPorFecha().get(LocalDate.of(2026, 12, 15)).get(0).getCodigo().equals("EV-101"));
        g.cambiarFecha("EV-114", LocalDate.of(2026, 12, 20)); // era la única del 11-18
        verificar("Si una fecha queda vacía, se elimina la clave del TreeMap",
                !g.agendaPorFecha().containsKey(LocalDate.of(2026, 11, 18)));
        g.cambiarCodigo("EV-111", "EV-211");
        verificar("Cambiar código: el Map elimina la clave vieja y crea la nueva",
                g.buscarEvento("EV-111") == null && g.buscarEvento("EV-211") != null);
        verificar("Cambiar código: el evento sigue en la cola con el código nuevo",
                g.estaEnColaPreparacion(g.buscarEvento("EV-211")) && g.totalEnColaPreparacion() == 2);
        verificar("Cambiar a un código que ya existe -> excepción", lanza(() -> g.cambiarCodigo("EV-211", "EV-101")));
        verificar("Cambiar a una categoría inexistente -> excepción", lanza(() -> g.cambiarCategoria("EV-101", "Moda")));
        verificar("Cupo menor que los inscritos -> excepción", lanza(() -> g.cambiarCupo("EV-101", 2)));
        verificar("Después de todos los cambios las colecciones siguen consistentes", g.verificarConsistencia().isEmpty());
    }

    private static void pruebasEliminacion() {
        System.out.println("=== Eliminación con consistencia ===");
        GestorEventos g = nuevoGestorConDatos();
        g.agregarAColaPreparacion("EV-112");
        Evento eliminado = g.eliminarEvento("EV-112");
        verificar("Eliminado de la lista", !g.listarEventos().contains(eliminado) && g.totalEventos() == 15);
        verificar("Eliminado del Map", g.buscarEvento("EV-112") == null);
        verificar("Eliminado de la agenda (su fecha 2026-12-12 desaparece)",
                !g.agendaPorFecha().containsKey(LocalDate.of(2026, 12, 12)));
        verificar("Eliminado de la cola de preparación", g.totalEnColaPreparacion() == 0);
        verificar("Eliminar inexistente -> excepción", lanza(() -> g.eliminarEvento("EV-112")));
        verificar("Colecciones consistentes después de eliminar", g.verificarConsistencia().isEmpty());
    }

    private static void pruebasEqualsHashCode() {
        System.out.println("=== equals() y hashCode() ===");
        Participante a = new Participante("Ana@Correo.com", "Ana", "123456");
        Participante b = new Participante("ana@correo.com", "Ana María", "654321");
        verificar("Mismo correo -> equals true y mismo hashCode", a.equals(b) && a.hashCode() == b.hashCode());
        Set<Participante> set = new HashSet<>();
        set.add(a);
        verificar("Un HashSet no admite el segundo", !set.add(b) && set.size() == 1);
        Evento e1 = new Evento("EV-1", "A", LocalDate.now(), "X", Prioridad.ALTA, 10);
        Evento e2 = new Evento("ev-1", "B", LocalDate.now(), "Y", Prioridad.BAJA, 20);
        verificar("Eventos con el mismo código son iguales y compareTo da 0",
                e1.equals(e2) && e1.hashCode() == e2.hashCode() && e1.compareTo(e2) == 0);
    }

    /* ---------------- utilidades ---------------- */

    private static void verificar(String descripcion, boolean condicion) {
        if (condicion) {
            aprobadas++;
            System.out.println("[OK]    " + descripcion);
        } else {
            fallidas++;
            System.out.println("[FALLA] " + descripcion);
        }
    }

    private static boolean lanza(Runnable accion) {
        try {
            accion.run();
            return false;
        } catch (IllegalArgumentException | IllegalStateException e) {
            return true;
        }
    }
}
