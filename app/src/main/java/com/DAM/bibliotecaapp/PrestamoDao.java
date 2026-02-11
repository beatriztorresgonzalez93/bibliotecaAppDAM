package com.DAM.bibliotecaapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PrestamoDao {

    @Insert
    void insert(Prestamo prestamo);

    @Query("SELECT * FROM prestamo WHERE idUsuario = :idUsuario ORDER BY fechaPrestamo DESC")
    List<Prestamo> getPrestamosUsuario(int idUsuario);

    @Query("SELECT COUNT(*) FROM prestamo WHERE idEjemplar = :idEjemplar AND estado = 'ACTIVO'")
    int ejemplarPrestadoActivo(int idEjemplar);
}
