package com.DAM.bibliotecaapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import com.DAM.bibliotecaapp.data.entities.Ejemplar;

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

    @Query("UPDATE prestamo SET fechaDevolucion = :fechaDev, estado = 'DEVUELTO' WHERE id = :idPrestamo")
    int marcarDevuelto(int idPrestamo, long fechaDev);

    @Query("SELECT idEjemplar FROM prestamo WHERE id = :idPrestamo LIMIT 1")
    int getIdEjemplarByPrestamo(int idPrestamo);

    @Query("SELECT * FROM ejemplar WHERE idLibro = :idLibro AND estado = 'DISPONIBLE' LIMIT 1")
    Ejemplar getDisponibleByLibro(int idLibro);
    @Query("UPDATE ejemplar SET estado = :estado WHERE id = :idEjemplar")
    int updateEstado(int idEjemplar, String estado);

    @Query("SELECT * FROM ejemplar WHERE idLibro = :idLibro AND estado = 'PRESTADO' LIMIT 1")
    Ejemplar getPrimerPrestado(int idLibro);

    @Query("SELECT COUNT(*) FROM ejemplar WHERE idLibro = :idLibro AND estado = 'PRESTADO'")
    int countPrestadosByLibro(int idLibro);

    @Query("DELETE FROM ejemplar WHERE idLibro = :idLibro")
    void deleteByLibro(int idLibro);











}

