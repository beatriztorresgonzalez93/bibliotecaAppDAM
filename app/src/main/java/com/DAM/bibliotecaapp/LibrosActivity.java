package com.DAM.bibliotecaapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LibrosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libros);

        RecyclerView rv = findViewById(R.id.rvLibros);
        rv.setLayoutManager(new LinearLayoutManager(this));

        AppDatabase db = AppDatabase.getInstance(this);
        List<Libro> libros = db.libroDao().getAll();

        int idUsuario = getIntent().getIntExtra("ID_USUARIO", -1);

        LibroAdapter adapter = new LibroAdapter(db, libros, idUsuario);
        rv.setAdapter(adapter);



    }
}
