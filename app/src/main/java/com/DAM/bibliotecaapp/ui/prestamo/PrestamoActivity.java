package com.DAM.bibliotecaapp.ui.prestamo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
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
import com.google.android.material.textfield.MaterialAutoCompleteTextView;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrestamoActivity extends BaseActivity {

    private static final double TARIFA_DIA = 0.50; // 50 céntimos por día
    private static final double TOPE = 20.0;       // máximo 20€

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private PrestamoGlobalAdapter prestamoAdapter;


    private MaterialAutoCompleteTextView actEstado;
    private String filtroActual = "Todos";


    private MaterialAutoCompleteTextView actUsuarioFiltro;
    private MaterialButton btnLimpiarFiltro;
    private Integer selectedUsuarioId = null;


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

        prestamoAdapter = new PrestamoGlobalAdapter(
                prestamo -> {
                    // Click fila (opcional)
                },
                prestamo -> mostrarDialogoAmpliar(prestamo.idPrestamo),
                prestamo -> mostrarDialogoDevolucion(prestamo.idPrestamo)
        );
        rv.setAdapter(prestamoAdapter);

        // ✅ Dropdown estado tipo "Libros"
        actEstado = findViewById(R.id.actEstado);
        setupEstadoDropdown();

        // ✅ Autocomplete usuario + limpiar
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

    private void setupEstadoDropdown() {
        final String[] opciones = new String[]{"Todos", "Activos", "Vencidos"};

        ArrayAdapter<String> estadoAdapter = new ArrayAdapter<>(
                this,
                R.layout.item_dropdown_material,
                opciones
        );
        estadoAdapter.setDropDownViewResource(R.layout.item_dropdown_material);

        actEstado.setAdapter(estadoAdapter);
        actEstado.setDropDownBackgroundDrawable(
                ContextCompat.getDrawable(this, R.drawable.bg_dropdown_menu)
        );

        actEstado.setText("Todos", false);
        filtroActual = "Todos";

        actEstado.setOnItemClickListener((parent, view, position, id) -> {
            filtroActual = opciones[position];
            cargarPrestamos();
        });
    }

    private void setupUserAutocomplete() {
        executor.execute(() -> {
            List<Usuario> list = db.usuarioDao().getAllOrderByNombre();

            List<UserChoice> choices = new ArrayList<>();
            for (Usuario u : list) {
                String label = u.nombre + " (" + u.email + ")";
                choices.add(new UserChoice(u.id, label));
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
                lista = (selectedUsuarioId == null)
                        ? db.prestamoDao().getPrestamosSoloActivosGlobal()
                        : db.prestamoDao().getPrestamosSoloActivosGlobalFiltrado(selectedUsuarioId);

            } else if ("Vencidos".equals(filtroActual)) {
                lista = (selectedUsuarioId == null)
                        ? db.prestamoDao().getPrestamosVencidosGlobal()
                        : db.prestamoDao().getPrestamosVencidosGlobalFiltrado(selectedUsuarioId);

            } else { // Todos
                lista = (selectedUsuarioId == null)
                        ? db.prestamoDao().getPrestamosNoDevueltosGlobal()
                        : db.prestamoDao().getPrestamosNoDevueltosGlobalFiltrado(selectedUsuarioId);
            }

            runOnUiThread(() -> prestamoAdapter.setData(lista));
        });
    }

    private void mostrarDialogoDevolucion(int idPrestamo) {
        executor.execute(() -> {
            Multa multaPendiente = db.multaDao().getPendienteByPrestamo(idPrestamo);

            runOnUiThread(() -> {
                if (multaPendiente != null) {
                    String importeTexto = String.format(Locale.getDefault(), "%.2f", multaPendiente.importe);

                    new AlertDialog.Builder(this)
                            .setTitle("Préstamo vencido")
                            .setMessage("Este préstamo tiene una multa pendiente de " + importeTexto + " €.\n\n¿Deseas pagar la multa y registrar la devolución?")
                            .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                            .setPositiveButton("Pagar y devolver", (d, w) -> pagarMultaYDevolver(idPrestamo, multaPendiente.id))
                            .show();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("Confirmar devolución")
                            .setMessage("¿Quieres marcar este préstamo como devuelto?")
                            .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                            .setPositiveButton("Devolver", (d, w) -> devolverPrestamo(idPrestamo))
                            .show();
                }
            });
        });
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


    public abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }

    private void pagarMultaYDevolver(int idPrestamo, int idMulta) {
        executor.execute(() -> {
            long ahora = System.currentTimeMillis();

            try {
                db.runInTransaction(() -> {
                    int idEjemplar = db.prestamoDao().getIdEjemplarByPrestamo(idPrestamo);

                    db.multaDao().pagar(idMulta, ahora);
                    db.prestamoDao().marcarDevuelto(idPrestamo, ahora);
                    db.ejemplarDao().actualizarEstado(idEjemplar, "DISPONIBLE");
                });

                runOnUiThread(() -> {
                    Toast.makeText(this, "Multa pagada y devolución registrada", Toast.LENGTH_SHORT).show();
                    cargarPrestamos();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al pagar la multa y devolver el préstamo", Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}