package com.empatica.sample;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.app.DatePickerDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;

import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityViewerCuidador extends AppCompatActivity implements HttpRequestTask.OnTaskCompleted {

    private EditText[] fechaEdit = {null, null};
    private EditText[] horaEdit = {null, null};
    private Calendar calendar;
    private Button boton;
    private LinearLayout layout;

    private String[] fecha = {null, null};
    private String[] hora = {null, null};

    //private boolean isRequestInProgress = false;
    private HttpRequestTask task;
    private String usuario;
    private String idCuidador = "trainerCuidador";
    private BottomNavigationView navegacion;
    private DataBase db;
    private TextView usuarioView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer_cuidador);

        navegacion = (BottomNavigationView) findViewById(R.id.navegacion);
        navegacion.setSelectedItemId(R.id.home);

        navegacion.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.home) {
                    //Acción para las actividades
                } else if (itemId == R.id.user) {
                    //Acción para el perfil
                    Intent intent_act = new Intent(ActivityViewerCuidador.this, ProfilePageCuidador.class);
                    startActivity(intent_act);
                }

                return false;
            }
        });

        db = new DataBase();

        usuarioView = findViewById(R.id.idUsuario);

        fechaEdit[0] = (EditText) findViewById(R.id.editTextDate);
        horaEdit[0] = (EditText) findViewById(R.id.editTextTime);
        fechaEdit[1] = (EditText) findViewById(R.id.editTextDateFinal);
        horaEdit[1] = (EditText) findViewById(R.id.editTextTimeFinal);

        boton = (Button) findViewById(R.id.button);
        layout = (LinearLayout) findViewById(R.id.keysLayout);

        calendar = Calendar.getInstance();

        // Configura el OnClickListener para abrir el diálogo de selección de fecha y hora
        fechaEdit[0].setOnClickListener(v -> showDatePickerDialog(0));
        horaEdit[0].setOnClickListener(v -> showTimePickerDialog(0));

        fechaEdit[1].setOnClickListener(v -> showDatePickerDialog(1));
        horaEdit[1].setOnClickListener(v -> showTimePickerDialog(1));

        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d("Actividad", "" + F_ini + " " + H_ini + " " + F_fin + " " + H_fin);
                if (usuario!=null){
                    if (fecha[0] != null && fecha[1] != null && hora[0] != null && hora[1] != null) {
                        if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
                            task.cancel(true);
                        }

                        //Log.d("ActivityViewerCuidador", "Valor usuario: " + usuario);

                        task = new HttpRequestTask(usuario, fecha[0], hora[0], fecha[1], hora[1], ActivityViewerCuidador.this);
                        task.execute();
                    }
                    else {
                        layout.removeAllViews();
                        Toast.makeText(ActivityViewerCuidador.this, "Por favor ingresa todos los campos", Toast.LENGTH_SHORT).show();
                        //Log.d("Actividad", "No debe haber valores sin seleccionar");
                    }
                }
                else {
                    Toast.makeText(ActivityViewerCuidador.this, "No hay aún un usuario asignado", Toast.LENGTH_SHORT).show();
                }
            }
        });

        db.getDataCuidador(idCuidador).observe(ActivityViewerCuidador.this, new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(Map<String, Object> stringObjectMap) {
                usuario = (String)stringObjectMap.get("asignado");
                if (usuario != null) {
                    db.getDataUser(usuario).observe(ActivityViewerCuidador.this, new Observer<Map<String, Object>>() {
                        @Override
                        public void onChanged(Map<String, Object> stringObjectMap) {
                            if (!stringObjectMap.isEmpty()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        usuarioView.setText(usuario);
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        usuarioView.setText("El usuario asignado no existe");
                                    }
                                });
                            }
                        }
                    });
                }
                else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            usuarioView.setText("No tienes a nadie asignado");
                        }
                    });
                }
            }
        });
    }

    // Listener para la respuesta del server
    @Override
    public void onTaskCompleted(String response) {
        if (response != null) {
            // Procesa la respuesta del servidor aquí
            //Log.d("Actividad", "Respuesta del servidor: " + response);
            mostrarKeys(response);
        } else {
            layout.removeAllViews();
            Toast.makeText(ActivityViewerCuidador.this, "El servidor no ha devuelto datos", Toast.LENGTH_SHORT).show();
            //Log.d("Actividad", "La respuesta del servidor es nula");
        }
    }

    // Método para mostrar el diálogo de selección de fecha
    private void showDatePickerDialog(int i) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Crea un nuevo DatePickerDialog con la fecha actual como valor inicial
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, monthOfYear, dayOfMonth1) -> {
                    // Cuando el usuario selecciona una fecha, actualiza el EditText
                    calendar.set(year1, monthOfYear, dayOfMonth1);
                    fechaEdit[i].setText(String.format("%02d/%02d/%d", dayOfMonth1, monthOfYear + 1, year1));

                    //Guardamos en la variable la fecha
                    fecha[i] = String.valueOf(dayOfMonth1) + "/" + String.valueOf(monthOfYear+1) + "/" + String.valueOf(year1);
                },
                year, month, dayOfMonth);

        // Muestra el diálogo
        datePickerDialog.show();
    }

    //Método para mostrar un diálogo para seleccionar la hora
    private void showTimePickerDialog(int i) {
        // Obtén la hora actual
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Crea un TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Actualiza el texto del EditText con la hora seleccionada
                        horaEdit[i].setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                        // Guardamos en la variable la hora
                        hora[i] = String.valueOf(hourOfDay) + ":" + String.valueOf(minute) + ":00";
                    }
                },
                hourOfDay, minute, true); // true indica si se debe mostrar el modo de 24 horas
        timePickerDialog.show();
    }

    //Método para mostrar las actividades devueltas por el server
    private void mostrarKeys(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            Iterator<String> keys = jsonObject.keys();

            // Limpia el LinearLayout antes de agregar nuevos TextView
            layout.removeAllViews();

            while (keys.hasNext()) {
                String key = keys.next();
                int value = jsonObject.getInt(key);
                TextView textView = new TextView(this);
                textView.setText(key+": "+value+" minutos");

                // Establece las propiedades del TextView
                textView.setGravity(View.TEXT_ALIGNMENT_CENTER); // Establece la gravedad
                textView.setPadding(16, 16, 16, 16); // Establece el padding
                textView.setTextSize(16); // Establece el tamaño del texto

                layout.addView(textView);
            }

            Toast.makeText(ActivityViewerCuidador.this, "Actividades calculadas con éxito", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Log.e("Actividad", "Error al procesar el JSON", e);
        }
    }
}