package com.DAM.bibliotecaapp.ui.libro;

import android.os.Bundle;
import android.view.Menu;

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
    private androidx.appcompat.widget.SearchView svLibros;
    private String queryActual = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libros);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarLibros);
        setSupportActionBar(toolbar);


        svLibros = findViewById(R.id.svLibros);

        svLibros.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                queryActual = query == null ? "" : query.trim();
                cargarLibros();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                queryActual = newText == null ? "" : newText.trim();
                cargarLibros();
                return true;
            }
        });


        rvLibros = findViewById(R.id.rvLibros);
        rvLibros.setLayoutManager(new LinearLayoutManager(this));

        db = AppDatabase.getInstance(this);

        // Primera carga
        cargarLibros();
    }

    private void cargarLibros() {
        executor.execute(() -> {
            List<Libro> libros;

            if (queryActual.isEmpty()) {
                libros = db.libroDao().getAll();
            } else {
                String q = "%" + queryActual + "%";
                libros = db.libroDao().search(q);
            }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_libros, menu);
        return true;
    }

}
