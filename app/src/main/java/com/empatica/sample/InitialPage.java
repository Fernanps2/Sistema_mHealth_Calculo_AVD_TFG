package com.empatica.sample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class InitialPage extends AppCompatActivity {

    private Button buttonUsuario;
    private Button buttonCuidador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_initial_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        buttonUsuario = (Button) findViewById(R.id.buttonUsuario);

        AlertDialog.Builder alerta = new AlertDialog.Builder(InitialPage.this);
        alerta.setMessage("Esta aplicación recopila datos fisiológicos y de ubicación, almacenándolos y tratándolos para su posterior uso en el cálculo de actividades. \n¿Está de acuerdo con el uso de sus datos y será responsable con el uso de los datos de otras personas?")
                .setCancelable(false)
                        .setPositiveButton("Estoy de acuerdo", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("No estoy de acuerdo", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
        AlertDialog titulo = alerta.create();
        titulo.setTitle("Permiso de uso de datos");
        titulo.show();


        //Navegación a la página principal del usuario
        buttonUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InitialPage.this, MainActivity.class);
                startActivity(intent);
            }
        });

        buttonCuidador = (Button) findViewById(R.id.buttonCuidador);

        //Navegación a la página principal del cuidador
        buttonCuidador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InitialPage.this, ActivityViewerCuidador.class);
                startActivity(intent);
            }
        });
    }
}