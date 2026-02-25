package com.DAM.bibliotecaapp.ui.usuario;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.RoleGuard;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.entities.Usuario;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddUsuarioActivity extends AppCompatActivity {

    private EditText etNombre, etEmail, etPassword;
    private EditText etRol; // puede existir aún en tu XML
    private Button btnGuardar;
    private TextView tvCancelar;

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleGuard.requireBibliotecario(this);
        setContentView(R.layout.activity_add_usuario);

        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnGuardar = findViewById(R.id.btnGuardar);
        tvCancelar = findViewById(R.id.tvCancelar);

        // ✅ Si aún tienes etRol en el layout, lo ocultamos y no lo usamos
        etRol = findViewById(R.id.etRol);
        if (etRol != null) {
            etRol.setText("LECTOR");
            etRol.setEnabled(false);
            etRol.setVisibility(View.GONE);
        }

        db = AppDatabase.getInstance(this);

        tvCancelar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void guardar() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim().toLowerCase(Locale.ROOT);
        String password = etPassword.getText().toString();

        // Validaciones
        if (nombre.isEmpty()) { etNombre.setError("Obligatorio"); return; }
        if (email.isEmpty()) { etEmail.setError("Obligatorio"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.setError("Email no válido"); return; }
        if (password.length() < 4) { etPassword.setError("Mínimo 4 caracteres"); return; }

        btnGuardar.setEnabled(false);

        executor.execute(() -> {
            try {
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
                u.rol = "LECTOR"; // ✅ fijo

                db.usuarioDao().insert(u);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Lector creado", Toast.LENGTH_SHORT).show();
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