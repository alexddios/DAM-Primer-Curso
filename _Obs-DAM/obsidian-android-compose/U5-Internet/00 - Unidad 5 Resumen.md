# Unidad 5 — Conectarse a Internet

**← [[../U4-Navegacion-Arquitectura/00 - Unidad 4 Resumen|Unidad 4]]** | **→ [[../U6-Persistencia/00 - Unidad 6 Resumen|Unidad 6]]**

Duración: ~9 horas | 2 rutas de aprendizaje

---

## Ruta 1 — Corrutinas de Kotlin y peticiones HTTP

### El problema: las operaciones lentas bloquean la UI

Cuando una app se abre, Android ejecuta el código en el **hilo principal** (*main thread* o *UI thread*). Este hilo es el responsable de dibujar la interfaz y responder a los toques del usuario.

Si en ese hilo haces una operación lenta — como descargar datos de Internet, que puede tardar segundos — la UI se **congela** completamente hasta que termina. El usuario ve la app bloqueada, y si tarda más de unos segundos, Android muestra el diálogo "La app no responde" (ANR).

La solución es ejecutar las operaciones lentas **en un hilo distinto** al hilo principal, de forma que la UI siga funcionando mientras espera. Kotlin lo resuelve con las **corrutinas**.

---

### Corrutinas de Kotlin

Las corrutinas son la forma moderna de manejar operaciones asíncronas en Kotlin. Son mucho más sencillas que los callbacks o los threads manuales.

#### Función `suspend`

Una función `suspend` es una función que puede **pausarse** en un punto (esperando una operación lenta) y **reanudarse** después sin bloquear el hilo. Solo se puede llamar desde otra función `suspend` o desde una corrutina.

```kotlin
suspend fun obtenerDatos(): String {
    // simula una operación que tarda 2 segundos
    delay(2000)
    return "datos listos"
}
```

#### Iniciar una corrutina: `launch` y `viewModelScope`

Para iniciar una corrutina desde el ViewModel se usa `viewModelScope.launch {}`. El `viewModelScope` cancela automáticamente la corrutina cuando el ViewModel se destruye.

```kotlin
class MiViewModel : ViewModel() {
    fun cargarDatos() {
        viewModelScope.launch {
            val resultado = obtenerDatos()   // función suspend, espera aquí
            _uiState.update { it.copy(datos = resultado) }  // actualiza la UI
        }
    }
}
```

Desde fuera, `cargarDatos()` es una función normal que devuelve inmediatamente. La corrutina se ejecuta en segundo plano.

#### `Dispatchers` — en qué hilo se ejecuta

Cada corrutina se ejecuta en un **Dispatcher** (planificador de hilos):

| Dispatcher | Cuándo usarlo |
|-----------|--------------|
| `Dispatchers.Main` | Para actualizar la UI. Es el hilo principal |
| `Dispatchers.IO` | Para operaciones de red, base de datos o archivos |
| `Dispatchers.Default` | Para cálculos intensivos de CPU |

`viewModelScope.launch {}` usa `Dispatchers.Main` por defecto. Para operaciones de red, Retrofit ya gestiona internamente el cambio a `Dispatchers.IO`, así que normalmente no tienes que preocuparte de esto.

#### `withContext` — cambiar de dispatcher dentro de una corrutina

Si necesitas hacer algo en un hilo distinto explícitamente:

```kotlin
viewModelScope.launch {
    // estamos en Main
    val resultado = withContext(Dispatchers.IO) {
        // estamos en IO: podemos hacer operaciones de red/BD
        api.getDatos()
    }
    // volvemos a Main: podemos actualizar la UI
    _uiState.update { it.copy(datos = resultado) }
}
```

#### Gestionar excepciones

Las llamadas de red pueden fallar (sin conexión, servidor caído...). Envuelve el código en `try-catch`:

```kotlin
viewModelScope.launch {
    _uiState.update { it.copy(estado = Loading) }
    try {
        val fotos = marsApi.getFotos()
        _uiState.update { it.copy(estado = Success(fotos)) }
    } catch (e: Exception) {
        _uiState.update { it.copy(estado = Error) }
    }
}
```

