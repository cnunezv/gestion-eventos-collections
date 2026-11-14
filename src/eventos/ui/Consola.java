package eventos.ui;

import eventos.modelo.Prioridad;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Lectura de datos desde el teclado. Convierte el texto al tipo pedido y, si no es válido,
 * lanza IllegalArgumentException con un mensaje claro (el menú la muestra como ERROR).
 */
public class Consola {

    /** Se lanza cuando ya no hay más entrada (por ejemplo, al redirigir un archivo). */
    public static class FinDeEntrada extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public FinDeEntrada() {
            super("No hay más datos de entrada");
        }
    }

    private final Scanner scanner;

    public Consola(Scanner scanner) {
        this.scanner = scanner;
    }

    public String leerTexto(String mensaje) {
        System.out.print(mensaje);
        if (!scanner.hasNextLine()) {
            throw new FinDeEntrada();
        }
        return scanner.nextLine().trim();
    }

    public int leerEntero(String mensaje, int minimo, int maximo) {
        String texto = leerTexto(mensaje);
        int valor;
        try {
            valor = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + texto + "' no es un número entero");
        }
        if (valor < minimo || valor > maximo) {
            throw new IllegalArgumentException("El valor debe estar entre " + minimo + " y " + maximo);
        }
        return valor;
    }

    /** Fecha en formato AAAA-MM-DD, por ejemplo 2026-12-05. */
    public LocalDate leerFecha(String mensaje) {
        String texto = leerTexto(mensaje);
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha inválida '" + texto + "'. Use el formato AAAA-MM-DD (ej. 2026-12-05)");
        }
    }

    public Prioridad leerPrioridad(String mensaje) {
        return Prioridad.desdeTexto(leerTexto(mensaje));
    }

    public boolean confirmar(String mensaje) {
        String r = leerTexto(mensaje + " (s/n): ");
        return r.equalsIgnoreCase("s") || r.equalsIgnoreCase("si") || r.equalsIgnoreCase("sí");
    }
}
