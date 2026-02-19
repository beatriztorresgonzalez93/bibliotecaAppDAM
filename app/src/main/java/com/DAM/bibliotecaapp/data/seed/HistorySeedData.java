package com.DAM.bibliotecaapp.data.seed;

import android.content.Context;

import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.entities.Multa;
import com.DAM.bibliotecaapp.data.entities.Prestamo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HistorySeedData {

    public static void seedHistoryIfEmpty(Context context) {
        try {
            AppDatabase db = AppDatabase.getInstance(context);

            // Si ya hay préstamos, no hacemos nada
            if (db.prestamoDao().count() > 0) return;

            // Leer historySeed.json
            InputStream is = context.getAssets().open("historySeed.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONObject root = new JSONObject(json);
            JSONArray prestamosJson = root.getJSONArray("prestamos");

            List<Prestamo> prestamos = new ArrayList<>(prestamosJson.length());

            for (int i = 0; i < prestamosJson.length(); i++) {
                JSONObject p = prestamosJson.getJSONObject(i);

                Prestamo prestamo = new Prestamo();
                prestamo.idUsuario = p.getInt("idUsuario");
                prestamo.idEjemplar = p.getInt("idEjemplar");
                prestamo.fechaPrestamo = p.getLong("fechaPrestamo");
                prestamo.fechaVencimiento = p.getLong("fechaVencimiento");

                // fechaDevolucion puede ser null
                if (p.isNull("fechaDevolucion")) {
                    prestamo.fechaDevolucion = null;
                } else {
                    prestamo.fechaDevolucion = p.getLong("fechaDevolucion");
                }

                prestamo.estado = p.getString("estado"); // ACTIVO / DEVUELTO / VENCIDO

                prestamos.add(prestamo);
            }


            db.prestamoDao().insertAll(prestamos);

            // ===============================
// GENERAR MULTAS DEMO AUTOMÁTICAS
// ===============================
            if (db.multaDao().count() == 0) {

                final double eurosPorDia = 0.50;

                List<Multa> multas = new ArrayList<>();

                // 1) MULTAS PAGADAS → DEVUELTOS fuera de plazo
                List<Prestamo> devueltosTarde = db.prestamoDao()
                        .getDevueltosFueraDePlazo();

                for (Prestamo p : devueltosTarde) {

                    long retrasoMs = p.fechaDevolucion - p.fechaVencimiento;

                    int diasRetraso = (int) Math.max(
                            1,
                            java.util.concurrent.TimeUnit.MILLISECONDS.toDays(retrasoMs)
                    );

                    Multa m = new Multa();

                    m.idPrestamo = p.id;
                    m.idUsuario = p.idUsuario;

                    m.fechaCreacion = p.fechaVencimiento;

                    // Como el libro se devolvió, la multa está PAGADA
                    m.fechaCierre = p.fechaDevolucion;

                    m.diasRetraso = diasRetraso;

                    m.importe = diasRetraso * eurosPorDia;

                    m.estado = "PAGADA";

                    multas.add(m);
                }


                // 2) MULTAS PENDIENTES → VENCIDOS sin devolver
                List<Prestamo> vencidos = db.prestamoDao()
                        .getVencidosSinDevolver();

                long ahora = System.currentTimeMillis();

                for (Prestamo p : vencidos) {

                    long retrasoMs = ahora - p.fechaVencimiento;

                    int diasRetraso = (int) Math.max(
                            1,
                            java.util.concurrent.TimeUnit.MILLISECONDS.toDays(retrasoMs)
                    );

                    Multa m = new Multa();

                    m.idPrestamo = p.id;
                    m.idUsuario = p.idUsuario;

                    m.fechaCreacion = p.fechaVencimiento;

                    m.fechaCierre = null;

                    m.diasRetraso = diasRetraso;

                    m.importe = diasRetraso * eurosPorDia;

                    m.estado = "PENDIENTE";

                    multas.add(m);
                }

                // ===============================
// CREAR 10 MULTAS CONDONADAS DEMO
// ===============================

                int objetivo = 10;
                int hechas = 0;

                for (Multa m : multas) {

                    if (hechas >= objetivo) break;

                    if ("PENDIENTE".equals(m.estado)) {
                        m.estado = "CONDONADA";
                        m.fechaCierre = ahora;   // usamos la variable ya existente
                        hechas++;
                    }
                }


                db.multaDao().insertAll(multas);
            }


            // 🔄 Sincronizar estado de ejemplares según préstamos activos/vencidos
            List<Integer> prestados = db.prestamoDao().getEjemplaresPrestados();

// 1) resetea todos a DISPONIBLE (menos BAJA)
            db.ejemplarDao().marcarTodosDisponiblesExceptoBaja();

// 2) marca como PRESTADO los que tienen préstamo ACTIVO/VENCIDO
            if (prestados != null && !prestados.isEmpty()) {
                db.ejemplarDao().marcarPrestados(prestados);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
