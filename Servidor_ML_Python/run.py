from flask import Flask, request, jsonify
import pandas as pd
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier, StackingClassifier
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import StandardScaler
import numpy as np
from collections import Counter
import pytz
import firebase_admin
from firebase_admin import credentials, firestore
from datetime import datetime
import csv
import os

app = Flask(__name__)

# Inicializar la aplicación Firebase con las credenciales descargadas
cred = credentials.Certificate("credenciales.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

###################################################################################
# INICIO FUNCIONES
###################################################################################

# Cálculo de las diferencias en latitud y longitud
def calculateDiffUbi(data):
        datos = []
        lonIt = 9999
        latIt = 9999
        for clave in data:
                if lonIt == 9999 and latIt == 9999:
                        clave["diffLat"] = 0
                        clave["diffLon"] = 0
                else:
                        clave["diffLat"] = clave.get("Lat") - latIt
                        clave["diffLon"] = clave.get("Lon") - lonIt
                latIt = clave.get("Lat")
                lonIt = clave.get("Lon")
                datos.append(clave)
        return datos

# Entrenar el modelo con datos iniciales
def load_training_data():
    return pd.read_csv('datos_multiclase.csv')

# Devuelve el contenido de la base de datos para procesar en el algoritmo
def getData(usuario, fecha_inicio, hora_inicio, fecha_final, hora_final):
    print(fecha_inicio+" "+hora_inicio+" "+fecha_final+" "+hora_final)
    # Crear timestamps y convertirlos a UTC
    #local_tz = pytz.timezone("Europe/Madrid")
    #startDate_local = pd.Timestamp(fecha_inicio + " " + hora_inicio).tz_localize(local_tz)
    #endDate_local = pd.Timestamp(fecha_final + " " + hora_final).tz_localize(local_tz)
    #startDate = startDate_local.tz_convert(pytz.utc)
    #endDate = endDate_local.tz_convert(pytz.utc)
    # Crear timestamps y convertirlos a UTC
    local_tz = pytz.timezone("Europe/Madrid")
    startDate_local = pd.to_datetime(fecha_inicio + " " + hora_inicio, format='%d/%m/%Y %H:%M:%S').tz_localize(local_tz)
    endDate_local = pd.to_datetime(fecha_final + " " + hora_final, format='%d/%m/%Y %H:%M:%S').tz_localize(local_tz)
    startDate = startDate_local
    #.tz_convert(pytz.utc)
    endDate = endDate_local
    #.tz_convert(pytz.utc)

    print(startDate)
    print(endDate)

    # Hacemos la consulta a la base de datos
    collection = db.collection("Usuarios/" + usuario + "/datosFisiologicos")
    query = collection.where('fecha', '>=', startDate).where('fecha', '<=', endDate)

    # Ejecutamos la consulta
    docs = query.stream()

    #print(docs.to_dict())

    # Procesamos cada documento
    HR =[]
    AC_x = []
    AC_y = []
    AC_z = []
    diffLat = []
    diffLon = []
    Lat = []
    Lon = []
    Tmp = []
    UBIS = []
    lon = 9999
    lat = 9999

    for doc in docs:
        datos = doc.to_dict()
        HR.append(datos.get("HR"))
        AC = datos.get("XLR8")
        AC_x.append(AC.get("x"))
        AC_y.append(AC.get("y"))
        AC_z.append(AC.get("z"))
        Tmp.append(datos.get("TMP"))
        UBI = datos.get("UBI")
        UBIS.append(UBI)
        if lat == 9999 and lon == 9999:
                diffLon.append(0)
                diffLat.append(0)
        else:
                diffLon.append(UBI.get("longitude") - lon)
                diffLat.append(UBI.get("latitude") - lat)

        lon = UBI.get("longitude")
        lat = UBI.get("latitude")
        Lat.append(lat)
        Lon.append(lon)

    data = []

    for i in range(0, len(HR)):
        data.append({"HR": HR[i], "AccX": AC_x[i], "AccY": AC_y[i], "AccZ": AC_z[i], "Temperature": Tmp[i], "Lat": Lat[i], "Lon": Lon[i], "diffLat": diffLat[i], "diffLon": diffLon[i], "UBI": UBIS[i]})

    return data

def getDataTrain(usuario, fecha_inicio, hora_inicio, fecha_final, hora_final, actividad):
    print(fecha_inicio+" "+hora_inicio+" "+fecha_final+" "+hora_final)
    # Crear timestamps y convertirlos a UTC
    local_tz = pytz.timezone("Europe/Madrid")
    startDate_local = pd.to_datetime(fecha_inicio + " " + hora_inicio, format='%d/%m/%Y %H:%M:%S').tz_localize(local_tz)
    endDate_local = pd.to_datetime(fecha_final + " " + hora_final, format='%d/%m/%Y %H:%M:%S').tz_localize(local_tz)
    startDate = startDate_local
    #.tz_convert(pytz.utc)
    endDate = endDate_local
    #.tz_convert(pytz.utc)

    print(startDate)
    print(endDate)

    # Hacemos la consulta a la base de datos
    collection = db.collection("Usuarios/" + usuario + "/datosFisiologicos")
    query = collection.where('fecha', '>=', startDate).where('fecha', '<=', endDate)

    # Ejecutamos la consulta
    docs = query.stream()

    #print(docs.to_dict())

    # Procesamos cada documento
    HR =[]
    AC_x = []
    AC_y = []
    AC_z = []
    diffLat = []
    diffLon = []
    Lat = []
    Lon = []
    Tmp = []
    etiqueta = []
    UBIS = []
    lon = 9999
    lat = 9999

    for doc in docs:
        datos = doc.to_dict()
        HR.append(datos.get("HR"))
        AC = datos.get("XLR8")
        AC_x.append(AC.get("x"))
        AC_y.append(AC.get("y"))
        AC_z.append(AC.get("z"))
        Tmp.append(datos.get("TMP"))
        UBI = datos.get("UBI")
        UBIS.append(UBI)
        if lat == 9999 and lon == 9999:
                diffLon.append(0)
                diffLat.append(0)
        else:
                diffLon.append(UBI.get("longitude") - lon)
                diffLat.append(UBI.get("latitude") - lat)

        lon = UBI.get("longitude")
        lat = UBI.get("latitude")
        Lat.append(lat)
        Lon.append(lon)
        etiqueta.append(actividad)

    data = []

    for i in range(0, len(HR)):
        data.append({"HR": HR[i], "AccX": AC_x[i], "AccY": AC_y[i], "AccZ": AC_z[i], "Temperature": Tmp[i], "Lat": Lat[i], "Lon": Lon[i], "diffLat": diffLat[i], "diffLon": diffLon[i], "Activity": etiqueta[i]})

    return data

# Función calculaUbicaciones
def calculaUbicaciones(usuario, ubicaciones):
    ubicacionesUsuario = db.collection("Usuarios").document(usuario).get().to_dict().get("ubicaciones")
    #print("Se han obtenido los datos de la db")
    conteoUbicaciones = {}
    RADIO = 0.0005

    for ubi in ubicaciones:
        for key in ubicacionesUsuario:
            latitude_diff = abs(ubicacionesUsuario.get(key).get("latitude") - ubi.get("UBI").get("latitude"))
            longitude_diff = abs(ubicacionesUsuario.get(key).get("longitude") - ubi.get("UBI").get("longitude"))
            if latitude_diff <= RADIO and longitude_diff <= RADIO:
                resKey = "Ha estado en " + key
                if conteoUbicaciones.get(resKey) is None:
                    conteoUbicaciones[resKey] = 0
                conteoUbicaciones[resKey] += 1
                break

    return conteoUbicaciones

###################################################################################
# FIN FUNCIONES
###################################################################################

# Inicializar los modelos base y el meta-modelo
base_estimators = [
    ('lr', make_pipeline(StandardScaler(), LogisticRegression(max_iter=1000))),
    ('rf', RandomForestClassifier(n_estimators=100))
]
meta_estimator = LogisticRegression(max_iter=1000)

# Configurar el ensamblado con Stacking
stacking_model = StackingClassifier(
    estimators=base_estimators,
    final_estimator=meta_estimator
)

data = load_training_data()
#X = data[['HR', 'AccX', 'AccY', 'AccZ']]
X = data[['HR', 'AccX', 'AccY', 'AccZ', 'Temperature', 'Lat', 'Lon', 'diffLat', 'diffLon']]
y = data['Activity']
stacking_model.fit(X, y)

@app.route('/predict', methods=['POST'])
def predict():
    try:
        # Obtener los datos del request
        data = request.get_json()

        usuario = data["usuario"]
        fecha_inicio = data["fecha_inicio"]
        hora_inicio = data["hora_inicio"]
        fecha_final = data["fecha_final"]
        hora_final = data["hora_final"]

        datos = getData(usuario, fecha_inicio, hora_inicio, fecha_final, hora_final)

        #print(datos)

        if not datos:
            return jsonify({"error": "No se encontraron datos para el rango de fechas y horas proporcionado."}), 400

        features = pd.DataFrame(datos, columns=['HR', 'AccX', 'AccY', 'AccZ', 'Temperature', 'Lat', 'Lon', 'diffLat', 'diffLon'])

        if features.empty:
            return jsonify({"error": "No se encontraron datos para el rango de fechas y horas proporcionado."}), 400

        # Realizar predicciones con el modelo de stacking
        predictions = stacking_model.predict(features)

        # Contar la frecuencia de cada actividad predicha
        activity_counts = Counter(predictions)

        # Convertir el contador a un diccionario para devolverlo como JSON
        activity_summary = dict(activity_counts)

        ubicaciones = calculaUbicaciones(data["usuario"], datos)

        #print(datos)

        activity_summary.update(ubicaciones)

        # Define el formato de la cadena de fecha y hora
        #formato = "%d/%m/%Y %H:%M:%S"

        # Convierte las cadenas en objetos datetime
        #fecha_hora_inicio = datetime.strptime(fecha_inicio+" "+hora_inicio, formato)
        #fecha_hora_final = datetime.strptime(fecha_final+" "+hora_final, formato)

        # Calcula la diferencia entre las dos fechas y horas
        #diferencia = fecha_hora_final - fecha_hora_inicio

        # Convierte la diferencia en minutos
        #diferencia_minutos = diferencia.total_seconds() / 60.0

        #print(diferencia_minutos)

        #if diferencia_minutos == 0:
        #    return jsonify({"error": "La diferencia de tiempo no puede ser cero."}), 400

        # Calculamos los porcentajes de cada actividad
        #for activity in activity_summary:
        #    activity_summary[activity] = activity_summary[activity] / diferencia_minutos * 100

        return jsonify(activity_summary)
    except Exception as e:
        return jsonify({"error": str(e)}), 400

@app.route('/train', methods=['POST'])
def train():
    try:
        # Obtener los datos de entrenamiento del request
        data = request.json

        usuario = data["usuario"]
        fecha_inicio = data["fecha_inicio"]
        hora_inicio = data["hora_inicio"]
        fecha_final = data["fecha_final"]
        hora_final = data["hora_final"]
        actividad = data["activity"]

        datos = getDataTrain(usuario, fecha_inicio, hora_inicio, fecha_final, hora_final, actividad)
        if not datos:
            return jsonify({"error": "No se encontraron datos para el rango de fechas y horas proporcionado."}), 400

        filename = "datos_multiclase.csv"

        # Abrir el archivo en modo de añadir (append)
        with open(filename, mode='a', newline='') as file:
            writer = csv.DictWriter(file, fieldnames=["HR", "AccX", "AccY", "AccZ", "Temperature", "Lat", "Lon", "diffLat", "diffLon", "Activity"])

            # Verificar si el archivo existe y tiene contenido
            if os.path.isfile(filename) and os.path.getsize(filename) == 0:
                writer.writeheader()

            # Escribir las nuevas filas en el archivo CSV
            writer.writerows(datos)

        print(f"Nuevos datos añadidos al archivo {filename} exitosamente.")

        #df = pd.DataFrame(datos)
        
        # Separar características y etiquetas
        #X_new = df[['HR', 'AccX', 'AccY', 'AccZ', 'Temperature', 'diffLat', 'diffLon']]
        #y_new = df['Activity']
        
        # Entrenar el modelo de manera incremental
        #stacking_model.fit(X_new, y_new)
        
        data = load_training_data()
        X = data[['HR', 'AccX', 'AccY', 'AccZ', 'Temperature', 'Lat', 'Lon', 'diffLat', 'diffLon']]
        y = data['Activity']
        stacking_model.fit(X, y)
        return jsonify({'message': 'Modelo entrenado con nuevos datos'})
    except Exception as e:
        return jsonify({"error": str(e)}), 400

@app.route('/activities', methods=['POST'])
def activities():
    try:
        return jsonify({'valores': activities})
    except Exception as e:
        return jsonify({'error': str(e)}), 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=80)