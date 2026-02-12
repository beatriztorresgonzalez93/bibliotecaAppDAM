package com.DAM.bibliotecaapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.VH> {

    public interface OnUsuarioClickListener {
        void onClick(Usuario u);
    }

    private List<Usuario> data = new ArrayList<>();  // ← ESTA ES LA VARIABLE QUE FALTABA
    private final OnUsuarioClickListener listener;

    public UsuarioAdapter(OnUsuarioClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<Usuario> list) {
        data.clear();
        if (list != null) {
            data.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Usuario u = data.get(position);

        holder.tvNombre.setText(u.nombre);
        holder.tvEmail.setText(u.email);

        holder.itemView.setOnClickListener(v -> listener.onClick(u));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView tvNombre;
        TextView tvEmail;

        public VH(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvEmail = itemView.findViewById(R.id.tvEmail);
        }
    }
}
