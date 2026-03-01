package com.DAM.bibliotecaapp.ui.devolucion;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.RoleGuard;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.dto.DevolucionItem;
import com.DAM.bibliotecaapp.data.entities.Usuario;
import com.DAM.bibliotecaapp.ui.base.BaseActivity;
import com.DAM.bibliotecaapp.ui.devoluciones.DevolucionAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class DevolucionesActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private DevolucionAdapter adapter;

    private AutoCompleteTextView actUsuario;
    private TextInputEditText etDesde, etHasta;
    private MaterialButton btnLimpiar;

    private Integer selectedUsuarioId = null;
    private Long desdeMillis = null;
    private Long hastaMillis = null;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleGuard.requireBibliotecario(this);

        setContentView(R.layout.activity_devoluciones);
        applySystemBarsPadding(R.id.main);

        db = AppDatabase.getInstance(this);

        recyclerView = findViewById(R.id.recyclerViewDevoluciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DevolucionAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        actUsuario = findViewById(R.id.actUsuario);
        etDesde = findViewById(R.id.etDesde);
        etHasta = findViewById(R.id.etHasta);
        btnLimpiar = findViewById(R.id.btnLimpiarFiltros);

        setupUserAutocomplete();
        setupDatePickers();
        setupClearButton();

        cargarDatosFiltrados();
    }

    private void setupUserAutocomplete() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Usuario> list = db.usuarioDao().getAllOrderByNombre();

            List<UserChoice> choices = new ArrayList<>();
            for (Usuario u : list) {
                String label = u.nombre + " (" + u.email + ")";
                choices.add(new UserChoice(u.id, label));
            }

            runOnUiThread(() -> {
                android.widget.ArrayAdapter<UserChoice> aa =
                        new android.widget.ArrayAdapter<>(
                                this,
                                android.R.layout.simple_dropdown_item_1line,
                                choices
                        );

                actUsuario.setAdapter(aa);
                actUsuario.setThreshold(1);

                actUsuario.setOnItemClickListener((parent, view, position, id) -> {
                    Object obj = parent.getItemAtPosition(position);
                    if (obj instanceof UserChoice) {
                        selectedUsuarioId = ((UserChoice) obj).id;
                        cargarDatosFiltrados();
                    }
                });

                actUsuario.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s == null || s.length() == 0) {
                            selectedUsuarioId = null;
                            cargarDatosFiltrados();
                        }
                    }
                });
            });
        });
    }

    private void setupDatePickers() {
        etDesde.setOnClickListener(v -> pickDate(true));
        etHasta.setOnClickListener(v -> pickDate(false));
    }

    private void setupClearButton() {
        btnLimpiar.setOnClickListener(v -> {
            selectedUsuarioId = null;
            desdeMillis = null;
            hastaMillis = null;

            actUsuario.setText("");
            etDesde.setText("");
            etHasta.setText("");

            cargarDatosFiltrados();
        });
    }

    private void pickDate(boolean isDesde) {
        Calendar c = Calendar.getInstance();

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.set(year, month, dayOfMonth, 0, 0, 0);
            chosen.set(Calendar.MILLISECOND, 0);

            if (!isDesde) {
                chosen.set(Calendar.HOUR_OF_DAY, 23);
                chosen.set(Calendar.MINUTE, 59);
                chosen.set(Calendar.SECOND, 59);
                chosen.set(Calendar.MILLISECOND, 999);
            }

            long millis = chosen.getTimeInMillis();
            String text = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                    dayOfMonth, month + 1, year);

            if (isDesde) {
                desdeMillis = millis;
                etDesde.setText(text);
            } else {
                hastaMillis = millis;
                etHasta.setText(text);
            }

            cargarDatosFiltrados();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void cargarDatosFiltrados() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<DevolucionItem> lista =
                    db.prestamoDao().getDevolucionesFiltradas(selectedUsuarioId, desdeMillis, hastaMillis);

            runOnUiThread(() -> adapter.setItems(lista));
        });
    }

    public abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }

    static class UserChoice {
        final int id;
        final String label;

        UserChoice(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}