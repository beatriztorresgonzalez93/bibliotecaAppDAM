package com.DAM.bibliotecaapp.ui.estadistica;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.data.pojo.TopUsuario;

import java.util.List;

public class TopUsuariosAdapter extends RecyclerView.Adapter<TopUsuariosAdapter.VH> {

    private final List<TopUsuario> items;

    public TopUsuariosAdapter(List<TopUsuario> items) {
        this.items = items;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_top, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        TopUsuario x = items.get(pos);
        h.tvTitle.setText((pos + 1) + ". " + x.nombre);
        h.tvSub.setText(x.email);
        h.tvValue.setText(String.valueOf(x.totalPrestamos));
    }

    @Override
    public int getItemCount() { return items == null ? 0 : items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSub, tvValue;
        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSub = itemView.findViewById(R.id.tvSub);
            tvValue = itemView.findViewById(R.id.tvValue);
        }
    }
}