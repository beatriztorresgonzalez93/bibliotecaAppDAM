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

    public static final String EXTRA_USUARIO_ID = "usuario_id"; //  una sola clave

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private int usuarioId = -1;

    private TextView tvNombre, tvEmail, tvRol, tvPrestamosActivos, tvSinPrestamos;
    private TextView tvMultasPendientes, tvNumMultasPendientes;
    private TextView tvTotalPrestamos, tvTotalMultas, tvTotalPagado, tvTotalPendiente;

    private PrestamoInfoActivoAdapter adapter;

    private View cardAvisoVencidos;
    private TextView tvAvisoVencidos;

    private com.google.android.material.button.MaterialButton btnEliminarUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleGuard.requireLogin(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        SessionManager s = new SessionManager(this);


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


        long idSesion = s.getUsuarioId();


        long elegido = (idIntent != -1L) ? idIntent : idSesion;


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
        btnEliminarUsuario = findViewById(R.id.btnEliminarUsuario);
        SessionManager sessionManager = new SessionManager(this);
        btnEliminarUsuario.setVisibility(sessionManager.isBibliotecario() ? View.VISIBLE : View.GONE);

        if (sessionManager.isBibliotecario()) {
            btnEliminarUsuario.setOnClickListener(v -> mostrarDialogoEliminarUsuario());
        }
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

                // ✅ Mejor en pantallas pequeñas: 2 líneas
                tvPrestamosActivos.setText("Activos: " + finalActivos + "\nVencidos: " + finalVencidos);

                // ✅ listado de préstamos activos con info
                adapter.setData(activosInfo);

                // ✅ mensaje “sin préstamos”
                tvSinPrestamos.setVisibility((activosInfo == null || activosInfo.isEmpty()) ? View.VISIBLE : View.GONE);

                // ✅ Mini KPIs: SOLO VALOR (sin frase larga)
                if (tvMultasPendientes != null) {
                    tvMultasPendientes.setText(formatEuros(finalTotalMultasPendientes)); // "16,50 €"
                }

                if (tvNumMultasPendientes != null) {
                    tvNumMultasPendientes.setText(String.valueOf(finalNumPendientes)); // "5"
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

    private String formatEuros(double value) {
        java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("es", "ES"));
        return nf.format(value); // -> "16,50 €"
    }


    private void mostrarDialogoEliminarUsuario() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar usuario")
                .setMessage("¿Seguro que quieres eliminar este usuario? Esta acción no se puede deshacer.")
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .setPositiveButton("Eliminar", (d, w) -> intentarEliminarUsuario())
                .show();
    }


    private void intentarEliminarUsuario() {
        executor.execute(() -> {
            int prestamosPendientes = db.prestamoDao().countActivosOVencidosPorUsuario(usuarioId);
            int multasPendientes = db.multaDao().countPendientesPorUsuario(usuarioId);

            if (prestamosPendientes > 0 || multasPendientes > 0) {
                runOnUiThread(() -> Toast.makeText(
                        this,
                        "No se puede eliminar: el usuario tiene préstamos activos/vencidos o multas pendientes.",
                        Toast.LENGTH_LONG
                ).show());
                return;
            }

            int deleted = db.usuarioDao().deleteById(usuarioId);

            runOnUiThread(() -> {
                if (deleted > 0) {
                    Toast.makeText(this, "Usuario eliminado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "No se pudo eliminar el usuario", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}