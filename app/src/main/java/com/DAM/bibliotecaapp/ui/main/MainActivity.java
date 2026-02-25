package com.DAM.bibliotecaapp.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.RoleGuard;
import com.DAM.bibliotecaapp.SessionManager;
import com.DAM.bibliotecaapp.ui.base.BaseActivity;
import com.DAM.bibliotecaapp.ui.devolucion.DevolucionesActivity;
import com.DAM.bibliotecaapp.ui.libro.LibrosActivity;
import com.DAM.bibliotecaapp.ui.login.LoginActivity;
import com.DAM.bibliotecaapp.ui.login.RegisterActivity;
import com.DAM.bibliotecaapp.ui.multa.MultasActivity;
import com.DAM.bibliotecaapp.ui.prestamo.PrestamoActivity;
import com.DAM.bibliotecaapp.ui.usuario.UsuarioActivity;

public class MainActivity extends BaseActivity {

    private TextView tvTitulo;
    private TextView btnAddBibliotecario;

    private Button btnPrestamos, btnMultas, btnDevoluciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RoleGuard.requireLogin(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        setContentView(R.layout.activity_main);
        applySystemBarsPadding(R.id.main);

        // Sesión
        SessionManager session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvTitulo = findViewById(R.id.tvTitulo);

        Button btnLibros = findViewById(R.id.btnLibros);
        btnPrestamos = findViewById(R.id.btnPrestamos);
        Button btnUsuario = findViewById(R.id.btnUsuarios);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnMultas = findViewById(R.id.btnMultas);
        btnDevoluciones = findViewById(R.id.btnDevolucion);

        btnAddBibliotecario = findViewById(R.id.btnAddBibliotecario);

        // Aplicar UI por rol + título
        aplicarUIRol();

        btnLogout.setOnClickListener(v -> {
            new SessionManager(MainActivity.this).logout();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        btnUsuario.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, UsuarioActivity.class))
        );

        btnPrestamos.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, PrestamoActivity.class))
        );

        btnLibros.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LibrosActivity.class))
        );

        btnMultas.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MultasActivity.class))
        );

        btnDevoluciones.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, DevolucionesActivity.class))
        );

        if (btnAddBibliotecario != null) {
            btnAddBibliotecario.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, RegisterActivity.class))
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        aplicarUIRol();
    }

    private void aplicarUIRol() {
        SessionManager session = new SessionManager(this);
        boolean isAdmin = session.isBibliotecario();

        // Botón "Añadir bibliotecario" solo admin
        if (btnAddBibliotecario != null) {
            btnAddBibliotecario.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }

        // Título dinámico
        if (tvTitulo != null) {
            tvTitulo.setText(isAdmin ? "Biblioteca (Modo Bibliotecario)" : "Biblioteca");
        }

        // Botones por rol
        if (btnPrestamos != null) btnPrestamos.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        if (btnMultas != null) btnMultas.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        if (btnDevoluciones != null) btnDevoluciones.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
    }
}