package com.DAM.bibliotecaapp.ui.libro;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.data.db.AppDatabase;

import com.DAM.bibliotecaapp.data.entities.Ejemplar;
import com.DAM.bibliotecaapp.data.entities.Libro;
import com.DAM.bibliotecaapp.ui.prestamo.NuevoPrestamoActivity;
import com.DAM.bibliotecaapp.R;

import java.util.ArrayList;
import java.util.List;

public class LibroAdapter extends RecyclerView.Adapter<LibroAdapter.LibroViewHolder> {

    private final AppDatabase db;
    private final List<Libro> libros = new ArrayList<>();
    private final int idUsuario;


    public LibroAdapter(AppDatabase db, List<Libro> inicial, int idUsuario) {
        this.db = db;
        this.idUsuario = idUsuario;
        if (inicial != null) libros.addAll(inicial);
    }


    @NonNull
    @Override
    public LibroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_libro, parent, false);
        return new LibroViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LibroViewHolder holder, int position) {
        Libro libro = libros.get(position);

        holder.tvTitulo.setText(libro.titulo);
        holder.tvAutor.setText("Autor: " + libro.autor);
        holder.tvIsbn.setText("ISBN: " + libro.isbn);
        holder.tvEditorial.setText("Editorial: " + (libro.editorial != null ? libro.editorial : ""));
        holder.tvGenero.setText("Género: " + (libro.genero != null ? libro.genero : ""));



        int total = db.ejemplarDao().countTotal(libro.id);
        int disp = db.ejemplarDao().countDisponibles(libro.id);
        int prestados = total - disp;
        holder.btnDevolver.setEnabled(prestados > 0);

        holder.tvDisponibles.setText("Disponibles: " + disp + " / " + total);

        holder.btnPrestar.setEnabled(disp > 0);

        // NUEVO FLUJO: abrir pantalla de nuevo préstamo
        holder.btnPrestar.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), NuevoPrestamoActivity.class);
            i.putExtra("idLibro", libro.id);
            i.putExtra("titulo", libro.titulo);
            v.getContext().startActivity(i);
        });

        holder.btnDevolver.setOnClickListener(view -> {

            Ejemplar ej = db.ejemplarDao().getPrimerPrestado(libro.id);

            if (ej == null) {
                Toast.makeText(view.getContext(), "No hay ejemplares prestados", Toast.LENGTH_SHORT).show();
                return;
            }

            int idPrestamo = db.prestamoDao().getPrestamoActivoByEjemplar(ej.id);

            long ahora = System.currentTimeMillis();

            db.runInTransaction(() -> {
                db.prestamoDao().marcarDevuelto(idPrestamo, ahora);
                db.ejemplarDao().actualizarEstado(ej.id, "DISPONIBLE");
            });

            Toast.makeText(view.getContext(), "Libro devuelto", Toast.LENGTH_SHORT).show();

            notifyDataSetChanged();
        });



    }

    @Override
    public int getItemCount() {
        return libros != null ? libros.size() : 0;
    }

    static class LibroViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitulo, tvAutor, tvIsbn, tvEditorial, tvGenero, tvDisponibles;
        Button btnPrestar; Button btnDevolver;

        public LibroViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvAutor = itemView.findViewById(R.id.tvAutor);
            tvIsbn = itemView.findViewById(R.id.tvIsbn);
            tvEditorial = itemView.findViewById(R.id.tvEditorial);
            tvGenero = itemView.findViewById(R.id.tvGenero);

            tvDisponibles = itemView.findViewById(R.id.tvDisponibles);
            btnPrestar = itemView.findViewById(R.id.btnPrestar);
            btnDevolver = itemView.findViewById(R.id.btnDevolver);
        }
    }

    public void setData(List<Libro> list) {
        libros.clear();
        if (list != null) libros.addAll(list);
        notifyDataSetChanged();
    }

}
