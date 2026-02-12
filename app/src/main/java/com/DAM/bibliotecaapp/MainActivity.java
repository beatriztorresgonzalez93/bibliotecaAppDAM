package com.DAM.bibliotecaapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        int usuarioSesion = getSharedPreferences("session", MODE_PRIVATE)
                .getInt("usuario_id", -1);


        if (usuarioSesion == -1) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button btnLibros = findViewById(R.id.btnLibros);
        Button btnPrestamos = findViewById(R.id.btnPrestamos);
        Button btnUsuario = findViewById(R.id.btnUsuarios);

        Button btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            getSharedPreferences("session", MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });


        btnUsuario.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, UsuarioActivity.class))
        );

        int usuarioId = getIntent().getIntExtra("usuario_id", -1);


// Estos dos los conectas a tus pantallas reales si ya existen.
// Si aún no existen, coméntalos para que compile.
        btnLibros.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LibrosActivity.class))
        );

//        btnPrestamos.setOnClickListener(v ->
//                startActivity(new Intent(MainActivity.this, PrestamosActivity.class))
//        );

    }
}