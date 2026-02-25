package com.DAM.bibliotecaapp.ui.libro;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.RoleGuard;
import com.DAM.bibliotecaapp.SessionManager;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.entities.Libro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LibrosActivity extends AppCompatActivity {

    private RecyclerView rvLibros;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private androidx.appcompat.widget.SearchView svLibros;

    private String queryActual = "";
    private String ordenActual = "titulo";

    private LibroAdapter adapter;

    private boolean isAdmin; // <-- CONTROL DE ROL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RoleGuard.requireLogin(this);
        setContentView(R.layout.activity_libros);

        // Rol
        SessionManager session = new SessionManager(this);
        isAdmin = session.isBibliotecario();

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

        // 🔥 Adapter con rol: admin = puede prestar/borrar, lector = oculto/bloqueado
        adapter = new LibroAdapter(db, new ArrayList<>(), isAdmin);
        rvLibros.setAdapter(adapter);

        // Borrar solo admin (aunque el adapter ya lo oculta)
        adapter.setOnLibroDeleteListener(libro -> {
            if (!isAdmin) {
                Toast.makeText(this, "Acceso solo para bibliotecario", Toast.LENGTH_SHORT).show();
                return;
            }
            borrarLibro(libro);
        });

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

            // ✅ Si es lector: solo libros con ejemplares disponibles
            if (!isAdmin) {
                List<Libro> filtrados = new ArrayList<>();
                for (Libro l : libros) {
                    int disp = db.ejemplarDao().countDisponibles(l.id);
                    if (disp > 0) filtrados.add(l);
                }
                libros = filtrados;
            }

            ordenarEnMemoria(libros);

            List<Libro> finalLibros = libros;
            runOnUiThread(() -> adapter.setData(finalLibros));
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
        cargarLibros();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_libros, menu);

        // ✅ Ocultar opción "Añadir libro" si es lector
        MenuItem add = menu.findItem(R.id.menu_add_libro);
        if (add != null) add.setVisible(isAdmin);

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

        if (id == R.id.menu_add_libro) {
            if (!isAdmin) {
                Toast.makeText(this, "Acceso solo para bibliotecario", Toast.LENGTH_SHORT).show();
                return true;
            }
            startActivity(new Intent(this, NuevoLibroActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void borrarLibro(Libro libro) {
        executor.execute(() -> {

            int prestados = db.ejemplarDao().countPrestadosByLibro(libro.id);

            if (prestados > 0) {
                runOnUiThread(() ->
                        Toast.makeText(this,
                                "No se puede borrar: hay ejemplares prestados",
                                Toast.LENGTH_LONG).show()
                );
                return;
            }

            db.runInTransaction(() -> {
                db.ejemplarDao().deleteByLibro(libro.id);
                db.libroDao().deleteById(libro.id);
            });

            runOnUiThread(() -> {
                Toast.makeText(this, "Libro borrado", Toast.LENGTH_SHORT).show();
                cargarLibros();
            });
        });
    }
}