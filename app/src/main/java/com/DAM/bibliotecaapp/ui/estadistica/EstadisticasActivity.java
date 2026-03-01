package com.DAM.bibliotecaapp.ui.estadistica;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.DAM.bibliotecaapp.R;
import com.DAM.bibliotecaapp.RoleGuard;
import com.DAM.bibliotecaapp.data.db.AppDatabase;
import com.DAM.bibliotecaapp.data.pojo.EstadoConteo;
import com.DAM.bibliotecaapp.data.pojo.GeneroConteo;
import com.DAM.bibliotecaapp.data.pojo.MesConteo;
import com.DAM.bibliotecaapp.data.pojo.MesImporte;
import com.DAM.bibliotecaapp.data.pojo.StatsResumen;
import com.DAM.bibliotecaapp.data.pojo.TopLibro;
import com.DAM.bibliotecaapp.data.pojo.TopUsuario;
import com.DAM.bibliotecaapp.ui.base.BaseActivity;
import android.widget.AutoCompleteTextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class EstadisticasActivity extends BaseActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private TextView tvUsuarios, tvLibros, tvEjemplares;
    private TextView tvActivos, tvVencidos, tvDevueltos;
    private TextView tvMultasPend, tvMultasPag, tvMultasCond;
    private TextView tvRecaudado;

    private RecyclerView rvTopLibros, rvTopUsuarios;

    private LineChart chartPrestamosMes;
    private BarChart chartMultasMes;
    private PieChart chartMultasEstado;

    private TextView tvPendiente, tvPctATiempo;
    private TextView tvDisponibles, tvPrestadosAhora;
    private RecyclerView rvTopLibrosMultas;
    private android.widget.ProgressBar pbPrestados;
    private TextView tvPctPrestados;
    private TextView tvEstadoDisponibilidad;

    private com.github.mikephil.charting.charts.HorizontalBarChart chartPrestamosGenero;
    private TextView tvPrestamosGeneroTitulo;

    // ✅ Dropdown bonito (AutoCompleteTextView)
    private AutoCompleteTextView spinnerYear;
    private String selectedYearStr = null; // null = TODOS

    // Comparación vs año anterior
    private TextView tvComparacionPrestamos;
    private TextView tvComparacionRecaudado;

    // ===== Datos actuales para exportar PDF =====
    private StatsResumen pdfResumen;
    private double pdfDineroPendiente;
    private double pdfPctATiempo;
    private int pdfDisponibles;
    private int pdfPrestadosAhora;
    private List<TopLibro> pdfTopLibros;
    private List<TopUsuario> pdfTopUsuarios;
    private List<TopLibro> pdfTopLibrosMultas;
    private String pdfYearLabel; // "TODOS" o "2023"

    private ActivityResultLauncher<String> createPdfLauncher;

    // Medidas A4 aproximadas (PdfDocument)
    private static final int PDF_W = 595;
    private static final int PDF_H = 842;

    // Márgenes
    private static final int PDF_MARGIN_X = 50;
    private static final int PDF_MARGIN_TOP = 70;
    private static final int PDF_MARGIN_BOTTOM = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoleGuard.requireBibliotecario(this);

        setContentView(R.layout.activity_estadisticas);
        applySystemBarsPadding(R.id.main);

        tvUsuarios = findViewById(R.id.tvUsuarios);
        tvLibros = findViewById(R.id.tvLibros);
        tvEjemplares = findViewById(R.id.tvEjemplares);

        tvActivos = findViewById(R.id.tvPrestamosActivos);
        tvVencidos = findViewById(R.id.tvPrestamosVencidos);
        tvDevueltos = findViewById(R.id.tvPrestamosDevueltos);

        tvMultasPend = findViewById(R.id.tvMultasPendientes);
        tvMultasPag = findViewById(R.id.tvMultasPagadas);
        tvMultasCond = findViewById(R.id.tvMultasCondonadas);

        tvRecaudado = findViewById(R.id.tvRecaudado);

        rvTopLibros = findViewById(R.id.rvTopLibros);
        rvTopUsuarios = findViewById(R.id.rvTopUsuarios);

        chartPrestamosMes = findViewById(R.id.chartPrestamosMes);
        chartMultasMes = findViewById(R.id.chartMultasMes);
        chartMultasEstado = findViewById(R.id.chartMultasEstado);

        tvPendiente = findViewById(R.id.tvPendiente);
        tvPctATiempo = findViewById(R.id.tvPctATiempo);

        tvDisponibles = findViewById(R.id.tvDisponibles);
        tvPrestadosAhora = findViewById(R.id.tvPrestadosAhora);

        pbPrestados = findViewById(R.id.pbPrestados);
        tvPctPrestados = findViewById(R.id.tvPctPrestados);

        tvEstadoDisponibilidad = findViewById(R.id.tvEstadoDisponibilidad);

        chartPrestamosGenero = findViewById(R.id.chartPrestamosGenero);
        tvPrestamosGeneroTitulo = findViewById(R.id.tvPrestamosGeneroTitulo);

        rvTopLibrosMultas = findViewById(R.id.rvTopLibrosMultas);
        rvTopLibrosMultas.setLayoutManager(new LinearLayoutManager(this));

        rvTopLibros.setLayoutManager(new LinearLayoutManager(this));
        rvTopUsuarios.setLayoutManager(new LinearLayoutManager(this));

        // ✅ id spinnerYear ahora es AutoCompleteTextView en XML
        spinnerYear = findViewById(R.id.spinnerYear);

        tvComparacionPrestamos = findViewById(R.id.tvComparacionPrestamos);
        tvComparacionRecaudado = findViewById(R.id.tvComparacionRecaudado);

        // PDF launcher
        createPdfLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/pdf"),
                uri -> {
                    if (uri == null) return;
                    exportarPdf(uri);
                }
        );

        Button btnExportPdf = findViewById(R.id.btnExportPdf);
        btnExportPdf.setOnClickListener(v -> {
            if (pdfResumen == null) {
                Toast.makeText(this, "Espera a que carguen los datos", Toast.LENGTH_SHORT).show();
                return;
            }
            String nombre = "estadisticas_" + pdfYearLabel + "_" +
                    new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date()) + ".pdf";
            createPdfLauncher.launch(nombre);
        });

        setupYearDropdownDesdeBD();
    }

    // ✅ Dropdown moderno con AutoCompleteTextView
    private void setupYearDropdownDesdeBD() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        executor.execute(() -> {
            List<String> yearsDb = db.estadisticasDao().getYearsDisponibles();

            List<String> years = new ArrayList<>();
            years.add("TODOS");
            if (yearsDb != null) years.addAll(yearsDb);

            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        years
                );
                spinnerYear.setAdapter(adapter);

                // Evita escritura manual (solo selección)
                spinnerYear.setKeyListener(null);

                spinnerYear.setOnItemClickListener((parent, view, position, id) -> {
                    String value = years.get(position);
                    selectedYearStr = value.equals("TODOS") ? null : value;
                    cargarDatos();
                });

                // Selección inicial
                spinnerYear.setText("TODOS", false);
                selectedYearStr = null;
                cargarDatos();
            });
        });
    }

    private void cargarDatos() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        executor.execute(() -> {

            int totalPrestamosThis = 0;
            int totalPrestamosPrev = 0;
            double recaudadoThis = 0.0;
            double recaudadoPrev = 0.0;
            int prevYearInt = 0;

            StatsResumen r;
            List<TopLibro> topLibros;
            List<TopUsuario> topUsuarios;

            List<MesConteo> prestamosMesRaw;
            List<MesImporte> multasMesRaw;

            List<EstadoConteo> multasEstado;

            double dineroPendiente;
            int totalDevueltos;
            int devueltosSinMulta;

            List<GeneroConteo> porGenero;
            List<TopLibro> topLibrosMultas;

            int disponibles = db.estadisticasDao().getEjemplaresDisponibles();
            int prestadosAhora = db.estadisticasDao().getEjemplaresPrestadosAhora();

            long desde = desdeUltimos12Meses();

            if (selectedYearStr == null) {
                r = db.estadisticasDao().getResumen();
                topLibros = db.estadisticasDao().getTopLibros(5);
                topUsuarios = db.estadisticasDao().getTopUsuarios(5);

                prestamosMesRaw = db.estadisticasDao().getPrestamosPorMesDesde(desde);
                multasMesRaw = db.estadisticasDao().getImporteMultasPorMesDesde(desde);

                multasEstado = db.estadisticasDao().getMultasPorEstado();

                dineroPendiente = db.estadisticasDao().getDineroPendiente();
                totalDevueltos = db.estadisticasDao().getTotalDevueltos();
                devueltosSinMulta = db.estadisticasDao().getDevueltosSinMulta();

                porGenero = db.estadisticasDao().getPrestamosPorGenero();
                topLibrosMultas = db.estadisticasDao().getTopLibrosConMultas(5);

                List<MesConteo> prestamosMes = rellenar12MesesConteoVentanaMovil(prestamosMesRaw);
                List<MesImporte> multasMes = rellenar12MesesImporteVentanaMovil(multasMesRaw);

                pdfResumen = r;
                pdfDineroPendiente = dineroPendiente;
                pdfDisponibles = disponibles;
                pdfPrestadosAhora = prestadosAhora;
                pdfTopLibros = topLibros;
                pdfTopUsuarios = topUsuarios;
                pdfTopLibrosMultas = topLibrosMultas;
                pdfYearLabel = "TODOS";
                pdfPctATiempo = (totalDevueltos == 0) ? 0 : (devueltosSinMulta * 100.0 / totalDevueltos);

                runOnUiThread(() -> pintarUI(r, topLibros, topUsuarios, topLibrosMultas,
                        prestamosMes, multasMes, multasEstado, porGenero,
                        dineroPendiente, totalDevueltos, devueltosSinMulta,
                        disponibles, prestadosAhora,
                        0, 0, 0.0, 0.0, 0, true));
                return;

            } else {
                String y = selectedYearStr;

                r = db.estadisticasDao().getResumenPorYear(y);
                topLibros = db.estadisticasDao().getTopLibrosPorYear(y, 5);
                topUsuarios = db.estadisticasDao().getTopUsuariosPorYear(y, 5);

                long desdeYear = inicioDeYear(y);
                long hastaYear = inicioDeYearSiguiente(y);

                prestamosMesRaw = db.estadisticasDao().getPrestamosPorMesEntre(desdeYear, hastaYear);
                multasMesRaw = db.estadisticasDao().getImporteMultasPorMesEntre(desdeYear, hastaYear);

                multasEstado = db.estadisticasDao().getMultasPorEstadoPorYear(y);

                dineroPendiente = db.estadisticasDao().getDineroPendientePorYear(y);
                totalDevueltos = db.estadisticasDao().getTotalDevueltosPorYear(y);
                devueltosSinMulta = db.estadisticasDao().getDevueltosSinMultaPorYear(y);

                porGenero = db.estadisticasDao().getPrestamosPorGeneroPorYear(y);
                topLibrosMultas = db.estadisticasDao().getTopLibrosConMultasPorYear(y, 5);

                totalPrestamosThis = db.estadisticasDao().getTotalPrestamosYear(y);
                recaudadoThis = db.estadisticasDao().getRecaudadoYear(y);

                prevYearInt = Integer.parseInt(y) - 1;
                String prev = String.valueOf(prevYearInt);

                totalPrestamosPrev = db.estadisticasDao().getTotalPrestamosYear(prev);
                recaudadoPrev = db.estadisticasDao().getRecaudadoYear(prev);

                List<MesConteo> prestamosMes = rellenar12MesesConteoDelYear(y, prestamosMesRaw);
                List<MesImporte> multasMes = rellenar12MesesImporteDelYear(y, multasMesRaw);

                pdfResumen = r;
                pdfDineroPendiente = dineroPendiente;
                pdfDisponibles = disponibles;
                pdfPrestadosAhora = prestadosAhora;
                pdfTopLibros = topLibros;
                pdfTopUsuarios = topUsuarios;
                pdfTopLibrosMultas = topLibrosMultas;
                pdfYearLabel = y;
                pdfPctATiempo = (totalDevueltos == 0) ? 0 : (devueltosSinMulta * 100.0 / totalDevueltos);

                final int fTotalPrestamosThis = totalPrestamosThis;
                final int fTotalPrestamosPrev = totalPrestamosPrev;
                final double fRecaudadoThis = recaudadoThis;
                final double fRecaudadoPrev = recaudadoPrev;
                final int fPrevYearInt = prevYearInt;

                runOnUiThread(() -> pintarUI(r, topLibros, topUsuarios, topLibrosMultas,
                        prestamosMes, multasMes, multasEstado, porGenero,
                        dineroPendiente, totalDevueltos, devueltosSinMulta,
                        disponibles, prestadosAhora,
                        fTotalPrestamosThis, fTotalPrestamosPrev, fRecaudadoThis, fRecaudadoPrev, fPrevYearInt, false));
            }
        });
    }

    private void pintarUI(
            StatsResumen r,
            List<TopLibro> topLibros,
            List<TopUsuario> topUsuarios,
            List<TopLibro> topLibrosMultas,
            List<MesConteo> prestamosMes,
            List<MesImporte> multasMes,
            List<EstadoConteo> multasEstado,
            List<GeneroConteo> porGenero,
            double dineroPendiente,
            int totalDevueltos,
            int devueltosSinMulta,
            int disponibles,
            int prestadosAhora,
            int totalPrestamosThis,
            int totalPrestamosPrev,
            double recaudadoThis,
            double recaudadoPrev,
            int prevYearInt,
            boolean esTodos
    ) {
        tvUsuarios.setText(String.valueOf(r.totalUsuarios));
        tvLibros.setText(String.valueOf(r.totalLibros));
        tvEjemplares.setText(String.valueOf(r.totalEjemplares));

        tvActivos.setText(String.valueOf(r.prestamosActivos));
        tvVencidos.setText(String.valueOf(r.prestamosVencidos));
        tvDevueltos.setText(String.valueOf(r.prestamosDevueltos));

        tvMultasPend.setText(String.valueOf(r.multasPendientes));
        tvMultasPag.setText(String.valueOf(r.multasPagadas));
        tvMultasCond.setText(String.valueOf(r.multasCondonadas));

        tvRecaudado.setText(String.format(Locale.getDefault(), "%.2f €", r.dineroRecaudado));
        tvPendiente.setText(String.format(Locale.getDefault(), "%.2f €", dineroPendiente));

        double pctATiempo = (totalDevueltos == 0) ? 0 : (devueltosSinMulta * 100.0 / totalDevueltos);
        tvPctATiempo.setText(String.format(Locale.getDefault(), "%.0f %%", pctATiempo));

        tvDisponibles.setText(String.valueOf(disponibles));
        tvPrestadosAhora.setText(String.valueOf(prestadosAhora));
        int total = disponibles + prestadosAhora;
        int pctPrestados = (total == 0) ? 0 : (int) Math.round(prestadosAhora * 100.0 / total);

        pbPrestados.setMax(100);
        pbPrestados.setProgress(pctPrestados);
        tvPctPrestados.setText(pctPrestados + "% prestados");

        if (pctPrestados < 50) {
            pbPrestados.setProgressDrawable(getResources().getDrawable(R.drawable.progress_green));
            tvEstadoDisponibilidad.setText("Estado: NORMAL");
            tvEstadoDisponibilidad.setTextColor(Color.parseColor("#388E3C"));
        } else if (pctPrestados < 80) {
            pbPrestados.setProgressDrawable(getResources().getDrawable(R.drawable.progress_orange));
            tvEstadoDisponibilidad.setText("Estado: ALTO USO");
            tvEstadoDisponibilidad.setTextColor(Color.parseColor("#F57C00"));
        } else {
            pbPrestados.setProgressDrawable(getResources().getDrawable(R.drawable.progress_red));
            tvEstadoDisponibilidad.setText("Estado: SATURADO");
            tvEstadoDisponibilidad.setTextColor(Color.parseColor("#D32F2F"));
        }

        tvMultasPend.setTextColor(Color.parseColor("#D32F2F"));
        tvMultasPag.setTextColor(Color.parseColor("#388E3C"));
        tvMultasCond.setTextColor(Color.parseColor("#1976D2"));
        tvPendiente.setTextColor(Color.parseColor("#D32F2F"));

        rvTopLibros.setAdapter(new TopLibrosAdapter(topLibros));
        rvTopUsuarios.setAdapter(new TopUsuariosAdapter(topUsuarios));
        rvTopLibrosMultas.setAdapter(new TopLibrosAdapter(topLibrosMultas));

        pintarLineaPrestamos(prestamosMes);
        pintarBarrasMultas(multasMes);
        pintarTartaMultasEstado(multasEstado);
        pintarBarrasPrestamosGeneroHorizontal(porGenero);


        if (esTodos) {
            tvComparacionPrestamos.setText("Comparación: —");
            tvComparacionPrestamos.setTextColor(Color.parseColor("#666666"));

            tvComparacionRecaudado.setText("Comparación: —");
            tvComparacionRecaudado.setTextColor(Color.parseColor("#666666"));
        } else {
            ComparacionText cPrest = buildComparacion("Préstamos", totalPrestamosThis, totalPrestamosPrev, prevYearInt);
            tvComparacionPrestamos.setText(cPrest.text);
            tvComparacionPrestamos.setTextColor(cPrest.color);

            ComparacionText cRec = buildComparacionEuros("Recaudado", recaudadoThis, recaudadoPrev, prevYearInt);
            tvComparacionRecaudado.setText(cRec.text);
            tvComparacionRecaudado.setTextColor(cRec.color);
        }
    }

    private static class ComparacionText {
        String text;
        int color;
        ComparacionText(String t, int c) { text = t; color = c; }
    }

    private ComparacionText buildComparacion(String label, int actual, int prev, int prevYear) {
        if (prev <= 0) {
            return new ComparacionText(
                    label + ": " + actual + " (sin datos en " + prevYear + ")",
                    Color.parseColor("#666666")
            );
        }

        double pct = (actual - prev) * 100.0 / prev;
        boolean up = pct >= 0;
        String arrow = up ? "↑" : "↓";
        int color = up ? Color.parseColor("#388E3C") : Color.parseColor("#D32F2F");

        String text = String.format(
                Locale.getDefault(),
                "%s: %d  %s %.1f%% vs %d",
                label, actual, arrow, Math.abs(pct), prevYear
        );
        return new ComparacionText(text, color);
    }

    private ComparacionText buildComparacionEuros(String label, double actual, double prev, int prevYear) {
        if (prev <= 0.0) {
            String text = String.format(
                    Locale.getDefault(),
                    "%s: %.2f € (sin datos en %d)",
                    label, actual, prevYear
            );
            return new ComparacionText(text, Color.parseColor("#666666"));
        }

        double pct = (actual - prev) * 100.0 / prev;
        boolean up = pct >= 0;
        String arrow = up ? "↑" : "↓";
        int color = up ? Color.parseColor("#388E3C") : Color.parseColor("#D32F2F");

        String text = String.format(
                Locale.getDefault(),
                "%s: %.2f €  %s %.1f%% vs %d",
                label, actual, arrow, Math.abs(pct), prevYear
        );
        return new ComparacionText(text, color);
    }

    // ---------------------------
    // Charts
    // ---------------------------
    private void pintarLineaPrestamos(List<MesConteo> data12) {
        if (chartPrestamosMes == null) return;

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < data12.size(); i++) {
            MesConteo m = data12.get(i);
            entries.add(new Entry(i, m.total));
            labels.add(formatearMesYYYYMM(m.mes));
        }

        LineDataSet set = new LineDataSet(entries, "Préstamos por mes");
        set.setLineWidth(2f);
        set.setCircleRadius(3f);
        set.setDrawCircleHole(false);
        set.setDrawValues(false);

        LineData lineData = new LineData(set);
        chartPrestamosMes.setData(lineData);

        XAxis x = chartPrestamosMes.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setValueFormatter(new IndexAxisValueFormatter(labels));
        x.setLabelRotationAngle(-45);
        x.setLabelCount(12, true);
        x.setAvoidFirstLastClipping(true);
        x.setGranularityEnabled(true);
        x.setTextSize(8f);

        chartPrestamosMes.getDescription().setEnabled(false);
        chartPrestamosMes.getAxisRight().setEnabled(false);
        chartPrestamosMes.getLegend().setDrawInside(false);
        chartPrestamosMes.setExtraBottomOffset(25f);
        chartPrestamosMes.invalidate();
    }

    private void pintarBarrasMultas(List<MesImporte> data12) {
        if (chartMultasMes == null) return;

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < data12.size(); i++) {
            MesImporte m = data12.get(i);
            entries.add(new BarEntry(i, (float) m.importe));
            labels.add(formatearMesYYYYMM(m.mes));
        }

        BarDataSet set = new BarDataSet(entries, "Importe multas por mes (€)");
        set.setDrawValues(false);

        BarData barData = new BarData(set);
        barData.setBarWidth(0.9f);

        chartMultasMes.setData(barData);
        chartMultasMes.setFitBars(true);

        XAxis x = chartMultasMes.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setValueFormatter(new IndexAxisValueFormatter(labels));
        x.setLabelRotationAngle(-45);
        x.setAxisMinimum(0f);
        x.setAxisMaximum(labels.size() - 1f);
        x.setLabelCount(12, true);
        x.setAvoidFirstLastClipping(true);
        x.setGranularityEnabled(true);
        x.setTextSize(8f);

        chartMultasMes.getDescription().setEnabled(false);
        chartMultasMes.getAxisRight().setEnabled(false);
        chartMultasMes.getLegend().setDrawInside(false);
        chartMultasMes.setExtraBottomOffset(25f);
        chartMultasMes.invalidate();
    }

    private void pintarTartaMultasEstado(List<EstadoConteo> data) {
        if (chartMultasEstado == null) return;

        ArrayList<PieEntry> entries = new ArrayList<>();
        if (data != null) {
            for (EstadoConteo e : data) {
                entries.add(new PieEntry(e.total, e.estado));
            }
        }

        PieDataSet set = new PieDataSet(entries, "Multas por estado");
        set.setColors(
                Color.parseColor("#EF9A9A"),
                Color.parseColor("#A5D6A7"),
                Color.parseColor("#64B5F6")
        );
        set.setValueTextSize(12f);
        set.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(set);
        chartMultasEstado.setData(pieData);

        chartMultasEstado.setEntryLabelColor(Color.BLACK);
        chartMultasEstado.setEntryLabelTextSize(13f);

        chartMultasEstado.getDescription().setEnabled(false);
        chartMultasEstado.setDrawEntryLabels(true);
        chartMultasEstado.setUsePercentValues(false);

        chartMultasEstado.getLegend().setDrawInside(false);
        chartMultasEstado.setExtraBottomOffset(10f);

        chartMultasEstado.invalidate();
    }

    private void pintarBarrasPrestamosGeneroHorizontal(List<GeneroConteo> data) {
        if (chartPrestamosGenero == null) return;

        chartPrestamosGenero.clear();
        chartPrestamosGenero.setNoDataText("Sin datos");

        if (data == null || data.isEmpty()) {
            chartPrestamosGenero.invalidate();
            if (tvPrestamosGeneroTitulo != null) {
                tvPrestamosGeneroTitulo.setText("Géneros\n0 préstamos");
            }
            return;
        }

        final int TOP = 6;

        // ===== 1) Ordenar desc + total =====
        List<GeneroConteo> sorted = new ArrayList<>();
        for (GeneroConteo g : data) if (g != null) sorted.add(g);
        Collections.sort(sorted, (a, b) -> Integer.compare(b.total, a.total));

        int total = 0;
        for (GeneroConteo g : sorted) total += g.total;

        // ===== 2) TOP + Otros =====
        List<String> labels = new ArrayList<>();
        List<com.github.mikephil.charting.data.BarEntry> entries = new ArrayList<>();

        int otros = 0;
        int idx = 0;

        for (int i = 0; i < sorted.size(); i++) {
            GeneroConteo g = sorted.get(i);
            if (i < TOP) {
                labels.add(safeLabelMultiLine(g.genero));
                entries.add(new com.github.mikephil.charting.data.BarEntry(idx, g.total));
                idx++;
            } else {
                otros += g.total;
            }
        }

        if (otros > 0) {
            labels.add("Otros");
            entries.add(new com.github.mikephil.charting.data.BarEntry(idx, otros));
        }

        // ===== 3) Paleta TFG (verde salvia) =====
        // - 1º color: salvia (tu identidad)
        // - resto: tonos complementarios suaves (dashboard)
        ArrayList<Integer> palette = new ArrayList<>();
        palette.add(Color.parseColor("#5C8D89")); // salvia principal
        palette.add(Color.parseColor("#3E6B68")); // salvia oscura
        palette.add(Color.parseColor("#8FB9B6")); // salvia clara
        palette.add(Color.parseColor("#2F6F9F")); // azul sobrio
        palette.add(Color.parseColor("#7A5C9B")); // morado suave
        palette.add(Color.parseColor("#C97A3D")); // naranja suave
        palette.add(Color.parseColor("#B54A4A")); // rojo suave
        palette.add(Color.parseColor("#6B7280")); // gris neutro

        ArrayList<Integer> colors = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) colors.add(palette.get(i % palette.size()));

        com.github.mikephil.charting.data.BarDataSet set =
                new com.github.mikephil.charting.data.BarDataSet(entries, "");
        set.setColors(colors);

        // Valores al final de cada barra
        set.setDrawValues(true);
        set.setValueTextSize(11f);
        set.setValueTextColor(Color.parseColor("#1F2937"));
        set.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getBarLabel(com.github.mikephil.charting.data.BarEntry e) {
                return String.format(Locale.getDefault(), "%,.0f", e.getY());
            }
        });

        // Espaciado entre barras (evita que se vea “apretado”)
        set.setBarShadowColor(Color.TRANSPARENT);
        set.setHighLightAlpha(0);

        com.github.mikephil.charting.data.BarData barData =
                new com.github.mikephil.charting.data.BarData(set);

        // Grosor de barra (con 280dp y 7 barras se ve muy bien)
        barData.setBarWidth(0.50f);

        chartPrestamosGenero.setData(barData);

        // ===== 4) Estilo general (dashboard limpio) =====
        chartPrestamosGenero.getDescription().setEnabled(false);
        chartPrestamosGenero.getLegend().setEnabled(false);
        chartPrestamosGenero.setDrawGridBackground(false);

        chartPrestamosGenero.setPinchZoom(false);
        chartPrestamosGenero.setScaleEnabled(false);
        chartPrestamosGenero.setDoubleTapToZoomEnabled(false);

        chartPrestamosGenero.setHighlightPerTapEnabled(false);
        chartPrestamosGenero.setHighlightPerDragEnabled(false);

        // valores fuera (a la derecha)
        chartPrestamosGenero.setDrawValueAboveBar(true);

        // ✅ CLAVE: mover TODO a la derecha para que NO se corten los géneros
        // left, top, right, bottom
        chartPrestamosGenero.setExtraOffsets(90f, 10f, 26f, 10f);

        // ===== 5) XAxis (categorías / géneros) =====
        com.github.mikephil.charting.components.XAxis x = chartPrestamosGenero.getXAxis();
        x.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false);

        x.setGranularity(1f);
        x.setGranularityEnabled(true);

        x.setTextSize(9f);
        x.setTextColor(Color.parseColor("#111827"));
        x.setLabelCount(labels.size(), false);

        // ✅ separa un poco el texto del eje (para que no “roce” el chart)
        x.setXOffset(10f);
        x.setYOffset(2f);

        x.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = Math.round(value);
                if (i >= 0 && i < labels.size()) return labels.get(i);
                return "";
            }
        });

        // Rango del eje de categorías
        x.setAxisMinimum(-0.5f);
        x.setAxisMaximum(labels.size() - 0.5f);

        // ===== 6) Eje de valores (préstamos) =====
        com.github.mikephil.charting.components.YAxis left = chartPrestamosGenero.getAxisLeft();
        com.github.mikephil.charting.components.YAxis right = chartPrestamosGenero.getAxisRight();

        left.setAxisMinimum(0f);
        left.setGranularity(1f);
        left.setGranularityEnabled(true);

        // grid suave para lectura (estilo dashboard)
        left.setDrawGridLines(true);
        left.setGridLineWidth(0.7f);
        left.setTextSize(11f);
        left.setTextColor(Color.parseColor("#374151"));

        // margen arriba para que no se corte el máximo
        left.setSpaceTop(12f);

        right.setEnabled(false);

        // ===== 7) Animación + refresh =====
        chartPrestamosGenero.animateY(600);
        chartPrestamosGenero.invalidate();

        if (tvPrestamosGeneroTitulo != null) {
            tvPrestamosGeneroTitulo.setText("Géneros\n" + total + " préstamos");
        }
    }

    /**
     * Parte labels largos en 2 líneas para evitar recorte/solape.
     */
    private String safeLabelMultiLine(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.length() <= 18) return s;

        int mid = Math.min(18, s.length() - 1);
        int cut = s.lastIndexOf(' ', mid);
        if (cut < 8) cut = mid;

        String a = s.substring(0, cut).trim();
        String b = s.substring(cut).trim();

        if (b.length() > 18) b = b.substring(0, 18).trim() + "…";
        return a + "\n" + b;
    }

    // ---------------------------
    // Utilidades meses
    // ---------------------------
    private String formatearMesYYYYMM(String yyyymm) {
        try {
            SimpleDateFormat inFmt = new SimpleDateFormat("yyyy-MM", Locale.US);
            SimpleDateFormat outFmt = new SimpleDateFormat("MMM yyyy", new Locale("es", "ES"));
            return outFmt.format(inFmt.parse(yyyymm));
        } catch (Exception e) {
            return yyyymm;
        }
    }

    private long desdeUltimos12Meses() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.MONTH, -11);
        return cal.getTimeInMillis();
    }

    private List<MesConteo> rellenar12MesesConteoVentanaMovil(List<MesConteo> originales) {
        Map<String, Integer> map = new HashMap<>();
        if (originales != null) {
            for (MesConteo m : originales) map.put(m.mes, m.total);
        }

        List<MesConteo> out = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, -11);

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM", Locale.US);

        for (int i = 0; i < 12; i++) {
            String key = fmt.format(cal.getTime());
            MesConteo mc = new MesConteo();
            mc.mes = key;
            mc.total = map.getOrDefault(key, 0);
            out.add(mc);
            cal.add(Calendar.MONTH, 1);
        }
        return out;
    }

    private List<MesImporte> rellenar12MesesImporteVentanaMovil(List<MesImporte> originales) {
        Map<String, Double> map = new HashMap<>();
        if (originales != null) {
            for (MesImporte m : originales) map.put(m.mes, m.importe);
        }

        List<MesImporte> out = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, -11);

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM", Locale.US);

        for (int i = 0; i < 12; i++) {
            String key = fmt.format(cal.getTime());
            MesImporte mi = new MesImporte();
            mi.mes = key;
            mi.importe = map.getOrDefault(key, 0.0);
            out.add(mi);
            cal.add(Calendar.MONTH, 1);
        }
        return out;
    }

    private List<MesConteo> rellenar12MesesConteoDelYear(String year, List<MesConteo> originales) {
        Map<String, Integer> map = new HashMap<>();
        if (originales != null) {
            for (MesConteo m : originales) map.put(m.mes, m.total);
        }

        List<MesConteo> out = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            String key = String.format(Locale.US, "%s-%02d", year, month);
            MesConteo mc = new MesConteo();
            mc.mes = key;
            mc.total = map.getOrDefault(key, 0);
            out.add(mc);
        }
        return out;
    }

    private List<MesImporte> rellenar12MesesImporteDelYear(String year, List<MesImporte> originales) {
        Map<String, Double> map = new HashMap<>();
        if (originales != null) {
            for (MesImporte m : originales) map.put(m.mes, m.importe);
        }

        List<MesImporte> out = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            String key = String.format(Locale.US, "%s-%02d", year, month);
            MesImporte mi = new MesImporte();
            mi.mes = key;
            mi.importe = map.getOrDefault(key, 0.0);
            out.add(mi);
        }
        return out;
    }

    // ===========================
    // PDF (tu sistema multipágina)
    // ===========================
    private void exportarPdf(Uri uri) {
        try (OutputStream os = getContentResolver().openOutputStream(uri)) {

            if (os == null) {
                Toast.makeText(this, "No se pudo guardar el PDF", Toast.LENGTH_SHORT).show();
                return;
            }

            PdfDocument doc = new PdfDocument();

            Paint h1 = new Paint(Paint.ANTI_ALIAS_FLAG);
            h1.setTextSize(18);
            h1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

            Paint h2 = new Paint(Paint.ANTI_ALIAS_FLAG);
            h2.setTextSize(12);
            h2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setTextSize(12);

            int pageNumber = 1;
            PdfDocument.Page page = doc.startPage(new PdfDocument.PageInfo.Builder(PDF_W, PDF_H, pageNumber).create());
            Canvas canvas = page.getCanvas();

            int x = PDF_MARGIN_X;
            int y = PDF_MARGIN_TOP;

            PdfCursor cursor = new PdfCursor(doc, page, canvas, pageNumber, x, y, h1, h2, p);
            pintarPdfMulti(cursor);
            cursor.finishCurrentPage();

            doc.writeTo(os);
            doc.close();

            Toast.makeText(this, "PDF generado correctamente", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creando PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void pintarPdfMulti(PdfCursor c) {
        int maxWidth = PDF_W - (PDF_MARGIN_X * 2);
        int line = 18;

        c.ensureSpace(60);
        c.canvas.drawText("ESTADÍSTICAS - " + pdfYearLabel, c.x, c.y, c.h1);
        c.y += 28;

        String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        c.canvas.drawText("Generado: " + fecha, c.x, c.y, c.p);
        c.y += 28;

        c.ensureSpace(40);
        c.canvas.drawText("RESUMEN", c.x, c.y, c.h2); c.y += line;
        c.canvas.drawText("Usuarios: " + pdfResumen.totalUsuarios, c.x, c.y, c.p); c.y += line;
        c.canvas.drawText("Libros: " + pdfResumen.totalLibros, c.x, c.y, c.p); c.y += line;
        c.canvas.drawText("Ejemplares: " + pdfResumen.totalEjemplares, c.x, c.y, c.p); c.y += (line + 6);

        c.ensureSpace(50);
        c.canvas.drawText("PRÉSTAMOS", c.x, c.y, c.h2); c.y += line;
        c.canvas.drawText("Activos: " + pdfResumen.prestamosActivos, c.x, c.y, c.p); c.y += line;
        c.canvas.drawText("Vencidos: " + pdfResumen.prestamosVencidos, c.x, c.y, c.p); c.y += line;
        c.canvas.drawText("Devueltos: " + pdfResumen.prestamosDevueltos, c.x, c.y, c.p); c.y += (line + 6);

        c.ensureSpace(80);
        c.canvas.drawText("MULTAS", c.x, c.y, c.h2); c.y += line;
        c.canvas.drawText("Pendientes: " + pdfResumen.multasPendientes, c.x, c.y, c.p); c.y += line;
        c.canvas.drawText("Pagadas: " + pdfResumen.multasPagadas, c.x, c.y, c.p); c.y += line;
        c.canvas.drawText("Condonadas: " + pdfResumen.multasCondonadas, c.x, c.y, c.p); c.y += line;

        c.canvas.drawText(String.format(Locale.getDefault(), "Recaudado: %.2f €", pdfResumen.dineroRecaudado), c.x, c.y, c.p); c.y += line;
        c.canvas.drawText(String.format(Locale.getDefault(), "Pendiente: %.2f €", pdfDineroPendiente), c.x, c.y, c.p); c.y += line;
        c.canvas.drawText(String.format(Locale.getDefault(), "%% devoluciones a tiempo: %.0f %%", pdfPctATiempo), c.x, c.y, c.p); c.y += (line + 6);

        c.ensureSpace(40);
        c.canvas.drawText("DISPONIBILIDAD", c.x, c.y, c.h2); c.y += line;
        c.canvas.drawText("Disponibles: " + pdfDisponibles, c.x, c.y, c.p); c.y += line;
        c.canvas.drawText("Prestados ahora: " + pdfPrestadosAhora, c.x, c.y, c.p); c.y += (line + 10);

        c.y = pintarTopLibrosPdf(c, "LIBROS MÁS PRESTADOS", pdfTopLibros, maxWidth);
        c.y += 10;

        c.y = pintarTopUsuariosPdf(c, "LECTORES MÁS ACTIVOS", pdfTopUsuarios, maxWidth);
        c.y += 10;

        c.y = pintarTopLibrosPdf(c, "LIBROS CON MÁS MULTAS", pdfTopLibrosMultas, maxWidth);
    }

    private int pintarTopLibrosPdf(PdfCursor c, String titulo, List<TopLibro> lista, int maxWidth) {
        int line = 18;

        c.ensureSpace(30);
        c.canvas.drawText(titulo, c.x, c.y, c.h2);
        c.y += line;

        if (lista == null || lista.isEmpty()) {
            c.ensureSpace(line);
            c.canvas.drawText("— Sin datos —", c.x, c.y, c.p);
            return c.y + line;
        }

        for (int i = 0; i < lista.size(); i++) {
            TopLibro t = lista.get(i);
            String txt = (i + 1) + ". " + t.titulo + " — " + t.autor + " (" + t.totalPrestamos + ")";
            c.ensureSpace(line * 2);
            c.y = drawWrappedText(c.canvas, txt, c.x, c.y, c.p, maxWidth, line);
        }
        return c.y;
    }

    private int pintarTopUsuariosPdf(PdfCursor c, String titulo, List<TopUsuario> lista, int maxWidth) {
        int line = 18;

        c.ensureSpace(30);
        c.canvas.drawText(titulo, c.x, c.y, c.h2);
        c.y += line;

        if (lista == null || lista.isEmpty()) {
            c.ensureSpace(line);
            c.canvas.drawText("— Sin datos —", c.x, c.y, c.p);
            return c.y + line;
        }

        for (int i = 0; i < lista.size(); i++) {
            TopUsuario t = lista.get(i);
            String txt = (i + 1) + ". " + t.nombre + " — " + t.email + " (" + t.totalPrestamos + ")";
            c.ensureSpace(line * 2);
            c.y = drawWrappedText(c.canvas, txt, c.x, c.y, c.p, maxWidth, line);
        }
        return c.y;
    }

    private class PdfCursor {
        PdfDocument doc;
        PdfDocument.Page page;
        Canvas canvas;
        int pageNumber;
        int x;
        int y;
        Paint h1, h2, p;

        PdfCursor(PdfDocument doc, PdfDocument.Page page, Canvas canvas, int pageNumber,
                  int x, int y, Paint h1, Paint h2, Paint p) {
            this.doc = doc;
            this.page = page;
            this.canvas = canvas;
            this.pageNumber = pageNumber;
            this.x = x;
            this.y = y;
            this.h1 = h1;
            this.h2 = h2;
            this.p = p;
        }

        int bottomLimit() {
            return PDF_H - PDF_MARGIN_BOTTOM;
        }

        void ensureSpace(int neededHeight) {
            if (y + neededHeight <= bottomLimit()) return;
            newPage();
        }

        void newPage() {
            doc.finishPage(page);

            pageNumber++;
            page = doc.startPage(new PdfDocument.PageInfo.Builder(PDF_W, PDF_H, pageNumber).create());
            canvas = page.getCanvas();

            y = PDF_MARGIN_TOP;
            canvas.drawText("ESTADÍSTICAS - " + pdfYearLabel + " (pág. " + pageNumber + ")", x, y, p);
            y += 25;
        }

        void finishCurrentPage() {
            doc.finishPage(page);
        }
    }

    private int drawWrappedText(Canvas canvas, String text, int x, int y, Paint paint, int maxWidth, int lineHeight) {
        if (text == null) text = "";
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();

        for (String w : words) {
            String test = line.length() == 0 ? w : (line + " " + w);
            if (paint.measureText(test) <= maxWidth) {
                line = new StringBuilder(test);
            } else {
                canvas.drawText(line.toString(), x, y, paint);
                y += lineHeight;
                line = new StringBuilder(w);
            }
        }
        if (line.length() > 0) {
            canvas.drawText(line.toString(), x, y, paint);
            y += lineHeight;
        }
        return y;
    }

    private long inicioDeYear(String yearStr) {
        int year = Integer.parseInt(yearStr);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long inicioDeYearSiguiente(String yearStr) {
        int year = Integer.parseInt(yearStr) + 1;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}