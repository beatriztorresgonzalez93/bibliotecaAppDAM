package com.DAM.bibliotecaapp.ui.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.RoleGuard;
import com.DAM.bibliotecaapp.SessionManager;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.entities.Usuario;
import com.DAM.bibliotecaapp.ui.base.BaseActivity;
import com.DAM.bibliotecaapp.ui.login.LoginActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsuarioActivity extends BaseActivity {

    private UsuarioAdapter adapter;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Debounce búsqueda
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleGuard.requireLogin(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        SessionManager s = new SessionManager(this);

        // ✅ Si es lector, va directo a su perfil
        if (s.isLector()) {
            long miId = s.getUsuarioId();

            if (miId == -1L) {
                Toast.makeText(this, "Sesión inválida. Vuelve a iniciar sesión.", Toast.LENGTH_SHORT).show();
                s.logout();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }

            Intent i = new Intent(this, UsuarioDetalleActivity.class);
            i.putExtra("usuario_id", miId);
            startActivity(i);
            finish();
            return;
        }

        setContentView(R.layout.activity_usuario);
        applySystemBarsPadding(R.id.main);

        db = AppDatabase.getInstance(this);

        TextInputEditText search = findViewById(R.id.searchUsuarios);
        RecyclerView rv = findViewById(R.id.rvUsuarios);
        FloatingActionButton fab = findViewById(R.id.fabAddUsuario);

        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UsuarioAdapter(u -> {
            Intent i = new Intent(UsuarioActivity.this, UsuarioDetalleActivity.class);
            i.putExtra("usuarioId", u.id);
            startActivity(i);
        });
        rv.setAdapter(adapter);

        // Carga inicial
        cargarUsuarios();

        // ✅ Búsqueda con debounce (300ms)
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String q = (s == null) ? "" : s.toString();

                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);

                searchRunnable = () -> buscarUsuarios(q);
                handler.postDelayed(searchRunnable, 300);
            }
        });

        // ✅ Abrir pantalla añadir usuario
        fab.setOnClickListener(v ->
                startActivity(new Intent(UsuarioActivity.this, AddUsuarioActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarUsuarios(); // al volver de AddUsuarioActivity, refresca lista
    }

    private void cargarUsuarios() {
        executor.execute(() -> {
            List<Usuario> lista = db.usuarioDao().getAllOrderByNombre();
            runOnUiThread(() -> adapter.setData(lista));
        });
    }

    private void buscarUsuarios(String texto) {
        executor.execute(() -> {
            List<Usuario> lista;
            if (texto == null || texto.trim().isEmpty()) {
                lista = db.usuarioDao().getAllOrderByNombre();
            } else {
                String q = "%" + texto.trim() + "%";
                lista = db.usuarioDao().search(q);
            }
            runOnUiThread(() -> adapter.setData(lista));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        executor.shutdown();
    }
}