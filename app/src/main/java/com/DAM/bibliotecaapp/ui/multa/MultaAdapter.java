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
import com.DAM.bibliotecaapp.data.pojo.MultaInfo;

import java.util.ArrayList;
import java.util.List;

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
        MultaInfo m = data.get(position);

        h.tvLinea1.setText(m.nombreUsuario);
        h.tvLinea2.setText(m.tituloLibro + " · " + m.diasRetraso + " días · " + m.importe + " €");
        h.tvEstado.setText(m.estado);

        boolean pendiente = "PENDIENTE".equals(m.estado);

        // Estilo simple por estado
        if (pendiente) {
            h.tvEstado.setTextColor(Color.parseColor("#C62828")); // rojo
        } else if ("PAGADA".equals(m.estado)) {
            h.tvEstado.setTextColor(Color.parseColor("#2E7D32")); // verde
        } else {
            h.tvEstado.setTextColor(Color.parseColor("#616161")); // gris
        }

        // Botones solo si pendiente
        h.btnPagar.setEnabled(pendiente);
        h.btnCondonar.setEnabled(pendiente);

        h.btnPagar.setAlpha(pendiente ? 1f : 0.4f);
        h.btnCondonar.setAlpha(pendiente ? 1f : 0.4f);

        h.btnPagar.setOnClickListener(v -> {
            if (pendiente && listener != null) listener.onPagar(m);
        });

        h.btnCondonar.setOnClickListener(v -> {
            if (pendiente && listener != null) listener.onCondonar(m);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvLinea1, tvLinea2, tvEstado;
        Button btnPagar, btnCondonar;

        VH(@NonNull View itemView) {
            super(itemView);
            tvLinea1 = itemView.findViewById(R.id.tvLinea1);
            tvLinea2 = itemView.findViewById(R.id.tvLinea2);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            btnPagar = itemView.findViewById(R.id.btnPagar);
            btnCondonar = itemView.findViewById(R.id.btnCondonar);
        }
    }
}
