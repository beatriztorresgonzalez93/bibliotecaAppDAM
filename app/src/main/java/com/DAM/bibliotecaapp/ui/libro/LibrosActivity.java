package com.DAM.bibliotecaapp.ui.libro;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.R;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.DAM.bibliotecaapp.data.entities.Libro;

public class LibrosActivity extends AppCompatActivity {

    private RecyclerView rvLibros;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libros);

        rvLibros = findViewById(R.id.rvLibros);
        rvLibros.setLayoutManager(new LinearLayoutManager(this));

        db = AppDatabase.getInstance(this);

        // Primera carga
        cargarLibros();
    }

    private void cargarLibros() {
        executor.execute(() -> {
            List<Libro> libros = db.libroDao().getAll();

            runOnUiThread(() -> {
                LibroAdapter adapter = new LibroAdapter(db, libros);
                rvLibros.setAdapter(adapter);
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Al volver de NuevoPrestamoActivity, refresca disponibles
        cargarLibros();
    }
}
