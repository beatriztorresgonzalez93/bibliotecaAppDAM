package com.DAM.bibliotecaapp.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.core.view.WindowCompat;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.RoleGuard;
import com.DAM.bibliotecaapp.SessionManager;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.ui.base.BaseActivity;
import com.DAM.bibliotecaapp.ui.devolucion.DevolucionesActivity;
import com.DAM.bibliotecaapp.ui.estadistica.EstadisticasActivity;
import com.DAM.bibliotecaapp.ui.libro.LibrosActivity;
import com.DAM.bibliotecaapp.ui.login.LoginActivity;
import com.DAM.bibliotecaapp.ui.login.RegisterActivity;
import com.DAM.bibliotecaapp.ui.multa.MultasActivity;
import com.DAM.bibliotecaapp.ui.prestamo.PrestamoActivity;
import com.DAM.bibliotecaapp.ui.usuario.UsuarioActivity;

import java.util.concurrent.Executors;

public class MainActivity extends BaseActivity {

    private TextView tvTitulo;
    private TextView btnAddBibliotecario;

    private View btnPrestamos, btnMultas, btnDevoluciones, btnEstadisticas;

    private TextView tvResumenPrestamos;
    private TextView tvResumenVencidos;
    private TextView tvResumenMultas;
    private TextView tvSubtitulo;
    private View cardResumen;

    private TextView tvLectorPrestamosActivos, tvLectorMultasPendientes;
    private View cardActividadLector;
    private final java.util.concurrent.ExecutorService dbExecutor =
            java.util.concurrent.Executors.newSingleThreadExecutor();

    private View chevronLibros;
    private View chevronUsuarios;

