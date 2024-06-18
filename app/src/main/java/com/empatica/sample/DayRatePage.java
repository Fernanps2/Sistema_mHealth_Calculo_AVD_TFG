package com.empatica.sample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DayRatePage extends AppCompatActivity {
    private LineChart lineChartDia;
    private LineChart lineChartMes;
    private DataBase db;
    private CalendarView calendario;
    private int YYYY;
    private int MM;
    private int DD;
    private String usuario;
    private BottomNavigationView navegacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_rate_page);
        lineChartDia = findViewById(R.id.lineChart);
        lineChartMes = findViewById(R.id.lineChartMes);
        calendario = findViewById(R.id.calendario);
        final Map<Integer, Map<String, Object>> dataMap = new HashMap<>();
        db = new DataBase();
        navegacion = (BottomNavigationView) findViewById(R.id.navegacion);
        navegacion.setSelectedItemId(R.id.analitycs);

        navegacion.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.home) {
                    //Acción para home
                    //Intent intent_an = new Intent(DayRatePage.this, MainActivity.class);
                    //startActivity(intent_an);
                    Toast.makeText(DayRatePage.this, "NO IMPLEMENTADO: Utilice el botón de retroceso del teléfono", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.analitycs) {
                    //Acción para analíticas
                } else if (itemId == R.id.activities) {
                    Intent intent_act = new Intent(DayRatePage.this, ActivityViewer.class);
                    startActivity(intent_act);
                } else if (itemId == R.id.user) {
                    //Acción para el perfil
                    Intent intent_act = new Intent(DayRatePage.this, ProfilePage.class);
                    startActivity(intent_act);
                }

                return false;
            }
        });

        YYYY = 0;
        MM = 0;
        DD = 0;
        usuario = "trainer";

        //Cuando se seleccione un día del calendario
        calendario.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                //############################################################################################
                // INICIO DE ANÁLISIS DÍA
                //############################################################################################
                YYYY = year;
                MM = month+1; //El mes empieza por 0
                DD = dayOfMonth;

                lineChartDia.clear();
                Log.d("DAY", "Año: " + YYYY + " Mes: " + MM + " Day: " + DD);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        db.getRateFisioDay(usuario, String.valueOf(YYYY), String.valueOf(MM), String.valueOf(DD)).

                                observe(DayRatePage.this, new Observer<Map<Integer, Map<String, Object>>>() {
                                    @Override
                                    public void onChanged(Map<Integer, Map<String, Object>> data) {
                                        dataMap.clear();
                                        dataMap.putAll(data);
                                        Log.d("DAY", "Valores de data: " + dataMap);

                                        // Crear listas de conjuntos de datos para cada métrica
                                        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

                                        // Definir colores para las métricas
                                        int[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN};
                                        int colorIndex = 0;

                                        // Obtener las etiquetas de métricas automáticamente
                                        Set<String> metricsLabels = new HashSet<>();
                                        for (Map.Entry<Integer, Map<String, Object>> entry : dataMap.entrySet()) {
                                            metricsLabels.addAll(entry.getValue().keySet());
                                        }

                                        // Recorrer cada etiqueta de métrica
                                        for (String metric : metricsLabels) {
                                            ArrayList<Entry> entries = new ArrayList<>();
                                            for (int hour = 0; hour < 24; hour++) {
                                                if (dataMap.containsKey(hour)) {
                                                    Map<String, Object> metrics = dataMap.get(hour);
                                                    if (metrics.containsKey(metric)) {
                                                        Double value = (Double) metrics.get(metric);
                                                        entries.add(new Entry(hour, value.floatValue()));
                                                    } else {
                                                        entries.add(new Entry(hour, 0f)); // Si no hay valor para la métrica, se añade 0
                                                    }
                                                } else {
                                                    entries.add(new Entry(hour, 0f)); // Si no hay datos para la hora, se añade 0
                                                }
                                            }
                                            LineDataSet dataSet = new LineDataSet(entries, metric);
                                            dataSet.setColor(colors[colorIndex % colors.length]); // Asignar color
                                            colorIndex++;
                                            dataSets.add(dataSet);
                                        }

                                        // Crear y configurar el objeto LineData
                                        LineData lineData = new LineData(dataSets);

                                        // Configurar los ejes del gráfico
                                        XAxis xAxis = lineChartDia.getXAxis();
                                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                                        xAxis.setAxisMinimum(0f);
                                        xAxis.setAxisMaximum(23f);
                                        xAxis.setLabelCount(24, true); // Asegurar que haya 24 etiquetas

                                        // Asignar los datos al gráfico
                                        lineChartDia.setData(lineData);
                                        lineChartDia.invalidate(); // Para refrescar el gráfico

                                    }
                                });
                    }
                });

                //############################################################################################
                // FIN DE ANÁLISIS DÍA
                //############################################################################################

                //############################################################################################
                // INICIO DE ANÁLISIS MES
                //############################################################################################

                YYYY = year;
                MM = month + 1; // El mes empieza por 0
                DD = dayOfMonth;

                lineChartMes.clear();
                Log.d("DAY", "Año: " + YYYY + " Mes: " + MM + " Day: " + DD);

                db.getRateFisioMonth(usuario, String.valueOf(YYYY), String.valueOf(MM)).observe(DayRatePage.this, new Observer<Map<Integer, Map<String, Object>>>() {
                    @Override
                    public void onChanged(Map<Integer, Map<String, Object>> data) {
                        if (data != null) {
                            dataMap.clear();
                            dataMap.putAll(data);
                            Log.d("MONTH", "Valores de data: " + dataMap);

                            // Crear listas de conjuntos de datos para cada métrica
                            ArrayList<ILineDataSet> dataSets = new ArrayList<>();

                            // Definir colores para las métricas
                            int[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN};
                            int colorIndex = 0;

                            // Obtener las etiquetas de métricas automáticamente
                            Set<String> metricsLabels = new HashSet<>();
                            for (Map.Entry<Integer, Map<String, Object>> entry : dataMap.entrySet()) {
                                metricsLabels.addAll(entry.getValue().keySet());
                            }

                            // Sacamos cuantos días tiene el mes actual
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                            // El mes y día tiene que ir como valor de dos cifras
                            String MM_string = (MM < 10) ? "0" + MM : String.valueOf(MM);
                            String DD_string = (DD < 10) ? "0"+DD : String.valueOf(DD);

                            LocalDate ld = LocalDate.parse(String.valueOf(YYYY) + "-" + MM_string + "-" + DD_string, dtf);
                            int totalDays = ld.lengthOfMonth();

                            // Recorrer cada etiqueta de métrica
                            for (String metric : metricsLabels) {
                                ArrayList<Entry> entries = new ArrayList<>();
                                for (int day = 1; day <= totalDays; day++) {
                                    if (dataMap.containsKey(day)) {
                                        Map<String, Object> metrics = dataMap.get(day);
                                        if (metrics.containsKey(metric)) {
                                            Double value = (Double) metrics.get(metric);
                                            entries.add(new Entry(day, value.floatValue()));
                                        } else {
                                            entries.add(new Entry(day, 0f)); // Si no hay valor para la métrica, se añade 0
                                        }
                                    } else {
                                        entries.add(new Entry(day, 0f)); // Si no hay datos para el día, se añade 0
                                    }
                                }
                                LineDataSet dataSet = new LineDataSet(entries, metric);
                                dataSet.setColor(colors[colorIndex % colors.length]); // Asignar color
                                colorIndex++;
                                dataSets.add(dataSet);
                            }

                            // Crear y configurar el objeto LineData
                            LineData lineData = new LineData(dataSets);

                            // Configurar los ejes del gráfico
                            XAxis xAxis = lineChartMes.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setAxisMinimum(1f);
                            xAxis.setAxisMaximum((float) totalDays);
                            xAxis.setLabelCount(totalDays, true); // Asegurar que haya tantas etiquetas como días

                            // Asignar los datos al gráfico
                            lineChartMes.setData(lineData);
                            lineChartMes.invalidate(); // Para refrescar el gráfico
                        }
                    }
                });

                //############################################################################################
                // FINAL DE ANÁLISIS MES
                //############################################################################################
            }
        });
    }
}