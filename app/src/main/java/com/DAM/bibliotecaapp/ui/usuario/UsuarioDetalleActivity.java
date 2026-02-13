package com.DAM.bibliotecaapp.ui.usuario;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.R;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.DAM.bibliotecaapp.data.entities.Usuario;
import com.DAM.bibliotecaapp.data.pojo.PrestamoInfo;
import com.DAM.bibliotecaapp.ui.prestamo.PrestamoInfoAdapter;
import com.DAM.bibliotecaapp.data.pojo.PrestamoInfo;



import androidx.appcompat.app.AlertDialog;


public class UsuarioDetalleActivity extends AppCompatActivity {

    private AppDatabase db;

    private TextView tvSinPrestamos;

    private int usuarioId;


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

        adapter = new PrestamoInfoAdapter(prestamoInfo -> {
            mostrarDialogoDevolucion(prestamoInfo.idPrestamo);
        });
        rv.setAdapter(adapter);



        usuarioId = getIntent().getIntExtra("usuario_id", -1);


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

    @Override
    protected void onResume() {
        super.onResume();
        if (usuarioId != -1) cargarDatos(usuarioId);
    }
    private void mostrarDialogoDevolucion(int idPrestamo) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirmar devolución")
                .setMessage("¿Quieres marcar este préstamo como devuelto?")
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .setPositiveButton("Devolver", (d, w) -> devolverPrestamo(idPrestamo))
                .show();
    }
    private void devolverPrestamo(int idPrestamo) {
        executor.execute(() -> {
            long ahora = System.currentTimeMillis();

            db.runInTransaction(() -> {
                int idEjemplar = db.prestamoDao().getIdEjemplarByPrestamo(idPrestamo);
                db.prestamoDao().marcarDevuelto(idPrestamo, ahora);
                db.ejemplarDao().actualizarEstado(idEjemplar, "DISPONIBLE");
            });

            runOnUiThread(() -> {
                android.widget.Toast.makeText(this, "Devolución registrada", android.widget.Toast.LENGTH_SHORT).show();
                cargarDatos(usuarioId); // refresca el detalle del usuario
            });
        });
    }

}
