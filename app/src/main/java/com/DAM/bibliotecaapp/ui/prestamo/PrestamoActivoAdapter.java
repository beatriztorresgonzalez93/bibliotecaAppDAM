package com.DAM.bibliotecaapp.ui.prestamo;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.data.entities.Prestamo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrestamoActivoAdapter extends RecyclerView.Adapter<PrestamoActivoAdapter.VH> {

    private final List<Prestamo> data = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public void setData(List<Prestamo> list) {
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
        Prestamo p = data.get(position);
        if (p == null) return;

        // 1) Título: Ejemplar
        h.tvLinea1.setText("Ejemplar #" + p.idEjemplar);

        // 2) Fechas seguras
        String fPrestamo = formatDateSafe(p.fechaPrestamo);
        String fVence = formatDateSafe(p.fechaVencimiento);
        h.tvLinea2.setText("Prestado: " + fPrestamo + " · Vence: " + fVence);

        // 3) Chip estado bonito
        if (h.tvEstadoChip != null) {
            String estado = (p.estado == null) ? "" : p.estado.trim().toUpperCase(Locale.ROOT);

            if (!estado.isEmpty()) {
                h.tvEstadoChip.setVisibility(View.VISIBLE);

                if (estado.contains("VENC")) { // VENCIDO
                    h.tvEstadoChip.setText("VENCIDO");
                    h.tvEstadoChip.setTextColor(Color.parseColor("#B71C1C"));
                    h.tvEstadoChip.setBackgroundResource(R.drawable.bg_chip_bad);

                } else if (estado.contains("ACT")) { // ACTIVO
                    h.tvEstadoChip.setText("ACTIVO");
                    h.tvEstadoChip.setTextColor(Color.parseColor("#1B5E20"));
                    h.tvEstadoChip.setBackgroundResource(R.drawable.bg_chip_ok);

                } else {
                    // Cualquier otro estado (por si acaso)
                    h.tvEstadoChip.setText(estado);
                    h.tvEstadoChip.setTextColor(Color.parseColor("#374151"));
                    h.tvEstadoChip.setBackgroundResource(R.drawable.bg_chip_neutral);
                }
            } else {
                h.tvEstadoChip.setVisibility(View.GONE);
            }
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