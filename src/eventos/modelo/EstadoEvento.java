package eventos.modelo;

/** Estado logístico de un evento. */
public enum EstadoEvento {
    /** Registrado; todavía no se ha preparado su logística. */
    PROGRAMADO,
    /** Ya salió de la cola de preparación: salón, equipos y materiales listos. */
    PREPARADO
}
