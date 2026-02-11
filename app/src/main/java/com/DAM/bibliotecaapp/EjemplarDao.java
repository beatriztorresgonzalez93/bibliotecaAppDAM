package com.DAM.bibliotecaapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EjemplarDao {

    @Insert
    void insertAll(List<Ejemplar> ejemplares);

    @Query("SELECT * FROM ejemplar WHERE idLibro = :idLibro")
    List<Ejemplar> getByLibro(int idLibro);

    @Query("SELECT COUNT(*) FROM ejemplar")
    int count();

    @Query("SELECT COUNT(*) FROM ejemplar WHERE idLibro = :idLibro")
    int countTotal(int idLibro);

    @Query("SELECT COUNT(*) FROM ejemplar WHERE idLibro = :idLibro AND estado = 'DISPONIBLE'")
    int countDisponibles(int idLibro);

    @Query("SELECT * FROM ejemplar WHERE idLibro = :idLibro AND estado = 'DISPONIBLE' LIMIT 1")
    Ejemplar getPrimerDisponible(int idLibro);

    @Query("UPDATE ejemplar SET estado = :nuevoEstado WHERE id = :idEjemplar")
    void actualizarEstado(int idEjemplar, String nuevoEstado);
}
