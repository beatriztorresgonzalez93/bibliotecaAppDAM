package com.DAM.bibliotecaapp.ui.libro;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.RoleGuard;
import com.DAM.bibliotecaapp.SessionManager;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.entities.Libro;
import com.DAM.bibliotecaapp.ui.base.BaseActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LibrosActivity extends BaseActivity {

    private RecyclerView rvLibros;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private String queryActual = "";
    private String ordenActual = "titulo";

    private LibroAdapter adapter;
    private boolean isAdmin; // CONTROL DE ROL

    private TextView tvEmpty; // ✅ ahora es campo

    private android.os.Handler searchHandler = new android.os.Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RoleGuard.requireLogin(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_libros);
        applySystemBarsPadding(R.id.main);

        // Rol
        SessionManager session = new SessionManager(this);
        isAdmin = session.isBibliotecario();

        MaterialToolbar toolbar = findViewById(R.id.toolbarLibros);
        setSupportActionBar(toolbar);

        // ✅ Buscar con el EditText del layout (NO SearchView)
        TextInputEditText etBuscar = findViewById(R.id.etBuscar);

        if (etBuscar != null) {
            etBuscar.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    queryActual = (s == null) ? "" : s.toString().trim();

                    // cancelar búsqueda anterior si existe
                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }

                    // esperar 300ms antes de buscar
                    searchRunnable = () -> cargarLibros();

                    searchHandler.postDelayed(searchRunnable, 300);
                }
            });
        }

        rvLibros = findViewById(R.id.rvLibros);
        rvLibros.setLayoutManager(new LinearLayoutManager(this));

        tvEmpty = findViewById(R.id.tvEmpty); // ✅ solo se inicializa aquí

        db = AppDatabase.getInstance(this);

        // Adapter con rol: admin = puede prestar/borrar, lector = oculto/bloqueado
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

        FloatingActionButton fab = findViewById(R.id.fabAddLibro);

        if (isAdmin) {
            fab.setVisibility(View.VISIBLE);

            fab.setOnClickListener(v -> {
                startActivity(new Intent(this, NuevoLibroActivity.class));
            });

        } else {
            fab.setVisibility(View.GONE);
        }

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

            runOnUiThread(() -> {
                adapter.setData(finalLibros);
                if (tvEmpty != null) {
                    tvEmpty.setVisibility(finalLibros.isEmpty() ? View.VISIBLE : View.GONE);
                }
            });
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