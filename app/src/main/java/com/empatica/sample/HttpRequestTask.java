package com.empatica.sample;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequestTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = "HttpRequestTask";
    private String usuario;
    private String fecha_inicio;
    private String hora_inicio;
    private String fecha_final;
    private String hora_final;
    private String BASE_URL = "http://13.49.229.149:80/predict";
    private OnTaskCompleted listener;

    public HttpRequestTask() {
        super();
    }

    public HttpRequestTask(String usuario, String fecha_inicio, String hora_inicio, String fecha_final, String hora_final, OnTaskCompleted listener) {
        this.usuario = usuario;
        this.fecha_inicio = fecha_inicio;
        this.hora_inicio = hora_inicio;
        this.fecha_final = fecha_final;
        this.hora_final = hora_final;
        this.listener = listener;
    }

    public HttpRequestTask(OnTaskCompleted listener) {
        this.listener = listener;
    }

    public void setEnv(String usuario, String fecha_inicio, String hora_inicio, String fecha_final, String hora_final) {
        this.usuario = usuario;
        this.fecha_inicio = fecha_inicio;
        this.hora_inicio = hora_inicio;
        this.fecha_final = fecha_final;
        this.hora_final = hora_final;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            // Cuerpo del JSON
            String jsonInputString = "{\"usuario\": \"" + usuario + "\", \"fecha_inicio\": \"" + fecha_inicio + "\", \"hora_inicio\":\""+hora_inicio+"\", \"fecha_final\": \""+
                fecha_final+"\", \"hora_final\": \""+hora_final+"\"}";
            URL url = new URL(this.BASE_URL);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");

            urlConnection.setDoOutput(true);
            urlConnection.getOutputStream().write(jsonInputString.getBytes());

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            reader.close();

            return stringBuilder.toString();

        } catch (IOException e) {
            Log.e("HttpRequestTask", "Error en la solicitud HTTP", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (listener != null) {
            listener.onTaskCompleted(result);
        }
    }

    public interface OnTaskCompleted {
        void onTaskCompleted(String response);
    }
}
