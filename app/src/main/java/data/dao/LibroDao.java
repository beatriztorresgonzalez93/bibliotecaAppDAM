package data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import data.entities.Libro;

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


}
