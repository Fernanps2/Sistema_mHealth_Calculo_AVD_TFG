package com.empatica.sample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProfilePage extends AppCompatActivity {
    private final String DEFAULT_TEXT = "CARGANDO DATOS...";
    private DataBase db;
    private List<LocationModel> locationList;
    private LocationAdapter locationAdapter;
    private RecyclerView recyclerView;
    private Button addLocation;
    private FloatingActionButton saveButton;
    private BottomNavigationView navegacion;
    private EditText nombre;
    private TextView usuario;
    private EditText cuidador;
    private String cuidadorAntiguo;
    private Map<String, Object> dataMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);
        navegacion = (BottomNavigationView) findViewById(R.id.navegacion);
        navegacion.setSelectedItemId(R.id.user);
        db = new DataBase();

        navegacion.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.home) {
                    //Acción para home
                    //Intent intent_act = new Intent(ProfilePage.this, MainActivity.class);
                    //startActivity(intent_act);
                    Toast.makeText(ProfilePage.this, "NO IMPLEMENTADO: Utilice el botón de retroceso del teléfono", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.analitycs) {
                    Intent intent_an = new Intent(ProfilePage.this, DayRatePage.class);
                    startActivity(intent_an);
                } else if (itemId == R.id.activities) {
                    Intent intent_act = new Intent(ProfilePage.this, ActivityViewer.class);
                    startActivity(intent_act);
                } else if (itemId == R.id.user) {
                    //Acción para el perfil
                }

                return false;
            }
        });

        usuario = findViewById(R.id.usuario);
        nombre = findViewById(R.id.nombre);
        cuidador = findViewById(R.id.cuidador);

        usuario.setText(DEFAULT_TEXT);
        nombre.setText(DEFAULT_TEXT);
        cuidador.setText(DEFAULT_TEXT);

        nombre.setActivated(false);
        cuidador.setActivated(false);

        nombre.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Este método se llama antes de que el texto cambie
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Este método se llama mientras el texto está cambiando
                dataMap.put("nombre", s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Este método se llama después de que el texto haya cambiado
            }
        });

        cuidador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Este método se llama antes de que el texto cambie
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Este método se llama mientras el texto está cambiando
                dataMap.put("asignado", s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Este método se llama después de que el texto haya cambiado
            }
        });

        locationList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerViewLocations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        locationAdapter = new LocationAdapter(locationList);
        recyclerView.setAdapter(locationAdapter);

        addLocation = findViewById(R.id.fabAddLocation);

        addLocation.setActivated(false);
        addLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddLocationDialog();
            }
        });

        saveButton = findViewById(R.id.saveButton);
        saveButton.setActivated(false);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarDatos();
            }
        });

        // Obtener datos asincrónicamente
        db.getDataUser("trainer").observe(ProfilePage.this, new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(Map<String, Object> data) {
                if (data != null) {
                    // Procesar datos una vez recibidos
                    processData(data);
                } else {
                    Log.e("ProfileUser", "Los datos son nulos");
                    // Manejar caso de datos nulos si es necesario
                }
            }
        });
    }

    private void addLocationDB(String locationName, double latitude, double longitude) {
        LocationModel location = new LocationModel(locationName, latitude, longitude);
        locationList.add(location);
        locationAdapter.notifyDataSetChanged();
    }
    private void addLocation(String locationName, double latitude, double longitude) {
        LocationModel location = new LocationModel(locationName, latitude, longitude);
        Map<String, Double> map = new HashMap<>();
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        ((Map<String, Map<String, Double>>) dataMap.get("ubicaciones")).put(locationName, map);
        locationList.add(location);
        locationAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Ubicación añadida: " + locationName, Toast.LENGTH_SHORT).show();
    }

    private void showAddLocationDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.location_input_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextLocationName = dialogView.findViewById(R.id.editTextLocationName);
        final EditText editTextLatitude = dialogView.findViewById(R.id.editTextLatitude);
        final EditText editTextLongitude = dialogView.findViewById(R.id.editTextLongitude);

        dialogBuilder.setTitle("Agregar Ubicación");
        dialogBuilder.setPositiveButton("Agregar", (dialog, whichButton) -> {
            String locationName = editTextLocationName.getText().toString().trim();
            String latitudeStr = editTextLatitude.getText().toString().trim();
            String longitudeStr = editTextLongitude.getText().toString().trim();

            if (!locationName.isEmpty() && !latitudeStr.isEmpty() && !longitudeStr.isEmpty()) {
                if (!((Map<String,Object>)dataMap.get("ubicaciones")).containsKey(locationName)) {
                    double latitude = Double.parseDouble(latitudeStr);
                    double longitude = Double.parseDouble(longitudeStr);
                        addLocation(locationName, latitude, longitude);
                } else {
                    Toast.makeText(ProfilePage.this, "Ese nombre de ubicación ya existe", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ProfilePage.this, "Por favor ingresa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });
        dialogBuilder.setNegativeButton("Cancelar", null);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void processData(Map<String, Object> data) {
        // Manejar y procesar los datos recibidos
        //dataMap.clear();
        dataMap = data;
        cuidadorAntiguo = (String) dataMap.get("asignado");
        Log.d("ProfileUser", "Valores de data: " + dataMap);

        // Actualizar la interfaz de usuario fuera del hilo principal
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateUIWithData(dataMap);
            }
        });
    }

    private void updateUIWithData(Map<String, Object> dataMap) {
        // Actualizar vistas de la interfaz de usuario
        usuario.setText((String) dataMap.get("usuario"));
        nombre.setText((String) dataMap.get("nombre"));
        cuidador.setText((String) dataMap.get("asignado"));

        if (dataMap.containsKey("ubicaciones")) {
            Map<String, Map<String, Double>> ubicacionesMap = (Map<String, Map<String, Double>>) dataMap.get("ubicaciones");
            for (String key : ubicacionesMap.keySet()) {
                Map<String, Double> ubicacion = ubicacionesMap.get(key);
                try {
                    double latitude = ((Number)ubicacion.get("latitude")).doubleValue();
                    double longitude = ((Number)ubicacion.get("longitude")).doubleValue();
                    addLocationDB(key, latitude, longitude);
                } catch (Exception e) {
                    Log.e("ProfileUser", "Error al procesar ubicación: " + e.getMessage());
                }
            }
        }

        nombre.setActivated(true);
        cuidador.setActivated(true);
        addLocation.setActivated(true);
        saveButton.setActivated(true);
    }

    private void guardarDatos() {
        //Comprobamos si se ha cambiado el cuidador
        if (!dataMap.get("asignado").equals(cuidadorAntiguo)) {
            //Comprobamos si el cuidador nuevo tiene a alguien ya asignado
            db.existeUsuarioCuidadorSinAsignar((String)dataMap.get("asignado")).observe(ProfilePage.this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    //Si es un buen candidato como cuidador
                    if (aBoolean) {
                        Log.d("ProfilePage", "Existe un cuidador sin asignar");
                        //Desasignamos el cuidado al antiguo
                        db.desasignarCuidador(cuidadorAntiguo);
                        db.asignarCuidador((String)dataMap.get("asignado"), (String)dataMap.get("usuario"));
                        //Ponemos como nuevo cuidador al actual
                        cuidadorAntiguo = (String)dataMap.get("asignado");
                        db.setDataUser((String)dataMap.get("usuario"), dataMap).observe(ProfilePage.this, new Observer<Boolean>() {
                            @Override
                            public void onChanged(Boolean completado) {
                                if (completado){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ProfilePage.this, "Los cambios se han guardado con éxito", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ProfilePage.this, "Ha habido un problema al guardar", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        dataMap.put("asignado", cuidadorAntiguo);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cuidador.setText((String)dataMap.get("asignado"));
                                Toast.makeText(ProfilePage.this, "El cuidador seleccionado no es válido", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
        else {
            db.setDataUser((String)dataMap.get("usuario"), dataMap).observe(ProfilePage.this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean completado) {
                    if (completado){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ProfilePage.this, "Los cambios se han guardado con éxito", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ProfilePage.this, "Ha habido un problema al guardar", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    }

}