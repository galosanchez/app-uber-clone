# App Uber Clone
Este proyecto es un clon de la aplicaci贸n Uber para Android. **(Proyecto personal)**

Ver el dise帽o [aqu铆](https://www.figma.com/file/EeuWLlpHQf0WdCGXtw74Qf/App-Uber-Clone).

## Construido con 馃敤
- [Android Studio](https://developer.android.com/studio) - IDE
- [Figma](https://www.figma.com/) - Creaci贸n del dise帽o
- [Firebase](https://firebase.google.com/) - Base de datos
	+ Authentication
	+ Realtime Database
	+ Cloud Messaging
	+ Cloud Storage
- [Google Cloud Platform](https://console.cloud.google.com/) - API Maps
	+ Directions API
	+ Maps SDK for Android
	+ Places API


## Caracter铆sticas

- Creaci贸n de una cuenta del cliente y conductor.
- El cliente puede ver a los conductores cercanos en tiempo real.
- Traza la ruta origen-destino del viaje.
- Permite realizar una petici贸n de viaje detallando el tiempo y distancia.
- El cliente y el conductor puede editar su perfil.

## Comenzando 馃殌
Agregar las llaves 馃攽 en el proyecto.

### Pre-requisitos
- Integrar el proyecto de Android Studio con Firebase.
- Cuenta en Google Cloud Platform.

### Archivo google-services
- Desde la consola de Firebase dirigirse a **Configuraci贸n de proyecto > General**
- Descargar el archivo **google-services.json**
![](https://i.imgur.com/8t1zY1d.jpg)
- Agregar el erchivo a la ruta del proyecto **app_uber_clone/app/**

### Clave de API
- Desde la consola de Google Cloud Platform dirigirse a **Menu > API y servicios > Credenciales**, crear una nueva credencial y copiar la llave.
![](https://i.imgur.com/lbL6RQA.jpg)
- Abrir el archivo **app_uber_clone/app/src/main/res/values/google_api.xml**
- Pegar la llave
```
<resources>
  <string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">YOUR_KEY_API</string>
</resources>
```

### Clave del servidor
- Desde la consola de Firebase dirigirse a **Configuraci贸n de proyecto > Cloud Messaging**
- Copiar la clave del servidor, en el caso de que no exista una clave de servidor primero crearlo.
![](https://i.imgur.com/wcVksTv.jpg)
- Abrir el archivo **app_uber_clone/app/src/main/java/com/galosanchez/appuberclone/retrofit/IFCMapi.java**
- Pegar la clave
```
String key_server ="YOUR_API_SERVER";
```

---

馃懆鈥嶐煉? por [Galo S谩nchez](https://github.com/galosanchez) 馃枻