---

### HTTP y REST

La mayoría de apps obtienen datos de un **servidor web** mediante peticiones HTTP. El patrón más común es **REST** (*Representational State Transfer*):

- La app hace una petición HTTP a una **URL** (endpoint)
- El servidor responde con datos en formato **JSON**
- La app parsea el JSON y usa los datos

#### JSON

JSON es el formato de texto más común para intercambiar datos. Ejemplo de respuesta JSON de una API de fotos de Marte:

```json
[
  {
    "id": "424906",
    "img_src": "https://mars.nasa.gov/msl-raw-images/proj/msl/redops/ods/surface/sol/03926/opgs/edr/ncam/NRB_726536758EDR_F1050948NCAM00707M_.JPG"
  },
  {
    "id": "424907",
    "img_src": "https://mars.nasa.gov/msl-raw-images/..."
  }
]
```

---

### Retrofit — cliente HTTP para Android

Retrofit es la librería más usada en Android para hacer peticiones HTTP. Convierte automáticamente el JSON de la respuesta en objetos Kotlin.

#### Dependencias

```kotlin
// build.gradle.kts
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")  // convierte JSON ↔ Kotlin
```

#### Paso 1: Modelo de datos

Crea una data class que represente los datos que recibirás del servidor. Los nombres de los campos deben coincidir con los del JSON (o usar `@SerializedName`):

```kotlin
data class MarsPhoto(
    val id: String,
    @SerializedName("img_src") val imgSrc: String  // "img_src" en JSON → imgSrc en Kotlin
)
```

#### Paso 2: Interfaz de la API

Define una interfaz con los endpoints disponibles:

```kotlin
interface MarsApiService {
    @GET("photos")   // la URL completa será BASE_URL + "photos"
    suspend fun getPhotos(): List<MarsPhoto>
}
```

Las anotaciones `@GET`, `@POST`, `@PUT`, `@DELETE` indican el método HTTP y la ruta.

#### Paso 3: Objeto Retrofit (Singleton)

```kotlin
private const val BASE_URL = "https://android-kotlin-fun-mars-server.appspot.com/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())  // JSON → Kotlin
    .baseUrl(BASE_URL)
    .build()

object MarsApi {
    val retrofitService: MarsApiService by lazy {
        retrofit.create(MarsApiService::class.java)
    }
}
```

El `by lazy` significa que el objeto solo se crea la primera vez que se usa, no al iniciar la app.

#### Paso 4: Llamar a la API desde el ViewModel

```kotlin
class MarsViewModel : ViewModel() {
    var marsUiState: MarsUiState by mutableStateOf(MarsUiState.Loading)
        private set

    init {
        getMarsPhotos()   // se llama automáticamente al crear el ViewModel
    }

    fun getMarsPhotos() {
        viewModelScope.launch {
            marsUiState = try {
                MarsUiState.Success(MarsApi.retrofitService.getPhotos())
            } catch (e: Exception) {
                MarsUiState.Error
            }
        }
    }
}
```

#### Modelo de estados de la UI de red

Es un patrón muy común modelar los posibles estados de una petición de red:

```kotlin
sealed interface MarsUiState {
    data class Success(val photos: List<MarsPhoto>) : MarsUiState
    object Error : MarsUiState
    object Loading : MarsUiState
}
```

Y en el composable:
```kotlin
when (viewModel.marsUiState) {
    is MarsUiState.Loading -> LoadingScreen()
    is MarsUiState.Success -> PhotosGridScreen(viewModel.marsUiState.photos)
    is MarsUiState.Error   -> ErrorScreen(onRetry = { viewModel.getMarsPhotos() })
}
```

#### Permiso de Internet

No olvides declarar el permiso en `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## Ruta 2 — Capa de datos, Repositorio e Inyección de Dependencias

### La capa de datos y el patrón Repositorio

A medida que la app crece, no conviene que el ViewModel acceda directamente a Retrofit. La razón es que si en el futuro cambias la fuente de datos (de Retrofit a otra cosa), tienes que modificar el ViewModel. Para evitarlo se introduce el **Repositorio**.

El Repositorio es una clase que:
- Es la **única fuente de verdad** de los datos para el ViewModel
- Abstrae el origen de los datos (el ViewModel no sabe si los datos vienen de la red, de una base de datos, de un archivo...)

```
ViewModel
    │
    ▼
