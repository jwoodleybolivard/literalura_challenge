package com.woodley.literalura_challenge.repository;

import com.woodley.literalura_challenge.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AutorRepository extends JpaRepository<Autor, Long> {

    Optional<Autor> findAutorByNombre(String nombre);

    @Query("SELECT a FROM Autor a LEFT JOIN FETCH a.libros")
    List<Autor> findAllConLibros();

    @Query("SELECT a FROM Autor a LEFT JOIN FETCH a.libros l WHERE a.anoFallecimiento IS NULL OR a.anoFallecimiento > :ano AND a.anoNacimiento <= :ano")
    List<Autor> findAutoresVivosEnAnoConLibros(@Param("ano")int ano);
}
