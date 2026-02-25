package com.DAM.bibliotecaapp.ui.prestamo;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.DAM.bibliotecaapp.RoleGuard;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.R;

import com.DAM.bibliotecaapp.data.entities.Prestamo;
import com.DAM.bibliotecaapp.data.entities.Usuario;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.DAM.bibliotecaapp.data.entities.Ejemplar;

public class NuevoPrestamoActivity extends AppCompatActivity {

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private AutoCompleteTextView actvUsuarios;

    private int idLibro = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleGuard.requireBibliotecario(this);
        setContentView(R.layout.activity_nuevo_prestamo);

        db = AppDatabase.getInstance(this);

        idLibro = getIntent().getIntExtra("idLibro", -1);
        String titulo = getIntent().getStringExtra("titulo");

        TextView tvLibro = findViewById(R.id.tvLibro);
        actvUsuarios = findViewById(R.id.actvUsuarios);
        Button btn = findViewById(R.id.btnConfirmarPrestamo);

        tvLibro.setText("Libro: " + (titulo != null ? titulo : ("ID " + idLibro)));

        configurarAutocompleteEmails();

        btn.setOnClickListener(v -> {
            String email = actvUsuarios.getText().toString().trim();

            if (idLibro == -1) {
                Toast.makeText(this, "Error: libro no recibido", Toast.LENGTH_SHORT).show();
                return;
            }
            if (email.isEmpty()) {
                Toast.makeText(this, "Introduce el email del usuario", Toast.LENGTH_SHORT).show();
                return;
            }

            crearPrestamo(idLibro, email);
        });
    }

    private void configurarAutocompleteEmails() {
        actvUsuarios.setThreshold(1);

        executor.execute(() -> {
            List<String> emails = db.usuarioDao().getAllEmails();

            runOnUiThread(() -> {
                if (emails == null || emails.isEmpty()) {
                    Toast.makeText(this, "No hay emails para autocompletar", Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        emails
                );

                actvUsuarios.setAdapter(adapter);

                actvUsuarios.setOnClickListener(v -> actvUsuarios.showDropDown());
                actvUsuarios.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) actvUsuarios.showDropDown();
                });
            });
        });
    }

    private void crearPrestamo(int idLibro, String emailUsuario) {
        executor.execute(() -> {

            // 1) Buscar usuario por email
            Usuario u = db.usuarioDao().getByEmail(emailUsuario);
            if (u == null) {
                runOnUiThread(() -> Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show());
                return;
            }

            // 2) Buscar ejemplar disponible de ese libro
            Ejemplar ej = db.ejemplarDao().getDisponibleByLibro(idLibro);
            if (ej == null) {
                runOnUiThread(() -> Toast.makeText(this, "No hay ejemplares disponibles", Toast.LENGTH_SHORT).show());
                return;
            }

            // 3) Crear préstamo
            long ahora = System.currentTimeMillis();
            long catorceDias = 14L * 24 * 60 * 60 * 1000;
//            long unDia = 24L * 60 * 60 * 1000;
//            prueba comprabar vencidos



            Prestamo p = new Prestamo();
            p.idUsuario = u.id;
            p.idEjemplar = ej.id;
            p.fechaPrestamo = ahora;
            p.fechaVencimiento = ahora + catorceDias;
//            p.fechaVencimiento = System.currentTimeMillis() - unDia;
//            prueba comprobar vencidos
            p.fechaDevolucion = null;
            p.estado = "ACTIVO";

            // 4) Transacción: insertar préstamo + marcar ejemplar PRESTADO
            db.runInTransaction(() -> {
                db.prestamoDao().insert(p);
                db.ejemplarDao().actualizarEstado(ej.id, "PRESTADO");
            });

            runOnUiThread(() -> {
                Toast.makeText(this,
                        "Préstamo creado para " + u.nombre + " (" + u.email + ")",
                        Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
