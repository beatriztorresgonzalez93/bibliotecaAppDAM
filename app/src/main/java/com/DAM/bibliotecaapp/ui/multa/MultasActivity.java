package com.DAM.bibliotecaapp.ui.multa;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.RoleGuard;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.entities.Usuario;
import com.DAM.bibliotecaapp.data.pojo.MultaInfo;
import com.DAM.bibliotecaapp.ui.base.BaseActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultasActivity extends BaseActivity {

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private MultaAdapter adapter;

    private Spinner spFiltro;
    private String filtroActual = "Todas";

    // filtro usuario
    private AutoCompleteTextView actUsuarioFiltro;
    private MaterialButton btnLimpiarFiltro;
    private Integer selectedUsuarioId = null;

    private MaterialTextView tvEmpty;

    static class UserChoice {
        final int id;
        final String label;
        UserChoice(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleGuard.requireBibliotecario(this);

        setContentView(R.layout.activity_multas);
        applySystemBarsPadding(R.id.main);

        db = AppDatabase.getInstance(this);


        tvEmpty = findViewById(R.id.tvEmpty);

        spFiltro = findViewById(R.id.spEstado);
        actUsuarioFiltro = findViewById(R.id.actvUsuario);
        btnLimpiarFiltro = findViewById(R.id.btnClearUser);


        RecyclerView rv = findViewById(R.id.rvMultas);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MultaAdapter(new MultaAdapter.OnMultaActionListener() {
            @Override
            public void onPagar(MultaInfo m) {
                mostrarDialogoPagar(m.id);
            }

            @Override
            public void onCondonar(MultaInfo m) {
                mostrarDialogoCondonar(m.id);
            }
        });
        rv.setAdapter(adapter);

        setupSpinnerFiltro();
        setupUserAutocomplete();
        setupClearButton();

        cargarMultas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarMultas();
    }

    private void setupSpinnerFiltro() {
        ArrayAdapter<String> filtroAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Todas", "Pendientes", "Pagadas", "Condonadas"}
        );
        filtroAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFiltro.setAdapter(filtroAdapter);

        spFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filtroActual = parent.getItemAtPosition(position).toString();
                cargarMultas();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupUserAutocomplete() {
        executor.execute(() -> {
            List<Usuario> list = db.usuarioDao().getAllOrderByNombre();

            List<UserChoice> choices = new ArrayList<>();
            for (Usuario u : list) {
                // Ajusta aquí si tu entidad usa otros nombres de campos
                choices.add(new UserChoice(u.id, u.nombre + " (" + u.email + ")"));
            }

            runOnUiThread(() -> {
                ArrayAdapter<UserChoice> aa = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        choices
                );
                actUsuarioFiltro.setAdapter(aa);
                actUsuarioFiltro.setThreshold(1);

                actUsuarioFiltro.setOnItemClickListener((parent, view, position, id) -> {
                    Object obj = parent.getItemAtPosition(position);
                    if (obj instanceof UserChoice) {
                        selectedUsuarioId = ((UserChoice) obj).id;
                        cargarMultas();
                    }
                });

                actUsuarioFiltro.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // si borran el texto, quitamos filtro
                        if (s == null || s.length() == 0) {
                            selectedUsuarioId = null;
                            cargarMultas();
                        }
                    }
                });
            });
        });
    }

    private void setupClearButton() {
        btnLimpiarFiltro.setOnClickListener(v -> {
            selectedUsuarioId = null;
            actUsuarioFiltro.setText("");
            cargarMultas();
        });
    }

    private void cargarMultas() {
        executor.execute(() -> {
            List<MultaInfo> lista;

            switch (filtroActual) {
                case "Pendientes":
                    lista = (selectedUsuarioId == null)
                            ? db.multaDao().getPendientesInfo()
                            : db.multaDao().getPendientesInfoFiltrado(selectedUsuarioId);
                    break;

                case "Pagadas":
                    lista = (selectedUsuarioId == null)
                            ? db.multaDao().getPagadasInfo()
                            : db.multaDao().getPagadasInfoFiltrado(selectedUsuarioId);
                    break;

                case "Condonadas":
                    lista = (selectedUsuarioId == null)
                            ? db.multaDao().getCondonadasInfo()
                            : db.multaDao().getCondonadasInfoFiltrado(selectedUsuarioId);
                    break;

                default:
                    lista = (selectedUsuarioId == null)
                            ? db.multaDao().getAllInfo()
                            : db.multaDao().getAllInfoFiltrado(selectedUsuarioId);
                    break;
            }

            runOnUiThread(() -> {
                adapter.setData(lista);
                boolean empty = (lista == null || lista.isEmpty());
                tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void mostrarDialogoPagar(int idMulta) {
        new AlertDialog.Builder(this)
                .setTitle("Pagar multa")
                .setMessage("¿Marcar esta multa como PAGADA?")
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .setPositiveButton("Pagar", (d, w) -> pagarMulta(idMulta))
                .show();
    }

    private void pagarMulta(int idMulta) {
        executor.execute(() -> {
            int ok = db.multaDao().pagar(idMulta, System.currentTimeMillis());
            runOnUiThread(() -> {
                Toast.makeText(this, ok > 0 ? "Multa pagada" : "No se pudo pagar", Toast.LENGTH_SHORT).show();
                cargarMultas();
            });
        });
    }

    private void mostrarDialogoCondonar(int idMulta) {
        new AlertDialog.Builder(this)
                .setTitle("Condonar multa")
                .setMessage("¿Marcar esta multa como CONDONADA?")
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .setPositiveButton("Condonar", (d, w) -> condonarMulta(idMulta))
                .show();
    }

    private void condonarMulta(int idMulta) {
        executor.execute(() -> {
            int ok = db.multaDao().condonar(idMulta, System.currentTimeMillis());
            runOnUiThread(() -> {
                Toast.makeText(this, ok > 0 ? "Multa condonada" : "No se pudo condonar", Toast.LENGTH_SHORT).show();
                cargarMultas();
            });
        });
    }

    public abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }
}