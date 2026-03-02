package com.DAM.bibliotecaapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import com.DAM.bibliotecaapp.data.entities.Libro;

@Dao
public interface LibroDao {
    @Insert
    void insertAll(List<Libro> libros);

    @Query("SELECT COUNT(*) FROM libro")
    int count();

    @Query("SELECT * FROM libro ORDER BY titulo")
    List<Libro> getAll();

    @Query("SELECT * FROM libro WHERE isbn = :isbn LIMIT 1")
    Libro getByIsbn(String isbn);
    @Query("SELECT * FROM libro WHERE titulo LIKE :q OR autor LIKE :q OR isbn LIKE :q OR editorial LIKE :q OR genero LIKE :q ORDER BY titulo")
    List<Libro> search(String q);

    @Insert
    long insert(Libro libro);

    @Query("SELECT COUNT(*) FROM libro WHERE isbn = :isbn")
    int countByIsbn(String isbn);

    @Query("DELETE FROM libro WHERE id = :idLibro")
    void deleteById(int idLibro);

    @Query("SELECT * FROM Libro ORDER BY COALESCE(genero,'') COLLATE NOCASE ASC, COALESCE(titulo,'') COLLATE NOCASE ASC")
    List<Libro> getAllOrderByGeneroSafe();






}
