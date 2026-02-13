package com.DAM.bibliotecaapp.ui.prestamo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class PrestamoInfoAdapter extends RecyclerView.Adapter<PrestamoInfoAdapter.VH> {

    public interface OnDevolverClickListener {
        void onDevolverClick(PrestamoInfo p);
    }

    private final List<PrestamoInfo> data = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final OnDevolverClickListener listener;

    public PrestamoInfoAdapter(OnDevolverClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<PrestamoInfo> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prestamo_activo_devolver, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PrestamoInfo p = data.get(position);

        String fPrestamo = sdf.format(new Date(p.fechaPrestamo));
        String fVence = sdf.format(new Date(p.fechaVencimiento));

        h.tvLinea1.setText(p.titulo + " · " + p.autor);
        h.tvLinea2.setText("Ejemplar: " + p.codigoInventario + " · Prestado: " + fPrestamo + " · Vence: " + fVence);

        h.btnDevolver.setOnClickListener(v -> {
            if (listener != null) listener.onDevolverClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvLinea1, tvLinea2;
        Button btnDevolver;

        VH(@NonNull View itemView) {
            super(itemView);
            tvLinea1 = itemView.findViewById(R.id.tvLinea1);
            tvLinea2 = itemView.findViewById(R.id.tvLinea2);
            btnDevolver = itemView.findViewById(R.id.btnDevolver);
        }
    }
}