Repository       ← capa de datos
    │
    ▼
MarsApiService   ← fuente de datos (red)
```

#### Crear el repositorio

```kotlin
// Interfaz (permite cambiar la implementación fácilmente en tests)
interface MarsPhotosRepository {
    suspend fun getMarsPhotos(): List<MarsPhoto>
}

// Implementación real (usa la red)
class NetworkMarsPhotosRepository(
    private val marsApiService: MarsApiService
) : MarsPhotosRepository {
    override suspend fun getMarsPhotos(): List<MarsPhoto> {
        return marsApiService.getPhotos()
    }
}
```

#### Usar el repositorio en el ViewModel

```kotlin
class MarsViewModel(
    private val marsPhotosRepository: MarsPhotosRepository
) : ViewModel() {

    fun getMarsPhotos() {
        viewModelScope.launch {
            marsUiState = try {
                MarsUiState.Success(marsPhotosRepository.getMarsPhotos())
            } catch (e: Exception) {
                MarsUiState.Error
            }
        }
    }
}
```

### Inyección de dependencias manual

El ViewModel necesita un `MarsPhotosRepository`, pero ¿quién lo crea? Esto se resuelve con **inyección de dependencias** (DI): en lugar de que el ViewModel cree sus dependencias, alguien externo se las proporciona.

#### Application Container

La forma manual que enseña el curso es crear un "contenedor" en la clase `Application`:

```kotlin
// Interfaz del contenedor
interface AppContainer {
    val marsPhotosRepository: MarsPhotosRepository
}

// Implementación
class DefaultAppContainer : AppContainer {
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val retrofitService: MarsApiService by lazy {
        retrofit.create(MarsApiService::class.java)
    }

    override val marsPhotosRepository: MarsPhotosRepository by lazy {
        NetworkMarsPhotosRepository(retrofitService)
    }
}

// Clase Application
class MarsPhotosApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}
```

Registra la clase Application en el `AndroidManifest.xml`:
```xml
<application
    android:name=".MarsPhotosApplication"
    ...>
```

#### ViewModelFactory para pasar el repositorio al ViewModel

```kotlin
class MarsViewModel(
    private val marsPhotosRepository: MarsPhotosRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as MarsPhotosApplication
                MarsViewModel(application.container.marsPhotosRepository)
            }
        }
    }
}

// En el composable, usar el Factory:
val viewModel: MarsViewModel = viewModel(factory = MarsViewModel.Factory)
```

### Cargar imágenes desde URL: Coil

Para mostrar imágenes desde una URL usamos la librería **Coil**, que gestiona la descarga, el caché y la visualización:

```kotlin
// Dependencia
implementation("io.coil-kt:coil-compose:2.4.0")
```

```kotlin
@Composable
fun MarsPhotoCard(photo: MarsPhoto) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(photo.imgSrc)       // URL de la imagen
            .crossfade(true)           // animación de fundido al cargar
            .build(),
        contentDescription = stringResource(R.string.mars_photo),
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxWidth()
    )
}
```

`AsyncImage` descarga la imagen en un hilo de fondo y la muestra cuando está lista, sin bloquear la UI.

### Mostrar la cuadrícula de fotos

```kotlin
@Composable
fun PhotosGridScreen(photos: List<MarsPhoto>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(items = photos, key = { it.id }) { photo ->
            MarsPhotoCard(photo, modifier = Modifier.padding(4.dp))
        }
    }
}
```

El parámetro `key = { it.id }` es importante en cuadrículas y listas: ayuda a Compose a identificar cada elemento de forma única, optimizando la recomposición cuando la lista cambia.

---

**→ Continúa con [[../U6-Persistencia/00 - Unidad 6 Resumen|Unidad 6 — Persistencia de datos]]**
