package com.DAM.bibliotecaapp.ui.libro;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.R;

import java.util.ArrayList;
import java.util.Collections;
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

    private String ordenActual = "titulo";

    private LibroAdapter adapter;


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

        adapter = new LibroAdapter(db, new ArrayList<>(), -1);
        rvLibros.setAdapter(adapter);

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

            ordenarEnMemoria(libros);

            runOnUiThread(() -> adapter.setData(libros));
        });
    }

    private void ordenarEnMemoria(List<Libro> libros) {
        if (libros == null) return;

        Collections.sort(libros, (a, b) -> {
            String sa, sb;

            switch (ordenActual) {
                case "autor":
                    sa = safe(a.autor);
                    sb = safe(b.autor);
                    break;
                case "editorial":
                    sa = safe(a.editorial);
                    sb = safe(b.editorial);
                    break;
                case "genero":
                    sa = safe(a.genero);
                    sb = safe(b.genero);
                    break;
                default:
                    sa = safe(a.titulo);
                    sb = safe(b.titulo);
            }
            return sa.compareTo(sb);
        });
    }

    private String safe(String s) {
        return s == null ? "" : s.toLowerCase();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.orden_titulo) {
            ordenActual = "titulo";
            cargarLibros();
            return true;
        } else if (id == R.id.orden_autor) {
            ordenActual = "autor";
            cargarLibros();
            return true;
        } else if (id == R.id.orden_editorial) {
            ordenActual = "editorial";
            cargarLibros();
            return true;
        } else if (id == R.id.orden_genero) {
            ordenActual = "genero";
            cargarLibros();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
