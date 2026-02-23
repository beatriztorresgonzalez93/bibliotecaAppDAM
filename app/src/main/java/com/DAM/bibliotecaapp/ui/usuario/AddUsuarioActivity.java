package com.DAM.bibliotecaapp.ui.usuario;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.entities.Usuario;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddUsuarioActivity extends AppCompatActivity {

    private EditText etNombre, etEmail, etPassword, etRol;
    private Button btnGuardar;
    private TextView tvCancelar;

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_usuario);

        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etRol = findViewById(R.id.etRol);
        btnGuardar = findViewById(R.id.btnGuardar);
        tvCancelar = findViewById(R.id.tvCancelar);

        db = AppDatabase.getInstance(this);

        tvCancelar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void guardar() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim().toLowerCase(Locale.ROOT);
        String password = etPassword.getText().toString();
        String rol = etRol.getText().toString().trim().toUpperCase(Locale.ROOT);

        // Validaciones
        if (nombre.isEmpty()) { etNombre.setError("Obligatorio"); return; }
        if (email.isEmpty()) { etEmail.setError("Obligatorio"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.setError("Email no válido"); return; }
        if (password.length() < 4) { etPassword.setError("Mínimo 4 caracteres"); return; }

        if (rol.isEmpty()) rol = "USER"; // por defecto
        if (!rol.equals("USER") && !rol.equals("ADMIN")) {
            etRol.setError("Rol debe ser USER o ADMIN");
            return;
        }

        btnGuardar.setEnabled(false);

        String finalRol = rol;
        executor.execute(() -> {
            try {
                // Evitar duplicado por email (tu DAO ya lo tiene)
                Usuario existe = db.usuarioDao().getByEmail(email);
                if (existe != null) {
                    runOnUiThread(() -> {
                        btnGuardar.setEnabled(true);
                        etEmail.setError("Ese email ya existe");
                    });
                    return;
                }

                Usuario u = new Usuario();
                u.nombre = nombre;
                u.email = email;
                u.password = password;
                u.rol = finalRol;

                db.usuarioDao().insert(u);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Usuario creado", Toast.LENGTH_SHORT).show();
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnGuardar.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}