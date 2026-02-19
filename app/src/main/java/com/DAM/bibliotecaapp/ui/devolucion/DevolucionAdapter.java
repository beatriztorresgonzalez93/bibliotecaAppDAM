package com.DAM.bibliotecaapp.ui.devoluciones;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.data.dto.DevolucionItem;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DevolucionAdapter extends RecyclerView.Adapter<DevolucionAdapter.ViewHolder> {

    private final List<DevolucionItem> items = new ArrayList<>();

    public DevolucionAdapter(List<DevolucionItem> initialItems) {
        if (initialItems != null) items.addAll(initialItems);
    }

    public void setItems(List<DevolucionItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_devolucion, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        DevolucionItem it = items.get(position);

        String titulo = safe(it.titulo);
        String autor = safe(it.autor);
        String codigo = safe(it.codigoInventario);
        String usuario = safe(it.usuarioNombre);
        String email = safe(it.usuarioEmail);

        h.tvTituloAutor.setText(titulo + " · " + autor);
        h.tvCodigoInventario.setText("Ejemplar: " + codigo);
        h.tvUsuario.setText("Usuario: " + usuario + " (" + email + ")");

        // Fecha devolución (epoch millis -> texto)
        String fechaDev = formatDate(it.fechaDevolucion);
        h.tvFechaDevolucion.setText("Devuelto: " + fechaDev);

        // Extra (opcional): fecha préstamo
        String fechaPres = formatDate(it.fechaPrestamo);
        h.tvFechaPrestamo.setText("Prestado: " + fechaPres);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloAutor, tvCodigoInventario, tvUsuario, tvFechaDevolucion, tvFechaPrestamo;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloAutor = itemView.findViewById(R.id.tvTituloAutor);
            tvCodigoInventario = itemView.findViewById(R.id.tvCodigoInventario);
            tvUsuario = itemView.findViewById(R.id.tvUsuario);
            tvFechaDevolucion = itemView.findViewById(R.id.tvFechaDevolucion);
            tvFechaPrestamo = itemView.findViewById(R.id.tvFechaPrestamo);
        }
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }

    private static String formatDate(long epochMillis) {
        try {
            if (epochMillis <= 0) return "-";
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
            return df.format(new Date(epochMillis));
        } catch (Exception e) {
            return "-";
        }
    }
}
