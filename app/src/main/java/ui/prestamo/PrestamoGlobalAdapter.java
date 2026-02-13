package ui.prestamo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrestamoGlobalAdapter extends RecyclerView.Adapter<PrestamoGlobalAdapter.VH> {

    private final List<PrestamoGlobal> data = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

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

        String fVence = sdf.format(new Date(p.fechaVencimiento));

        h.tvLinea1.setText(p.titulo + " · " + p.autor);
        h.tvLinea2.setText("Usuario: " + p.nombreUsuario + " (" + p.emailUsuario + ")"
                + " · Ejemplar: " + p.codigoInventario
                + " · Vence: " + fVence
                + " · " + p.estado);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvLinea1, tvLinea2;
        VH(@NonNull View itemView) {
            super(itemView);
            tvLinea1 = itemView.findViewById(R.id.tvLinea1);
            tvLinea2 = itemView.findViewById(R.id.tvLinea2);
        }
    }
}
