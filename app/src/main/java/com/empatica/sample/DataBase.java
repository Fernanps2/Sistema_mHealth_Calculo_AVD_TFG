package com.empatica.sample;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.api.core.ApiFuture;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;


public class DataBase extends ViewModel {
    private static final String TAG = "DATABASE";
    private static final String USUARIOS = "Usuarios";
    private static final String CUIDADORES = "Cuidadores";
    private static final String R_FISIO = "datosFisiologicos";
    //private static final String BVP = "BVP";
    //private static final String EDA = "EDA";
    private static final String HR = "HR";
    private static final String TMP = "TMP";
    private static final String FECHA = "fecha";

    private Calendar calendario;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    //#######################################################################################
    //#                                 DATOS FISIOLÓGICOS                                  #
    //#######################################################################################

    /**
     * @title addDataFisio
     * @description Añade los datos fisiológicos a un usuario
     * @param usuario Usuario (HASH) identificativo
     * @param data Map<String, Object> con key tipo de dato y valores de cada tipo de dato
     */
    public void addDataFisio (String usuario, Map<String, Object> data) {
        //Añadimos la fecha y hora actual
        data.put(FECHA, new Timestamp(new Date()));

        db.collection("Usuarios")
                .document(usuario)
                .collection(R_FISIO)
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    // Documento añadido exitosamente
                    Log.d(TAG, "Documento añadido con éxito con ID: " + documentReference.getId());
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error al crear la colección y/o documento", e);
                    }
                });
    }

    /*public void addDataFisio (String usuario, Map<String, Object> data) {
        //Guardamos el año, mes, día y hora actual
        calendario = Calendar.getInstance();

        String YYYY = String.valueOf(calendario.get(Calendar.YEAR));
        String MM = String.valueOf(calendario.get(Calendar.MONTH)+1); //El mes se indexa desde 0
        String DD = String.valueOf(calendario.get(Calendar.DAY_OF_MONTH));

        String time = String.valueOf(calendario.get(Calendar.HOUR_OF_DAY)) + "_" + String.valueOf(calendario.get(Calendar.MINUTE)) + "_" + String.valueOf(calendario.get(Calendar.SECOND));

        //Creamos la ruta
        String route = R_FISIO + "/" + usuario + "/" + YYYY + "/" + MM + "/" + DD + "/" + time;

        //Obtenemos una referencia a la colección
        DocumentReference docRef = db.document(route);

        //Usamos el método set() con el parámetro merge para crear la colección si no existe
        docRef.set(data, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Colección y documento creados exitosamente");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error al crear la colección y/o documento", e);
                    }
                });
    }*/

    /**
     * @title getDataFisioDay
     * @description Método que devuelve los datos de un día en concreto
     * @param usuario Usuario (HASH) identificativo
     * @param YYYY Año del que se quiere obtener los datos
     * @param MM Mes del que se quiere obtener los datos
     * @param DD Día del que se quiere obtener los datos
     * @return Hash map con key como el número del valor, y como valores hash map con todos los valores del día a cada hora
     *
     * @warning Se sobre entiende que los valores que se pasen son los correctos
     */

    public LiveData<Map<Integer, Map<String, Object>>> getDataFisioDay (String usuario, String YYYY, String MM, String DD) {
        MutableLiveData<Map<Integer,Map<String, Object>>> liveData = new MutableLiveData<>();
        Map<Integer,Map<String, Object>> dataMap = new HashMap<>(); //Guarda hora y media de los valores

        String MM_str = (MM.length() == 1) ? "0"+MM : MM;
        String DD_str = (DD.length() == 1) ? "0"+DD : DD;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date startDate = dateFormat.parse(YYYY + "-" + MM_str + "-" + DD_str);

            //Inicializamos la hora
            startDate.setHours(0);
            startDate.setMinutes(0);
            startDate.setSeconds(0);

            Date endDate = (Date) startDate.clone();

            endDate.setHours(23);
            endDate.setMinutes(59);
            endDate.setSeconds(59);

            Timestamp startTimestamp = new Timestamp(startDate);
            Timestamp endTimestamp = new Timestamp(endDate);

            db.collection(USUARIOS)
                    .document(usuario)
                    .collection(R_FISIO)
                    .whereGreaterThanOrEqualTo("fecha", startTimestamp)
                    .whereLessThanOrEqualTo("fecha", endTimestamp)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            int cantidad = 0;

                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                Timestamp time = (Timestamp) document.getData().get("fecha");
                                Date date = time.toDate();
                                SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                                Log.d(TAG, "Fecha: " + timeFormat.format(date) + " Valores: " + document.getData());

                                dataMap.put(cantidad, document.getData());
                                cantidad++;
                            }

                            liveData.setValue(dataMap);

                        } else {
                            Log.d(TAG, "No se encontraron documentos en el rango");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error al consultar documentos: ", e);
                    });
        } catch(ParseException e){
            e.printStackTrace();
        }

        return liveData;
    }

    /**
     * @title getDataFisioBetween
     * @description Método que devuelve los datos entre dos tiempos dados
     * @param usuario Usuario (HASH) identificativo
     * @param tiempoInicial Tiempo inicial del que se quiere obtener los datos
     * @param tiempoFinal Tiempo final del que se quiere obtener los datos
     * @return Hash map con key como el número del valor, y como valores hash map con todos los valores del rango dado
     *
     * @warning Los tiempos tienen que estar con el formato yyyy-MM-dd
     */

    public LiveData<Map<Integer, Map<String, Object>>> getDataFisioDay (String usuario, Date tiempoInicial, Date tiempoFinal) {
        MutableLiveData<Map<Integer,Map<String, Object>>> liveData = new MutableLiveData<>();
        Map<Integer,Map<String, Object>> dataMap = new HashMap<>(); //Guarda hora y media de los valores

        Timestamp startTimestamp = new Timestamp(tiempoInicial);
        Timestamp endTimestamp = new Timestamp(tiempoFinal);

        db.collection(USUARIOS)
                .document(usuario)
                .collection(R_FISIO)
                .whereGreaterThanOrEqualTo("fecha", startTimestamp)
                .whereLessThanOrEqualTo("fecha", endTimestamp)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        int cantidad = 0;

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Timestamp time = (Timestamp) document.getData().get("fecha");
                            Date date = time.toDate();
                            SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                            Log.d(TAG, "Fecha: " + timeFormat.format(date) + " Valores: " + document.getData());

                            dataMap.put(cantidad, document.getData());
                            cantidad++;
                        }

                        liveData.setValue(dataMap);

                    } else {
                        Log.d(TAG, "No se encontraron documentos en el rango");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al consultar documentos: ", e);
                });

        return liveData;
    }

    /**
     * @title getRateFisioDay
     * @description Método que devuelve la media de los análisis que se obtuvieron cada día de ese mes
     * @param usuario Usuario (HASH) identificativo
     * @param YYYY Año del que se quiere obtener la media
     * @param MM Mes del que se quiere obtener la media
     * @param DD Día del que se quiere obtener la media
     * @return Un hash map como key la hora del día y valor otro hash map con los valores medios devueltos
     *
     * @warning Se sobre entiende que los valores que se pasen son los correctos
     */
    public LiveData<Map<Integer, Map<String, Object>>> getRateFisioDay (String usuario, String YYYY, String MM, String DD) {
        MutableLiveData<Map<Integer,Map<String, Object>>> liveData = new MutableLiveData<>();
        Map<Integer,Map<String, Object>> dataMap = new HashMap<>(); //Guarda hora y media de los valores
        Map<Integer, Integer> cantidad = new HashMap<>(); //Guarda cantidad de registros en la hora

        String MM_str = (MM.length() == 1) ? "0"+MM : MM;
        String DD_str = (DD.length() == 1) ? "0"+DD : DD;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date startDate = dateFormat.parse(YYYY + "-" + MM_str + "-" + DD_str);

            //Inicializamos la hora
            startDate.setHours(0);
            startDate.setMinutes(0);
            startDate.setSeconds(0);

            Date endDate = (Date) startDate.clone();

            AtomicInteger daysProcessed = new AtomicInteger(0);

            for (int h = 0; h < 24; h++) {
                final int hora = h;
                startDate.setHours(hora);
                endDate.setHours(hora+1);

                //Si es la última hora, asignamos el final como 23:59:59
                if (hora == 23) {
                    endDate.setHours(23);
                    endDate.setMinutes(59);
                    endDate.setSeconds(59);
                }

                Timestamp startTimestamp = new Timestamp(startDate);
                Timestamp endTimestamp = new Timestamp(endDate);

                db.collection(USUARIOS)
                        .document(usuario)
                        .collection(R_FISIO)
                        .whereGreaterThanOrEqualTo("fecha", startTimestamp)
                        .whereLessThanOrEqualTo("fecha", endTimestamp)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    Timestamp time = (Timestamp) document.getData().get("fecha");
                                    Date date = time.toDate();
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                                    Log.d(TAG, "Fecha: " + timeFormat.format(date) + " Valores: " + document.getData());

                                    //Comprobamos si existe ya este número como índice
                                    if (dataMap.containsKey(hora)) {
                                        Map<String, Object> suma = dataMap.get(hora);

                                        //Sumamos los valores
                                        //suma.put(BVP, (Double)suma.get(BVP) + (Double)document.getData().get(BVP));
                                        //suma.put(EDA, (Double)suma.get(EDA) + (Double)document.getData().get(EDA));
                                        suma.put(HR, (Double)suma.get(HR) + (Double)document.getData().get(HR));
                                        suma.put(TMP, (Double)suma.get(TMP) + (Double)document.getData().get(TMP));

                                        //Y actualizamos los datos
                                        dataMap.put(hora, suma);

                                        //Añadimos el registro a la cantidad
                                        cantidad.put(hora, cantidad.get(hora) + 1);
                                    }
                                    else {
                                        //Si no existe el registro, lo añadimos

                                        Map<String, Object> nuevo = new HashMap<>();
                                        //nuevo.put(EDA, document.getData().get(EDA));
                                        nuevo.put(HR, document.getData().get(HR));
                                        nuevo.put(TMP, document.getData().get(TMP));
                                        //nuevo.put(BVP, document.getData().get(BVP));
                                        dataMap.put(hora, nuevo);

                                        //Añadimos la nueva cantidad
                                        cantidad.put(hora, 1);
                                    }
                                }

                                //Hacemos las medias a través de los datos sumados y las cantidades recogidas
                                //Hago la media a partir de la hora actual
                                if (cantidad.containsKey(hora)) {
                                    int num = cantidad.get(hora);

                                    Map<String, Object> media = new HashMap<>(dataMap.get(hora));

                                    //Hacemos las medias de cada registro
                                    //media.put(BVP, (Double) media.get(BVP) / (num * 1.0));
                                    //media.put(EDA, (Double) media.get(EDA) / (num * 1.0));
                                    media.put(HR, (Double) media.get(HR) / (num * 1.0));
                                    media.put(TMP, (Double) media.get(TMP) / (num * 1.0));

                                    //Actualizamos los valores
                                    dataMap.put(hora, media);
                                }

                                // Incrementa el contador de días procesados y verifica si se ha completado el procesamiento de todas las horas
                                if (daysProcessed.incrementAndGet() == 23) {
                                    liveData.setValue(dataMap);
                                    Log.d(TAG, "DataMap valor final: " + liveData.getValue());
                                }

                                // Llamamos al listener cuando se hayan obtenido y calculado los datos
                                //listener.onDataFetched(dataMap);
                            } else {
                                Log.d(TAG, "No se encontraron documentos en el rango");

                                //Si no existe también proceso el día
                                if (daysProcessed.incrementAndGet() == 23) {
                                    liveData.setValue(dataMap);
                                    Log.d(TAG, "DataMap valor final: " + liveData.getValue());
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Error al consultar documentos: ", e);
                        });
            }
        } catch(ParseException e){
            e.printStackTrace();
        }

        return liveData;
    }

    //################# VERSIÓN 1 #############################

    /*public LiveData<Map<Integer, Map<String, Object>>> getRateFisioDay (String usuario, String YYYY, String MM, String DD) {
        MutableLiveData<Map<Integer,Map<String, Object>>> liveData = new MutableLiveData<>();
        Map<Integer,Map<String, Object>> dataMap = new HashMap<>(); //Guarda hora y media de los valores
        Map<Integer, Integer> cantidad = new HashMap<>(); //Guarda cantidad de registros en la hora

        //Calculamos la ruta
        String route = R_FISIO + "/" + usuario + "/" + YYYY + "/" + MM + "/" + DD;

        //Log.d(TAG, "Valor ruta:" + route);

        db.collection(route)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //Log.d(TAG, document.getId() + " => " + document.getData());

                                //Encontrar la posición del primer guión bajo
                                int indice = document.getId().indexOf('_');

                                //Log.d(TAG, "Valor indice: " + indice);

                                //Log.d(TAG, document.getId() + " => " + document.getData());

                                //Extraer la subcadena antes del primer guión bajo
                                String subcadena = document.getId().substring(0, indice);

                                //Log.d(TAG, "Valor subcadena: " + subcadena);

                                //Convertir la subcadena en un entero
                                int numero = Integer.parseInt(subcadena);

                                //Log.d(TAG, "Antes de contar");

                                //Comprobamos si existe ya este número como índice
                                if (dataMap.containsKey(numero)) {
                                    Map<String, Object> suma = dataMap.get(numero);

                                    //Sumamos los valores
                                    suma.put(BVP, (Double)suma.get(BVP) + (Double)document.getData().get(BVP));
                                    suma.put(EDA, (Double)suma.get(EDA) + (Double)document.getData().get(EDA));
                                    suma.put(HR, (Double)suma.get(HR) + (Double)document.getData().get(HR));
                                    suma.put(TMP, (Double)suma.get(TMP) + (Double)document.getData().get(TMP));

                                    //Y actualizamos los datos
                                    dataMap.put(numero, suma);

                                    //Añadimos el registro a la cantidad
                                    cantidad.put(numero, cantidad.get(numero) + 1);
                                }
                                else {
                                    //Si no existe el registro, lo añadimos

                                    Map<String, Object> nuevo = new HashMap<>();
                                    nuevo.put(EDA, document.getData().get(EDA));
                                    nuevo.put(HR, document.getData().get(HR));
                                    nuevo.put(TMP, document.getData().get(TMP));
                                    nuevo.put(BVP, document.getData().get(BVP));
                                    dataMap.put(numero, nuevo);

                                    //Añadimos la nueva cantidad
                                    cantidad.put(numero, 1);
                                }
                            }

                            //Hacemos las medias a través de los datos sumados y las cantidades recogidas
                            //Recorremos las horas del día, desde 0 hasta 23
                            for (int i = 0; i < 24; i++) {
                                if (cantidad.containsKey(i)) {
                                    int num = cantidad.get(i);

                                    Map<String, Object> media = new HashMap<>(dataMap.get(i));

                                    //Hacemos las medias de cada registro
                                    media.put(BVP, (Double) media.get(BVP) / (num * 1.0));
                                    media.put(EDA, (Double) media.get(EDA) / (num * 1.0));
                                    media.put(HR, (Double) media.get(HR) / (num * 1.0));
                                    media.put(TMP, (Double) media.get(TMP) / (num * 1.0));

                                    //Actualizamos los valores
                                    dataMap.put(i, media);
                                }
                            }

                            /*if (dataMap.isEmpty()) {
                                Map<String, Object> nuevo = new HashMap<>();
                                nuevo.put(EDA, 0.0);
                                nuevo.put(HR, 0.0);
                                nuevo.put(TMP, 0.0);
                                nuevo.put(BVP, 0.0);
                                dataMap.put(1, nuevo);
                                liveData.setValue(dataMap);
                            }*/

                            //Log.d(TAG, "Valores dataMap: " + dataMap);
                            /*liveData.setValue(dataMap);

                            //liveData.setValue(dataMap);

                            // Llamamos al listener cuando se hayan obtenido y calculado los datos
                            //listener.onDataFetched(dataMap);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        return liveData;
    }*/

    /**
     * @title getRateFisioMonth
     * @description Método que devuelve la media de los análisis que se obtuvieron cada día de ese mes
     * @param usuario Usuario (HASH) identificativo
     * @param YYYY Año del que se quiere obtener la media
     * @param MM Mes del que se quiere obtener la media
     * @return Un Map donde la Key es el día del mes y el valor es otro Map con key tipo de análisis y valor su media del día
     *
     * @warning Se sobre entiende que los valores que se pasen son los correctos
     */
    public LiveData<Map<Integer, Map<String, Object>>> getRateFisioMonth (String usuario, String YYYY, String MM) {
        MutableLiveData<Map<Integer,Map<String, Object>>> liveData = new MutableLiveData<>();
        Map<Integer, Map<String, Object>> dataMap = new HashMap<>();

        //Sacamos cuantos días tiene el mes actual
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        //El mes tiene que ir como valor de dos cifras
        String MM_string = MM.length() == 1 ? "0"+MM : MM;

        Log.d("DATABASE", "Mes String: " + MM_string);
        LocalDate ld = LocalDate.parse(YYYY + "-" + MM_string + "-01", dtf);
        int totalDays = ld.lengthOfMonth();
        Log.d("DATABASE", "Total días: " + totalDays);
        //Variable para contar los días procesados
        AtomicInteger daysProcessed = new AtomicInteger(0);

        try {

            //Vamos sacando las medias de cada día del mes actual
            for (int i = 1; i <= totalDays; i++) {
                String DD = (Integer.toString(i).length() == 1) ? "0" + Integer.toString(i) : Integer.toString(i);

                final int dia = i;

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                Date startDate = dateFormat.parse(YYYY + "-" + MM_string + "-" + DD);

                //Inicializamos la hora
                startDate.setHours(0);
                startDate.setMinutes(0);
                startDate.setSeconds(0);

                Date endDate = (Date) startDate.clone();

                endDate.setHours(23);
                endDate.setMinutes(59);
                endDate.setSeconds(59);

                Timestamp startTimestamp = new Timestamp(startDate);
                Timestamp endTimestamp = new Timestamp(endDate);

                db.collection(USUARIOS)
                        .document(usuario)
                        .collection(R_FISIO)
                        .whereGreaterThanOrEqualTo("fecha", startTimestamp)
                        .whereLessThanOrEqualTo("fecha", endTimestamp)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                Map<String, Object> suma = new HashMap<>();
                                int cantidad = 0;

                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    if (cantidad == 0) {
                                        //suma.put(EDA, document.getData().get(EDA));
                                        suma.put(HR, document.getData().get(HR));
                                        //suma.put(BVP, document.getData().get(BVP));
                                        suma.put(TMP, document.getData().get(TMP));
                                    } else {
                                        //suma.put(EDA, (Double) suma.get(EDA) + (Double) document.getData().get(EDA));
                                        suma.put(HR, (Double) suma.get(HR) + (Double) document.getData().get(HR));
                                        //suma.put(BVP, (Double) suma.get(BVP) + (Double) document.getData().get(BVP));
                                        suma.put(TMP, (Double) suma.get(TMP) + (Double) document.getData().get(TMP));
                                    }

                                    cantidad++;
                                }

                                //Hacemos la media de esa cantidad de valores
                                if (cantidad > 0) {
                                    //suma.put(EDA, (Double) suma.get(EDA) / cantidad);
                                    suma.put(HR, (Double) suma.get(HR) / cantidad);
                                    //suma.put(BVP, (Double) suma.get(BVP) / cantidad);
                                    suma.put(TMP, (Double) suma.get(TMP) / cantidad);

                                    //Añadimos la media con su día correspondiente
                                    dataMap.put(dia, suma);
                                }

                                // Incrementa el contador de días procesados y verifica si se ha completado el procesamiento de todos los días
                                if (daysProcessed.incrementAndGet() == totalDays) {
                                    liveData.setValue(dataMap);
                                    Log.d(TAG, "DataMap valor final: " + liveData.getValue());
                                }
                            } else {
                                Log.d(TAG, "No se encontraron resultados para la búsqueda");

                                // Incrementa el contador de días procesados y verifica si se ha completado el procesamiento de todos los días
                                if (daysProcessed.incrementAndGet() == totalDays) {
                                    liveData.setValue(dataMap);
                                    Log.d(TAG, "DataMap valor final: " + liveData.getValue());
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Error al consultar documentos: ", e);
                        });
            }
        } catch(ParseException e){
            e.printStackTrace();
        }

        return liveData;
    }

    //############### VERSIÓN 1 ######################

    /*public LiveData<Map<Integer, Map<String, Object>>> getRateFisioMonth (String usuario, String YYYY, String MM) {
        MutableLiveData<Map<Integer,Map<String, Object>>> liveData = new MutableLiveData<>();
        Map<Integer, Map<String, Object>> dataMap = new HashMap<>();

        //Sacamos cuantos días tiene el mes actual
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        //El mes tiene que ir como valor de dos cifras
        String MM_string = MM.length() == 1 ? "0"+MM : MM;

        Log.d("DATABASE", "Mes String: " + MM_string);
        LocalDate ld = LocalDate.parse(YYYY + "-" + MM_string + "-01", dtf);
        int totalDays = ld.lengthOfMonth();
        Log.d("DATABASE", "Total días: " + totalDays);
        //Variable para contar los días procesados
        AtomicInteger daysProcessed = new AtomicInteger(0);

            //Vamos sacando las medias de cada día del mes actual
            for (int i = 1; i <= totalDays; i++) {
                String DD = (Integer.toString(i).length() == 1) ? "0" + Integer.toString(i) : Integer.toString(i);

                final int dia = i;

                // Observa los datos del día específico
                getRateFisioDay(usuario, YYYY, MM, DD).observeForever(new Observer<Map<Integer, Map<String, Object>>>() {
                @Override
                public void onChanged(Map<Integer, Map<String, Object>> rateDay) {
                        Log.d("DATABASE", "Valores de data mes dia " + DD + ": " + rateDay);
                        int horas = rateDay.size();

                        rateDay.forEach((k, v) ->
                        {
                            Map<String, Object> suma = new HashMap<>();

                            //Si ya existe sumamos los valores nuevos con los que ya tenemos y los superponemos
                            if (dataMap.containsKey(dia)) {
                                suma.put(EDA, (Double) v.get(EDA) + (Double) dataMap.get(dia).get(EDA));
                                suma.put(HR, (Double) v.get(HR) + (Double) dataMap.get(dia).get(HR));
                                suma.put(TMP, (Double) v.get(TMP) + (Double) dataMap.get(dia).get(TMP));
                                suma.put(BVP, (Double) v.get(BVP) + (Double) dataMap.get(dia).get(BVP));
                                dataMap.put(dia, suma);
                            }
                            //Si no existe añadimos el valor que tenemos
                            else {
                                Map<String, Object> nuevo = new HashMap<>();
                                nuevo.put(EDA, v.get(EDA));
                                nuevo.put(HR, v.get(HR));
                                nuevo.put(TMP, v.get(TMP));
                                nuevo.put(BVP, v.get(BVP));
                                dataMap.put(dia, nuevo);
                            }
                        });

                        //Ahora hacemos la media de las sumas de los valores obtenidos
                        if (dataMap.containsKey(dia)) {
                            Map<String, Object> orig = dataMap.get(dia);

                            dataMap.get(dia).put(EDA, (Double) orig.get(EDA) / horas * 1.0f);
                            dataMap.get(dia).put(HR, (Double) orig.get(HR) / horas * 1.0f);
                            dataMap.get(dia).put(TMP, (Double) orig.get(TMP) / horas * 1.0f);
                            dataMap.get(dia).put(BVP, (Double) orig.get(BVP) / horas * 1.0f);
                        }

                        // Incrementa el contador de días procesados y verifica si se ha completado el procesamiento de todos los días
                        if (daysProcessed.incrementAndGet() == totalDays) {
                            liveData.setValue(dataMap);
                            Log.d(TAG, "DataMap valor final: " + liveData.getValue());
                        }
                    }
            });
            }

        return liveData;
    }*/

    //#######################################################################################
    //#                                 DATOS PERSONALES                                    #
    //#######################################################################################
}
