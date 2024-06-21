import firebase_admin
from firebase_admin import credentials, firestore
from datetime import datetime
import csv
import os
import pandas as pd
import pytz

filename = "datos_multiclase.csv"

# Verificar si el archivo existe y tiene contenido
if os.path.isfile(filename) and os.path.getsize(filename) > 0:
    print("El archivo no está vacío.")
else:
    print("El archivo está vacío o no existe.")

# Inicializar la aplicación Firebase con las credenciales descargadas
cred = credentials.Certificate("credenciales.json")
firebase_admin.initialize_app(cred)

# Capturamos los datos de entrada
print("Hola, buenas. Este programa capturará datos de la base de datos para crear o añadir a un archivo los datos para entrenar al algoritmo Machine Learning")
print("Introduzca fecha de incio (formato dd/mm/yyyy): ")
fecha_origen = input()

print("Introduzca hora de inicio (formato hh:mm:ss, defecto 00:00:00): ")
hora_origen = input()

print("Introduzca fecha de final (formato dd/mm/yyyy): ")
fecha_final = input()

print("Introduzca hora de inicio (formato hh:mm:ss, defecto 00:00:00): ")
hora_final = input()

print("¿Qué tipo de actividad se ha realizado durante este tiempo?")
tipo = input()

# Filtramos los datos de entrada
# Comprobamos si está vacío y si es así introducimos la hora por defecto
if len(hora_origen) < 1:
        hora_origen = "00:00:00"
if len(hora_final) < 1:
        hora_final = "00:00:00"

# Crear timestamps y convertirlos a UTC
local_tz = pytz.timezone("Europe/Madrid")
startDate_local = pd.Timestamp(fecha_origen + " " + hora_origen).tz_localize(local_tz)
endDate_local = pd.Timestamp(fecha_final + " " + hora_final).tz_localize(local_tz)

#startDate = startDate_local.tz_convert(pytz.utc)
#endDate = endDate_local.tz_convert(pytz.utc)

print(startDate_local)
print(endDate_local)

# Obtener una referencia a la base de datos
db = firestore.client()

# Hacemos la consulta a la base de datos
collection = db.collection("Usuarios/trainer/datosFisiologicos")
query = collection.where('fecha', '>=', startDate_local).where('fecha', '<=', endDate_local)

# Ejecutamos la consulta
docs = query.stream()

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
        etiqueta.append(tipo)

# Guardamos los datos en un .csv

data = []

for i in range(0, len(HR)):
        data.append({"HR": HR[i], "AccX": AC_x[i], "AccY": AC_y[i], "AccZ": AC_z[i], "Temperature": Tmp[i], "Lat": Lat[i], "Lon": Lon[i], "diffLat": diffLat[i], "diffLon": diffLon[i], "Activity": etiqueta[i]})

filename = "datos_multiclase.csv"

# Abrir el archivo en modo de añadir (append)
with open(filename, mode='a', newline='') as file:
    writer = csv.DictWriter(file, fieldnames=["HR", "AccX", "AccY", "AccZ", "Temperature", "Lat", "Lon", "diffLat", "diffLon", "Activity"])

    # Verificar si el archivo existe y tiene contenido
    if os.path.isfile(filename) and os.path.getsize(filename) == 0:
        writer.writeheader()

    # Escribir las nuevas filas en el archivo CSV
    writer.writerows(data)

print(f"Nuevos datos añadidos al archivo {filename} exitosamente.")