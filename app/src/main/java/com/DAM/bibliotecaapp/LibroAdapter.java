package com.DAM.bibliotecaapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LibroAdapter extends RecyclerView.Adapter<LibroAdapter.LibroViewHolder> {

    private final AppDatabase db;
    private final List<Libro> libros;
    private final int idUsuario;

    public LibroAdapter(AppDatabase db, List<Libro> libros, int idUsuario) {
        this.db = db;
        this.libros = libros;
        this.idUsuario = idUsuario;
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

        int total = db.ejemplarDao().countTotal(libro.id);
        int disp = db.ejemplarDao().countDisponibles(libro.id);
        holder.tvDisponibles.setText("Disponibles: " + disp + " / " + total);

        holder.btnPrestar.setEnabled(disp > 0);

        holder.btnPrestar.setOnClickListener(view -> {
            // 0) Seguridad: si no tenemos usuario logueado
            if (idUsuario == -1) {
                Toast.makeText(view.getContext(), "Error: usuario no válido", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1) Buscar un ejemplar disponible
            Ejemplar ej = db.ejemplarDao().getPrimerDisponible(libro.id);
            if (ej == null) {
                Toast.makeText(view.getContext(), "No hay ejemplares disponibles", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2) Crear préstamo
            Prestamo p = new Prestamo();
            p.idUsuario = idUsuario;
            p.idEjemplar = ej.id;
            p.fechaPrestamo = System.currentTimeMillis();

            long catorceDias = 14L * 24 * 60 * 60 * 1000;
            p.fechaVencimiento = p.fechaPrestamo + catorceDias;

            p.fechaDevolucion = null;
            p.estado = "ACTIVO";

            db.prestamoDao().insert(p);

            // 3) Marcar ejemplar como PRESTADO
            db.ejemplarDao().actualizarEstado(ej.id, "PRESTADO");

            Toast.makeText(view.getContext(), "Préstamo realizado", Toast.LENGTH_SHORT).show();

            // 4) Refrescar la lista completa (así se actualizan los disponibles seguro)
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return libros.size();
    }

    static class LibroViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvAutor, tvIsbn, tvDisponibles;
        Button btnPrestar;

        public LibroViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvAutor = itemView.findViewById(R.id.tvAutor);
            tvIsbn = itemView.findViewById(R.id.tvIsbn);
            tvDisponibles = itemView.findViewById(R.id.tvDisponibles);
            btnPrestar = itemView.findViewById(R.id.btnPrestar);
        }
    }
}
