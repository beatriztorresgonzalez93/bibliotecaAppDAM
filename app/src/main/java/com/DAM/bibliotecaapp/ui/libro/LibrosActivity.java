package com.DAM.bibliotecaapp.ui.libro;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.DAM.bibliotecaapp.data.seed.HistorySeedData;
import com.DAM.bibliotecaapp.data.seed.SeedData;
import com.DAM.bibliotecaapp.ui.base.BaseActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LibrosActivity extends BaseActivity {

    private RecyclerView rvLibros;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private String queryActual = "";

    // orden: titulo | autor | editorial | genero
    private String ordenActual = "titulo";

    private LibroAdapter adapter;
    private boolean isAdmin;

    private TextView tvEmpty;

    private final android.os.Handler searchHandler = new android.os.Handler();
    private Runnable searchRunnable;


    private static final String PREFS_LIBROS = "prefs_libros";
    private static final String KEY_ORDEN = "orden_libros";

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

        // recuperar orden guardado (opcional)
        ordenActual = getSharedPreferences(PREFS_LIBROS, MODE_PRIVATE)
                .getString(KEY_ORDEN, "titulo");

        // Buscar (debounce)
        TextInputEditText etBuscar = findViewById(R.id.etBuscar);
        if (etBuscar != null) {
            etBuscar.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    queryActual = (s == null) ? "" : s.toString().trim();

                    if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                    searchRunnable = () -> cargarLibros();
                    searchHandler.postDelayed(searchRunnable, 300);
                }
            });
        }

        rvLibros = findViewById(R.id.rvLibros);
        rvLibros.setLayoutManager(new LinearLayoutManager(this));

        tvEmpty = findViewById(R.id.tvEmpty);

        db = AppDatabase.getInstance(this);

        adapter = new LibroAdapter(new ArrayList<>(), isAdmin);
        rvLibros.setAdapter(adapter);

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
            fab.setOnClickListener(v -> startActivity(new Intent(this, NuevoLibroActivity.class)));
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

            // Lector: solo libros con ejemplares disponibles (OJO: esto hace muchas queries)
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
            List<LibroAdapter.LibroItem> uiItems = new ArrayList<>();

            for (Libro l : libros) {
                int total = db.ejemplarDao().countTotal(l.id);
                int disp = db.ejemplarDao().countDisponibles(l.id);
                uiItems.add(new LibroAdapter.LibroItem(l, total, disp));
            }

            List<LibroAdapter.LibroItem> finalItems = uiItems;

            runOnUiThread(() -> {
                adapter.setData(finalItems);
                if (tvEmpty != null) {
                    tvEmpty.setVisibility(finalItems.isEmpty() ? View.VISIBLE : View.GONE);
                }
            });
        });
    }

    private static String safe(String s) {
        return (s == null) ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    private void ordenarEnMemoria(List<Libro> libros) {
        if (libros == null) return;

        Collections.sort(libros, (a, b) -> {
            if (a == null && b == null) return 0;
            if (a == null) return 1;
            if (b == null) return -1;

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

            // ✅ vacíos al final (sobre todo para género/editorial/autor)
            boolean ea = sa.isEmpty();
            boolean eb = sb.isEmpty();
            if (ea && !eb) return 1;
            if (!ea && eb) return -1;

            int c = sa.compareTo(sb);
            if (c != 0) return c;

            // desempate por título (para que el orden sea estable)
            return safe(a.titulo).compareTo(safe(b.titulo));
        });
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
            setOrden("titulo");
            return true;
        } else if (id == R.id.orden_autor) {
            setOrden("autor");
            return true;
        } else if (id == R.id.orden_editorial) {
            setOrden("editorial");
            return true;
        } else if (id == R.id.orden_genero) {
            setOrden("genero");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setOrden(String orden) {
        ordenActual = orden;

        // guardar orden (opcional)
        getSharedPreferences(PREFS_LIBROS, MODE_PRIVATE)
                .edit()
                .putString(KEY_ORDEN, ordenActual)
                .apply();

        cargarLibros();
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

    private void seedOnce() {
        SharedPreferences p = getSharedPreferences("seed_flags", MODE_PRIVATE);
        boolean done = p.getBoolean("seed_done", false);
        if (done) return;

        ExecutorService ex = Executors.newSingleThreadExecutor();
        ex.execute(() -> {
            SeedData.seedIfEmpty(this);
            HistorySeedData.seedHistoryIfEmpty(this);
            p.edit().putBoolean("seed_done", true).apply();
        });
    }
}