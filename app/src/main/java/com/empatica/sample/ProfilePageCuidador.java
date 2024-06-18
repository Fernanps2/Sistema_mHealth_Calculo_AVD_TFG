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

public class ProfilePageCuidador extends AppCompatActivity {
    private final String DEFAULT_TEXT = "CARGANDO DATOS...";
    private DataBase db;
    private FloatingActionButton saveButton;
    private BottomNavigationView navegacion;
    private EditText nombre;
    private TextView usuario;
    private TextView cuidador;
    private Map<String, Object> dataMap;
    private String idUsuario = "trainerCuidador";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page_cuidador);
        navegacion = (BottomNavigationView) findViewById(R.id.navegacion);
        navegacion.setSelectedItemId(R.id.user);
        db = new DataBase();

        navegacion.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.home) {
                    //Acción para home
                    Intent intent_act = new Intent(ProfilePageCuidador.this, ActivityViewerCuidador.class);
                    startActivity(intent_act);
                } else if (itemId == R.id.trainer) {
                    //Acción para el trainer
                    Intent intent_act = new Intent(ProfilePageCuidador.this, TrainViewer.class);
                    startActivity(intent_act);
                }else if (itemId == R.id.user) {
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

        saveButton = findViewById(R.id.saveButton);
        saveButton.setActivated(false);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarDatos();
            }
        });

        // Obtener datos asincrónicamente
        db.getDataCuidador(idUsuario).observe(ProfilePageCuidador.this, new Observer<Map<String, Object>>() {
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

    private void processData(Map<String, Object> data) {
        // Manejar y procesar los datos recibidos
        //dataMap.clear();
        dataMap = data;
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

        if (((String)dataMap.get("asignado")).isEmpty())
            cuidador.setText("No tiene a nadie asignado");
        else
            cuidador.setText((String) dataMap.get("asignado"));

        nombre.setActivated(true);
        saveButton.setActivated(true);
    }

    private void guardarDatos() {
        db.setDataCuidador((String) dataMap.get("usuario"), dataMap).observe(ProfilePageCuidador.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean completado) {
                if (completado) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ProfilePageCuidador.this, "Los cambios se han guardado con éxito", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ProfilePageCuidador.this, "Ha habido un problema al guardar", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}

