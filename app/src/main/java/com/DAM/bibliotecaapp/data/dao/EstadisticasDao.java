package com.DAM.bibliotecaapp.data.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.DAM.bibliotecaapp.data.pojo.StatsResumen;
import com.DAM.bibliotecaapp.data.pojo.TopLibro;
import com.DAM.bibliotecaapp.data.pojo.TopUsuario;

import java.util.List;

@Dao
public interface EstadisticasDao {

    // Resumen global (una sola fila)
    @Query(
            "SELECT " +
                    " (SELECT COUNT(*) FROM usuario) AS totalUsuarios, " +
                    " (SELECT COUNT(*) FROM libro) AS totalLibros, " +
                    " (SELECT COUNT(*) FROM ejemplar) AS totalEjemplares, " +

                    " (SELECT COUNT(*) FROM prestamo WHERE estado = 'ACTIVO') AS prestamosActivos, " +
                    " (SELECT COUNT(*) FROM prestamo WHERE estado = 'VENCIDO') AS prestamosVencidos, " +
                    " (SELECT COUNT(*) FROM prestamo WHERE estado = 'DEVUELTO') AS prestamosDevueltos, " +

                    " (SELECT COUNT(*) FROM multa WHERE estado = 'PENDIENTE') AS multasPendientes, " +
                    " (SELECT COUNT(*) FROM multa WHERE estado = 'PAGADA') AS multasPagadas, " +
                    " (SELECT COUNT(*) FROM multa WHERE estado = 'CONDONADA') AS multasCondonadas, " +

                    " (SELECT COALESCE(SUM(importe), 0) FROM multa WHERE estado = 'PAGADA') AS dineroRecaudado"
    )
    StatsResumen getResumen();

    // Libros más prestados (TOP N)
    @Query(
            "SELECT l.id AS idLibro, l.titulo AS titulo, l.autor AS autor, COUNT(p.id) AS totalPrestamos " +
                    "FROM prestamo p " +
                    "JOIN ejemplar e ON e.id = p.idEjemplar " +
                    "JOIN libro l ON l.id = e.idLibro " +
                    "GROUP BY l.id " +
                    "ORDER BY totalPrestamos DESC " +
                    "LIMIT :limit"
    )
    List<TopLibro> getTopLibros(int limit);

    // Usuario más activo (TOP N)
    @Query(
            "SELECT u.id AS idUsuario, u.nombre AS nombre, u.email AS email, COUNT(p.id) AS totalPrestamos " +
                    "FROM prestamo p " +
                    "JOIN usuario u ON u.id = p.idUsuario " +
                    "GROUP BY u.id " +
                    "ORDER BY totalPrestamos DESC " +
                    "LIMIT :limit"
    )
    List<TopUsuario> getTopUsuarios(int limit);
}