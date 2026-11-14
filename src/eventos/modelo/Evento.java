package eventos.modelo;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Evento organizado (conferencia, torneo, taller...).
 *
 * Orden natural (Comparable): por código. Igualdad (equals/hashCode): por código.
 */
public class Evento implements Comparable<Evento> {

    public static final int CUPO_MINIMO = 1;
    public static final int CUPO_MAXIMO = 5000;

    private String codigo;
    private String nombre;
    private LocalDate fecha;
    private String categoria;
    private Prioridad prioridad;
    private int cupoMaximo;
    private EstadoEvento estado;

    /**
     * Participantes inscritos. LinkedHashSet porque:
     *  - un mismo participante NO puede inscribirse dos veces (Set, usa equals/hashCode por correo);
     *  - conserva el orden de inscripción (Linked), útil para la lista de asistencia.
     */
    private final Set<Participante> inscritos = new LinkedHashSet<>();

    public Evento(String codigo, String nombre, LocalDate fecha, String categoria,
                  Prioridad prioridad, int cupoMaximo) {
        setCodigo(codigo);
        setNombre(nombre);
        setFecha(fecha);
        setCategoria(categoria);
        setPrioridad(prioridad);
        setCupoMaximo(cupoMaximo);
        this.estado = EstadoEvento.PROGRAMADO;
    }

    /** " ev-01 " -> "EV-01" */
    public static String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }

    /* ---------------- inscripciones ---------------- */

    /**
     * Inscribe al participante. Retorna false si ya estaba inscrito (el Set lo rechaza).
     * Lanza excepción si el cupo está lleno.
     */
    public boolean inscribir(Participante participante) {
        if (participante == null) {
            throw new IllegalArgumentException("El participante no puede ser null");
        }
        if (inscritos.contains(participante)) {
            return false;
        }
        if (inscritos.size() >= cupoMaximo) {
            throw new IllegalStateException("El evento " + codigo + " ya completó su cupo de " + cupoMaximo);
        }
        return inscritos.add(participante);
    }

    public boolean retirar(Participante participante) {
        return inscritos.remove(participante);
    }

    /** Vista de solo lectura: nadie modifica el Set sin pasar por inscribir/retirar. */
    public Set<Participante> getInscritos() {
        return Collections.unmodifiableSet(inscritos);
    }

    public int cuposDisponibles() {
        return cupoMaximo - inscritos.size();
    }

    /* ---------------- getters y setters con validación ---------------- */

    public String getCodigo() {
        return codigo;
    }

    /** OJO: el código es la clave del Map; solo el GestorEventos debe cambiarlo. */
    public void setCodigo(String codigo) {
        this.codigo = validarCodigo(codigo);
    }

    /** Normaliza y valida un código; lo usa también el gestor antes de cambiar una clave del Map. */
    public static String validarCodigo(String codigo) {
        String c = normalizarCodigo(codigo);
        if (c.isEmpty()) {
            throw new IllegalArgumentException("El código del evento es obligatorio");
        }
        if (!c.matches("[A-Z0-9-]{3,12}")) {
            throw new IllegalArgumentException("Código inválido: use de 3 a 12 letras, números o guiones (ej. EV-101)");
        }
        return c;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del evento es obligatorio");
        }
        this.nombre = nombre.trim();
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha del evento es obligatoria");
        }
        this.fecha = fecha;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        this.categoria = categoria.trim();
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Prioridad prioridad) {
        if (prioridad == null) {
            throw new IllegalArgumentException("La prioridad es obligatoria");
        }
        this.prioridad = prioridad;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(int cupoMaximo) {
        if (cupoMaximo < CUPO_MINIMO || cupoMaximo > CUPO_MAXIMO) {
            throw new IllegalArgumentException("El cupo debe estar entre " + CUPO_MINIMO + " y " + CUPO_MAXIMO);
        }
        if (cupoMaximo < inscritos.size()) {
            throw new IllegalArgumentException("El cupo no puede ser menor que los " + inscritos.size() + " inscritos");
        }
        this.cupoMaximo = cupoMaximo;
    }

    public EstadoEvento getEstado() {
        return estado;
    }

    public void setEstado(EstadoEvento estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        this.estado = estado;
    }

    /* ---------------- comparación ---------------- */

    /** Orden natural: alfabético por código (lo usa el TreeSet de "eventos por código"). */
    @Override
    public int compareTo(Evento otro) {
        return codigo.compareTo(otro.codigo);
    }

    /** Dos eventos son el mismo si tienen el mismo código. */
    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof Evento)) {
            return false;
        }
        return codigo.equals(((Evento) otro).codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return String.format("%-7s | %-34s | %s | %-11s | %-5s | %3d/%-4d | %s",
                codigo, recortar(nombre, 34), fecha, recortar(categoria, 11), prioridad,
                inscritos.size(), cupoMaximo, estado);
    }

    private static String recortar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max - 1) + ".";
    }
}
