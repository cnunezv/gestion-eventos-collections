package eventos.servicio;

import eventos.modelo.EstadoEvento;
import eventos.modelo.Evento;
import eventos.modelo.Participante;
import eventos.modelo.Prioridad;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Reglas de negocio del sistema de eventos. Es la ÚNICA clase que modifica las colecciones,
 * así puede mantenerlas sincronizadas.
 */
public class GestorEventos {

    /**
     * Registro general de eventos en ORDEN DE REGISTRO.
     * ArrayList: se recorre completo para listar y se accede por posición; las inserciones son
     * siempre al final (O(1) amortizado). Los duplicados los impide el Map, no la lista.
     */
    private final List<Evento> eventos = new ArrayList<>();

    /**
     * Índice de búsqueda: código -> evento.
     * HashMap: get/containsKey en O(1) promedio, sin recorrer la lista. No necesita orden.
     */
    private final Map<String, Evento> eventosPorCodigo = new HashMap<>();

    /**
     * Participantes registrados: correo -> participante.
     * HashMap: el correo es único; containsKey impide registrar dos veces a la misma persona y
     * get la recupera directamente para inscribirla. (La unicidad DENTRO de cada evento la
     * garantiza el LinkedHashSet de inscritos de la clase Evento.)
     */
    private final Map<String, Participante> participantesPorCorreo = new HashMap<>();

    /**
     * Categorías de evento: únicas y en orden alfabético.
     * TreeSet con String.CASE_INSENSITIVE_ORDER: "Cultural" y "cultural" se consideran la
     * MISMA categoría (el comparador devuelve 0), así que la segunda no se agrega.
     */
    private final NavigableSet<String> categorias = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * Agenda: fecha -> eventos de ese día.
     * TreeMap porque mantiene las FECHAS ORDENADAS (LocalDate es Comparable: orden cronológico)
     * y permite consultas por rango (subMap) o "el próximo día con eventos" (ceilingKey),
     * cosas que un HashMap no puede hacer. Una fecha puede tener varios eventos: valor = List.
     */
    private final NavigableMap<LocalDate, List<Evento>> agenda = new TreeMap<>();

    /**
     * Cola de preparación logística (salón, sonido, refrigerios...).
     * PriorityQueue: NO sale el primero que llegó (eso sería FIFO), sino el de mayor prioridad
     * según ComparadoresEvento.POR_PRIORIDAD_DE_PREPARACION.
     */
    private final Queue<Evento> colaPreparacion = new PriorityQueue<>(ComparadoresEvento.POR_PRIORIDAD_DE_PREPARACION);

    /* ======================= EVENTOS ======================= */

