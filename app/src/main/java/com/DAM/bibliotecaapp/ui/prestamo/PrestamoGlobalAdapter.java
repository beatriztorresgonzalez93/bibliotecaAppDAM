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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class PrestamoGlobalAdapter extends RecyclerView.Adapter<PrestamoGlobalAdapter.VH> {

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());


    // Click en la fila (opcional, por si luego quieres abrir detalle)
    public interface OnItemClickListener {
        void onClick(PrestamoGlobal p);
    }

    // Click en ampliar
    public interface OnAmpliarClickListener {
        void onAmpliarClick(PrestamoGlobal p);
    }

    // Click en devolver
    public interface OnDevolverClickListener {
        void onDevolverClick(PrestamoGlobal p);
    }

    private final List<PrestamoGlobal> data = new ArrayList<>();

    private final OnItemClickListener onItemClick;
    private final OnAmpliarClickListener onAmpliarClick;
    private final OnDevolverClickListener onDevolverClick;

    // ✅ Constructor ÚNICO (así no hay "might not have been initialized")
    public PrestamoGlobalAdapter(OnItemClickListener onItemClick,
                                 OnAmpliarClickListener onAmpliarClick,
                                 OnDevolverClickListener onDevolverClick) {
        this.onItemClick = onItemClick;
        this.onAmpliarClick = onAmpliarClick;
        this.onDevolverClick = onDevolverClick;
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

        String vence = sdf.format(new Date(p.fechaVencimiento));
        h.tvVence.setText("Vence: " + vence);


        h.tvLinea1.setText(p.titulo + " · " + p.autor);
        h.tvLinea3.setText(p.nombreUsuario);
        h.tvLinea4.setText(p.emailUsuario);
        h.tvLinea5.setText(p.codigoInventario);


        h.tvEstado.setText(p.estado);

        boolean esVencido = "VENCIDO".equals(p.estado);

        // Colores estado (si tienes los colores definidos)
        if (esVencido) {
            h.tvEstado.setTextColor(h.itemView.getResources().getColor(R.color.estado_vencido));
            h.container.setBackgroundColor(h.itemView.getResources().getColor(R.color.fondo_vencido));
        } else if ("ACTIVO".equals(p.estado)) {
            h.tvEstado.setTextColor(h.itemView.getResources().getColor(R.color.estado_activo));
            h.container.setBackgroundColor(h.itemView.getResources().getColor(android.R.color.transparent));
        } else {
            h.tvEstado.setTextColor(h.itemView.getResources().getColor(android.R.color.black));
            h.container.setBackgroundColor(h.itemView.getResources().getColor(android.R.color.transparent));
        }

        // ✅ Ampliar: deshabilitado si vencido
        h.btnAmpliar.setEnabled(!esVencido);
        h.btnAmpliar.setAlpha(esVencido ? 0.4f : 1f);

        h.btnAmpliar.setOnClickListener(v -> {
            if (esVencido) return;
            if (onAmpliarClick != null) onAmpliarClick.onAmpliarClick(p);
        });

        // ✅ Devolver: SIEMPRE visible
        h.btnDevolver.setOnClickListener(v -> {
            if (onDevolverClick != null) onDevolverClick.onDevolverClick(p);
        });

        // ✅ Click en la fila (opcional). No devuelve, solo lo que tú quieras.
        h.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView tvLinea1, tvEstado, tvVence;

        TextView tvLinea3, tvLinea4, tvLinea5;
        Button btnAmpliar;
        Button btnDevolver;
        View container;


        VH(@NonNull View itemView) {
            super(itemView);
            tvLinea1 = itemView.findViewById(R.id.tvLinea1);;
            tvLinea3 = itemView.findViewById(R.id.tvLinea3);
            tvLinea4 = itemView.findViewById(R.id.tvLinea4);
            tvLinea5 = itemView.findViewById(R.id.tvLinea5);
            tvEstado = itemView.findViewById(R.id.tvEstado);

            btnAmpliar = itemView.findViewById(R.id.btnAmpliar);
            btnDevolver = itemView.findViewById(R.id.btnDevolver);

            container = itemView.findViewById(R.id.container);
            tvVence = itemView.findViewById(R.id.tvVence);

        }
    }
}
