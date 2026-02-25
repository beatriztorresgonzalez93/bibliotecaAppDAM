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

// ✅ leer id desde intent con compatibilidad
        long idIntent = -1L;
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            // caso nuevo: "usuario_id"
            if (extras.containsKey("usuario_id")) {
                Object v = extras.get("usuario_id");
                if (v instanceof Long) idIntent = (Long) v;
                else if (v instanceof Integer) idIntent = ((Integer) v).longValue();
            }
            // caso antiguo: "usuarioId"
            else if (extras.containsKey("usuarioId")) {
                Object v = extras.get("usuarioId");
                if (v instanceof Long) idIntent = (Long) v;
                else if (v instanceof Integer) idIntent = ((Integer) v).longValue();
            }
        }

// ✅ lector: si no viene por intent, usar sesión
        long idSesion = s.getUsuarioId();

// decidir id final
        long elegido = (idIntent != -1L) ? idIntent : idSesion;

// validar
        if (elegido == -1L) {
            Toast.makeText(this, "Error: usuario no recibido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        usuarioId = (int) elegido;

// seguridad lector
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