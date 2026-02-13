package com.DAM.bibliotecaapp.ui.prestamo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;
import android.widget.Toast;


import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.data.pojo.PrestamoGlobal;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrestamoActivity extends AppCompatActivity {

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private PrestamoGlobalAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prestamo);

        db = AppDatabase.getInstance(this);

        RecyclerView rv = findViewById(R.id.rvPrestamos);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PrestamoGlobalAdapter(prestamo -> {
            mostrarDialogoDevolucion(prestamo.idPrestamo);
        });
        rv.setAdapter(adapter);


        cargarPrestamos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPrestamos();
    }

    private void cargarPrestamos() {
        executor.execute(() -> {
            List<PrestamoGlobal> lista = db.prestamoDao().getPrestamosActivosGlobal();
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
                cargarPrestamos(); // refresca lista global
            });
        });
    }

}
