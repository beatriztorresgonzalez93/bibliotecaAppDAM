package com.DAM.bibliotecaapp.ui.usuario;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.RoleGuard;
import com.DAM.bibliotecaapp.SessionManager;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.entities.Usuario;
import com.DAM.bibliotecaapp.data.pojo.PrestamoInfo;
import com.DAM.bibliotecaapp.ui.prestamo.PrestamoInfoAdapter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsuarioDetalleActivity extends AppCompatActivity {

    public static final String EXTRA_USUARIO_ID = "usuario_id"; // ✅ una sola clave

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private int usuarioId = -1; // ✅ solo 1 usuarioId y es int (tu DAO usa int)

    private TextView tvNombre, tvEmail, tvRol, tvPrestamosActivos, tvSinPrestamos;
    private PrestamoInfoAdapter adapter;
    private TextView tvMultasPendientes;
    private TextView tvNumMultasPendientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleGuard.requireLogin(this);

        SessionManager s = new SessionManager(this);

        // 1) Intent (si viene)
        long idIntent = getIntent().getLongExtra(EXTRA_USUARIO_ID, -1L);

        // 2) Sesión (para lector)
        long idSesion = s.getUsuarioId();

        // 3) Elegir id
        if (idIntent != -1L) usuarioId = (int) idIntent;
        else if (idSesion != -1L) usuarioId = (int) idSesion;

        // 4) Validación
        if (usuarioId == -1) {
            Toast.makeText(this, "Error: usuario no recibido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 5) Seguridad: lector solo puede ver su propio perfil
        if (s.isLector() && usuarioId != (int) s.getUsuarioId()) {
            Toast.makeText(this, "Acceso restringido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_usuario_detalle);

        db = AppDatabase.getInstance(this);

        tvNombre = findViewById(R.id.tvNombre);
        tvEmail = findViewById(R.id.tvEmail);
        tvRol = findViewById(R.id.tvRol);
        tvPrestamosActivos = findViewById(R.id.tvPrestamosActivos);
        tvSinPrestamos = findViewById(R.id.tvSinPrestamos);

        RecyclerView rv = findViewById(R.id.rvPrestamosActivos);
        rv.setLayoutManager(new LinearLayoutManager(this));

        tvMultasPendientes = findViewById(R.id.tvMultasPendientes);
        tvNumMultasPendientes = findViewById(R.id.tvNumMultasPendientes);

        adapter = new PrestamoInfoAdapter(prestamoInfo -> mostrarDialogoDevolucion(prestamoInfo.idPrestamo));
        rv.setAdapter(adapter);

        cargarDatos(usuarioId);
    }

    private void cargarDatos(int usuarioId) {
        executor.execute(() -> {
            db.prestamoDao().marcarVencidos(System.currentTimeMillis());

            Usuario usuario = db.usuarioDao().getById(usuarioId);

            List<PrestamoInfo> lista = db.prestamoDao().getNoDevueltosByUsuario(usuarioId);

            int activos = 0;
            int vencidos = 0;

            if (lista != null) {
                for (PrestamoInfo p : lista) {
                    if ("VENCIDO".equals(p.estado)) vencidos++;
                    else activos++;
                }
            }

            double totalMultasPendientes = db.multaDao().totalPendienteUsuario(usuarioId);

            List<com.DAM.bibliotecaapp.data.entities.Multa> multasUsuario =
                    db.multaDao().getByUsuario(usuarioId);

            int numPendientes = 0;
            if (multasUsuario != null) {
                for (com.DAM.bibliotecaapp.data.entities.Multa m : multasUsuario) {
                    if ("PENDIENTE".equals(m.estado)) numPendientes++;
                }
            }

            int finalActivos = activos;
            int finalVencidos = vencidos;
            double finalTotalMultasPendientes = totalMultasPendientes;
            int finalNumPendientes = numPendientes;

            runOnUiThread(() -> {
                if (usuario == null) {
                    tvNombre.setText("Usuario no encontrado");
                    return;
                }

                tvNombre.setText(usuario.nombre);
                tvEmail.setText(usuario.email);
                tvRol.setText("Rol: " + usuario.rol);

                tvPrestamosActivos.setText("Activos: " + finalActivos + " · Vencidos: " + finalVencidos);

                adapter.setData(lista);

                tvSinPrestamos.setVisibility((lista == null || lista.isEmpty()) ? View.VISIBLE : View.GONE);

                if (tvMultasPendientes != null) {
                    tvMultasPendientes.setText(
                            "Multas pendientes: " +
                                    String.format(java.util.Locale.getDefault(), "%.2f", finalTotalMultasPendientes) +
                                    " €"
                    );
                }

                if (tvNumMultasPendientes != null) {
                    tvNumMultasPendientes.setText("Nº multas pendientes: " + finalNumPendientes);
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
        // ✅ Solo bibliotecario debería poder devolver
        if (!new SessionManager(this).isBibliotecario()) {
            Toast.makeText(this, "Solo bibliotecario puede devolver", Toast.LENGTH_SHORT).show();
            return;
        }

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
                Toast.makeText(this, "Devolución registrada", Toast.LENGTH_SHORT).show();
                cargarDatos(usuarioId);
            });
        });
    }
}