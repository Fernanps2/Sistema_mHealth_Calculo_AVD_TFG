# Sistema_mHealth_Calculo_AVD_TFG
**Autor:** Fernando Molina Delgado
 
 Aplicación wereable capaz de analizar las actividades diarias que realiza el usuario a través de los sensores de los dispositivos wereables que conecte, en este caso, dispositivo empatica e4.

### Partes del repositorio
 - **MoldelFit App**: Código de la aplicación móvil, es la interacción principal del usuario, escrita en Java usando el entorno de desarrollo de aplicaciones móviles Android Studio.
 - **Server ML**: Código del entorno Machine Learning, usada para el cálculo de las actividades del usuario, escrita en Python.

### Referencias de código/bibliotecas externas usadas
 - **ModelFit App**:
   - Código del que se parte: [Ejemplo app empatica oficial](https://github.com/empatica/e4link-sample-project-android)
   - Biblioteca para las gráficas usadas: [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
 - **MachineLearning Server**:
   - Biblioteca para los algoritmos de Machine Learning: [scikit-learn](https://scikit-learn.org/stable/install.html)
