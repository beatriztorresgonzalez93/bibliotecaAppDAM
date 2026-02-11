package com.DAM.bibliotecaapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String usuario = etUsuario.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (usuario.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Rellena usuario y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            // De momento: login “correcto” si no está vacío
            Toast.makeText(this, "Login correcto", Toast.LENGTH_SHORT).show();

            // Ir a la pantalla principal (de momento MainActivity)
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
