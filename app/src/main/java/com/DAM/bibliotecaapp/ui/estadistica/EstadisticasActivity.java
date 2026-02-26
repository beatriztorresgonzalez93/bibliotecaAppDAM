package com.DAM.bibliotecaapp.ui.estadistica;


import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.RoleGuard;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.pojo.StatsResumen;
import com.DAM.bibliotecaapp.data.pojo.TopLibro;
import com.DAM.bibliotecaapp.data.pojo.TopUsuario;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EstadisticasActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private TextView tvUsuarios, tvLibros, tvEjemplares;
    private TextView tvActivos, tvVencidos, tvDevueltos;
    private TextView tvMultasPend, tvMultasPag, tvMultasCond;
    private TextView tvRecaudado;

    private RecyclerView rvTopLibros, rvTopUsuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleGuard.requireBibliotecario(this);
        setContentView(R.layout.activity_estadisticas);

        tvUsuarios = findViewById(R.id.tvUsuarios);
        tvLibros = findViewById(R.id.tvLibros);
        tvEjemplares = findViewById(R.id.tvEjemplares);

        tvActivos = findViewById(R.id.tvPrestamosActivos);
        tvVencidos = findViewById(R.id.tvPrestamosVencidos);
        tvDevueltos = findViewById(R.id.tvPrestamosDevueltos);

        tvMultasPend = findViewById(R.id.tvMultasPendientes);
        tvMultasPag = findViewById(R.id.tvMultasPagadas);
        tvMultasCond = findViewById(R.id.tvMultasCondonadas);

        tvRecaudado = findViewById(R.id.tvRecaudado);

        rvTopLibros = findViewById(R.id.rvTopLibros);
        rvTopUsuarios = findViewById(R.id.rvTopUsuarios);

        rvTopLibros.setLayoutManager(new LinearLayoutManager(this));
        rvTopUsuarios.setLayoutManager(new LinearLayoutManager(this));

        cargarDatos();
    }

    private void cargarDatos() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        executor.execute(() -> {
            StatsResumen r = db.estadisticasDao().getResumen();
            List<TopLibro> topLibros = db.estadisticasDao().getTopLibros(5);
            List<TopUsuario> topUsuarios = db.estadisticasDao().getTopUsuarios(5);

            runOnUiThread(() -> {
                // Resumen
                tvUsuarios.setText(String.valueOf(r.totalUsuarios));
                tvLibros.setText(String.valueOf(r.totalLibros));
                tvEjemplares.setText(String.valueOf(r.totalEjemplares));

                tvActivos.setText(String.valueOf(r.prestamosActivos));
                tvVencidos.setText(String.valueOf(r.prestamosVencidos));
                tvDevueltos.setText(String.valueOf(r.prestamosDevueltos));

                tvMultasPend.setText(String.valueOf(r.multasPendientes));
                tvMultasPag.setText(String.valueOf(r.multasPagadas));
                tvMultasCond.setText(String.valueOf(r.multasCondonadas));

                tvRecaudado.setText(String.format(Locale.getDefault(), "%.2f €", r.dineroRecaudado));

                // Listas
                rvTopLibros.setAdapter(new TopLibrosAdapter(topLibros));
                rvTopUsuarios.setAdapter(new TopUsuariosAdapter(topUsuarios));
            });
        });
    }
}