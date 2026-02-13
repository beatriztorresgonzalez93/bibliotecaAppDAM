package ui.libro;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import data.db.AppDatabase;

import data.entities.Libro;
import ui.prestamo.NuevoPrestamoActivity;
import com.DAM.bibliotecaapp.R;

import java.util.List;

public class LibroAdapter extends RecyclerView.Adapter<LibroAdapter.LibroViewHolder> {

    private final AppDatabase db;
    private final List<Libro> libros;

    public LibroAdapter(AppDatabase db, List<Libro> libros) {
        this.db = db;
        this.libros = libros;
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

        // NUEVO FLUJO: abrir pantalla de nuevo préstamo
        holder.btnPrestar.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), NuevoPrestamoActivity.class);
            i.putExtra("idLibro", libro.id);
            i.putExtra("titulo", libro.titulo);
            v.getContext().startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return libros != null ? libros.size() : 0;
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
