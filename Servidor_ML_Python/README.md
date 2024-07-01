# Servidor para la detección de Actividades de la Vida Diaria

Servidor que ejecuta algoritmos de aprendizaje automático desarrollados en Python para poder detectar actividades de la vida diaria.

Se divide en diferentes archivos:
- **run.py** es el programa principal el cual abrirá dos accesos "/predict" donde, a través de los datos que le lleguen enviará una respuesta con las actividades detectadas vinculadas a esos datos, y "/train" que servirá para entrenar al algoritmo a través de etiquetado de actividades.
- **datos_multiclase.csv** matriz de datos que sirven tanto para inicializar como para entrenar al algoritmo de aprendizaje automático.
- **addDataTrain.py** es el programa para inicializar el documento **datos_multiclase.csv** a través de una interfaz por terminal pedirá una serie de datos para crear dicho documento.
- **credenciales.json** las credenciales de la base de datos donde se almacenan los datos de los usuarios.

---

## Instalación

Para poder ejecutar los programas se necesitarán seguir los diferentes pasos.

Actualizar e instalar los diferentes paquetes para usar scikit-learn.

  sudo apt update
  sudo apt install python3-sklearn python3-sklearn-lib
  sudo apt install python3.12-venv

Crear el entorno para ejecutar Python3

  python3 -m venv sklearn-env
  source sklearn-env/bin/activate

Instalar todos los paquetes necesarios

  pip3 install -U sctik-learn
  pip3 install firebase-admin
  pip3 install flask

Y ya sólo nos quedaría ejecutarlo

  sudo sklearn-env/bin/python3 run.py

## Nota importante

Cada vez que se acceda a la ruta "/predict" dando los datos pertinentes, se generará un documento, si no existía antes, llamado **summary.log**. Dentro de este documento encontraremos el resultado de los algoritmos de aprendizaje automático y su precisión al predecir.
