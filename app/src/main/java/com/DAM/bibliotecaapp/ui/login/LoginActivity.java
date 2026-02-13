package com.DAM.bibliotecaapp.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.ui.main.MainActivity;
import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.data.seed.SeedData;
import com.DAM.bibliotecaapp.data.entities.Usuario;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario, etPassword;
    private Button btnLogin;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SeedData.seedIfEmpty(this);

        setContentView(R.layout.activity_login);

        db = AppDatabase.getInstance(this);

        // Si la tabla está vacía, crea un admin de prueba
        if (db.usuarioDao().count() == 0) {
            Usuario admin = new Usuario();
            admin.nombre = "Admin";
            admin.email = "admin@demo.com";
            admin.password = "admin123";
            admin.rol = "ADMIN";
            db.usuarioDao().insert(admin);
        }

        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String email = etUsuario.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Rellena email y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            Usuario u = db.usuarioDao().login(email, pass);

            if (u == null) {
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                return;
            }



            Toast.makeText(this, "Bienvenida/o " + u.nombre, Toast.LENGTH_SHORT).show();

            getSharedPreferences("session", MODE_PRIVATE)
                    .edit()
                    .putInt("usuario_id", u.id)
                    .putString("rol", u.rol)
                    .apply();


            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("usuario_id", u.id);
            startActivity(intent);
            finish();
        });
    }
}
