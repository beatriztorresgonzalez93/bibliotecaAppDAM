package com.DAM.bibliotecaapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsuarioDetalleActivity extends AppCompatActivity {

    private AppDatabase db;

    private TextView tvSinPrestamos;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private TextView tvNombre, tvEmail, tvRol, tvPrestamosActivos;
    private PrestamoInfoAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_detalle);

        db = AppDatabase.getInstance(this);

        tvNombre = findViewById(R.id.tvNombre);
        tvEmail = findViewById(R.id.tvEmail);
        tvRol = findViewById(R.id.tvRol);
        tvPrestamosActivos = findViewById(R.id.tvPrestamosActivos);
        tvSinPrestamos = findViewById(R.id.tvSinPrestamos);


        RecyclerView rv = findViewById(R.id.rvPrestamosActivos);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PrestamoInfoAdapter();

        rv.setAdapter(adapter);

        int usuarioId = getIntent().getIntExtra("usuario_id", -1);

        if (usuarioId == -1) {
            tvNombre.setText("Error: usuario no recibido");
            return;
        }

        cargarDatos(usuarioId);
    }

    private void cargarDatos(int usuarioId) {

        executor.execute(() -> {

            Usuario usuario = db.usuarioDao().getById(usuarioId);

            int prestamosActivos =
                    db.prestamoDao().countActivosByUsuario(usuarioId);

            List<PrestamoInfo> listaActivos = db.prestamoDao().getActivosInfoByUsuario(usuarioId);
            adapter.setData(listaActivos);


            runOnUiThread(() -> {

                if (usuario == null) {
                    tvNombre.setText("Usuario no encontrado");
                    return;
                }

                tvNombre.setText(usuario.nombre);
                tvEmail.setText(usuario.email);
                tvRol.setText("Rol: " + usuario.rol);

                tvPrestamosActivos.setText(
                        "Préstamos activos: " + prestamosActivos
                );

                adapter.setData(listaActivos);

                if (listaActivos == null || listaActivos.isEmpty()) {
                    tvSinPrestamos.setVisibility(View.VISIBLE);
                } else {
                    tvSinPrestamos.setVisibility(View.GONE);
                }

            });
        });
    }
}
