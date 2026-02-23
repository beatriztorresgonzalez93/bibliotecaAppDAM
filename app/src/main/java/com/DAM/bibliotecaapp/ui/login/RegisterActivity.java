package com.DAM.bibliotecaapp.ui.login;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.entities.Bibliotecario;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNombre, etEmail, etPassword, etPassword2;
    private Button btnCrear;
    private TextView tvVolverLogin;

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPassword2 = findViewById(R.id.etPassword2);
        btnCrear = findViewById(R.id.btnCrear);
        tvVolverLogin = findViewById(R.id.tvVolverLogin);

        db = AppDatabase.getInstance(this);

        tvVolverLogin.setOnClickListener(v -> finish());

        btnCrear.setOnClickListener(v -> intentarRegistro());
    }

    private void intentarRegistro() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim().toLowerCase(Locale.ROOT);
        String pass1 = etPassword.getText().toString();
        String pass2 = etPassword2.getText().toString();

        // Validaciones básicas
        if (nombre.isEmpty()) { etNombre.setError("Obligatorio"); return; }
        if (email.isEmpty()) { etEmail.setError("Obligatorio"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.setError("Email no válido"); return; }
        if (pass1.length() < 6) { etPassword.setError("Mínimo 6 caracteres"); return; }
        if (!pass1.equals(pass2)) { etPassword2.setError("No coincide"); return; }

        btnCrear.setEnabled(false);

        executor.execute(() -> {
            try {
                int existe = db.bibliotecarioDao().countByEmail(email);
                if (existe > 0) {
                    runOnUiThread(() -> {
                        btnCrear.setEnabled(true);
                        etEmail.setError("Ese email ya está registrado");
                    });
                    return;
                }

                Bibliotecario b = new Bibliotecario(
                        nombre,
                        email,
                        pass1,
                        System.currentTimeMillis()
                );

                db.bibliotecarioDao().insert(b);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Cuenta creada", Toast.LENGTH_SHORT).show();
                    finish(); // vuelve al login
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnCrear.setEnabled(true);
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