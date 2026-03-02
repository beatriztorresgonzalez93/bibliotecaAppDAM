package com.DAM.bibliotecaapp.ui.usuario;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.data.entities.Usuario;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.VH> {

    public interface OnUsuarioClickListener {
        void onClick(Usuario u);
    }

    private final List<Usuario> data = new ArrayList<>();
    private final OnUsuarioClickListener listener;

    public UsuarioAdapter(OnUsuarioClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<Usuario> list) {
        data.clear();
        if (list != null) data.addAll(list);
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
    public void onBindViewHolder(@NonNull VH h, int position) {
        Usuario u = data.get(position);

        h.tvNombre.setText(u.nombre != null ? u.nombre : "");
        h.tvEmail.setText(u.email != null ? u.email : "");

        // Avatar (inicial)
        String inicial = "?";
        if (u.nombre != null && u.nombre.trim().length() > 0) {
            inicial = u.nombre.trim().substring(0, 1).toUpperCase(Locale.ROOT);
        }
        h.tvAvatar.setText(inicial);

        // Chip rol
        String rol = "LECTOR";
        try {
            if (u.rol != null) rol = u.rol.trim().toUpperCase(Locale.ROOT);
        } catch (Exception ignored) {}

        if ("BIBLIOTECARIO".equals(rol)) {
            h.tvRolChip.setText("BIBLIO");
            h.tvRolChip.setBackgroundResource(R.drawable.bg_chip_biblio);
            h.tvRolChip.setTextColor(ContextCompat.getColor(h.itemView.getContext(), android.R.color.white));
            // si quieres icono: usa los que ya tengas
            // h.tvRolChip.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_users_24, 0, 0, 0);
            // h.tvRolChip.setCompoundDrawableTintList(ContextCompat.getColorStateList(h.itemView.getContext(), android.R.color.white));
        } else {
            h.tvRolChip.setText("LECTOR");
            h.tvRolChip.setBackgroundResource(R.drawable.bg_chip_lector);
            h.tvRolChip.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.text_secondary));
            // h.tvRolChip.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_user_20, 0, 0, 0);
            // h.tvRolChip.setCompoundDrawableTintList(ContextCompat.getColorStateList(h.itemView.getContext(), R.color.text_secondary));
        }

        // ✅ Click seguro
        View.OnClickListener click = v -> {
            if (listener != null) listener.onClick(u);
        };

        if (h.cardUsuario != null) h.cardUsuario.setOnClickListener(click);
        else h.itemView.setOnClickListener(click);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        MaterialCardView cardUsuario;
        TextView tvAvatar, tvNombre, tvEmail, tvRolChip;

        VH(@NonNull View itemView) {
            super(itemView);
            cardUsuario = itemView.findViewById(R.id.cardUsuario);

            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRolChip = itemView.findViewById(R.id.tvRolChip);
        }
    }
}