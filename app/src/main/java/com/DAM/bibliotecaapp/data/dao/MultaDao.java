package com.DAM.bibliotecaapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.DAM.bibliotecaapp.data.entities.Multa;

import java.util.List;
import com.DAM.bibliotecaapp.data.pojo.MultaGlobal;
import com.DAM.bibliotecaapp.data.pojo.MultaInfo;


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

    @Query(
            "SELECT " +
                    " m.id AS idMulta, " +
                    " m.idUsuario AS idUsuario, " +
                    " u.nombre AS nombreUsuario, " +
                    " u.email AS emailUsuario, " +
                    " m.idPrestamo AS idPrestamo, " +
                    " m.diasRetraso AS diasRetraso, " +
                    " m.importe AS importe, " +
                    " m.fechaCreacion AS fechaCreacion, " +
                    " m.fechaCierre AS fechaCierre, " +
                    " m.estado AS estado " +
                    "FROM multa m " +
                    "JOIN Usuario u ON u.id = m.idUsuario " +
                    "ORDER BY (m.estado = 'PENDIENTE') DESC, m.fechaCreacion DESC"
    )
    List<MultaGlobal> getAllGlobal();

    @Query(
            "SELECT " +
                    " m.id AS idMulta, m.idUsuario AS idUsuario, u.nombre AS nombreUsuario, u.email AS emailUsuario, " +
                    " m.idPrestamo AS idPrestamo, m.diasRetraso AS diasRetraso, m.importe AS importe, " +
                    " m.fechaCreacion AS fechaCreacion, m.fechaCierre AS fechaCierre, m.estado AS estado " +
                    "FROM multa m " +
                    "JOIN Usuario u ON u.id = m.idUsuario " +
                    "WHERE m.estado = 'PENDIENTE' " +
                    "ORDER BY m.fechaCreacion DESC"
    )
    List<MultaGlobal> getPendientesGlobal();

    @Query(
            "SELECT m.id, m.idUsuario, u.nombre AS nombreUsuario, " +
                    "m.idPrestamo, l.titulo AS tituloLibro, " +
                    "m.diasRetraso, m.importe, m.estado, m.fechaCreacion, m.fechaCierre " +
                    "FROM multa m " +
                    "JOIN usuario u ON u.id = m.idUsuario " +
                    "JOIN prestamo p ON p.id = m.idPrestamo " +
                    "JOIN ejemplar e ON e.id = p.idEjemplar " +
                    "JOIN libro l ON l.id = e.idLibro " +
                    "ORDER BY m.fechaCreacion DESC"
    )
    List<MultaInfo> getAllInfo();

    @Query(
            "SELECT m.id, m.idUsuario, u.nombre AS nombreUsuario, " +
                    "m.idPrestamo, l.titulo AS tituloLibro, " +
                    "m.diasRetraso, m.importe, m.estado, m.fechaCreacion, m.fechaCierre " +
                    "FROM multa m " +
                    "JOIN usuario u ON u.id = m.idUsuario " +
                    "JOIN prestamo p ON p.id = m.idPrestamo " +
                    "JOIN ejemplar e ON e.id = p.idEjemplar " +
                    "JOIN libro l ON l.id = e.idLibro " +
                    "WHERE m.estado = 'PENDIENTE' " +
                    "ORDER BY m.fechaCreacion DESC"
    )
    List<MultaInfo> getPendientesInfo();

    @Query("SELECT COUNT(*) FROM multa")
    int count();

    @Insert
    void insertAll(List<Multa> multas);

    @Query(
            "SELECT m.id AS id, " +
                    "u.id AS idUsuario, u.nombre AS nombreUsuario, " +
                    "p.id AS idPrestamo, l.titulo AS tituloLibro, " +
                    "m.diasRetraso AS diasRetraso, m.importe AS importe, " +
                    "m.estado AS estado, " +
                    "m.fechaCreacion AS fechaCreacion, m.fechaCierre AS fechaCierre " +
                    "FROM multa m " +
                    "JOIN usuario u ON u.id = m.idUsuario " +
                    "JOIN prestamo p ON p.id = m.idPrestamo " +
                    "JOIN ejemplar e ON e.id = p.idEjemplar " +
                    "JOIN libro l ON l.id = e.idLibro " +
                    "WHERE m.estado = 'PAGADA' " +
                    "ORDER BY m.fechaCreacion DESC"
    )
    List<MultaInfo> getPagadasInfo();

    @Query(
            "SELECT m.id AS id, " +
                    "u.id AS idUsuario, u.nombre AS nombreUsuario, " +
                    "p.id AS idPrestamo, l.titulo AS tituloLibro, " +
                    "m.diasRetraso AS diasRetraso, m.importe AS importe, " +
                    "m.estado AS estado, " +
                    "m.fechaCreacion AS fechaCreacion, m.fechaCierre AS fechaCierre " +
                    "FROM multa m " +
                    "JOIN usuario u ON u.id = m.idUsuario " +
                    "JOIN prestamo p ON p.id = m.idPrestamo " +
                    "JOIN ejemplar e ON e.id = p.idEjemplar " +
                    "JOIN libro l ON l.id = e.idLibro " +
                    "WHERE m.estado = 'CONDONADA' " +
                    "ORDER BY m.fechaCreacion DESC"
    )
    List<MultaInfo> getCondonadasInfo();

    @Query(
            "SELECT m.id, m.idUsuario, u.nombre AS nombreUsuario, " +
                    "m.idPrestamo, l.titulo AS tituloLibro, " +
                    "m.diasRetraso, m.importe, m.estado, m.fechaCreacion, m.fechaCierre " +
                    "FROM multa m " +
                    "JOIN usuario u ON u.id = m.idUsuario " +
                    "JOIN prestamo p ON p.id = m.idPrestamo " +
                    "JOIN ejemplar e ON e.id = p.idEjemplar " +
                    "JOIN libro l ON l.id = e.idLibro " +
                    "WHERE (:idUsuario IS NULL OR m.idUsuario = :idUsuario) " +
                    "ORDER BY m.fechaCreacion DESC"
    )
    List<MultaInfo> getAllInfoFiltrado(Integer idUsuario);

    @Query(
            "SELECT m.id, m.idUsuario, u.nombre AS nombreUsuario, " +
                    "m.idPrestamo, l.titulo AS tituloLibro, " +
                    "m.diasRetraso, m.importe, m.estado, m.fechaCreacion, m.fechaCierre " +
                    "FROM multa m " +
                    "JOIN usuario u ON u.id = m.idUsuario " +
                    "JOIN prestamo p ON p.id = m.idPrestamo " +
                    "JOIN ejemplar e ON e.id = p.idEjemplar " +
                    "JOIN libro l ON l.id = e.idLibro " +
                    "WHERE m.estado = 'PENDIENTE' " +
                    "AND (:idUsuario IS NULL OR m.idUsuario = :idUsuario) " +
                    "ORDER BY m.fechaCreacion DESC"
    )
    List<MultaInfo> getPendientesInfoFiltrado(Integer idUsuario);

    @Query(
            "SELECT m.id AS id, " +
                    "u.id AS idUsuario, u.nombre AS nombreUsuario, " +
                    "p.id AS idPrestamo, l.titulo AS tituloLibro, " +
                    "m.diasRetraso AS diasRetraso, m.importe AS importe, " +
                    "m.estado AS estado, " +
                    "m.fechaCreacion AS fechaCreacion, m.fechaCierre AS fechaCierre " +
                    "FROM multa m " +
                    "JOIN usuario u ON u.id = m.idUsuario " +
                    "JOIN prestamo p ON p.id = m.idPrestamo " +
                    "JOIN ejemplar e ON e.id = p.idEjemplar " +
                    "JOIN libro l ON l.id = e.idLibro " +
                    "WHERE m.estado = 'PAGADA' " +
                    "AND (:idUsuario IS NULL OR m.idUsuario = :idUsuario) " +
                    "ORDER BY m.fechaCreacion DESC"
    )
    List<MultaInfo> getPagadasInfoFiltrado(Integer idUsuario);

    @Query(
            "SELECT m.id AS id, " +
                    "u.id AS idUsuario, u.nombre AS nombreUsuario, " +
                    "p.id AS idPrestamo, l.titulo AS tituloLibro, " +
                    "m.diasRetraso AS diasRetraso, m.importe AS importe, " +
                    "m.estado AS estado, " +
                    "m.fechaCreacion AS fechaCreacion, m.fechaCierre AS fechaCierre " +
                    "FROM multa m " +
                    "JOIN usuario u ON u.id = m.idUsuario " +
                    "JOIN prestamo p ON p.id = m.idPrestamo " +
                    "JOIN ejemplar e ON e.id = p.idEjemplar " +
                    "JOIN libro l ON l.id = e.idLibro " +
                    "WHERE m.estado = 'CONDONADA' " +
                    "AND (:idUsuario IS NULL OR m.idUsuario = :idUsuario) " +
                    "ORDER BY m.fechaCreacion DESC"
    )
    List<MultaInfo> getCondonadasInfoFiltrado(Integer idUsuario);

    @Query("SELECT COUNT(*) FROM multa WHERE idUsuario = :idUsuario")
    int contarMultasTotalesUsuario(int idUsuario);

    @Query("SELECT COALESCE(SUM(importe), 0) FROM multa WHERE idUsuario = :idUsuario AND estado = 'PAGADA'")
    double sumarMultasPagadasUsuario(int idUsuario);

    @Query("SELECT COALESCE(SUM(importe), 0) FROM multa WHERE idUsuario = :idUsuario AND estado = 'PENDIENTE'")
    double sumarMultasPendientesUsuario(int idUsuario);

    @Query("SELECT COALESCE(SUM(importe),0) FROM multa WHERE estado = 'PENDIENTE'")
    double getTotalMultasPendientes();

    @Query("SELECT COUNT(*) FROM Multa WHERE idUsuario = :idUsuario AND estado = 'PENDIENTE'")
    int countPendientesPorUsuario(long idUsuario);



}
