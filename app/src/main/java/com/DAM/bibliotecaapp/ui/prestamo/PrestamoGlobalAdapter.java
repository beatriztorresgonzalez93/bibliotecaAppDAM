package com.DAM.bibliotecaapp.ui.prestamo;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.data.pojo.PrestamoGlobal;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrestamoGlobalAdapter extends RecyclerView.Adapter<PrestamoGlobalAdapter.VH> {

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnItemClickListener { void onClick(PrestamoGlobal p); }
    public interface OnAmpliarClickListener { void onAmpliarClick(PrestamoGlobal p); }
    public interface OnDevolverClickListener { void onDevolverClick(PrestamoGlobal p); }

    private final List<PrestamoGlobal> data = new ArrayList<>();

    private final OnItemClickListener onItemClick;
    private final OnAmpliarClickListener onAmpliarClick;
    private final OnDevolverClickListener onDevolverClick;

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

        // Texto
        h.tvLinea1.setText(p.titulo + " · " + p.autor);
        h.tvLinea3.setText(p.nombreUsuario);
        h.tvLinea4.setText(p.emailUsuario);
        h.tvLinea5.setText(p.codigoInventario);

        String vence = sdf.format(new Date(p.fechaVencimiento));
        h.tvVence.setText("Vence: " + vence);

        // Estado
        String estado = (p.estado == null) ? "" : p.estado.trim().toUpperCase(Locale.ROOT);
        h.tvEstadoChip.setText(estado.isEmpty() ? "—" : estado);

        // Colores
        int colorEstado = ContextCompat.getColor(h.itemView.getContext(), R.color.estado_activo);

        if ("VENCIDO".equals(estado)) {
            colorEstado = ContextCompat.getColor(h.itemView.getContext(), R.color.estado_vencido);
        } else if ("ACTIVO".equals(estado)) {
            colorEstado = ContextCompat.getColor(h.itemView.getContext(), R.color.estado_activo);
        } else if ("DEVUELTO".equals(estado)) {
            // si no tienes color, pon uno neutro
            colorEstado = ContextCompat.getColor(h.itemView.getContext(), R.color.estado_devuelto);
        }

        // Barrita lateral
        h.viewEstado.setBackgroundColor(colorEstado);

        // Chip (background shape)
        GradientDrawable bg = (GradientDrawable) h.tvEstadoChip.getBackground().mutate();
        bg.setColor(colorEstado);

        // Ampliar: deshabilitado si vencido
        boolean esVencido = "VENCIDO".equals(estado);
        h.btnAmpliar.setEnabled(!esVencido);
        h.btnAmpliar.setAlpha(esVencido ? 0.45f : 1f);

        h.btnAmpliar.setOnClickListener(v -> {
            if (esVencido) return;
            if (onAmpliarClick != null) onAmpliarClick.onAmpliarClick(p);
        });

        h.btnDevolver.setOnClickListener(v -> {
            if (onDevolverClick != null) onDevolverClick.onDevolverClick(p);
        });

        h.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView tvLinea1, tvLinea3, tvLinea4, tvLinea5, tvVence, tvEstadoChip;
        MaterialButton btnAmpliar, btnDevolver;
        View viewEstado;

        VH(@NonNull View itemView) {
            super(itemView);
            tvLinea1 = itemView.findViewById(R.id.tvLinea1);
            tvLinea3 = itemView.findViewById(R.id.tvLinea3);
            tvLinea4 = itemView.findViewById(R.id.tvLinea4);
            tvLinea5 = itemView.findViewById(R.id.tvLinea5);
            tvVence = itemView.findViewById(R.id.tvVence);

            tvEstadoChip = itemView.findViewById(R.id.tvEstadoChip);
            viewEstado = itemView.findViewById(R.id.viewEstado);

            btnAmpliar = itemView.findViewById(R.id.btnAmpliar);
            btnDevolver = itemView.findViewById(R.id.btnDevolver);
        }
    }
}