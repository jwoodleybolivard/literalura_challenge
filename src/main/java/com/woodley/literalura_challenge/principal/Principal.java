package com.woodley.literalura_challenge.principal;

import com.woodley.literalura_challenge.dto.AutorDTO;
import com.woodley.literalura_challenge.dto.LibroDTO;
import com.woodley.literalura_challenge.dto.RespuestaLibrosDTO;
import com.woodley.literalura_challenge.model.Autor;
import com.woodley.literalura_challenge.model.Libro;
import com.woodley.literalura_challenge.service.AutorService;
import com.woodley.literalura_challenge.service.ConsumoAPI;
import com.woodley.literalura_challenge.service.ConvierteDatos;
import com.woodley.literalura_challenge.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

@Component
public class Principal {
   @Autowired
    private LibroService libroService;

   @Autowired
    private AutorService autorService;

   @Autowired
    private ConsumoAPI consumoAPI;

    @Autowired
    private ConvierteDatos convierteDatos;

    private final String URL_BASE = "https://gutendex.com/books";

    private Scanner scanner = new Scanner(System.in);

    public void mostrarMenu() {
        while (true) {
            System.out.println("""
                    
                    |***********************************|
                    |        LITERALURA CHALLENGE       |
                    |***********************************|
                    | Selecione una opción:             |
                    |   1. Buscar libro por título      |
                    |   2. Listar libros registrados    |
                    |   3. Listar autores registrados   |
                    |   4. Listar autores vivos en un   |
                    |      año                          |
                    |   5. Listar libros por idioma     |
                    |                                   |
                    |   0. Sair                         |
                    |***********************************|
                    """);
            System.out.print("Digite una opción: => ");
            var opcion = scanner.nextInt();
            scanner.nextLine();
            if (opcion == 0) {
                System.out.println("Saliendo...");
                break;
            }
            switch (opcion) {
                case 1 -> {
                    System.out.print("Ingrese el título del libro: ");
                    String titulo = scanner.nextLine();
                    try {
                        String encodedTitulo = URLEncoder.encode(titulo, StandardCharsets.UTF_8);
                        String json = consumoAPI.consumirApi(URL_BASE + "?search=" + encodedTitulo);
                        RespuestaLibrosDTO respuestaLibrosDTO = convierteDatos.convertirDatos(json, RespuestaLibrosDTO.class);
                        List<LibroDTO> librosDTO = respuestaLibrosDTO.getLibros();
                        if (librosDTO.isEmpty()) {
                            System.out.println("Libro no encontrado en la API");
                        } else {
                            boolean libroRegistrado = false;
                            for (LibroDTO libroDTO : librosDTO) {
                                if (libroDTO.getTitulo().equalsIgnoreCase(titulo)) {
                                    Optional<Libro> libroExistente = libroService.obtenerLibroPorTitulo(titulo);
                                    if (libroExistente.isPresent()) {
                                        System.out.println("Detalle: Clave (titulo)=(" + titulo + ") ya existe");
                                        System.out.println("No se puede registrar el mismo libro más de una vez");
                                        libroRegistrado = true;
                                        break;
                                    } else {
                                        Libro libro = new Libro();
                                        libro.setTitulo(libroDTO.getTitulo());
                                        libro.setIdioma(libroDTO.getIdiomas().get(0));
                                        libro.setNumeroDescargas(libroDTO.getNumeroDescargas());

                                        // Buscar o crear el Autor
                                        AutorDTO primerAutorDTO = libroDTO.getAutores().get(0);
                                        Autor autor = autorService.obtenerAutorPorNombre(primerAutorDTO.getNombre())
                                                .orElseGet(() -> {
                                                    Autor nuevoAutor = new Autor();
                                                    nuevoAutor.setNombre(primerAutorDTO.getNombre());
                                                    nuevoAutor.setAnoNacimiento(Integer.parseInt(primerAutorDTO.getAnoNacimiento()));
                                                    nuevoAutor.setAnoFallecimiento(Integer.valueOf(primerAutorDTO.getAnoFallecimiento()));
                                                    return autorService.crearAutor(nuevoAutor);
                                                });

                                        // Asociar el Autor al Libro
                                        libro.setAutor(autor);

                                        // Guardar el libro en la base de datos
                                        libroService.crearLibro(libro);
                                        System.out.println("Libro registrado: " + libro.getTitulo());
                                        mostrarDetallesLibro(libroDTO);
                                        libroRegistrado = true;
                                        break;
                                    }
                                }
                            }
                            if (!libroRegistrado) {
                                System.out.println("No se encontró un libro exactamente con el título '" + titulo + "' en la API");
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Error al obtener datos de la API: " + e.getMessage());
                    }
                    break;
                }
                case 2 -> {
                    libroService.listarLibros().forEach(libro -> {
                        System.out.println("------LIBRO--------");
                        System.out.println("Título: " + libro.getTitulo());
                        System.out.println("Autor: " + (libro.getAutor() != null ? libro.getAutor().getNombre() : "Desconocido"));
                        System.out.println("Idioma: " + libro.getIdioma());
                        System.out.println("Número de descargas: " + libro.getNumeroDescargas());
                    });
                    break;
                }
                case 3 -> {
                    autorService.listarAutores().forEach(autor -> {
                        System.out.println("-------AUTOR-------");
                        System.out.println("Autor: " + autor.getNombre());
                        System.out.println("Fecha de nacimiento: " + autor.getAnoNacimiento());
                        System.out.println("Fecha de fallecimiento: " + (autor.getAnoFallecimiento() != null ? autor.getAnoFallecimiento() : "Desconocido"));
                        String libros = autor.getLibros().stream()
                                .map(Libro::getTitulo)
                                .collect(Collectors.joining(", "));
                        System.out.println("Libros: [ " + libros + " ]");
                    });
                    break;
                }
                case 4 -> {
                    System.out.print("Ingrese el año vivo del autor(es) que desea buscar: ");
                    int ano = scanner.nextInt();
                    scanner.nextLine(); // Consumir el salto de línea
                    List<Autor> autoresVivos = autorService.listarAutoresVivosEnAno(ano);
                    if (autoresVivos.isEmpty()) {
                        System.out.println("No se encontraron autores vivos en el año " + ano);
                    } else {
                        autoresVivos.forEach(autor -> {
                            System.out.println("-------AUTOR-------");
                            System.out.println("Autor: " + autor.getNombre());
                            System.out.println("Fecha de nacimiento: " + autor.getAnoNacimiento());
                            System.out.println("Fecha de fallecimiento: " + (autor.getAnoFallecimiento() != null ? autor.getAnoFallecimiento() : "Desconocido"));
                            System.out.println("Libros: " + autor.getLibros().size());
                        });
                    }
                    break;
                }
                case 5 -> {
                    System.out.println("Ingrese el idioma:");
                    System.out.println("es");
                    System.out.println("en");
                    System.out.println("fr");
                    System.out.println("pt");
                    String idioma = scanner.nextLine();
                    if ("es".equalsIgnoreCase(idioma) || "en".equalsIgnoreCase(idioma) || "fr".equalsIgnoreCase(idioma) || "pt".equalsIgnoreCase(idioma)) {
                        libroService.listarLibrosPorIdioma(idioma).forEach(libro -> {
                            System.out.println("------LIBRO--------");
                            System.out.println("Título: " + libro.getTitulo());
                            System.out.println("Autor: " + (libro.getAutor() != null ? libro.getAutor().getNombre() : "Desconocido"));
                            System.out.println("Idioma: " + libro.getIdioma());
                            System.out.println("Número de descargas: " + libro.getNumeroDescargas());
                        });
                    } else {
                        System.out.println("Idioma no válido. Intente de nuevo.");
                    }
                    break;
                }
                case 0 -> {
                    System.out.println("Saliendo...");
                    break;
                }
                default -> System.out.println("Opción no válida, intente de nuevo.");
            }
        }
    }
    private void mostrarDetallesLibro(LibroDTO libroDTO) {
        System.out.println("------LIBRO--------");
        System.out.println("Título: " + libroDTO.getTitulo());
        System.out.println("Autor: " + (libroDTO.getAutores().isEmpty() ? "Desconocido" : libroDTO.getAutores().get(0).getNombre()));
        System.out.println("Idioma: " + libroDTO.getIdiomas().get(0));
        System.out.println("Número de descargas: " + libroDTO.getNumeroDescargas());
    }
}
