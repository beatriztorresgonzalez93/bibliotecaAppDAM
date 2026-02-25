package com.DAM.bibliotecaapp.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.SessionManager;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.entities.Bibliotecario;
import com.DAM.bibliotecaapp.data.entities.Usuario;
import com.DAM.bibliotecaapp.data.seed.HistorySeedData;
import com.DAM.bibliotecaapp.data.seed.SeedData;
import com.DAM.bibliotecaapp.ui.base.BaseActivity;
import com.DAM.bibliotecaapp.ui.main.MainActivity;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends BaseActivity {

    private EditText etUsuario, etPassword;
    private Button btnLogin;

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // DB primero
        db = AppDatabase.getInstance(this);

        // Seeds
        SeedData.seedIfEmpty(this);
        HistorySeedData.seedHistoryIfEmpty(this);

        setContentView(R.layout.activity_login);
        applySystemBarsPadding(R.id.main);

        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);


        // ✅ Asegurar bibliotecario demo
        executor.execute(() -> {
            try {
                Bibliotecario existe = db.bibliotecarioDao().findByEmail("biblio@demo.com");
                if (existe == null) {
                    Bibliotecario b = new Bibliotecario();
                    b.nombre = "Bibliotecario Demo";
                    b.email = "biblio@demo.com";
                    b.password = "biblio123";
                    db.bibliotecarioDao().insert(b);
                }
            } catch (Exception ignored) {}
        });

        btnLogin.setOnClickListener(v -> {

            String email = etUsuario.getText().toString().trim().toLowerCase(Locale.ROOT);
            String pass = etPassword.getText().toString();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Rellena email y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setEnabled(false);

            executor.execute(() -> {

                // ✅ 1) Bibliotecario primero
                Bibliotecario b = db.bibliotecarioDao().login(email, pass);
                if (b != null) {
                    runOnUiThread(() -> {
                        btnLogin.setEnabled(true);

                        SessionManager s = new SessionManager(LoginActivity.this);
                        s.loginBibliotecario(b.id);

                        Toast.makeText(LoginActivity.this, "Modo bibliotecario: " + b.nombre, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    });
                    return;
                }

                // ✅ 2) Luego Usuario (lector)
                Usuario u = db.usuarioDao().login(email, pass);
                if (u != null) {
                    runOnUiThread(() -> {
                        btnLogin.setEnabled(true);

                        SessionManager s = new SessionManager(LoginActivity.this);
                        s.loginLector(u.id);

                        Toast.makeText(LoginActivity.this, "Modo lector: " + u.nombre, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    });
                    return;
                }

                // ❌ Ninguno
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                });
            });
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}