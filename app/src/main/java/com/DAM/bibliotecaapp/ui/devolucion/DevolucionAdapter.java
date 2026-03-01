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
import java.util.Locale;

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
        h.tvChipEstado.setText("DEVUELTO"); // historial de devoluciones => siempre devuelto

        h.tvCodigoInventario.setText("🏷 Ejemplar: " + codigo);

        h.tvUsuarioNombre.setText("👤 " + usuario);
        h.tvUsuarioEmail.setText("📧 " + email);

        // Fechas
        String fechaDev = formatDate(it.fechaDevolucion);
        String fechaPres = formatDate(it.fechaPrestamo);

        h.tvFechaDevolucion.setText("✅ Devuelto: " + fechaDev);
        h.tvFechaPrestamo.setText("📦 Prestado: " + fechaPres);

        // Duración (opcional PRO)
        long dias = calcularDias(it.fechaPrestamo, it.fechaDevolucion);
        if (dias > 0) {
            h.tvDuracion.setVisibility(View.VISIBLE);
            h.tvDuracion.setText(String.format(Locale.getDefault(), "⏱ Duración: %d %s", dias, (dias == 1 ? "día" : "días")));
        } else {
            h.tvDuracion.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloAutor, tvChipEstado, tvCodigoInventario;
        TextView tvUsuarioNombre, tvUsuarioEmail;
        TextView tvFechaDevolucion, tvFechaPrestamo, tvDuracion;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloAutor = itemView.findViewById(R.id.tvTituloAutor);
            tvChipEstado = itemView.findViewById(R.id.chipEstado);

            tvCodigoInventario = itemView.findViewById(R.id.tvCodigoInventario);

            tvUsuarioNombre = itemView.findViewById(R.id.tvUsuarioNombre);
            tvUsuarioEmail = itemView.findViewById(R.id.tvUsuarioEmail);

            tvFechaDevolucion = itemView.findViewById(R.id.tvFechaDevolucion);
            tvFechaPrestamo = itemView.findViewById(R.id.tvFechaPrestamo);

            tvDuracion = itemView.findViewById(R.id.tvDuracion);
        }
    }

    private static String safe(String s) {
        return (s == null) ? "" : s.trim();
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

    private static long calcularDias(long desde, long hasta) {
        try {
            if (desde <= 0 || hasta <= 0 || hasta < desde) return 0;
            long diff = hasta - desde;
            return diff / (1000L * 60L * 60L * 24L);
        } catch (Exception e) {
            return 0;
        }
    }
}