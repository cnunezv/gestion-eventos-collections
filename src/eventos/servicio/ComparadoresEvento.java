package eventos.servicio;

import eventos.modelo.Evento;

import java.util.Comparator;

/**
 * Criterios de comparación de eventos distintos al orden natural (que es por código).
 */
public final class ComparadoresEvento {

    private ComparadoresEvento() {
    }

    /**
     * Criterio de la cola de preparación logística (PriorityQueue):
     *   1. Prioridad: ALTA (nivel 1) antes que MEDIA (2) y que BAJA (3).
     *   2. Si empatan, el evento con FECHA más cercana (hay menos tiempo para prepararlo).
     *   3. Si también empatan, por código, para que el orden sea siempre determinista.
     * La PriorityQueue deja en la cabeza al elemento "menor" según este comparador.
     */
    public static final Comparator<Evento> POR_PRIORIDAD_DE_PREPARACION =
            Comparator.comparingInt((Evento e) -> e.getPrioridad().getNivel())
                    .thenComparing(Evento::getFecha)
                    .thenComparing(Evento::getCodigo);
}
