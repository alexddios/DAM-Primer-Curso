# Ruta 1 — Corrutinas de Kotlin y peticiones HTTP

**← [[00 - Unidad 5 Resumen|Unidad 5]]** | **→ [[02 - Repositorio e Imagenes|Ruta 2]]**

---

## El problema: las operaciones lentas bloquean la UI

Cuando Android arranca una app, todo el código se ejecuta en un único hilo llamado **hilo principal** (*main thread* o *UI thread*). Este hilo es el responsable de:
- Dibujar la interfaz de usuario
- Responder a los toques del usuario
- Procesar eventos de la UI

Si en ese hilo haces algo lento, como descargar datos de Internet (puede tardar segundos), el hilo se **bloquea** completamente. La UI se congela, el usuario ve la pantalla paralizada, y si la app no responde en 5 segundos Android muestra el diálogo "La aplicación no responde" (ANR) y propone cerrarla.

La solución es ejecutar las operaciones lentas **fuera del hilo principal**, en un hilo secundario. Kotlin lo resuelve de forma elegante con las **corrutinas**.

---

## Corrutinas de Kotlin

Las corrutinas son la herramienta de Kotlin para escribir código asíncrono de forma que parece síncrono (secuencial y fácil de leer), sin el lío de callbacks anidados o la complejidad de los threads manuales.

### Función `suspend`

Una función `suspend` es una función que puede **pausarse** en medio de su ejecución (mientras espera algo lento) y **reanudarse** después, sin bloquear el hilo en el que se ejecuta.

```kotlin
suspend fun obtenerDatosDeRed(): String {
    delay(2000)      // pausa 2 segundos SIN bloquear el hilo
    return "datos"
}
```

Restricción: solo puedes llamar a una función `suspend` desde otra función `suspend` o desde dentro de una corrutina.

### Lanzar corrutinas: `viewModelScope.launch`

Para iniciar una corrutina desde el ViewModel se usa `viewModelScope.launch {}`:

```kotlin
class MiViewModel : ViewModel() {
    fun cargarDatos() {
        viewModelScope.launch {
            // Este bloque se ejecuta como una corrutina
            val resultado = obtenerDatosDeRed()   // función suspend: espera sin bloquear
            _uiState.update { it.copy(datos = resultado) }
        }
    }
}
```

El `viewModelScope` es un `CoroutineScope` vinculado al ciclo de vida del ViewModel: cuando el ViewModel se destruye, cancela automáticamente todas las corrutinas que lanzó. Así evitas fugas de memoria.

### Dispatchers — en qué hilo corre la corrutina

Un `Dispatcher` indica en qué hilo o grupo de hilos se ejecuta la corrutina:

| Dispatcher | Cuándo usarlo |
|-----------|--------------|
| `Dispatchers.Main` | Para actualizar la UI. Es el hilo principal. |
| `Dispatchers.IO` | Para red, base de datos, archivos — operaciones de entrada/salida. |
| `Dispatchers.Default` | Para cálculos intensivos de CPU (ordenar listas enormes, procesar imágenes...). |

`viewModelScope.launch {}` usa `Dispatchers.Main` por defecto. Retrofit (la librería que usaremos para la red) gestiona internamente el cambio a `Dispatchers.IO`, así que normalmente no tienes que preocuparte.

Si necesitas cambiar de dispatcher explícitamente:

```kotlin
viewModelScope.launch {
    // estamos en Main
    val resultado = withContext(Dispatchers.IO) {
        // estamos en IO: aquí podemos hacer operaciones lentas
        baseDeDatos.consultarTodo()
    }
    // volvemos automáticamente a Main
    _uiState.update { it.copy(items = resultado) }
}
```

### Gestionar errores en corrutinas

Las llamadas de red pueden fallar por muchas razones: sin conexión, servidor caído, timeout... Envuelve el código en `try-catch`:

```kotlin
viewModelScope.launch {
    _uiState.update { it.copy(estado = Estado.Cargando) }
    try {
        val fotos = marsApiService.getFotos()
        _uiState.update { it.copy(estado = Estado.Exito(fotos)) }
    } catch (e: IOException) {
        // Sin conexión a Internet
        _uiState.update { it.copy(estado = Estado.Error) }
    } catch (e: HttpException) {
        // Respuesta del servidor con error (4xx, 5xx)
        _uiState.update { it.copy(estado = Estado.Error) }
    }
}
```

---

## HTTP y REST

La mayoría de apps obtienen datos de servidores web mediante peticiones **HTTP**. El patrón más común es **REST** (*Representational State Transfer*):

1. La app hace una petición HTTP a una **URL** (llamada *endpoint*)
2. El servidor procesa la petición y responde con datos en formato **JSON**
3. La app parsea el JSON y usa los datos

### Métodos HTTP

