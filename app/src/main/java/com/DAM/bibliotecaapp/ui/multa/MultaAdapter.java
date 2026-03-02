package com.DAM.bibliotecaapp.ui.multa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.data.pojo.MultaInfo;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MultaAdapter extends RecyclerView.Adapter<MultaAdapter.VH> {

    public interface OnMultaActionListener {
        void onPagar(MultaInfo m);
        void onCondonar(MultaInfo m);
    }

    private final List<MultaInfo> data = new ArrayList<>();
    private final OnMultaActionListener listener;

    public MultaAdapter(OnMultaActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<MultaInfo> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_multa, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Context ctx = h.itemView.getContext();
        MultaInfo m = data.get(position);

        // Título
        h.tvUsuario.setText(safe(m.nombreUsuario));
        h.tvLibro.setText("📖 " + safe(m.tituloLibro));

        String estado = safe(m.estado).toUpperCase(Locale.ROOT);
        boolean pendiente = "PENDIENTE".equals(estado);

        // --- Estado chip (texto + fondo + color) ---
        h.chipEstado.setText(estado);
        switch (estado) {
            case "PENDIENTE":
                h.chipEstado.setBackgroundResource(R.drawable.chip_danger);
                h.chipEstado.setTextColor(ctx.getColor(R.color.chip_danger_text));
                break;
            case "PAGADA":
                h.chipEstado.setBackgroundResource(R.drawable.bg_chip_success);
                h.chipEstado.setTextColor(ctx.getColor(R.color.chip_success_text));
                break;
            case "CONDONADA":
                h.chipEstado.setBackgroundResource(R.drawable.bg_chip_neutral);
                h.chipEstado.setTextColor(ctx.getColor(R.color.chip_neutral_text));
                break;
            default:
                h.chipEstado.setBackgroundResource(R.drawable.bg_chip_neutral);
                h.chipEstado.setTextColor(ctx.getColor(R.color.chip_neutral_text));
                h.chipEstado.setText(estado.isEmpty() ? "—" : estado);
                break;
        }

        // --- Chip días ---
        int dias = m.diasRetraso;
        if (dias <= 0) {

            h.chipDias.setVisibility(View.GONE);


        } else {
            h.chipDias.setVisibility(View.VISIBLE);
            h.chipDias.setBackgroundResource(R.drawable.bg_chip_neutral);
            h.chipDias.setText("⏱ " + dias + (dias == 1 ? " día" : " días"));
        }

        // --- Chip importe ---
        double importe = m.importe;
        if (importe <= 0.00001) {

            h.chipImporte.setVisibility(View.GONE);


        } else {
            h.chipImporte.setVisibility(View.VISIBLE);
            h.chipImporte.setBackgroundResource(R.drawable.bg_chip_success);
            h.chipImporte.setText(String.format(Locale.getDefault(), "💶 %.2f €", importe));
        }


        h.btnPagar.setVisibility(pendiente ? View.VISIBLE : View.GONE);
        h.btnCondonar.setVisibility(pendiente ? View.VISIBLE : View.GONE);

        h.btnPagar.setOnClickListener(v -> {
            if (listener != null) listener.onPagar(m);
        });

        h.btnCondonar.setOnClickListener(v -> {
            if (listener != null) listener.onCondonar(m);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvUsuario, tvLibro, chipEstado, chipDias, chipImporte;
        MaterialButton btnPagar, btnCondonar;

        VH(@NonNull View itemView) {
            super(itemView);
            tvUsuario = itemView.findViewById(R.id.tvUsuario);
            tvLibro = itemView.findViewById(R.id.tvLibro);

            chipEstado = itemView.findViewById(R.id.chipEstado);
            chipDias = itemView.findViewById(R.id.chipDias);
            chipImporte = itemView.findViewById(R.id.chipImporte);

            btnPagar = itemView.findViewById(R.id.btnPagar);
            btnCondonar = itemView.findViewById(R.id.btnCondonar);
        }
    }
}