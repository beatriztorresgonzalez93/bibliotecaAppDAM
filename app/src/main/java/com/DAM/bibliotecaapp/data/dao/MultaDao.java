package com.DAM.bibliotecaapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.DAM.bibliotecaapp.data.entities.Multa;

import java.util.List;

@Dao
public interface MultaDao {

    @Insert
    void insert(Multa m);

    @Query("SELECT COUNT(*) FROM multa WHERE idPrestamo = :idPrestamo")
    int existePorPrestamo(int idPrestamo);

    @Query("UPDATE multa SET diasRetraso = :dias, importe = :importe WHERE idPrestamo = :idPrestamo AND estado = 'PENDIENTE'")
    int actualizarPendiente(int idPrestamo, int dias, double importe);

    @Query("SELECT * FROM multa WHERE idUsuario = :idUsuario ORDER BY fechaCreacion DESC")
    List<Multa> getByUsuario(int idUsuario);

    @Query("SELECT COALESCE(SUM(importe), 0) FROM multa WHERE idUsuario = :idUsuario AND estado = 'PENDIENTE'")
    double totalPendienteUsuario(int idUsuario);

    @Query("UPDATE multa SET estado = 'PAGADA', fechaCierre = :ahora WHERE id = :idMulta AND estado = 'PENDIENTE'")
    int pagar(int idMulta, long ahora);

    @Query("UPDATE multa SET estado = 'CONDONADA', fechaCierre = :ahora WHERE id = :idMulta AND estado = 'PENDIENTE'")
    int condonar(int idMulta, long ahora);
}
