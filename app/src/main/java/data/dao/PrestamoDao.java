package data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import data.entities.Prestamo;
import ui.prestamo.PrestamoGlobal;
import ui.prestamo.PrestamoInfo;

@Dao
public interface PrestamoDao {

    @Insert
    void insert(Prestamo prestamo);

    @Query("SELECT * FROM prestamo WHERE idUsuario = :idUsuario ORDER BY fechaPrestamo DESC")
    List<Prestamo> getPrestamosUsuario(int idUsuario);

    @Query("SELECT COUNT(*) FROM prestamo WHERE idEjemplar = :idEjemplar AND estado = 'ACTIVO'")
    int ejemplarPrestadoActivo(int idEjemplar);
    @Query("SELECT COUNT(*) FROM prestamo WHERE idUsuario = :idUsuario AND fechaDevolucion IS NULL")
    int countActivosByUsuario(int idUsuario);

    @Query("SELECT * FROM prestamo WHERE idUsuario = :idUsuario AND fechaDevolucion IS NULL ORDER BY fechaPrestamo DESC")
    List<Prestamo> getActivosByUsuario(int idUsuario);

    @Query(
            "SELECT " +
                    "  p.id AS idPrestamo, " +
                    "  p.idEjemplar AS idEjemplar, " +
                    "  e.codigoInventario AS codigoInventario, " +
                    "  l.id AS idLibro, " +
                    "  l.titulo AS titulo, " +
                    "  l.autor AS autor, " +
                    "  p.fechaPrestamo AS fechaPrestamo, " +
                    "  p.fechaVencimiento AS fechaVencimiento, " +
                    "  p.estado AS estado " +
                    "FROM prestamo p " +
                    "JOIN ejemplar e ON e.id = p.idEjemplar " +
                    "JOIN Libro l ON l.id = e.idLibro " +
                    "WHERE p.idUsuario = :idUsuario AND p.fechaDevolucion IS NULL " +
                    "ORDER BY p.fechaPrestamo DESC"
    )
    List<PrestamoInfo> getActivosInfoByUsuario(int idUsuario);

    @Query(
            "SELECT " +
                    " p.id AS idPrestamo, " +
                    " l.titulo AS titulo, " +
                    " l.autor AS autor, " +
                    " u.nombre AS nombreUsuario, " +
                    " u.email AS emailUsuario, " +
                    " e.codigoInventario AS codigoInventario, " +
                    " p.fechaPrestamo AS fechaPrestamo, " +
                    " p.fechaVencimiento AS fechaVencimiento, " +
                    " p.estado AS estado " +
                    "FROM prestamo p " +
                    "JOIN ejemplar e ON e.id = p.idEjemplar " +
                    "JOIN libro l ON l.id = e.idLibro " +
                    "JOIN Usuario u ON u.id = p.idUsuario " +
                    "WHERE p.fechaDevolucion IS NULL " +
                    "ORDER BY p.fechaVencimiento ASC"
    )
    List<PrestamoGlobal> getPrestamosActivosGlobal();





}
