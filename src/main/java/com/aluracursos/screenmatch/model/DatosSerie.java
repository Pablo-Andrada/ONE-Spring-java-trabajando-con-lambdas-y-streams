package com.aluracursos.screenmatch.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Representa los datos principales de una serie obtenidos de la API OMDB.
 *
 * <p>Esta clase es un "record" que modela la estructura básica de los datos
 * de una serie de televisión. Utiliza anotaciones de Jackson para mapear
 * propiedades del JSON a los campos del record.</p>
 *
 * <p>Características principales:
 * <ul>
 *   <li>Inmutable por defecto (propiedad de los records)</li>
 *   <li>Genera automáticamente constructor, getters, equals, hashCode y toString</li>
 *   <li>Optimizado para deserialización JSON</li>
 * </ul>
 *
 * @JsonIgnoreProperties(ignoreUnknown = true)
 *   - Ignora propiedades no mapeadas en el JSON
 *   - Evita errores si la API devuelve campos adicionales
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DatosSerie(
        /**
         * Título de la serie.
         *
         * @JsonAlias("Title") - Mapea el campo "Title" del JSON a este atributo
         * Ejemplo JSON: {"Title": "Breaking Bad", ...}
         */
        @JsonAlias("Title")
        String titulo,

        /**
         * Número total de temporadas de la serie.
         *
         * @JsonAlias("totalSeasons") - Mapea el campo "totalSeasons" del JSON
         * Nota: Usamos Integer para permitir valores nulos (la API podría no enviar este dato)
         * Ejemplo JSON: {"totalSeasons": "5", ...}
         */
        @JsonAlias("totalSeasons")
        Integer totalDeTemporadas,

        /**
         * Evaluación de la serie en IMDB.
         *
         * @JsonAlias("imdbRating") - Mapea el campo "imdbRating" del JSON
         * Usamos String en lugar de Double porque:
         *   - La API puede devolver "N/A" para series sin evaluación
         *   - Facilita el manejo de valores no numéricos
         * Ejemplo JSON: {"imdbRating": "9.5", ...}
         */
        @JsonAlias("imdbRating")
        String evaluacion
) {
    // Los records no necesitan cuerpo adicional
    // Generan automáticamente:
    //   - Constructor: DatosSerie(String titulo, Integer totalDeTemporadas, String evaluacion)
    //   - Métodos de acceso: titulo(), totalDeTemporadas(), evaluacion()
    //   - equals(), hashCode(), toString()
}