| Método | Para qué |
|--------|----------|
| `GET` | Obtener datos (el más común para leer) |
| `POST` | Enviar datos nuevos al servidor |
| `PUT` / `PATCH` | Actualizar datos existentes |
| `DELETE` | Eliminar datos |

### JSON

JSON (*JavaScript Object Notation*) es el formato de texto estándar para intercambiar datos entre apps y servidores. Ejemplo de respuesta de una API de fotos de Marte:

```json
[
  {
    "id": "424906",
    "img_src": "https://mars.nasa.gov/msl-raw-images/NRB_726536758EDR.JPG"
  },
  {
    "id": "424907",
    "img_src": "https://mars.nasa.gov/msl-raw-images/NRB_726536759EDR.JPG"
  }
]
```

Un array JSON (`[ ]`) corresponde a una `List` en Kotlin. Un objeto JSON (`{ }`) corresponde a una `data class`.

---

## Retrofit — cliente HTTP para Android

Retrofit es la librería más usada para hacer peticiones HTTP en Android. Convierte automáticamente el JSON de la respuesta en objetos Kotlin.

### Dependencias

```kotlin
// build.gradle.kts (módulo app)
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
```

`converter-gson` le dice a Retrofit que use la librería Gson para convertir JSON ↔ objetos Kotlin.

### Paso 1: modelo de datos

Crea una `data class` que represente los datos que recibirás. Los nombres de los campos deben coincidir con los del JSON, o usar `@SerializedName` para renombrarlos:

```kotlin
data class MarsPhoto(
    val id: String,
    @SerializedName("img_src") val imgSrc: String
    // "img_src" es el nombre en el JSON, imgSrc es el nombre en Kotlin
)
```

### Paso 2: interfaz de la API

Define una interfaz con los endpoints disponibles. Cada función representa un endpoint:

```kotlin
interface MarsApiService {
    @GET("photos")               // petición GET a BASE_URL + "photos"
    suspend fun getPhotos(): List<MarsPhoto>
}
```

La función es `suspend` porque la llamada de red es asíncrona.

### Paso 3: instancia de Retrofit (Singleton)

```kotlin
private const val BASE_URL = "https://android-kotlin-fun-mars-server.appspot.com/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

object MarsApi {
    val retrofitService: MarsApiService by lazy {
        retrofit.create(MarsApiService::class.java)
    }
}
```

`by lazy` significa que el objeto solo se crea la primera vez que se usa. Después devuelve siempre el mismo (es un Singleton). Esto es eficiente porque crear la instancia de Retrofit es costoso.

### Paso 4: llamar a la API desde el ViewModel

```kotlin
class MarsViewModel : ViewModel() {
    var marsUiState: MarsUiState by mutableStateOf(MarsUiState.Loading)
        private set

    init {
        getMarsPhotos()   // se llama al crear el ViewModel
    }

    fun getMarsPhotos() {
        viewModelScope.launch {
            marsUiState = try {
                val photos = MarsApi.retrofitService.getPhotos()
                MarsUiState.Success(photos)
            } catch (e: Exception) {
                MarsUiState.Error
            }
        }
    }
}
```

### Modelo de estados de la UI para red

Es un patrón muy común modelar los tres posibles estados de una petición de red con una `sealed interface`:

```kotlin
sealed interface MarsUiState {
    data class Success(val photos: List<MarsPhoto>) : MarsUiState
    object Error : MarsUiState
    object Loading : MarsUiState
}
```

Y en el composable mostrar algo distinto según el estado:

```kotlin
@Composable
fun HomeScreen(marsUiState: MarsUiState, retryAction: () -> Unit) {
    when (marsUiState) {
        is MarsUiState.Loading ->
            LoadingScreen()
        is MarsUiState.Success ->
            PhotosGridScreen(photos = marsUiState.photos)
        is MarsUiState.Error ->
            ErrorScreen(retryAction = retryAction)
    }
}

@Composable
fun LoadingScreen() {
    Image(
        painter = painterResource(R.drawable.loading_img),
        contentDescription = stringResource(R.string.loading),
        modifier = Modifier.size(200.dp)
    )
}

@Composable
fun ErrorScreen(retryAction: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painterResource(R.drawable.ic_connection_error), contentDescription = null)
        Text(text = stringResource(R.string.loading_failed))
        Button(onClick = retryAction) { Text(stringResource(R.string.retry)) }
    }
}
```

### Permiso de Internet en el Manifest

Sin esto la app no puede acceder a la red:

```xml
<!-- AndroidManifest.xml, dentro de <manifest> pero fuera de <application> -->
<uses-permission android:name="android.permission.INTERNET" />
```

---

**→ Continúa con [[02 - Repositorio e Imagenes|Ruta 2 — Repositorio, DI e imágenes desde URL]]**
