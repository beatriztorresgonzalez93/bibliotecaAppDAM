package com.DAM.bibliotecaapp.ui.prestamo;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.DAM.bibliotecaapp.data.entities.Multa;
import com.DAM.bibliotecaapp.data.entities.Usuario;
import com.DAM.bibliotecaapp.data.pojo.PrestamoGlobal;
import com.DAM.bibliotecaapp.data.pojo.PrestamoVencidoMini;
import com.DAM.bibliotecaapp.ui.base.BaseActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrestamoActivity extends BaseActivity {

    private static final double TARIFA_DIA = 0.50; // 50 céntimos por día
    private static final double TOPE = 20.0;       // máximo 20€

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private PrestamoGlobalAdapter adapter;

    private Spinner spFiltro;
    private String filtroActual = "Todos";

    // NUEVO: filtro usuario
    private AutoCompleteTextView actUsuarioFiltro;
    private MaterialButton btnLimpiarFiltro;
    private Integer selectedUsuarioId = null;

    // Helper para el autocompletar
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
        setContentView(R.layout.activity_prestamo);
        applySystemBarsPadding(R.id.main);

        db = AppDatabase.getInstance(this);

        RecyclerView rv = findViewById(R.id.rvPrestamos);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PrestamoGlobalAdapter(
                prestamo -> {
                    // Click fila (opcional)
                },
                prestamo -> {
                    // ampliar
                    mostrarDialogoAmpliar(prestamo.idPrestamo);
                },
                prestamo -> {
                    // devolver
                    mostrarDialogoDevolucion(prestamo.idPrestamo);
                }
        );
        rv.setAdapter(adapter);

        // Spinner
        spFiltro = findViewById(R.id.spFiltroPrestamos);
        ArrayAdapter<String> filtroAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Todos", "Activos", "Vencidos"}
        );
        filtroAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFiltro.setAdapter(filtroAdapter);

        spFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                filtroActual = parent.getItemAtPosition(position).toString();
                cargarPrestamos();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // NUEVO: Autocomplete usuario + limpiar
        actUsuarioFiltro = findViewById(R.id.actUsuarioFiltroPrestamos);
        btnLimpiarFiltro = findViewById(R.id.btnLimpiarFiltroPrestamos);

        setupUserAutocomplete();
        setupClearButton();

        cargarPrestamos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPrestamos();
    }

    private void setupUserAutocomplete() {
        executor.execute(() -> {
            List<Usuario> list = db.usuarioDao().getAllOrderByNombre();

            List<UserChoice> choices = new ArrayList<>();
            for (Usuario u : list) {
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
                        cargarPrestamos();
                    }
                });

                actUsuarioFiltro.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s == null || s.length() == 0) {
                            selectedUsuarioId = null;
                            cargarPrestamos();
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
            cargarPrestamos();
        });
    }

    private void cargarPrestamos() {
        executor.execute(() -> {
            long ahora = System.currentTimeMillis();

            // 1) marcar vencidos
            db.prestamoDao().marcarVencidos(ahora);

            // 2) crear/actualizar multas para vencidos
            actualizarMultas(ahora, TARIFA_DIA, TOPE);

            List<PrestamoGlobal> lista;
            if ("Activos".equals(filtroActual)) {
                if (selectedUsuarioId == null) lista = db.prestamoDao().getPrestamosSoloActivosGlobal();
                else lista = db.prestamoDao().getPrestamosSoloActivosGlobalFiltrado(selectedUsuarioId);

            } else if ("Vencidos".equals(filtroActual)) {
                if (selectedUsuarioId == null) lista = db.prestamoDao().getPrestamosVencidosGlobal();
                else lista = db.prestamoDao().getPrestamosVencidosGlobalFiltrado(selectedUsuarioId);

            } else {
                if (selectedUsuarioId == null) lista = db.prestamoDao().getPrestamosNoDevueltosGlobal();
                else lista = db.prestamoDao().getPrestamosNoDevueltosGlobalFiltrado(selectedUsuarioId);
            }

            runOnUiThread(() -> adapter.setData(lista));
        });
    }

    private void mostrarDialogoDevolucion(int idPrestamo) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar devolución")
                .setMessage("¿Quieres marcar este préstamo como devuelto?")
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .setPositiveButton("Devolver", (d, w) -> devolverPrestamo(idPrestamo))
                .show();
    }

    private void devolverPrestamo(int idPrestamo) {
        executor.execute(() -> {
            long ahora = System.currentTimeMillis();

            db.runInTransaction(() -> {
                int idEjemplar = db.prestamoDao().getIdEjemplarByPrestamo(idPrestamo);
                db.prestamoDao().marcarDevuelto(idPrestamo, ahora);
                db.ejemplarDao().actualizarEstado(idEjemplar, "DISPONIBLE");
            });

            runOnUiThread(() -> {
                Toast.makeText(this, "Devolución registrada", Toast.LENGTH_SHORT).show();
                cargarPrestamos();
            });
        });
    }

    private void mostrarDialogoAmpliar(int idPrestamo) {
        final String[] opciones = {"7 días", "14 días", "30 días"};
        final int[] dias = {7, 14, 30};

        new AlertDialog.Builder(this)
                .setTitle("Ampliar plazo")
                .setItems(opciones, (dialog, which) -> ampliarPlazo(idPrestamo, dias[which]))
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .show();
    }

    private void ampliarPlazo(int idPrestamo, int diasExtra) {
        executor.execute(() -> {
            long msExtra = diasExtra * 24L * 60 * 60 * 1000;
            int updated = db.prestamoDao().ampliarPlazo(idPrestamo, msExtra);

            runOnUiThread(() -> {
                if (updated > 0) {
                    Toast.makeText(this, "Plazo ampliado +" + diasExtra + " días", Toast.LENGTH_SHORT).show();
                    cargarPrestamos();
                } else {
                    Toast.makeText(this, "No se puede ampliar: préstamo vencido o ya devuelto", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void actualizarMultas(long ahora, double tarifaDia, double tope) {
        List<PrestamoVencidoMini> vencidos = db.prestamoDao().getVencidosMini();

        for (PrestamoVencidoMini p : vencidos) {
            long diff = ahora - p.fechaVencimiento;
            int dias = (int) Math.max(0, diff / (24L * 60 * 60 * 1000));
            double importe = Math.min(tope, dias * tarifaDia);

            if (db.multaDao().existePorPrestamo(p.idPrestamo) == 0) {
                Multa m = new Multa();
                m.idPrestamo = p.idPrestamo;
                m.idUsuario = p.idUsuario;
                m.fechaCreacion = ahora;
                m.fechaCierre = null;
                m.diasRetraso = dias;
                m.importe = importe;
                m.estado = "PENDIENTE";
                db.multaDao().insert(m);
            } else {
                db.multaDao().actualizarPendiente(p.idPrestamo, dias, importe);
            }
        }
    }

    // TextWatcher simple local
    public abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }
}