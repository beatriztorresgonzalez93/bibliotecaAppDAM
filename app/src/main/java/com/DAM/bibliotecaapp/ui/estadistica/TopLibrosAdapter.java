package com.DAM.bibliotecaapp.ui.estadistica;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.data.pojo.TopLibro;

import java.util.List;

public class TopLibrosAdapter extends RecyclerView.Adapter<TopLibrosAdapter.VH> {

    private final List<TopLibro> items;

    public TopLibrosAdapter(List<TopLibro> items) {
        this.items = items;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_top, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        TopLibro x = items.get(pos);
        h.tvTitle.setText((pos + 1) + ". " + x.titulo);
        h.tvSub.setText(x.autor);
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