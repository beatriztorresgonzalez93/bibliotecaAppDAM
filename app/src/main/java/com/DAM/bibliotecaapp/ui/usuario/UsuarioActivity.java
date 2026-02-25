package com.DAM.bibliotecaapp.ui.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.RoleGuard;
import com.DAM.bibliotecaapp.SessionManager;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.entities.Usuario;
import com.DAM.bibliotecaapp.ui.login.LoginActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsuarioActivity extends AppCompatActivity {

    private UsuarioAdapter adapter;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleGuard.requireLogin(this);

        SessionManager s = new SessionManager(this);

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
            // opcional: puedes pasar el id, pero ya no hace falta
            i.putExtra("usuario_id", s.getUsuarioId());
            startActivity(i);
            finish();
            return;
        }


        setContentView(R.layout.activity_usuario);


        db = AppDatabase.getInstance(this);

        SearchView search = findViewById(R.id.searchUsuarios);
        RecyclerView rv = findViewById(R.id.rvUsuarios);
        FloatingActionButton fab = findViewById(R.id.fabAddUsuario);

        rv.setLayoutManager(new LinearLayoutManager(this));

        // ✅ Adapter con lista vacía al inicio
        adapter = new UsuarioAdapter(u -> {
            Intent i = new Intent(UsuarioActivity.this, UsuarioDetalleActivity.class);
            i.putExtra("usuarioId", u.id);
            startActivity(i);
        });
        rv.setAdapter(adapter);

        // Carga inicial
        cargarUsuarios();

        // Búsqueda
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return true; }

            @Override
            public boolean onQueryTextChange(String newText) {
                buscarUsuarios(newText);
                return true;
            }
        });

        // ✅ Abrir pantalla de añadir usuario
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
        executor.shutdown();
    }
}