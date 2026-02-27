package com.DAM.bibliotecaapp.ui.prestamo;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.data.pojo.PrestamoInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrestamoInfoActivoAdapter extends RecyclerView.Adapter<PrestamoInfoActivoAdapter.VH> {

    private final List<PrestamoInfo> data = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public void setData(List<PrestamoInfo> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prestamo_activo, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PrestamoInfo p = data.get(position);
        if (p == null) return;

        // Línea 1: título
        String titulo = safe(p.titulo, "Libro");
        h.tvLinea1.setText(titulo);

        // Línea 2: autor + inventario + fechas
        String autor = safe(p.autor, "Autor desconocido");
        String inv = safe(p.codigoInventario, "—");
        String fPrestamo = formatDateSafe(p.fechaPrestamo);
        String fVence = formatDateSafe(p.fechaVencimiento);

        h.tvLinea2.setText(autor + "  ·  " + inv + "\nPrestado: " + fPrestamo + "  ·  Vence: " + fVence);

        // Chip estado
        setChip(h.tvEstadoChip, p.estado);
    }

    private void setChip(TextView chip, String estadoRaw) {
        if (chip == null) return;

        String estado = (estadoRaw == null) ? "" : estadoRaw.trim().toUpperCase(Locale.ROOT);
        if (estado.isEmpty()) {
            chip.setVisibility(View.GONE);
            return;
        }

        chip.setVisibility(View.VISIBLE);

        if (estado.contains("VENC")) {
            chip.setText("VENCIDO");
            chip.setTextColor(Color.parseColor("#B71C1C"));
            chip.setBackgroundResource(R.drawable.bg_chip_bad);
        } else if (estado.contains("ACT")) {
            chip.setText("ACTIVO");
            chip.setTextColor(Color.parseColor("#1B5E20"));
            chip.setBackgroundResource(R.drawable.bg_chip_ok);
        } else {
            chip.setText(estado);
            chip.setTextColor(Color.parseColor("#374151"));
            chip.setBackgroundResource(R.drawable.bg_chip_neutral);
        }
    }

    private String formatDateSafe(long millis) {
        try {
            if (millis <= 0) return "—";
            return sdf.format(new Date(millis));
        } catch (Exception e) {
            return "—";
        }
    }

    private String safe(String v, String fallback) {
        if (v == null) return fallback;
        v = v.trim();
        return v.isEmpty() ? fallback : v;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvLinea1, tvLinea2, tvEstadoChip;

        VH(@NonNull View itemView) {
            super(itemView);
            tvLinea1 = itemView.findViewById(R.id.tvLinea1);
            tvLinea2 = itemView.findViewById(R.id.tvLinea2);
            tvEstadoChip = itemView.findViewById(R.id.tvEstadoChip);
        }
    }
}