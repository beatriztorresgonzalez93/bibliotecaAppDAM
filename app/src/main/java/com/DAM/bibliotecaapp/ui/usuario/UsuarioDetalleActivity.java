package com.DAM.bibliotecaapp.ui.usuario;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.entities.Usuario;
import com.DAM.bibliotecaapp.data.pojo.PrestamoInfo;
import com.DAM.bibliotecaapp.ui.prestamo.PrestamoInfoAdapter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsuarioDetalleActivity extends AppCompatActivity {

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private int usuarioId;

    private TextView tvNombre, tvEmail, tvRol, tvPrestamosActivos, tvSinPrestamos;
    private PrestamoInfoAdapter adapter;

    public static final String EXTRA_USUARIO_ID = "usuarioId";


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

        adapter = new PrestamoInfoAdapter(prestamoInfo ->
                mostrarDialogoDevolucion(prestamoInfo.idPrestamo)
        );
        rv.setAdapter(adapter);

        // OJO: clave consistente "usuarioId"
        usuarioId = getIntent().getIntExtra(EXTRA_USUARIO_ID, -1);


        if (usuarioId == -1) {
            tvNombre.setText("Error: usuario no recibido");
            return;
        }

        cargarDatos(usuarioId);
    }

    private void cargarDatos(int usuarioId) {
        executor.execute(() -> {
            // 1) Actualiza vencidos antes de leer
            db.prestamoDao().marcarVencidos(System.currentTimeMillis());

            Usuario usuario = db.usuarioDao().getById(usuarioId);

            // 2) Lista NO DEVUELTOS (activos + vencidos)
            List<PrestamoInfo> lista = db.prestamoDao().getNoDevueltosByUsuario(usuarioId);

            // 3) Contadores
            int activos = 0;
            int vencidos = 0;

            if (lista != null) {
                for (PrestamoInfo p : lista) {
                    if ("VENCIDO".equals(p.estado)) vencidos++;
                    else activos++;
                }
            }

            int finalActivos = activos;
            int finalVencidos = vencidos;

            runOnUiThread(() -> {
                if (usuario == null) {
                    tvNombre.setText("Usuario no encontrado");
                    return;
                }

                tvNombre.setText(usuario.nombre);
                tvEmail.setText(usuario.email);
                tvRol.setText("Rol: " + usuario.rol);

                // Aquí ya puedes mostrar ambos si quieres:
                tvPrestamosActivos.setText("Activos: " + finalActivos + " · Vencidos: " + finalVencidos);

                adapter.setData(lista);

                if (lista == null || lista.isEmpty()) {
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
                cargarDatos(usuarioId);
            });
        });
    }
}
