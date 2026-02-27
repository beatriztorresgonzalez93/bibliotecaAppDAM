package com.DAM.bibliotecaapp.ui.usuario;

import android.os.Bundle;
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
import com.DAM.bibliotecaapp.data.entities.Multa;
import com.DAM.bibliotecaapp.data.entities.Usuario;
import com.DAM.bibliotecaapp.data.pojo.PrestamoInfo;
import com.DAM.bibliotecaapp.ui.base.BaseActivity;
import com.DAM.bibliotecaapp.ui.prestamo.PrestamoInfoActivoAdapter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsuarioDetalleActivity extends BaseActivity {

    public static final String EXTRA_USUARIO_ID = "usuario_id"; // ✅ una sola clave

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private int usuarioId = -1;

    private TextView tvNombre, tvEmail, tvRol, tvPrestamosActivos, tvSinPrestamos;
    private TextView tvMultasPendientes, tvNumMultasPendientes;
    private TextView tvTotalPrestamos, tvTotalMultas, tvTotalPagado, tvTotalPendiente;

    private PrestamoInfoActivoAdapter adapter;

    private View cardAvisoVencidos;
    private TextView tvAvisoVencidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleGuard.requireLogin(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

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

        // seguridad lector: solo puede ver su propio perfil
        if (s.isLector() && usuarioId != (int) s.getUsuarioId()) {
            Toast.makeText(this, "Acceso restringido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_usuario_detalle);
        applySystemBarsPadding(R.id.main);

        db = AppDatabase.getInstance(this);

        tvNombre = findViewById(R.id.tvNombre);
        tvEmail = findViewById(R.id.tvEmail);
        tvRol = findViewById(R.id.tvRol);
        tvPrestamosActivos = findViewById(R.id.tvPrestamosActivos);
        tvSinPrestamos = findViewById(R.id.tvSinPrestamos);

        tvMultasPendientes = findViewById(R.id.tvMultasPendientes);
        tvNumMultasPendientes = findViewById(R.id.tvNumMultasPendientes);

        tvTotalPrestamos = findViewById(R.id.tvTotalPrestamos);
        tvTotalMultas = findViewById(R.id.tvTotalMultas);
        tvTotalPagado = findViewById(R.id.tvTotalPagado);
        tvTotalPendiente = findViewById(R.id.tvTotalPendiente);

        cardAvisoVencidos = findViewById(R.id.cardAvisoVencidos);
        tvAvisoVencidos = findViewById(R.id.tvAvisoVencidos);

        RecyclerView rv = findViewById(R.id.rvPrestamosActivos);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PrestamoInfoActivoAdapter();
        rv.setAdapter(adapter);

        cargarDatos(usuarioId);
    }

    private void cargarDatos(int usuarioId) {
        executor.execute(() -> {
            // 1) actualiza vencidos antes de consultar
            db.prestamoDao().marcarVencidos(System.currentTimeMillis());

            // 2) usuario
            Usuario usuario = db.usuarioDao().getById(usuarioId);

            // 3) préstamos NO devueltos para contar activos/vencidos (incluye ACTIVO + VENCIDO)
            List<PrestamoInfo> noDevueltos = db.prestamoDao().getNoDevueltosByUsuario(usuarioId);

            int activos = 0;
            int vencidos = 0;

            if (noDevueltos != null) {
                for (PrestamoInfo p : noDevueltos) {
                    if (p == null || p.estado == null) continue;
                    if ("VENCIDO".equalsIgnoreCase(p.estado)) vencidos++;
                    else if ("ACTIVO".equalsIgnoreCase(p.estado)) activos++;
                    else activos++; // por si hay algún estado raro
                }
            }

            // 4) listado que verá el lector (solo ACTIVO + info completa)
            List<PrestamoInfo> activosInfo = db.prestamoDao().getActivosInfoByUsuario(usuarioId);

            // 5) multas pendientes (importe)
            double totalMultasPendientes = db.multaDao().totalPendienteUsuario(usuarioId);

            // 6) nº multas pendientes
            List<Multa> multasUsuario = db.multaDao().getByUsuario(usuarioId);
            int numPendientes = 0;
            if (multasUsuario != null) {
                for (Multa m : multasUsuario) {
                    if (m != null && "PENDIENTE".equalsIgnoreCase(m.estado)) numPendientes++;
                }
            }

            int finalActivos = activos;
            int finalVencidos = vencidos;
            if (cardAvisoVencidos != null) {
                if (finalVencidos > 0) {
                    cardAvisoVencidos.setVisibility(View.VISIBLE);
                    tvAvisoVencidos.setText(
                            "Tienes " + finalVencidos + " préstamos vencidos. Devuélvelos lo antes posible."
                    );
                } else {
                    cardAvisoVencidos.setVisibility(View.GONE);
                }
            }
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

                // ✅ listado de préstamos activos con info
                adapter.setData(activosInfo);

                // ✅ mensaje “sin préstamos”
                tvSinPrestamos.setVisibility((activosInfo == null || activosInfo.isEmpty()) ? View.VISIBLE : View.GONE);

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

                // ✅ estadísticas del usuario (siempre)
                cargarEstadisticasUsuario(usuarioId);
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (usuarioId != -1) cargarDatos(usuarioId);
    }

    // ⚠️ En esta pantalla (perfil lector) ya NO deberíamos devolver desde el listado.
    // Si quieres devolución, déjalo para pantallas de bibliotecario (PrestamoActivity / DevolucionesActivity).

    private void cargarEstadisticasUsuario(int idUsuario) {
        executor.execute(() -> {
            int totalPrestamos = db.prestamoDao().contarPrestamosTotalesUsuario(idUsuario);
            int totalMultas = db.multaDao().contarMultasTotalesUsuario(idUsuario);
            double totalPagado = db.multaDao().sumarMultasPagadasUsuario(idUsuario);
            double totalPendiente = db.multaDao().sumarMultasPendientesUsuario(idUsuario);

            runOnUiThread(() -> {
                if (tvTotalPrestamos != null) tvTotalPrestamos.setText("Total préstamos: " + totalPrestamos);
                if (tvTotalMultas != null) tvTotalMultas.setText("Total multas: " + totalMultas);
                if (tvTotalPagado != null)
                    tvTotalPagado.setText("Total pagado en multas: " +
                            String.format(java.util.Locale.getDefault(), "%.2f €", totalPagado));
                if (tvTotalPendiente != null)
                    tvTotalPendiente.setText("Total pendiente en multas: " +
                            String.format(java.util.Locale.getDefault(), "%.2f €", totalPendiente));
            });
        });
    }
}