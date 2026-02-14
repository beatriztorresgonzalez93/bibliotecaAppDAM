package com.DAM.bibliotecaapp.ui.prestamo;

import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.pojo.PrestamoGlobal;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrestamoActivity extends AppCompatActivity {

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private PrestamoGlobalAdapter adapter;

    private Spinner spFiltro;
    private String filtroActual = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prestamo); // <- revisa si tu layout se llama distinto

        db = AppDatabase.getInstance(this);

        // 1) Recycler
        RecyclerView rv = findViewById(R.id.rvPrestamos); // <- revisa si tu id se llama distinto
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PrestamoGlobalAdapter(
                prestamo -> mostrarDialogoDevolucion(prestamo.idPrestamo),
                prestamo -> mostrarDialogoAmpliar(prestamo.idPrestamo)
        );
        rv.setAdapter(adapter);


        // 2) Spinner filtro
        spFiltro = findViewById(R.id.spFiltroPrestamos); // <- asegúrate de añadir este Spinner al XML

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

        // Primera carga
        cargarPrestamos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPrestamos();
    }

    private void cargarPrestamos() {
        executor.execute(() -> {
            // Marca vencidos automáticamente
            db.prestamoDao().marcarVencidos(System.currentTimeMillis());

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

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Ampliar plazo")
                .setItems(opciones, (dialog, which) -> {
                    int d = dias[which];
                    ampliarPlazo(idPrestamo, d);
                })
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

}
