package com.DAM.bibliotecaapp.ui.libro;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        // Título / autor
        holder.tvTitulo.setText(libro.titulo != null ? libro.titulo : "");
        holder.tvAutor.setText(libro.autor != null ? libro.autor : "");

        // Info compacta (ISBN + Editorial)
        String isbn = (libro.isbn != null) ? libro.isbn : "";
        String editorial = (libro.editorial != null) ? libro.editorial : "";
        String info = "ISBN: " + isbn;
        if (!editorial.trim().isEmpty()) info += " · Editorial: " + editorial;
        holder.tvInfo.setText(info);

        // Chips: género
        String genero = (libro.genero != null) ? libro.genero : "Sin género";
        holder.chipGenero.setText(genero);

        // Disponibilidad
        int total = db.ejemplarDao().countTotal(libro.id);
        int disp = db.ejemplarDao().countDisponibles(libro.id);

        // Chip disponibilidad (verde si hay, rojo si no)
        if (disp > 0) {
            holder.chipDisponibilidad.setText("Disponible · " + disp + "/" + total);
            holder.chipDisponibilidad.setBackgroundResource(R.drawable.bg_chip_success);
            holder.chipDisponibilidad.setTextColor(Color.parseColor("#065F46"));
        } else {
            holder.chipDisponibilidad.setText("No disponible · " + disp + "/" + total);
            holder.chipDisponibilidad.setBackgroundResource(R.drawable.bg_chip_danger);
            holder.chipDisponibilidad.setTextColor(Color.parseColor("#991B1B"));
        }

        // ===== CONTROL DE ROLES =====
        if (!isAdmin) {
            holder.btnPrestar.setVisibility(View.GONE);
            holder.btnBorrar.setVisibility(View.GONE);

            holder.btnPrestar.setOnClickListener(null);
            holder.btnBorrar.setOnClickListener(null);
            return;
        }

        // ADMIN
        holder.btnPrestar.setVisibility(View.VISIBLE);
        holder.btnBorrar.setVisibility(View.VISIBLE);

        holder.btnPrestar.setEnabled(disp > 0);

        holder.btnPrestar.setOnClickListener(v -> {
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

        TextView tvTitulo, tvAutor, tvInfo;
        TextView chipGenero, chipDisponibilidad;
        View btnPrestar, btnBorrar;

        public LibroViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvAutor = itemView.findViewById(R.id.tvAutor);
            tvInfo = itemView.findViewById(R.id.tvInfo);

            chipGenero = itemView.findViewById(R.id.chipGenero);
            chipDisponibilidad = itemView.findViewById(R.id.chipDisponibilidad);

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