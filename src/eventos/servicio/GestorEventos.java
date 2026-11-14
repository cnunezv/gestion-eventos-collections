package eventos.servicio;

import eventos.modelo.Evento;
import eventos.modelo.Prioridad;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /* ======================= EVENTOS ======================= */

    /** Registra un evento nuevo en la lista general y en el índice por código. */
    public Evento registrarEvento(String codigo, String nombre, LocalDate fecha, String categoria,
                                  Prioridad prioridad, int cupoMaximo) {
        Evento evento = new Evento(codigo, nombre, fecha, categoria, prioridad, cupoMaximo); // valida datos
        if (eventosPorCodigo.containsKey(evento.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un evento con el código " + evento.getCodigo());
        }
        eventos.add(evento);
        eventosPorCodigo.put(evento.getCodigo(), evento);
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
}
