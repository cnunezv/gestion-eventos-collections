package eventos.ui;

import eventos.modelo.Evento;
import eventos.modelo.Participante;
import eventos.modelo.Prioridad;
import eventos.servicio.GestorEventos;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Menú de consola. Solo pide datos y muestra resultados: las reglas y las colecciones
 * están en GestorEventos.
 */
public class MenuEventos {

    private static final String LINEA = "--------------------------------------------------------------------------------------------";
    private static final String ENCABEZADO = String.format("%-7s | %-34s | %-10s | %-11s | %-5s | %-8s | %s",
            "Código", "Nombre", "Fecha", "Categoría", "Prio.", "Inscr.", "Estado");

    private final GestorEventos gestor;
    private final Consola consola;

    public MenuEventos(GestorEventos gestor, Consola consola) {
        this.gestor = gestor;
        this.consola = consola;
    }

    public void iniciar() {
        boolean continuar = true;
        while (continuar) {
            mostrarMenuPrincipal();
            try {
                String opcion = consola.leerTexto("Opción: ");
                System.out.println();
                switch (opcion) {
                    case "1": registrarEvento(); break;
                    case "2": verTodos(); break;
                    case "3": buscarPorCodigo(); break;
                    case "4": menuParticipantesYCategorias(); break;
                    case "5": menuInformacionOrdenada(); break;
                    case "6": agregarAColaPreparacion(); break;
                    case "7": consultarProximo(); break;
                    case "8": prepararSiguiente(); break;
                    case "9": menuModificar(); break;
                    case "10": eliminarEvento(); break;
                    case "11": estadoColecciones(); break;
                    case "0": continuar = false; break;
                    default: System.out.println("Opción no válida.");
                }
            } catch (Consola.FinDeEntrada e) {
                continuar = false;
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
        System.out.println("Fin del programa.");
    }

    private void mostrarMenuPrincipal() {
        System.out.println();
        System.out.println("================== GESTIÓN DE EVENTOS ==================");
        System.out.println(" 1. Registrar evento");
        System.out.println(" 2. Ver todos los eventos (orden de registro)");
        System.out.println(" 3. Buscar evento por código");
        System.out.println(" 4. Participantes y categorías (datos únicos)");
        System.out.println(" 5. Ver información ordenada (agenda, códigos, categorías)");
        System.out.println(" 6. Enviar evento a la cola de preparación logística");
        System.out.println(" 7. Consultar próximo evento a preparar");
        System.out.println(" 8. Preparar siguiente evento (por prioridad)");
        System.out.println(" 9. Modificar evento");
        System.out.println("10. Eliminar evento");
        System.out.println("11. Ver estado de las colecciones");
        System.out.println(" 0. Salir");
    }

    /* ---------------- 1, 2 y 3 ---------------- */

    private void registrarEvento() {
        System.out.println("Categorías disponibles: " + gestor.listarCategorias());
        String codigo = consola.leerTexto("Código (ej. EV-120): ");
        String nombre = consola.leerTexto("Nombre: ");
        LocalDate fecha = consola.leerFecha("Fecha (AAAA-MM-DD): ");
        String categoria = consola.leerTexto("Categoría: ");
        Prioridad prioridad = consola.leerPrioridad("Prioridad (ALTA, MEDIA, BAJA): ");
        int cupo = consola.leerEntero("Cupo máximo (" + Evento.CUPO_MINIMO + "-" + Evento.CUPO_MAXIMO + "): ",
                Evento.CUPO_MINIMO, Evento.CUPO_MAXIMO);
        Evento evento = gestor.registrarEvento(codigo, nombre, fecha, categoria, prioridad, cupo);
        System.out.println("Evento registrado:");
        imprimirEvento(evento);
        System.out.println("Quedó almacenado en:");
        System.out.println(gestor.ubicacionEnColecciones(evento));
    }

    private void verTodos() {
        System.out.println("Eventos en orden de registro (ArrayList) - " + gestor.totalEventos() + " eventos");
        imprimirLista(gestor.listarEventos());
    }

    private void buscarPorCodigo() {
        String codigo = consola.leerTexto("Código del evento: ");
        Evento evento = gestor.buscarEvento(codigo); // HashMap.get: acceso directo por clave
        if (evento == null) {
            System.out.println("No existe un evento con el código " + Evento.normalizarCodigo(codigo)
                    + " (HashMap.get devolvió null).");
            return;
        }
        imprimirEvento(evento);
        System.out.println("Inscritos (" + evento.getInscritos().size() + "), en orden de inscripción:");
        int i = 1;
        for (Participante p : evento.getInscritos()) {
            System.out.println("  " + (i++) + ". " + p);
        }
        System.out.println("Ubicación en las colecciones:");
        System.out.println(gestor.ubicacionEnColecciones(evento));
    }

    /* ---------------- 4. Participantes y categorías ---------------- */

    private void menuParticipantesYCategorias() {
        System.out.println("--- Participantes y categorías ---");
        System.out.println("1. Registrar participante");
        System.out.println("2. Inscribir participante en un evento");
        System.out.println("3. Ver inscritos de un evento");
        System.out.println("4. Retirar inscripción");
        System.out.println("5. Ver participantes registrados");
        System.out.println("6. Registrar categoría");
        System.out.println("7. Ver categorías");
        System.out.println("0. Volver");
        String op = consola.leerTexto("Opción: ");
        switch (op) {
            case "1": {
                String correo = consola.leerTexto("Correo: ");
                String nombre = consola.leerTexto("Nombre: ");
                String documento = consola.leerTexto("Documento: ");
                Participante p = gestor.registrarParticipante(correo, nombre, documento);
                System.out.println("Participante registrado: " + p);
                break;
            }
            case "2": {
                String codigo = consola.leerTexto("Código del evento: ");
                String correo = consola.leerTexto("Correo del participante: ");
                gestor.inscribir(codigo, correo);
                Evento e = gestor.obtenerEvento(codigo);
                System.out.println("Inscripción realizada. " + e.getCodigo() + " tiene " + e.getInscritos().size()
                        + " inscritos y " + e.cuposDisponibles() + " cupos disponibles.");
                break;
            }
            case "3": {
                Evento e = gestor.obtenerEvento(consola.leerTexto("Código del evento: "));
                System.out.println(e.getCodigo() + " - " + e.getNombre() + ": " + e.getInscritos().size() + "/" + e.getCupoMaximo());
                int i = 1;
                for (Participante p : e.getInscritos()) {
                    System.out.println("  " + (i++) + ". " + p);
                }
                break;
            }
            case "4": {
                String codigo = consola.leerTexto("Código del evento: ");
                String correo = consola.leerTexto("Correo del participante: ");
                gestor.retirarInscripcion(codigo, correo);
                System.out.println("Inscripción retirada.");
                break;
            }
            case "5": {
                System.out.println("Participantes registrados (" + gestor.totalParticipantes() + "):");
                for (Participante p : gestor.listarParticipantes()) {
                    System.out.println("  " + p);
                }
                break;
            }
            case "6": {
                String nombre = consola.leerTexto("Nombre de la categoría: ");
                if (gestor.registrarCategoria(nombre)) {
                    System.out.println("Categoría registrada. Categorías: " + gestor.listarCategorias());
                } else {
                    System.out.println("La categoría '" + nombre + "' ya existe (TreeSet.add devolvió false). Categorías: "
                            + gestor.listarCategorias());
                }
                break;
            }
            case "7":
                System.out.println("Categorías (TreeSet, orden alfabético): " + gestor.listarCategorias());
                break;
            case "0":
                break;
            default:
                System.out.println("Opción no válida.");
        }
    }

    /* ---------------- 5. Información ordenada ---------------- */

    private void menuInformacionOrdenada() {
        System.out.println("--- Información ordenada ---");
        System.out.println("1. Agenda por fecha (TreeMap)");
        System.out.println("2. Eventos entre dos fechas (TreeMap.subMap)");
        System.out.println("3. Próxima fecha con eventos desde una fecha (TreeMap.ceilingKey)");
        System.out.println("4. Eventos ordenados por código (TreeSet con Comparable)");
        System.out.println("5. Categorías en orden alfabético (TreeSet)");
        System.out.println("0. Volver");
        String op = consola.leerTexto("Opción: ");
        switch (op) {
            case "1":
                imprimirAgenda(gestor.agendaPorFecha());
                break;
            case "2": {
                LocalDate desde = consola.leerFecha("Desde (AAAA-MM-DD): ");
                LocalDate hasta = consola.leerFecha("Hasta (AAAA-MM-DD): ");
                imprimirAgenda(gestor.agendaEntre(desde, hasta));
                break;
            }
            case "3": {
                LocalDate desde = consola.leerFecha("Desde (AAAA-MM-DD): ");
                LocalDate proxima = gestor.proximaFechaConEventos(desde);
                if (proxima == null) {
                    System.out.println("No hay eventos desde " + desde);
                } else {
                    System.out.println("Próxima fecha con eventos: " + proxima);
                    imprimirAgenda(gestor.agendaEntre(proxima, proxima));
                }
                break;
            }
            case "4":
                System.out.println("Eventos ordenados por código (compareTo de Evento):");
                imprimirLista(gestor.eventosOrdenadosPorCodigo());
                break;
            case "5":
                System.out.println("Categorías: " + gestor.listarCategorias());
                break;
            case "0":
                break;
            default:
                System.out.println("Opción no válida.");
        }
    }

    /* ---------------- 6, 7 y 8. Cola de preparación ---------------- */

    private void agregarAColaPreparacion() {
        Evento e = gestor.agregarAColaPreparacion(consola.leerTexto("Código del evento a enviar a preparación: "));
        System.out.println(e.getCodigo() + " (" + e.getPrioridad() + ", " + e.getFecha() + ") entró a la cola de preparación.");
        imprimirColaEnOrden();
    }

    private void consultarProximo() {
        Evento e = gestor.consultarProximoAPreparar(); // peek: mira sin sacar
        if (e == null) {
            System.out.println("La cola de preparación está vacía.");
            return;
        }
        System.out.println("Próximo evento a preparar (peek):");
        imprimirEvento(e);
        imprimirColaEnOrden();
    }

    private void prepararSiguiente() {
        Evento e = gestor.prepararSiguienteEvento(); // poll: saca el de mayor prioridad
        System.out.println("Evento preparado (poll):");
        imprimirEvento(e);
        imprimirColaEnOrden();
    }

    private void imprimirColaEnOrden() {
        List<Evento> cola = gestor.colaPreparacionEnOrden();
        System.out.println("Cola de preparación en orden de salida (" + cola.size() + "):");
        int i = 1;
        for (Evento e : cola) {
            System.out.println("  " + (i++) + ". " + e.getCodigo() + "  " + e.getPrioridad() + "  " + e.getFecha() + "  " + e.getNombre());
        }
    }

    /* ---------------- 9. Modificar ---------------- */

    private void menuModificar() {
        Evento e = gestor.obtenerEvento(consola.leerTexto("Código del evento a modificar: "));
        imprimirEvento(e);
        System.out.println("1. Nombre   2. Fecha   3. Categoría   4. Prioridad   5. Cupo   6. Código   0. Volver");
        String op = consola.leerTexto("¿Qué desea modificar?: ");
        String codigo = e.getCodigo();
        switch (op) {
            case "1":
                gestor.cambiarNombre(codigo, consola.leerTexto("Nuevo nombre: "));
                break;
            case "2":
                gestor.cambiarFecha(codigo, consola.leerFecha("Nueva fecha (AAAA-MM-DD): "));
                System.out.println("La agenda (TreeMap) movió el evento a su nueva fecha.");
                break;
            case "3":
                System.out.println("Categorías: " + gestor.listarCategorias());
                gestor.cambiarCategoria(codigo, consola.leerTexto("Nueva categoría: "));
                break;
            case "4": {
                boolean reubicado = gestor.cambiarPrioridad(codigo, consola.leerPrioridad("Nueva prioridad (ALTA, MEDIA, BAJA): "));
                if (reubicado) {
                    System.out.println("Estaba en la cola de preparación: se sacó y se volvió a insertar para reordenarla.");
                    imprimirColaEnOrden();
                }
                break;
            }
            case "5":
                gestor.cambiarCupo(codigo, consola.leerEntero("Nuevo cupo: ", Evento.CUPO_MINIMO, Evento.CUPO_MAXIMO));
                break;
            case "6": {
                String anterior = codigo;
                String nuevo = consola.leerTexto("Nuevo código: ");
                gestor.cambiarCodigo(anterior, nuevo);
                codigo = Evento.normalizarCodigo(nuevo);
                System.out.println("HashMap: se eliminó la clave " + anterior + " y se registró la clave " + codigo
                        + ". buscarEvento(\"" + anterior + "\") -> " + gestor.buscarEvento(anterior));
                break;
            }
            case "0":
                return;
            default:
                System.out.println("Opción no válida.");
                return;
        }
        Evento actualizado = gestor.obtenerEvento(codigo);
        System.out.println("Evento actualizado:");
        imprimirEvento(actualizado);
        System.out.println(gestor.ubicacionEnColecciones(actualizado));
    }

    /* ---------------- 10. Eliminar ---------------- */

    private void eliminarEvento() {
        Evento e = gestor.obtenerEvento(consola.leerTexto("Código del evento a eliminar: "));
        imprimirEvento(e);
        if (!consola.confirmar("¿Eliminar definitivamente este evento?")) {
            System.out.println("Eliminación cancelada.");
            return;
        }
        gestor.eliminarEvento(e.getCodigo());
        System.out.println("Evento " + e.getCodigo() + " eliminado. Comprobación:");
        System.out.println(gestor.ubicacionEnColecciones(e));
        System.out.println("  - buscarEvento(\"" + e.getCodigo() + "\") -> " + gestor.buscarEvento(e.getCodigo()));
    }

    /* ---------------- 11. Estado ---------------- */

    private void estadoColecciones() {
        System.out.println(gestor.resumenColecciones());
        imprimirColaEnOrden();
    }

    /* ---------------- utilidades de impresión ---------------- */

    private void imprimirEvento(Evento e) {
        System.out.println(ENCABEZADO);
        System.out.println(e);
    }

    private void imprimirLista(Iterable<Evento> lista) {
        System.out.println(ENCABEZADO);
        System.out.println(LINEA);
        for (Evento e : lista) {
            System.out.println(e);
        }
    }

    private void imprimirAgenda(Map<LocalDate, List<Evento>> agenda) {
        if (agenda.isEmpty()) {
            System.out.println("(no hay eventos en ese rango)");
            return;
        }
        for (Map.Entry<LocalDate, List<Evento>> dia : agenda.entrySet()) {
            System.out.println(dia.getKey() + " (" + dia.getValue().size() + "):");
            for (Evento e : dia.getValue()) {
                System.out.println("    " + e.getCodigo() + "  " + e.getNombre() + "  [" + e.getCategoria() + ", " + e.getPrioridad() + "]");
            }
        }
    }
}
