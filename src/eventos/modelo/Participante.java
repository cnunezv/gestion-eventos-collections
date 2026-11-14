package eventos.modelo;

import java.util.Objects;

/**
 * Persona que puede inscribirse en uno o varios eventos.
 * Se identifica por su CORREO: dos participantes con el mismo correo son la misma persona.
 */
public class Participante {

    private final String correo;
    private String nombre;
    private String documento;

    public Participante(String correo, String nombre, String documento) {
        this.correo = normalizarCorreo(correo);
        if (!this.correo.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new IllegalArgumentException("Correo inválido: '" + correo + "'");
        }
        setNombre(nombre);
        setDocumento(documento);
    }

    /** "  Ana@Correo.COM " -> "ana@correo.com": el correo no distingue mayúsculas. */
    public static String normalizarCorreo(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase();
    }

    public String getCorreo() {
        return correo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del participante es obligatorio");
        }
        this.nombre = nombre.trim();
    }

    public void setDocumento(String documento) {
        if (documento == null || !documento.trim().matches("\\d{5,12}")) {
            throw new IllegalArgumentException("El documento debe tener entre 5 y 12 dígitos");
        }
        this.documento = documento.trim();
    }

    /**
     * Igualdad por correo. Es el atributo que usan el HashMap de participantes (como clave)
     * y el LinkedHashSet de inscritos de cada evento (para impedir inscripciones repetidas).
     */
    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof Participante)) {
            return false;
        }
        return correo.equals(((Participante) otro).correo);
    }

    /** Coherente con equals: dos participantes iguales tienen el mismo hashCode. */
    @Override
    public int hashCode() {
        return Objects.hash(correo);
    }

    @Override
    public String toString() {
        return nombre + " <" + correo + "> doc. " + documento;
    }
}
