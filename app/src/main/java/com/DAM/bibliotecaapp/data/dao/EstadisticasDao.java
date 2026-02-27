package com.DAM.bibliotecaapp.data.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.DAM.bibliotecaapp.data.pojo.StatsResumen;
import com.DAM.bibliotecaapp.data.pojo.TopLibro;
import com.DAM.bibliotecaapp.data.pojo.TopUsuario;

import com.DAM.bibliotecaapp.data.pojo.MesConteo;
import com.DAM.bibliotecaapp.data.pojo.MesImporte;
import com.DAM.bibliotecaapp.data.pojo.EstadoConteo;

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

    @Query(
            "SELECT strftime('%Y-%m', datetime(fechaPrestamo/1000, 'unixepoch')) AS mes, COUNT(*) AS total " +
                    "FROM prestamo " +
                    "GROUP BY mes " +
                    "ORDER BY mes DESC " +
                    "LIMIT 12"
    )
    List<MesConteo> getPrestamosUltimos12Meses();

    @Query(
            "SELECT strftime('%Y-%m', datetime(fechaCreacion/1000, 'unixepoch')) AS mes, COALESCE(SUM(importe),0) AS importe " +
                    "FROM multa " +
                    "GROUP BY mes " +
                    "ORDER BY mes DESC " +
                    "LIMIT 12"
    )
    List<MesImporte> getImporteMultasUltimos12Meses();



    // Multas por estado (conteo)
    @Query(
            "SELECT estado AS estado, COUNT(*) AS total " +
                    "FROM multa " +
                    "GROUP BY estado " +
                    "ORDER BY total DESC"
    )
    List<EstadoConteo> getMultasPorEstado();

    @Query(
            "SELECT strftime('%Y-%m', datetime(fechaPrestamo/1000, 'unixepoch')) AS mes, COUNT(*) AS total " +
                    "FROM prestamo " +
                    "WHERE fechaPrestamo >= :desde " +
                    "GROUP BY mes " +
                    "ORDER BY mes ASC"
    )
    List<MesConteo> getPrestamosPorMesDesde(long desde);

    @Query(
            "SELECT strftime('%Y-%m', datetime(fechaCreacion/1000, 'unixepoch')) AS mes, COALESCE(SUM(importe),0) AS importe " +
                    "FROM multa " +
                    "WHERE fechaCreacion >= :desde " +
                    "GROUP BY mes " +
                    "ORDER BY mes ASC"
    )
    List<MesImporte> getImporteMultasPorMesDesde(long desde);

    // 1) Dinero pendiente
    @Query("SELECT COALESCE(SUM(importe),0) FROM multa WHERE estado='PENDIENTE'")
    double getDineroPendiente();

    // 1) % devueltos a tiempo (devuelto sin multa)
    @Query("SELECT COUNT(*) FROM prestamo WHERE estado='DEVUELTO'")
    int getTotalDevueltos();

    @Query(
            "SELECT COUNT(*) " +
                    "FROM prestamo p " +
                    "LEFT JOIN multa m ON m.idPrestamo = p.id " +
                    "WHERE p.estado='DEVUELTO' AND m.id IS NULL"
    )
    int getDevueltosSinMulta();

    // 2) Disponibles vs prestados ahora
    @Query(
            "SELECT COUNT(*) FROM ejemplar e " +
                    "WHERE NOT EXISTS (" +
                    "  SELECT 1 FROM prestamo p " +
                    "  WHERE p.idEjemplar = e.id AND (p.estado='ACTIVO' OR p.estado='VENCIDO')" +
                    ")"
    )
    int getEjemplaresDisponibles();

    @Query("SELECT COUNT(*) FROM prestamo WHERE estado='ACTIVO' OR estado='VENCIDO'")
    int getEjemplaresPrestadosAhora();

    // 3) Top libros con más multas (usa TopLibro reutilizando totalPrestamos como contador)
    @Query(
            "SELECT l.id AS idLibro, l.titulo AS titulo, l.autor AS autor, COUNT(m.id) AS totalPrestamos " +
                    "FROM multa m " +
                    "JOIN prestamo p ON p.id = m.idPrestamo " +
                    "JOIN ejemplar e ON e.id = p.idEjemplar " +
                    "JOIN libro l ON l.id = e.idLibro " +
                    "GROUP BY l.id " +
                    "ORDER BY totalPrestamos DESC " +
                    "LIMIT :limit"
    )
    List<TopLibro> getTopLibrosConMultas(int limit);

    @Query(
            "SELECT " +
                    "  COALESCE(NULLIF(TRIM(l.genero), ''), 'Sin género') AS genero, " +
                    "  COUNT(p.id) AS total " +
                    "FROM prestamo p " +
                    "JOIN ejemplar e ON e.id = p.idEjemplar " +
                    "JOIN libro l ON l.id = e.idLibro " +
                    "GROUP BY genero " +
                    "ORDER BY total DESC"
    )
    List<com.DAM.bibliotecaapp.data.pojo.GeneroConteo> getPrestamosPorGenero();
}