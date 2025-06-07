package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.DatosEpisodio;
import com.aluracursos.screenmatch.model.DatosSerie;
import com.aluracursos.screenmatch.model.DatosTemporadas;
import com.aluracursos.screenmatch.model.Episodio;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    // Scanner para entrada de usuario
    private Scanner teclado = new Scanner(System.in);

    // Servicio para consumo de APIs
    private ConsumoAPI consumoApi = new ConsumoAPI();

    // URLs base y clave API para OMDB
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=4fc7c187";

    // Conversor de JSON a objetos Java
    private ConvierteDatos conversor = new ConvierteDatos();

    public void muestraElMenu() {
        // Paso 1: Solicitar nombre de la serie al usuario
        System.out.println("Por favor escribe el nombre de la serie que deseas buscar: ");
        var nombreSerie = teclado.nextLine();

        // Paso 2: Obtener datos generales de la serie
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + API_KEY);
        var datos = conversor.obtenerDatos(json, DatosSerie.class);
        System.out.println(datos);

        // Paso 3: Obtener datos de todas las temporadas
        List<DatosTemporadas> temporadas = new ArrayList<>();
        for (int i = 1; i <= datos.totalDeTemporadas(); i++) {
            json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + "&Season=" + i + API_KEY);
            var datosTemporadas = conversor.obtenerDatos(json, DatosTemporadas.class);
            temporadas.add(datosTemporadas);
        }

        // Paso 4: Mostrar títulos de episodios (dos métodos)
        // Método tradicional con bucles anidados
        for (int i = 0; i < datos.totalDeTemporadas() ; i++) {
            List<DatosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
            for (int j = 0; j < episodiosTemporada.size(); j++) {
                System.out.println(episodiosTemporada.get(j).titulo());
            }
        }

        // Método mejorado con Lambdas
        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        // Paso 5: Crear lista plana de todos los episodios
        List<DatosEpisodio> datosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

        // Paso 6: Top 5 episodios mejor evaluados
        System.out.println("\nTop 5 mejores episodios");
        datosEpisodios.stream()
                .filter(e -> !e.evaluacion().equalsIgnoreCase("N/A"))  // Filtrar episodios sin evaluación
                .peek(e -> System.out.println("Primer filtro (N/A) " + e))  // Debug: muestra después de primer filtro
                .sorted(Comparator.comparing(DatosEpisodio::evaluacion).reversed())  // Ordenar de mayor a menor evaluación
                .peek(e -> System.out.println("Segundo ordenación (M>m) " + e))  // Debug: muestra después de ordenar
                .map(e -> e.titulo().toUpperCase())  // Convertir títulos a mayúsculas
                .peek(e -> System.out.println("Tercer Filtro Mayúscula (m>M) " + e))  // Debug: muestra después de mapeo
                .limit(5)  // Limitar a los primeros 5 resultados
                .forEach(System.out::println);  // Imprimir resultados

        // Paso 7: Convertir datos a objetos Episodio
        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d)))  // Crear Episodio con número de temporada
                .collect(Collectors.toList());

        // Paso 8: Buscar episodios a partir de un año
        System.out.println("\nPor favor indica el año a partir del cual deseas ver los episodios");
        var fecha = teclado.nextInt();
        teclado.nextLine();  // Limpiar buffer

        LocalDate fechaBusqueda = LocalDate.of(fecha, 1, 1);  // Crear fecha de búsqueda
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");  // Formateador de fecha

        episodios.stream()
                .filter(e -> e.getFechaDeLanzamiento() != null &&
                        e.getFechaDeLanzamiento().isAfter(fechaBusqueda))  // Filtrar por fecha
                .forEach(e -> System.out.println(
                        "Temporada " + e.getTemporada() +
                                " Episodio " + e.getTitulo() +
                                " Fecha de Lanzamiento " + e.getFechaDeLanzamiento().format(dtf)
                ));

        // Paso 9: Buscar episodio por fragmento de título
        System.out.println("\nPor favor escriba el título del episodio que desea ver");
        var pedazoTitulo = teclado.nextLine();

        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(pedazoTitulo.toUpperCase()))  // Búsqueda case-insensitive
                .findFirst();  // Obtener el primer resultado

        // Manejar resultado de la búsqueda
        if(episodioBuscado.isPresent()){
            System.out.println("\nEpisodio encontrado");
            System.out.println("Los datos son: " + episodioBuscado.get());
        } else {
            System.out.println("\nEpisodio no encontrado");
        }

        // Paso 10: Estadísticas por temporada
        Map<Integer, Double> evaluacionesPorTemporada = episodios.stream()
                .filter(e -> e.getEvaluacion() > 0.0)  // Filtrar episodios con evaluación válida
                .collect(Collectors.groupingBy(
                        Episodio::getTemporada,  // Agrupar por temporada
                        Collectors.averagingDouble(Episodio::getEvaluacion)  // Calcular promedio
                ));
        System.out.println("\nEvaluaciones promedio por temporada:");
        System.out.println(evaluacionesPorTemporada);

        // Paso 11: Estadísticas generales
        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getEvaluacion() > 0.0)  // Filtrar evaluaciones válidas
                .collect(Collectors.summarizingDouble(Episodio::getEvaluacion));  // Recopilar estadísticas

        System.out.println("\nEstadísticas generales:");
        System.out.println("Media de las evaluaciones: " + est.getAverage());
        System.out.println("Episodio Mejor evaluado: " + est.getMax());
        System.out.println("Episodio Peor evaluado: " + est.getMin());
    }

    public static void main(String[] args) {
        Principal programa = new Principal();
        programa.muestraElMenu();
    }
}