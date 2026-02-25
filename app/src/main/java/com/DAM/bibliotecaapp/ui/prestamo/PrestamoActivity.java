package com.DAM.bibliotecaapp.ui.prestamo;

import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.RoleGuard;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.pojo.PrestamoGlobal;
import com.DAM.bibliotecaapp.ui.base.BaseActivity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrestamoActivity extends BaseActivity {

    private static final double TARIFA_DIA = 0.50; // 50 céntimos por día
    private static final double TOPE = 20.0;       // máximo 20€

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private PrestamoGlobalAdapter adapter;

    private Spinner spFiltro;
    private String filtroActual = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleGuard.requireBibliotecario(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_prestamo);
        applySystemBarsPadding(R.id.main);

        db = AppDatabase.getInstance(this);

        RecyclerView rv = findViewById(R.id.rvPrestamos);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PrestamoGlobalAdapter(
                prestamo -> {
                    // Click en la fila (opcional). Si no quieres que haga nada:
                    // no pongas nada aquí.
                },
                prestamo -> {
                    // Click en ampliar
                    mostrarDialogoAmpliar(prestamo.idPrestamo);
                },
                prestamo -> {
                    // Click en devolver
                    mostrarDialogoDevolucion(prestamo.idPrestamo);
                }
        );
        rv.setAdapter(adapter);


        spFiltro = findViewById(R.id.spFiltroPrestamos);

        ArrayAdapter<String> filtroAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Todos", "Activos", "Vencidos"}
        );
        filtroAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFiltro.setAdapter(filtroAdapter);

        spFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                filtroActual = parent.getItemAtPosition(position).toString();
                cargarPrestamos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        cargarPrestamos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPrestamos();
    }

    private void cargarPrestamos() {
        executor.execute(() -> {
            db.prestamoDao().marcarVencidos(System.currentTimeMillis());

            // 1) marcar vencidos
            db.prestamoDao().marcarVencidos(System.currentTimeMillis());

// 2) crear/actualizar multas para vencidos
            actualizarMultas(System.currentTimeMillis(), TARIFA_DIA, TOPE);


            final double TARIFA_DIA = 0.50;
            final double TOPE = 20.0;


            List<PrestamoGlobal> lista;
            if ("Activos".equals(filtroActual)) {
                lista = db.prestamoDao().getPrestamosSoloActivosGlobal();
            } else if ("Vencidos".equals(filtroActual)) {
                lista = db.prestamoDao().getPrestamosVencidosGlobal();
            } else {
                lista = db.prestamoDao().getPrestamosNoDevueltosGlobal();
            }

            runOnUiThread(() -> adapter.setData(lista));
        });
    }

    private void mostrarDialogoDevolucion(int idPrestamo) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar devolución")
                .setMessage("¿Quieres marcar este préstamo como devuelto?")
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .setPositiveButton("Devolver", (d, w) -> devolverPrestamo(idPrestamo))
                .show();
    }

    private void devolverPrestamo(int idPrestamo) {
        executor.execute(() -> {
            long ahora = System.currentTimeMillis();

            db.runInTransaction(() -> {
                int idEjemplar = db.prestamoDao().getIdEjemplarByPrestamo(idPrestamo);
                db.prestamoDao().marcarDevuelto(idPrestamo, ahora);
                db.ejemplarDao().actualizarEstado(idEjemplar, "DISPONIBLE");
            });

            runOnUiThread(() -> {
                Toast.makeText(this, "Devolución registrada", Toast.LENGTH_SHORT).show();
                cargarPrestamos();
            });
        });
    }

    private void mostrarDialogoAmpliar(int idPrestamo) {
        final String[] opciones = {"7 días", "14 días", "30 días"};
        final int[] dias = {7, 14, 30};

        new AlertDialog.Builder(this)
                .setTitle("Ampliar plazo")
                .setItems(opciones, (dialog, which) -> ampliarPlazo(idPrestamo, dias[which]))
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .show();
    }

    private void ampliarPlazo(int idPrestamo, int diasExtra) {
        executor.execute(() -> {
            long msExtra = diasExtra * 24L * 60 * 60 * 1000;

            int updated = db.prestamoDao().ampliarPlazo(idPrestamo, msExtra);

            runOnUiThread(() -> {
                if (updated > 0) {
                    Toast.makeText(this, "Plazo ampliado +" + diasExtra + " días", Toast.LENGTH_SHORT).show();
                    cargarPrestamos();
                } else {
                    Toast.makeText(this, "No se puede ampliar: préstamo vencido o ya devuelto", Toast.LENGTH_LONG).show();
                }
            });
        });
    }
    private void actualizarMultas(long ahora, double tarifaDia, double tope) {
        List<com.DAM.bibliotecaapp.data.pojo.PrestamoVencidoMini> vencidos = db.prestamoDao().getVencidosMini();

        for (com.DAM.bibliotecaapp.data.pojo.PrestamoVencidoMini p : vencidos) {

            // días completos de retraso
            long diff = ahora - p.fechaVencimiento;
            int dias = (int) Math.max(0, diff / (24L * 60 * 60 * 1000));

            double importe = Math.min(tope, dias * tarifaDia);

            // si no existe multa, crearla
            if (db.multaDao().existePorPrestamo(p.idPrestamo) == 0) {
                com.DAM.bibliotecaapp.data.entities.Multa m = new com.DAM.bibliotecaapp.data.entities.Multa();
                m.idPrestamo = p.idPrestamo;
                m.idUsuario = p.idUsuario;
                m.fechaCreacion = ahora;
                m.fechaCierre = null;
                m.diasRetraso = dias;
                m.importe = importe;
                m.estado = "PENDIENTE";
                db.multaDao().insert(m);
            } else {
                // si ya existe, actualizarla (solo si está pendiente)
                db.multaDao().actualizarPendiente(p.idPrestamo, dias, importe);
            }
        }
    }

}
