package com.DAM.bibliotecaapp.ui.devolucion;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.RoleGuard;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.dto.DevolucionItem;

import java.util.List;
import java.util.concurrent.Executors;

public class DevolucionesActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    com.DAM.bibliotecaapp.ui.devoluciones.DevolucionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleGuard.requireBibliotecario(this);
        setContentView(R.layout.activity_devoluciones);

        recyclerView = findViewById(R.id.recyclerViewDevoluciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cargarDatos();
    }

    private void cargarDatos() {

        Executors.newSingleThreadExecutor().execute(() -> {

            AppDatabase db = AppDatabase.getInstance(this);

            List<DevolucionItem> lista =
                    db.prestamoDao().getDevoluciones();

            runOnUiThread(() -> {
                adapter = new com.DAM.bibliotecaapp.ui.devoluciones.DevolucionAdapter(lista);
                recyclerView.setAdapter(adapter);
            });
        });
    }
}