    private TextView tvSaludo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        RoleGuard.requireLogin(this);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_main);

        applySystemBarsPadding(R.id.main);

        SessionManager session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        tvTitulo = findViewById(R.id.tvTitulo);


        View btnLibros = findViewById(R.id.btnLibros);
        btnPrestamos = findViewById(R.id.btnPrestamos);
        View btnUsuario = findViewById(R.id.btnUsuarios);

        btnMultas = findViewById(R.id.btnMultas);
        btnDevoluciones = findViewById(R.id.btnDevolucion);
        btnEstadisticas = findViewById(R.id.btnEstadisticas);

        View btnLogout = findViewById(R.id.btnLogout);

        btnAddBibliotecario = findViewById(R.id.btnAddBibliotecario);

        tvResumenPrestamos = findViewById(R.id.tvResumenPrestamos);
        tvResumenVencidos = findViewById(R.id.tvResumenVencidos);
        tvResumenMultas = findViewById(R.id.tvResumenMultas);

        tvSubtitulo = findViewById(R.id.tvSubtitulo);

        cardResumen = findViewById(R.id.cardResumen);

        tvLectorPrestamosActivos = findViewById(R.id.tvLectorPrestamosActivos);
        tvLectorMultasPendientes = findViewById(R.id.tvLectorMultasPendientes);
        cardActividadLector = findViewById(R.id.cardActividadLector);

        chevronLibros = findViewById(R.id.chevronLibros);
        chevronUsuarios = findViewById(R.id.chevronUsuarios);

        tvSaludo = findViewById(R.id.tvSaludo);


        // Clicks
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                new SessionManager(MainActivity.this).logout();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            });
        }

        if (btnUsuario != null) {
            btnUsuario.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, UsuarioActivity.class))
            );
        }

        if (btnLibros != null) {
            btnLibros.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, LibrosActivity.class))
            );
        }

        if (btnPrestamos != null) {
            btnPrestamos.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, PrestamoActivity.class))
            );
        }

        if (btnMultas != null) {
            btnMultas.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, MultasActivity.class))
            );
        }

        if (btnDevoluciones != null) {
            btnDevoluciones.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, DevolucionesActivity.class))
            );
        }

        if (btnAddBibliotecario != null) {
            btnAddBibliotecario.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, RegisterActivity.class))
            );
        }

        if (btnEstadisticas != null) {
            btnEstadisticas.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, EstadisticasActivity.class))
            );
        }

        aplicarUIRol();
        cargarResumenBibliotecario();
    }

    @Override
    protected void onResume() {
        super.onResume();
        aplicarUIRol();
        cargarSaludo();
        cargarResumenLectorSiToca();
    }

    private void aplicarUIRol() {

        SessionManager sm = new SessionManager(this);

        if (sm.isLector()) {


            chevronLibros.setVisibility(View.VISIBLE);
            chevronUsuarios.setVisibility(View.VISIBLE);

        } else {


            chevronLibros.setVisibility(View.GONE);
            chevronUsuarios.setVisibility(View.GONE);
    }
        SessionManager session = new SessionManager(this);
        boolean isAdmin = session.isBibliotecario();

        // Subtítulo
        if (tvSubtitulo != null) {
            tvSubtitulo.setText(isAdmin ? "Modo Bibliotecario" : "Modo Lector");
        }

        // ✅ Card resumen SOLO bibliotecario
        if (cardResumen != null) {
            cardResumen.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }

        // Botón "Añadir bibliotecario" solo admin
        if (btnAddBibliotecario != null) {
            btnAddBibliotecario.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }

        // Menú por rol (como ya lo tienes)
        if (btnPrestamos != null) btnPrestamos.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        if (btnMultas != null) btnMultas.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        if (btnDevoluciones != null) btnDevoluciones.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        if (btnEstadisticas != null) btnEstadisticas.setVisibility(isAdmin ? View.VISIBLE : View.GONE);


        if (isAdmin) {
            cargarResumenBibliotecario();
        }
    }

    private void cargarResumenBibliotecario() {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            com.DAM.bibliotecaapp.data.db.AppDatabase db =
                    com.DAM.bibliotecaapp.data.db.AppDatabase.getInstance(this);

            // Asegura que estados vencidos están actualizados
            db.prestamoDao().marcarVencidos(System.currentTimeMillis());

            int prestamos = db.prestamoDao().countPrestamosActivos();      // no devueltos
            int vencidos = db.prestamoDao().countPrestamosVencidos();      // vencidos no devueltos
            double multas = db.multaDao().getTotalMultasPendientes();      // pendientes global

            runOnUiThread(() -> {
                if (tvResumenPrestamos != null) tvResumenPrestamos.setText(String.valueOf(prestamos));
                if (tvResumenVencidos != null) tvResumenVencidos.setText(String.valueOf(vencidos));
                if (tvResumenMultas != null) {
                    tvResumenMultas.setText(String.format(
                            java.util.Locale.getDefault(), "%.2f €", multas));
                }
            });
        });
    }

    private void cargarResumenLectorSiToca() {

        SessionManager sm = new SessionManager(this);

        if (!sm.isLector()) {   // si es bibliotecario, oculta la card
            if (cardActividadLector != null) cardActividadLector.setVisibility(View.GONE);
            return;
        }

        long idUsuario = sm.getUsuarioId(); // <- tu método real
        if (idUsuario <= 0) {
            // si no hay id, no podemos consultar
            if (tvLectorPrestamosActivos != null) tvLectorPrestamosActivos.setText("—");
            if (tvLectorMultasPendientes != null) tvLectorMultasPendientes.setText("—");
            return;
        }

        if (cardActividadLector != null) cardActividadLector.setVisibility(View.VISIBLE);


        if (tvLectorPrestamosActivos != null) tvLectorPrestamosActivos.setText("…");
        if (tvLectorMultasPendientes != null) tvLectorMultasPendientes.setText("…");

        dbExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());

            int prestamosActivos = db.prestamoDao().countActivosOVencidosPorUsuario(idUsuario);
            int multasPendientes = db.multaDao().countPendientesPorUsuario(idUsuario);

            runOnUiThread(() -> {
                if (tvLectorPrestamosActivos != null) {
                    tvLectorPrestamosActivos.setText(String.valueOf(prestamosActivos));
                }
                if (tvLectorMultasPendientes != null) {
                    tvLectorMultasPendientes.setText(String.valueOf(multasPendientes));
                }
            });
        });
    }

    private void cargarSaludo() {

        SessionManager sm = new SessionManager(this);

        if (tvSaludo == null) return; // seguridad por si acaso

        if (sm.isLector()) {

            String nombre = sm.getNombre();

            if (nombre != null && !nombre.trim().isEmpty()) {


                String primerNombre = nombre.trim().split(" ")[0];

                tvSaludo.setText("¡Hola, " + primerNombre + "! Bienvenida 🎉");

            } else {

                tvSaludo.setText("¡Hola! Bienvenida 🎉");
            }

        } else if (sm.isBibliotecario()) {

            tvSaludo.setText("Panel de gestión");
        }
    }


}