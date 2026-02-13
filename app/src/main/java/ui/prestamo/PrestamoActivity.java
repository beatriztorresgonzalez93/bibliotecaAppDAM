package ui.prestamo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import data.db.AppDatabase;
import com.DAM.bibliotecaapp.R;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrestamoActivity extends AppCompatActivity {

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private PrestamoGlobalAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prestamo);

        db = AppDatabase.getInstance(this);

        RecyclerView rv = findViewById(R.id.rvPrestamos);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PrestamoGlobalAdapter();
        rv.setAdapter(adapter);

        cargarPrestamos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPrestamos();
    }

    private void cargarPrestamos() {
        executor.execute(() -> {
            List<PrestamoGlobal> lista = db.prestamoDao().getPrestamosActivosGlobal();
            runOnUiThread(() -> adapter.setData(lista));
        });
    }
}
