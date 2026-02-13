package com.DAM.bibliotecaapp.ui.usuario;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.widget.SearchView;

import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.DAM.bibliotecaapp.data.entities.Usuario;

public class UsuarioActivity extends AppCompatActivity {

    private UsuarioAdapter adapter;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);

        db = AppDatabase.getInstance(this);

        SearchView search = findViewById(R.id.searchUsuarios);
        RecyclerView rv = findViewById(R.id.rvUsuarios);
        FloatingActionButton fab = findViewById(R.id.fabAddUsuario);

        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UsuarioAdapter(u -> {
            Intent intent = new Intent(UsuarioActivity.this, UsuarioDetalleActivity.class);
            intent.putExtra("usuario_id", u.id);
            startActivity(intent);
        });


        rv.setAdapter(adapter);

        cargarUsuarios();

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return true; }

            @Override
            public boolean onQueryTextChange(String newText) {
                buscarUsuarios(newText);
                return true;
            }
        });

        fab.setOnClickListener(v -> {
            // Próximo paso: crear usuario
        });
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
}
