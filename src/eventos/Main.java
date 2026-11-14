package eventos;

import eventos.datos.DatosPrueba;
import eventos.servicio.GestorEventos;
import eventos.ui.Consola;
import eventos.ui.MenuEventos;

import java.util.Scanner;

/**
 * Punto de entrada: crea el gestor, ofrece cargar los datos de prueba y arranca el menú.
 */
public class Main {

    public static void main(String[] args) {
        GestorEventos gestor = new GestorEventos();
        Consola consola = new Consola(new Scanner(System.in));

        System.out.println("=== SISTEMA DE GESTIÓN DE EVENTOS - Java Collections Framework ===");
        try {
            if (consola.confirmar("¿Cargar datos de prueba (16 eventos, 12 participantes, 5 categorías)?")) {
                DatosPrueba.cargar(gestor);
                System.out.println("Datos cargados: " + gestor.totalEventos() + " eventos, "
                        + gestor.totalParticipantes() + " participantes, " + gestor.listarCategorias().size() + " categorías.");
            }
        } catch (Consola.FinDeEntrada e) {
            return;
        }
        new MenuEventos(gestor, consola).iniciar();
    }
}
