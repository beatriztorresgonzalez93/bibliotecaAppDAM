package com.DAM.bibliotecaapp.ui.multa;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.data.pojo.MultaGlobal;

import java.util.ArrayList;
import java.util.List;

public class MultaGlobalAdapter extends RecyclerView.Adapter<MultaGlobalAdapter.VH> {

    public interface OnPagarClickListener { void onPagar(MultaGlobal m); }
    public interface OnCondonarClickListener { void onCondonar(MultaGlobal m); }

    private final List<MultaGlobal> data = new ArrayList<>();
    private final OnPagarClickListener onPagar;
    private final OnCondonarClickListener onCondonar;

    public MultaGlobalAdapter(OnPagarClickListener onPagar, OnCondonarClickListener onCondonar) {
        this.onPagar = onPagar;
        this.onCondonar = onCondonar;
    }

    public void setData(List<MultaGlobal> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_global_multa, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MultaGlobal m = data.get(position);

        h.tvLinea1.setText(m.nombreUsuario + " (" + m.emailUsuario + ")");
        h.tvLinea2.setText("Días retraso: " + m.diasRetraso + " · Importe: " +
                String.format(java.util.Locale.getDefault(), "%.2f", m.importe) + " €");

        h.tvEstado.setText(m.estado);

        boolean pendiente = "PENDIENTE".equals(m.estado);

        h.btnPagar.setEnabled(pendiente);
        h.btnCondonar.setEnabled(pendiente);

        h.btnPagar.setAlpha(pendiente ? 1f : 0.4f);
        h.btnCondonar.setAlpha(pendiente ? 1f : 0.4f);

        if ("PENDIENTE".equals(m.estado)) {
            h.tvEstado.setTextColor(Color.RED);
        } else {
            h.tvEstado.setTextColor(Color.DKGRAY);
        }

        h.btnPagar.setOnClickListener(v -> { if (pendiente && onPagar != null) onPagar.onPagar(m); });
        h.btnCondonar.setOnClickListener(v -> { if (pendiente && onCondonar != null) onCondonar.onCondonar(m); });
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        View container;
        TextView tvLinea1, tvLinea2, tvEstado;
        Button btnPagar, btnCondonar;

        VH(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            tvLinea1 = itemView.findViewById(R.id.tvLinea1);
            tvLinea2 = itemView.findViewById(R.id.tvLinea2);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            btnPagar = itemView.findViewById(R.id.btnPagar);
            btnCondonar = itemView.findViewById(R.id.btnCondonar);
        }
    }
}
