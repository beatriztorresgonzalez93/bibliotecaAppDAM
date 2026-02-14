package com.DAM.bibliotecaapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import com.DAM.bibliotecaapp.data.entities.Prestamo;
import com.DAM.bibliotecaapp.data.pojo.PrestamoGlobal;
import com.DAM.bibliotecaapp.data.pojo.PrestamoInfo;

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

    @Query("SELECT idEjemplar FROM prestamo WHERE id = :idPrestamo LIMIT 1")
    int getIdEjemplarByPrestamo(int idPrestamo);

    @Query("UPDATE prestamo SET fechaDevolucion = :fechaDev, estado = 'DEVUELTO' WHERE id = :idPrestamo")
    int marcarDevuelto(int idPrestamo, long fechaDev);

    // --------- DETALLE USUARIO: activos con info del libro/ejemplar ---------

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
                    "JOIN libro l ON l.id = e.idLibro " +
                    "WHERE p.idUsuario = :idUsuario AND p.fechaDevolucion IS NULL " +
                    "ORDER BY p.fechaPrestamo DESC"
    )
    List<PrestamoInfo> getActivosInfoByUsuario(int idUsuario);

    // --------- VENCIDOS AUTOMÁTICOS ---------

    @Query("UPDATE prestamo SET estado = 'VENCIDO' " +
            "WHERE fechaVencimiento < :ahora " +
            "AND fechaDevolucion IS NULL " +
            "AND estado = 'ACTIVO'")
    int marcarVencidos(long ahora);

    // --------- LISTA GLOBAL (para filtros) ---------

    // TODOS (no devueltos): ACTIVO + VENCIDO
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
    List<PrestamoGlobal> getPrestamosNoDevueltosGlobal();

    // SOLO VENCIDOS
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
                    "WHERE p.fechaDevolucion IS NULL AND p.estado = 'VENCIDO' " +
                    "ORDER BY p.fechaVencimiento ASC"
    )
    List<PrestamoGlobal> getPrestamosVencidosGlobal();

    // SOLO ACTIVOS
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
                    "WHERE p.fechaDevolucion IS NULL AND p.estado = 'ACTIVO' " +
                    "ORDER BY p.fechaVencimiento ASC"
    )
    List<PrestamoGlobal> getPrestamosSoloActivosGlobal();

    @Query("UPDATE prestamo " +
            "SET fechaVencimiento = fechaVencimiento + :msExtra " +
            "WHERE id = :idPrestamo " +
            "AND fechaDevolucion IS NULL " +
            "AND estado = 'ACTIVO'")
    int ampliarPlazo(int idPrestamo, long msExtra);

    @Query(
            "SELECT " +
                    "  p.id AS idPrestamo, " +
                    "  p.idEjemplar AS idEjemplar, " +          // 👈 AÑADIDO
                    "  l.id AS idLibro, " +                      // 👈 AÑADIDO
                    "  l.titulo AS titulo, " +
                    "  l.autor AS autor, " +
                    "  e.codigoInventario AS codigoInventario, " +
                    "  p.fechaPrestamo AS fechaPrestamo, " +
                    "  p.fechaVencimiento AS fechaVencimiento, " +
                    "  p.estado AS estado " +
                    "FROM prestamo p " +
                    "JOIN ejemplar e ON e.id = p.idEjemplar " +
                    "JOIN libro l ON l.id = e.idLibro " +
                    "WHERE p.idUsuario = :idUsuario " +
                    "AND p.fechaDevolucion IS NULL " +
                    "ORDER BY p.fechaVencimiento ASC"
    )
    List<PrestamoInfo> getNoDevueltosByUsuario(int idUsuario);


}
