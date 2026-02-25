package com.DAM.bibliotecaapp.ui.libro;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.RoleGuard;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.entities.Ejemplar;
import com.DAM.bibliotecaapp.data.entities.Libro;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NuevoLibroActivity extends AppCompatActivity {

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private EditText etTitulo, etAutor, etGenero, etEditorial, etIsbn, etNumEjemplares;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleGuard.requireBibliotecario(this);
        setContentView(R.layout.activity_nuevo_libro);

        db = AppDatabase.getInstance(this);

        etTitulo = findViewById(R.id.etTitulo);
        etAutor = findViewById(R.id.etAutor);
        etGenero = findViewById(R.id.etGenero);
        etEditorial = findViewById(R.id.etEditorial);
        etIsbn = findViewById(R.id.etIsbn);
        etNumEjemplares = findViewById(R.id.etNumEjemplares);

        Button btnGuardar = findViewById(R.id.btnGuardarLibro);
        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void guardar() {
        String titulo = etTitulo.getText().toString().trim();
        String autor = etAutor.getText().toString().trim();
        String genero = etGenero.getText().toString().trim();
        String editorial = etEditorial.getText().toString().trim();
        String isbn = etIsbn.getText().toString().trim();
        String numStr = etNumEjemplares.getText().toString().trim();

        if (titulo.isEmpty() || autor.isEmpty() || genero.isEmpty() || editorial.isEmpty() || isbn.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int n = 1; // por defecto 1 ejemplar
        if (!numStr.isEmpty()) {
            try {
                n = Integer.parseInt(numStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Número de ejemplares inválido", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (n <= 0) {
            Toast.makeText(this, "El número de ejemplares debe ser mayor que 0", Toast.LENGTH_SHORT).show();
            return;
        }

        final int numEjemplares = n;

        executor.execute(() -> {
            // 1) Validar ISBN único
            int existe = db.libroDao().countByIsbn(isbn);
            if (existe > 0) {
                runOnUiThread(() -> Toast.makeText(this, "Ya existe un libro con ese ISBN", Toast.LENGTH_LONG).show());
                return;
            }

            // 2) Crear libro + ejemplares en transacción
            db.runInTransaction(() -> {
                Libro l = new Libro();
                l.titulo = titulo;
                l.autor = autor;
                l.genero = genero;
                l.editorial = editorial;
                l.isbn = isbn;

                long idLibroLong = db.libroDao().insert(l);
                int idLibro = (int) idLibroLong;

                List<Ejemplar> ejemplares = new ArrayList<>();

                for (int i = 1; i <= numEjemplares; i++) {
                    Ejemplar e = new Ejemplar();
                    e.idLibro = idLibro;

                    // Código inventario: ISBN-001, ISBN-002...
                    e.codigoInventario = isbn + "-" + String.format(Locale.getDefault(), "%03d", i);

                    e.estado = "DISPONIBLE";
                    ejemplares.add(e);
                }

                db.ejemplarDao().insertAll(ejemplares);
            });

            runOnUiThread(() -> {
                Toast.makeText(this, "Libro y ejemplares creados", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
