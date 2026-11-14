package eventos.modelo;

/**
 * Prioridad de un evento para la cola de preparación logística.
 * El nivel define el orden de atención: 1 se prepara antes que 2, y 2 antes que 3.
 */
public enum Prioridad {
    ALTA(1),
    MEDIA(2),
    BAJA(3);

    private final int nivel;

    Prioridad(int nivel) {
        this.nivel = nivel;
    }

    public int getNivel() {
        return nivel;
    }

    /** Convierte "alta", "Media", "BAJA"... en la constante. Lanza excepción si no es válida. */
    public static Prioridad desdeTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("La prioridad es obligatoria (ALTA, MEDIA o BAJA)");
        }
        try {
            return Prioridad.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Prioridad inválida: '" + texto + "'. Use ALTA, MEDIA o BAJA");
        }
    }
}
