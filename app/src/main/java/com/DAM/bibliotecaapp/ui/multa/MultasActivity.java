package com.DAM.bibliotecaapp.ui.multa;

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
import com.DAM.bibliotecaapp.data.pojo.MultaGlobal;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultasActivity extends AppCompatActivity {

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private MultaGlobalAdapter adapter;

    private Spinner spFiltro;
    private String filtroActual = "Todas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multas);

        db = AppDatabase.getInstance(this);

        RecyclerView rv = findViewById(R.id.rvMultas);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MultaGlobalAdapter(
                multa -> mostrarDialogoPagar(multa.idMulta),
                multa -> mostrarDialogoCondonar(multa.idMulta)
        );
        rv.setAdapter(adapter);

        spFiltro = findViewById(R.id.spFiltroMultas);
        ArrayAdapter<String> filtroAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Todas", "Pendientes"}
        );
        filtroAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFiltro.setAdapter(filtroAdapter);

        spFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                filtroActual = parent.getItemAtPosition(position).toString();
                cargarMultas();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        cargarMultas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarMultas();
    }

    private void cargarMultas() {
        executor.execute(() -> {
            List<MultaGlobal> lista;
            if ("Pendientes".equals(filtroActual)) {
                lista = db.multaDao().getPendientesGlobal();
            } else {
                lista = db.multaDao().getAllGlobal();
            }

            runOnUiThread(() -> adapter.setData(lista));
        });
    }

    private void mostrarDialogoPagar(int idMulta) {
        new AlertDialog.Builder(this)
                .setTitle("Pagar multa")
                .setMessage("¿Marcar esta multa como PAGADA?")
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .setPositiveButton("Pagar", (d, w) -> pagarMulta(idMulta))
                .show();
    }

    private void pagarMulta(int idMulta) {
        executor.execute(() -> {
            int ok = db.multaDao().pagar(idMulta, System.currentTimeMillis());
            runOnUiThread(() -> {
                Toast.makeText(this, ok > 0 ? "Multa pagada" : "No se pudo pagar", Toast.LENGTH_SHORT).show();
                cargarMultas();
            });
        });
    }

    private void mostrarDialogoCondonar(int idMulta) {
        new AlertDialog.Builder(this)
                .setTitle("Condonar multa")
                .setMessage("¿Marcar esta multa como CONDONADA?")
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .setPositiveButton("Condonar", (d, w) -> condonarMulta(idMulta))
                .show();
    }

    private void condonarMulta(int idMulta) {
        executor.execute(() -> {
            int ok = db.multaDao().condonar(idMulta, System.currentTimeMillis());
            runOnUiThread(() -> {
                Toast.makeText(this, ok > 0 ? "Multa condonada" : "No se pudo condonar", Toast.LENGTH_SHORT).show();
                cargarMultas();
            });
        });
    }
}
