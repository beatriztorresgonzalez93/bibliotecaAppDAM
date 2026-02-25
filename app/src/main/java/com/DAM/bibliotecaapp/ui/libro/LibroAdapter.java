package com.DAM.bibliotecaapp.ui.libro;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.SessionManager;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.entities.Libro;
import com.DAM.bibliotecaapp.ui.prestamo.NuevoPrestamoActivity;

import java.util.ArrayList;
import java.util.List;

public class LibroAdapter extends RecyclerView.Adapter<LibroAdapter.LibroViewHolder> {

    private final AppDatabase db;
    private final List<Libro> libros = new ArrayList<>();
    private final boolean isAdmin;

    public LibroAdapter(AppDatabase db, List<Libro> inicial, boolean isAdmin) {
        this.db = db;
        this.isAdmin = isAdmin;
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
        holder.tvDisponibles.setText("Disponibles: " + disp + " / " + total);

        // ===== CONTROL DE ROLES =====
        if (!isAdmin) {
            // LECTOR: no puede prestar ni borrar (ni verlos)
            holder.btnPrestar.setVisibility(View.GONE);
            holder.btnBorrar.setVisibility(View.GONE);

            // evita “clicks reciclados” del RecyclerView
            holder.btnPrestar.setOnClickListener(null);
            holder.btnBorrar.setOnClickListener(null);
            return;
        }

        // ADMIN
        holder.btnPrestar.setVisibility(View.VISIBLE);
        holder.btnBorrar.setVisibility(View.VISIBLE);

        holder.btnPrestar.setEnabled(disp > 0);

        holder.btnPrestar.setOnClickListener(v -> {
            // seguridad extra por si acaso
            if (!new SessionManager(v.getContext()).isBibliotecario()) return;

            Intent i = new Intent(v.getContext(), NuevoPrestamoActivity.class);
            i.putExtra("idLibro", libro.id);
            i.putExtra("titulo", libro.titulo);
            v.getContext().startActivity(i);
        });

        holder.btnBorrar.setOnClickListener(v -> {
            if (!new SessionManager(v.getContext()).isBibliotecario()) return;

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Borrar libro")
                    .setMessage("¿Seguro que quieres borrar este libro?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Borrar", (d, w) -> {
                        if (deleteListener != null) deleteListener.onDelete(libro);
                    })
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return libros.size();
    }

    static class LibroViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitulo, tvAutor, tvIsbn, tvEditorial, tvGenero, tvDisponibles;
        Button btnPrestar, btnBorrar;

        public LibroViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvAutor = itemView.findViewById(R.id.tvAutor);
            tvIsbn = itemView.findViewById(R.id.tvIsbn);
            tvEditorial = itemView.findViewById(R.id.tvEditorial);
            tvGenero = itemView.findViewById(R.id.tvGenero);
            tvDisponibles = itemView.findViewById(R.id.tvDisponibles);
            btnPrestar = itemView.findViewById(R.id.btnPrestar);
            btnBorrar = itemView.findViewById(R.id.btnBorrar);
        }
    }

    public void setData(List<Libro> list) {
        libros.clear();
        if (list != null) libros.addAll(list);
        notifyDataSetChanged();
    }

    public interface OnLibroDeleteListener {
        void onDelete(Libro libro);
    }

    private OnLibroDeleteListener deleteListener;

    public void setOnLibroDeleteListener(OnLibroDeleteListener listener) {
        this.deleteListener = listener;
    }
}