    /** Registra un evento nuevo en la lista general y en el índice por código. */
    public Evento registrarEvento(String codigo, String nombre, LocalDate fecha, String categoria,
                                  Prioridad prioridad, int cupoMaximo) {
        String categoriaRegistrada = categoriaExistente(categoria); // referencia a categoría existente
        Evento evento = new Evento(codigo, nombre, fecha, categoriaRegistrada, prioridad, cupoMaximo); // valida datos
        if (eventosPorCodigo.containsKey(evento.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un evento con el código " + evento.getCodigo());
        }
        eventos.add(evento);
        eventosPorCodigo.put(evento.getCodigo(), evento);
        agregarAAgenda(evento);
        return evento;
    }

    /** Búsqueda directa por clave en el HashMap. Retorna null si no existe. */
    public Evento buscarEvento(String codigo) {
        return eventosPorCodigo.get(Evento.normalizarCodigo(codigo));
    }

    /** Igual que buscarEvento, pero lanza excepción si el código no existe. */
    public Evento obtenerEvento(String codigo) {
        Evento evento = buscarEvento(codigo);
        if (evento == null) {
            throw new IllegalArgumentException("No existe un evento con el código " + Evento.normalizarCodigo(codigo));
        }
        return evento;
    }

    public boolean existeEvento(String codigo) {
        return eventosPorCodigo.containsKey(Evento.normalizarCodigo(codigo));
    }

    /** Todos los eventos en el orden en que se registraron (vista de solo lectura). */
    public List<Evento> listarEventos() {
        return Collections.unmodifiableList(eventos);
    }

    public int totalEventos() {
        return eventos.size();
    }

    /* ======================= MODIFICACIÓN ======================= */
    /*
     * Un mismo objeto Evento está en varias colecciones a la vez (lista, mapa, agenda y quizá
     * la cola). Cada cambio de un atributo que alguna colección usa para ubicarlo obliga a
     * actualizar esa colección:
     *   - código    -> clave del HashMap, comparador de la PriorityQueue y orden por código;
     *   - fecha     -> clave del TreeMap agenda y comparador de la PriorityQueue;
     *   - prioridad -> comparador de la PriorityQueue.
     * La PriorityQueue NO se reordena sola si un elemento cambia: hay que sacarlo y volverlo a meter.
     */

    public void cambiarNombre(String codigo, String nuevoNombre) {
        obtenerEvento(codigo).setNombre(nuevoNombre); // no afecta a ninguna colección
    }

    public void cambiarCategoria(String codigo, String nuevaCategoria) {
        Evento evento = obtenerEvento(codigo);
        evento.setCategoria(categoriaExistente(nuevaCategoria));
    }

    public void cambiarCupo(String codigo, int nuevoCupo) {
        obtenerEvento(codigo).setCupoMaximo(nuevoCupo);
    }

    /** Cambia la fecha: mueve el evento de día en la agenda y lo reubica en la cola si está en ella. */
    public void cambiarFecha(String codigo, LocalDate nuevaFecha) {
        Evento evento = obtenerEvento(codigo);
        if (nuevaFecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        LocalDate fechaAnterior = evento.getFecha();
        boolean estabaEnCola = colaPreparacion.remove(evento);
        quitarDeAgenda(evento, fechaAnterior);
        evento.setFecha(nuevaFecha);
        agregarAAgenda(evento);
        if (estabaEnCola) {
            colaPreparacion.offer(evento);
        }
    }

    /**
     * Cambia la prioridad. Retorna true si el evento estaba en la cola de preparación y fue
     * reubicado (se saca y se vuelve a insertar para que la PriorityQueue lo ordene de nuevo).
     */
    public boolean cambiarPrioridad(String codigo, Prioridad nuevaPrioridad) {
        Evento evento = obtenerEvento(codigo);
        if (nuevaPrioridad == null) {
            throw new IllegalArgumentException("La prioridad es obligatoria");
        }
        boolean estabaEnCola = colaPreparacion.remove(evento);
        evento.setPrioridad(nuevaPrioridad);
        if (estabaEnCola) {
            colaPreparacion.offer(evento);
        }
        return estabaEnCola;
    }

    /**
     * Cambia el código (la clave del Map). Se elimina la asociación anterior y se crea la nueva.
     * Como equals() usa el código, el evento se saca de la cola ANTES de cambiarlo (con el
     * código viejo remove() lo encuentra) y se vuelve a insertar después.
     */
    public void cambiarCodigo(String codigoActual, String codigoNuevo) {
        Evento evento = obtenerEvento(codigoActual);
        String nuevo = Evento.validarCodigo(codigoNuevo);
        if (nuevo.equals(evento.getCodigo())) {
            return;
        }
        if (eventosPorCodigo.containsKey(nuevo)) {
            throw new IllegalArgumentException("Ya existe un evento con el código " + nuevo);
        }
        boolean estabaEnCola = colaPreparacion.remove(evento);
        eventosPorCodigo.remove(evento.getCodigo());   // quitar la asociación anterior
        evento.setCodigo(nuevo);
        eventosPorCodigo.put(nuevo, evento);           // registrar la nueva asociación
        if (estabaEnCola) {
            colaPreparacion.offer(evento);
        }
    }

    /* ======================= ELIMINACIÓN ======================= */

    /**
     * Elimina el evento de TODAS las colecciones donde participa, para que no quede disponible
     * por ningún camino (lista, mapa, agenda ni cola).
     */
    public Evento eliminarEvento(String codigo) {
        Evento evento = obtenerEvento(codigo);
        eventos.remove(evento);
        eventosPorCodigo.remove(evento.getCodigo());
        quitarDeAgenda(evento, evento.getFecha());
        colaPreparacion.remove(evento);
        return evento;
    }

    /* ======================= CONSISTENCIA ======================= */

    /**
     * Revisa que las colecciones estén sincronizadas. Retorna la lista de problemas
     * encontrados (vacía si todo está bien).
     */
    public List<String> verificarConsistencia() {
        List<String> problemas = new ArrayList<>();
        if (eventos.size() != eventosPorCodigo.size()) {
            problemas.add("La lista tiene " + eventos.size() + " eventos y el mapa " + eventosPorCodigo.size());
        }
        int enAgenda = 0;
        for (List<Evento> delDia : agenda.values()) {
            enAgenda += delDia.size();
        }
        if (enAgenda != eventos.size()) {
            problemas.add("La agenda tiene " + enAgenda + " eventos y la lista " + eventos.size());
        }
        for (Evento e : eventos) {
            if (eventosPorCodigo.get(e.getCodigo()) != e) {
                problemas.add("El mapa no apunta al evento " + e.getCodigo());
            }
            List<Evento> delDia = agenda.get(e.getFecha());
            if (delDia == null || !delDia.contains(e)) {
                problemas.add("El evento " + e.getCodigo() + " no está en la agenda del " + e.getFecha());
            }
        }
        for (Evento e : colaPreparacion) {
            if (eventosPorCodigo.get(e.getCodigo()) != e) {
                problemas.add("La cola contiene un evento que no está registrado: " + e.getCodigo());
            }
            if (e.getEstado() != EstadoEvento.PROGRAMADO) {
                problemas.add("La cola contiene un evento ya preparado: " + e.getCodigo());
            }
        }
        return problemas;
    }

    /** Resumen del contenido de cada colección (opción "estado de las colecciones" del menú). */
    public String resumenColecciones() {
        StringBuilder sb = new StringBuilder();
        sb.append("ArrayList<Evento>  eventos               : ").append(eventos.size()).append(" eventos (orden de registro)\n");
        sb.append("HashMap<String,Evento> eventosPorCodigo  : ").append(eventosPorCodigo.size()).append(" claves ")
          .append(eventosPorCodigo.keySet()).append("\n");
        sb.append("HashMap<String,Participante> por correo  : ").append(participantesPorCorreo.size()).append(" participantes\n");
        sb.append("TreeSet<String> categorias               : ").append(categorias).append("\n");
        sb.append("TreeMap<LocalDate,List<Evento>> agenda   : ").append(agenda.size()).append(" fechas, de ")
          .append(agenda.isEmpty() ? "-" : agenda.firstKey()).append(" a ")
          .append(agenda.isEmpty() ? "-" : agenda.lastKey()).append("\n");
        sb.append("PriorityQueue<Evento> colaPreparacion    : ").append(colaPreparacion.size()).append(" eventos\n");
        int inscripciones = 0;
        for (Evento e : eventos) {
            inscripciones += e.getInscritos().size();
        }
        sb.append("LinkedHashSet<Participante> (por evento) : ").append(inscripciones).append(" inscripciones en total\n");
        List<String> problemas = verificarConsistencia();
        sb.append("Consistencia entre colecciones           : ").append(problemas.isEmpty() ? "OK" : "PROBLEMAS " + problemas);
        return sb.toString();
    }

    /* ======================= COLA DE PREPARACIÓN (PRIORIDAD) ======================= */

    /** Pone un evento programado en la cola de preparación logística. */
    public Evento agregarAColaPreparacion(String codigo) {
        Evento evento = obtenerEvento(codigo);
        if (evento.getEstado() == EstadoEvento.PREPARADO) {
            throw new IllegalStateException("El evento " + evento.getCodigo() + " ya fue preparado");
        }
        if (colaPreparacion.contains(evento)) {
            throw new IllegalStateException("El evento " + evento.getCodigo() + " ya está en la cola de preparación");
        }
        colaPreparacion.offer(evento);
        return evento;
    }

    /** Consulta (sin sacar) el próximo evento a preparar: peek. null si la cola está vacía. */
    public Evento consultarProximoAPreparar() {
        return colaPreparacion.peek();
    }

    /** Saca de la cola el evento de mayor prioridad (poll) y lo marca como PREPARADO. */
    public Evento prepararSiguienteEvento() {
        Evento evento = colaPreparacion.poll();
        if (evento == null) {
            throw new IllegalStateException("No hay eventos en la cola de preparación");
        }
        evento.setEstado(EstadoEvento.PREPARADO);
        return evento;
    }

    public boolean estaEnColaPreparacion(Evento evento) {
        return colaPreparacion.contains(evento);
    }

    public int totalEnColaPreparacion() {
        return colaPreparacion.size();
    }

    /**
     * Contenido de la cola EN EL ORDEN EN QUE SALDRÍA. Ojo: recorrer una PriorityQueue con
     * for-each no da ese orden (es un heap), por eso se vacía una COPIA con poll().
     */
    public List<Evento> colaPreparacionEnOrden() {
        PriorityQueue<Evento> copia = new PriorityQueue<>(ComparadoresEvento.POR_PRIORIDAD_DE_PREPARACION);
        copia.addAll(colaPreparacion);
        List<Evento> enOrden = new ArrayList<>();
        while (!copia.isEmpty()) {
            enOrden.add(copia.poll());
        }
        return enOrden;
    }

    /* ======================= INFORMACIÓN ORDENADA ======================= */

    /** Agenda completa, recorrida en orden cronológico por el TreeMap. */
    public NavigableMap<LocalDate, List<Evento>> agendaPorFecha() {
        return Collections.unmodifiableNavigableMap(agenda);
    }

    /** Eventos entre dos fechas (incluidas) usando subMap del TreeMap, sin recorrer todo. */
    public NavigableMap<LocalDate, List<Evento>> agendaEntre(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            throw new IllegalArgumentException("Rango de fechas inválido");
        }
        return Collections.unmodifiableNavigableMap(agenda.subMap(desde, true, hasta, true));
    }

    /** Primera fecha con eventos igual o posterior a la indicada (ceilingKey). null si no hay. */
    public LocalDate proximaFechaConEventos(LocalDate desde) {
        return agenda.ceilingKey(desde);
    }

    /**
     * Eventos ordenados por código. Usa un TreeSet que ordena con el compareTo de Evento
     * (orden natural, Comparable). Se construye en el momento de la consulta.
     */
    public Set<Evento> eventosOrdenadosPorCodigo() {
        return Collections.unmodifiableSet(new TreeSet<>(eventos));
    }

    private void agregarAAgenda(Evento evento) {
        agenda.computeIfAbsent(evento.getFecha(), f -> new ArrayList<>()).add(evento);
    }

    /** Quita el evento de la lista de su fecha; si la fecha queda vacía, elimina la clave. */
    private void quitarDeAgenda(Evento evento, LocalDate fecha) {
        List<Evento> delDia = agenda.get(fecha);
        if (delDia != null) {
            delDia.remove(evento);
            if (delDia.isEmpty()) {
                agenda.remove(fecha);
            }
        }
    }

    /* ======================= CATEGORÍAS ======================= */

    /**
     * Registra una categoría. Retorna false si ya existía (sin importar mayúsculas):
     * el método add del Set es el que detecta el duplicado.
     */
    public boolean registrarCategoria(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
        return categorias.add(nombre.trim());
    }

    /**
     * Devuelve la categoría tal como quedó registrada ("cultural" -> "Cultural").
     * Lanza excepción si no existe: un evento no puede referenciar una categoría inexistente.
     */
    public String categoriaExistente(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        String encontrada = categorias.ceiling(nombre.trim()); // menor elemento >= nombre según el comparador
        if (encontrada == null || !encontrada.equalsIgnoreCase(nombre.trim())) {
            throw new IllegalArgumentException("La categoría '" + nombre.trim() + "' no existe. Regístrela primero");
        }
        return encontrada;
    }

    /** Categorías en orden alfabético (lo garantiza el TreeSet). */
    public Set<String> listarCategorias() {
        return Collections.unmodifiableSet(categorias);
    }

    /* ======================= PARTICIPANTES ======================= */

    /** Registra un participante; el correo no puede repetirse. */
    public Participante registrarParticipante(String correo, String nombre, String documento) {
        Participante participante = new Participante(correo, nombre, documento); // valida datos
        if (participantesPorCorreo.containsKey(participante.getCorreo())) {
            throw new IllegalArgumentException("Ya existe un participante con el correo " + participante.getCorreo());
        }
        participantesPorCorreo.put(participante.getCorreo(), participante);
        return participante;
    }

    public Participante buscarParticipante(String correo) {
        return participantesPorCorreo.get(Participante.normalizarCorreo(correo));
    }

    public Participante obtenerParticipante(String correo) {
        Participante p = buscarParticipante(correo);
        if (p == null) {
            throw new IllegalArgumentException("No existe un participante con el correo "
                    + Participante.normalizarCorreo(correo));
        }
        return p;
    }

    /**
     * Inscribe a un participante registrado en un evento existente.
     * Lanza excepción si el evento o el participante no existen, si ya estaba inscrito
     * (el Set lo rechaza) o si el cupo está lleno.
     */
    public void inscribir(String codigoEvento, String correo) {
        Evento evento = obtenerEvento(codigoEvento);
        Participante participante = obtenerParticipante(correo);
        boolean agregado = evento.inscribir(participante);
        if (!agregado) {
            throw new IllegalArgumentException(participante.getNombre() + " ya está inscrito en el evento "
                    + evento.getCodigo() + " (el Set no admite duplicados)");
        }
    }

    /** Retira a un participante de un evento. */
    public void retirarInscripcion(String codigoEvento, String correo) {
        Evento evento = obtenerEvento(codigoEvento);
        Participante participante = obtenerParticipante(correo);
        if (!evento.retirar(participante)) {
            throw new IllegalArgumentException(participante.getNombre() + " no está inscrito en " + evento.getCodigo());
        }
    }

    /** Participantes registrados (el orden de un HashMap no está garantizado). */
    public Collection<Participante> listarParticipantes() {
        return Collections.unmodifiableCollection(participantesPorCorreo.values());
    }

    public int totalParticipantes() {
        return participantesPorCorreo.size();
    }
}
