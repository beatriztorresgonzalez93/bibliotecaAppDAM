package com.DAM.bibliotecaapp.ui.prestamo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.data.pojo.PrestamoGlobal;

import java.util.ArrayList;
import java.util.List;

public class PrestamoGlobalAdapter extends RecyclerView.Adapter<PrestamoGlobalAdapter.VH> {

    public interface OnItemClickListener {
        void onClick(PrestamoGlobal p); // devolver (o acción principal)
    }

    public interface OnAmpliarClickListener {
        void onAmpliarClick(PrestamoGlobal p);
    }

    private final List<PrestamoGlobal> data = new ArrayList<>();

    private final OnItemClickListener onItemClick;
    private final OnAmpliarClickListener onAmpliarClick;

    public PrestamoGlobalAdapter(OnItemClickListener onItemClick,
                                 OnAmpliarClickListener onAmpliarClick) {
        this.onItemClick = onItemClick;
        this.onAmpliarClick = onAmpliarClick;
    }

    public void setData(List<PrestamoGlobal> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prestamo_global, parent, false);
        return new VH(v);


    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PrestamoGlobal p = data.get(position);

        // Ajusta esto a cómo lo muestras tú
        h.tvLinea1.setText(p.titulo + " · " + p.autor);
        h.tvLinea2.setText(p.nombreUsuario + " (" + p.emailUsuario + ") · Ejemplar: " + p.codigoInventario);

        h.tvEstado.setText(p.estado);

        boolean esVencido = "VENCIDO".equals(p.estado);


        h.btnAmpliar.setEnabled(!esVencido);
        h.btnAmpliar.setAlpha(esVencido ? 0.4f : 1f);

        if (esVencido) {
            h.container.setBackgroundColor(
                    h.itemView.getResources().getColor(R.color.fondo_vencido)
            );
        } else {
            h.container.setBackgroundColor(
                    h.itemView.getResources().getColor(android.R.color.transparent)
            );
        }


// (opcional) si prefieres ocultarlo en vez de desactivarlo:
// h.btnAmpliar.setVisibility(esVencido ? View.GONE : View.VISIBLE);


        if ("VENCIDO".equals(p.estado)) {
            h.tvEstado.setTextColor(h.itemView.getResources().getColor(R.color.estado_vencido));
        } else if ("ACTIVO".equals(p.estado)) {
            h.tvEstado.setTextColor(h.itemView.getResources().getColor(R.color.estado_activo));
        } else {
            h.tvEstado.setTextColor(h.itemView.getResources().getColor(android.R.color.black));
        }

        // Click en fila (devolver)
        h.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(p);
        });

        // Botón ampliar
        h.btnAmpliar.setOnClickListener(v -> {
            if ("VENCIDO".equals(p.estado)) return; // seguridad extra
            if (onAmpliarClick != null) onAmpliarClick.onAmpliarClick(p);
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvLinea1, tvLinea2, tvEstado;
        Button btnAmpliar;
        View container;


        VH(@NonNull View itemView) {
            super(itemView);
            tvLinea1 = itemView.findViewById(R.id.tvLinea1);
            tvLinea2 = itemView.findViewById(R.id.tvLinea2);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            btnAmpliar = itemView.findViewById(R.id.btnAmpliar);
            container = itemView.findViewById(R.id.container);

        }
    }
}
