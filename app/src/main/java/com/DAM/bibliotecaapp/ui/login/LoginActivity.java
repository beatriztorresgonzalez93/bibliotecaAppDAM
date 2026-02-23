package com.DAM.bibliotecaapp.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.entities.Bibliotecario;
import com.DAM.bibliotecaapp.data.entities.Usuario;
import com.DAM.bibliotecaapp.data.seed.HistorySeedData;
import com.DAM.bibliotecaapp.data.seed.SeedData;
import com.DAM.bibliotecaapp.ui.main.MainActivity;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario, etPassword;
    private Button btnLogin, btnRegister;

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SeedData.seedIfEmpty(this);
        HistorySeedData.seedHistoryIfEmpty(this);

        setContentView(R.layout.activity_login);

        db = AppDatabase.getInstance(this);

        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Crea admin demo si no hay usuarios (en background)
        executor.execute(() -> {
            try {
                if (db.usuarioDao().count() == 0) {
                    Usuario admin = new Usuario();
                    admin.nombre = "Admin";
                    admin.email = "admin@demo.com";
                    admin.password = "admin123";
                    admin.rol = "ADMIN";
                    db.usuarioDao().insert(admin);
                }
            } catch (Exception ignored) {}
        });

        btnLogin.setOnClickListener(v -> {
            String email = etUsuario.getText().toString().trim().toLowerCase(Locale.ROOT);
            String pass = etPassword.getText().toString(); // no trim

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Rellena email y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setEnabled(false);

            executor.execute(() -> {
                // 1) Intentar login como Usuario
                Usuario u = db.usuarioDao().login(email, pass);

                if (u != null) {
                    runOnUiThread(() -> {
                        btnLogin.setEnabled(true);

                        Toast.makeText(this, "Bienvenida/o " + u.nombre, Toast.LENGTH_SHORT).show();

                        getSharedPreferences("session", MODE_PRIVATE)
                                .edit()
                                .putInt("usuario_id", u.id)
                                .putLong("bibliotecario_id", -1)
                                .putString("rol", u.rol)
                                .apply();

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    });
                    return;
                }

                // 2) Si no es Usuario, intentar como Bibliotecario
                Bibliotecario b = db.bibliotecarioDao().login(email, pass);

                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);

                    if (b == null) {
                        Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(this, "Bienvenida/o " + b.nombre, Toast.LENGTH_SHORT).show();

                    getSharedPreferences("session", MODE_PRIVATE)
                            .edit()
                            .putInt("usuario_id", (int) b.id)   // <-- para que MainActivity no te expulse
                            .putLong("bibliotecario_id", b.id)
                            .putString("rol", "BIBLIOTECARIO")
                            .apply();

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                });
            });
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}