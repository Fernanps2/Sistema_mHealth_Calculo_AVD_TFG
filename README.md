# Sistema_mHealth_Calculo_AVD_TFG
**Autor:** Fernando Molina Delgado
 
 Aplicación móvil que conecta con un dispositivo wearable (pulsera Empatica E4) obteniendo sus datos, a través de los sensores de la pulsera y del teléfono móvil, y almacenándolos para posteriormente ser capaz de detectar las actividades de la vida diaria (AVD) que realiza el usuario. 

 Para el proyecto se han desarrollado diferentes componentes que componen el sistema:
 - **Dispositivo wearable**: Se ha utilizado la API de Empatica E4 para conectarse a este dispositivo a través de Bluetooth.
 - **Aplicación móvil**: Se ha desarrollado en Android Studio con el lenguaje de programación Java.
 - **Servidor base de datos**: Creado en Firebase con la herramienta Firestore con una cuenta propia de Google.
 - **Servidor para los algoritmos de detección de AVD**: Desplegado en Amazon Web Service y desarrollado en Python usando la biblioteca scikit-learn para el uso de algoritmos de aprendizaje automático con el objetivo de detectar AVDs. Además, se ha implementado un sistema basado en reglas para complementar los algoritmos de aprendizaje automático.

---

## Estructura del repositorio

Los archivos que se ven en la raíz del repositorio son los archivos a importar en Android Studio, salvo las siguientes carpetas:

- **Servidor_ML_Python**: Contiene los programas escritos en python para ejecutar en un servidor.
- **imagenesReadme**: Contienen las imágenes que se usarán en este archivo README.md.

---

## Instalación

Para poder usar el código de la app será necesario descargar la versión más reciente de [Android Studio](https://developer.android.com/studio?hl=es-419), en mi caso es la versión Android Studio Iguana.

Simplemente clonar el repositorio, abrir Android Studio e importar el proyecto. 

Puede ser que al abrir y buildearlo salte un error de dependencia y que necesita el gradle versión 8.4 o así. Para poder arreglar dicho problema, descargaremos la versión gradle 8.4 en las [releases](https://gradle.org/releases/), iremos a la raíz del proyecto y ejecutaremos lo siguiente.

         cd /ruta/a/Sistema_mHealth_Calculo_AVD_TFG/app
         #Deberemos haber almacenado los archivos de la descarga en app/.gradle
         ./.gradle/gradle-8.4/bin/gradle wrapper --gradle-version 8.4
         
Además, necesitaremos descargar la [API de Empatica E4](https://developer.empatica.com) (E4link-1.0.0.aar) y añadirla a una carpeta que deberemos crear en la ubicación app/libs. 

Al subir el código al repositorio parece ser que estos últimos archivos mencionados no se pueden llegar a subir. 

---

 ## Referencias
 - Código del que se parte: [Ejemplo app empatica oficial](https://github.com/empatica/e4link-sample-project-android)
 - Biblioteca para las gráficas usadas: [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
