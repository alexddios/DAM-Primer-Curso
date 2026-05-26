# Ruta 2 — Repositorio, inyección de dependencias e imágenes desde URL

**← [[01 - Corrutinas y HTTP|Ruta 1]]** | **→ [[../U6-Persistencia/00 - Unidad 6 Resumen|Unidad 6]]**

---

## La capa de datos y el patrón Repositorio

En la Ruta 1 el ViewModel accedía directamente a `MarsApi.retrofitService`. Esto funciona, pero tiene un problema: si en el futuro quieres cambiar la fuente de datos (usar otra API, cachear en base de datos, usar datos de prueba en los tests...) tienes que modificar el ViewModel.

La solución es introducir una capa intermedia: el **Repositorio**.

```
ViewModel
    │  solo conoce el Repositorio, no sabe de dónde vienen los datos
    ▼
Repository          ← única fuente de verdad de los datos
    │
    ▼
MarsApiService      ← fuente de datos concreta (la red)
```

### Crear el repositorio

```kotlin
// Interfaz: define qué puede hacer el repositorio
interface MarsPhotosRepository {
    suspend fun getMarsPhotos(): List<MarsPhoto>
}

// Implementación real: usa la red
class NetworkMarsPhotosRepository(
    private val marsApiService: MarsApiService
) : MarsPhotosRepository {
    override suspend fun getMarsPhotos(): List<MarsPhoto> {
        return marsApiService.getPhotos()
    }
}
```

Usar una interfaz tiene una ventaja enorme para los tests: puedes crear una implementación falsa (`FakeMarsPhotosRepository`) que devuelve datos de prueba sin tocar la red.

### Usar el repositorio en el ViewModel

```kotlin
class MarsViewModel(
    private val marsPhotosRepository: MarsPhotosRepository  // recibe la interfaz, no la implementación
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

---

## Inyección de dependencias manual

El ViewModel necesita un `MarsPhotosRepository` para funcionar. Hay que decidir quién lo crea y cómo se lo pasa.

**Inyección de dependencias (DI)**: en lugar de que el ViewModel cree sus propias dependencias internamente (`val repo = NetworkMarsPhotosRepository(...)`), alguien externo se las proporciona. Esto hace el código más flexible y testeable.

El curso enseña la versión **manual** de DI (sin librerías como Hilt). El patrón es crear un contenedor de dependencias en la clase `Application`.

### Application Container

```kotlin
// 1. Interfaz del contenedor
interface AppContainer {
    val marsPhotosRepository: MarsPhotosRepository
}

// 2. Implementación real
class DefaultAppContainer : AppContainer {

    private val BASE_URL = "https://android-kotlin-fun-mars-server.appspot.com/"

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()

    private val retrofitService: MarsApiService by lazy {
        retrofit.create(MarsApiService::class.java)
    }

    // El repositorio se crea con lazy: solo cuando alguien lo pida por primera vez
    override val marsPhotosRepository: MarsPhotosRepository by lazy {
        NetworkMarsPhotosRepository(retrofitService)
    }
}

// 3. Clase Application que crea el contenedor
class MarsPhotosApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}
```

Registra la clase `Application` personalizada en el `AndroidManifest.xml`:

```xml
<application
    android:name=".MarsPhotosApplication"
    ... >
```

Sin esto, Android crea la `Application` por defecto y tu contenedor nunca se inicializa.

### ViewModelFactory para inyectar el repositorio

Necesitas decirle al sistema cómo crear el ViewModel con el repositorio:

```kotlin
class MarsViewModel(
    private val marsPhotosRepository: MarsPhotosRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                // Accede a la Application y de ahí al contenedor
                val application = (this[APPLICATION_KEY] as MarsPhotosApplication)
                MarsViewModel(application.container.marsPhotosRepository)
            }
        }
    }
}
```

En el composable, usa el Factory al obtener el ViewModel:

```kotlin
val viewModel: MarsViewModel = viewModel(factory = MarsViewModel.Factory)
```

---

## Test con repositorio falso

La ventaja de todo esto se ve en los tests: puedes crear un `FakeMarsPhotosRepository` que devuelve datos inventados sin hacer peticiones reales a la red:

```kotlin
class FakeMarsPhotosRepository : MarsPhotosRepository {
    override suspend fun getMarsPhotos(): List<MarsPhoto> {
        return listOf(
            MarsPhoto(id = "1", imgSrc = "url1"),
            MarsPhoto(id = "2", imgSrc = "url2")
        )
    }
}

class MarsViewModelTest {
    @Test
    fun marsViewModel_getMarsPhotos_verifyMarsUiStateSuccess() = runTest {
        val viewModel = MarsViewModel(
            marsPhotosRepository = FakeMarsPhotosRepository()  // ← repositorio falso
        )

        assertEquals(
            MarsUiState.Success(listOf(
                MarsPhoto("1", "url1"),
                MarsPhoto("2", "url2")
            )),
            viewModel.marsUiState
        )
    }
}
```

`runTest` es una función especial para tests de corrutinas: hace que el tiempo avance artificialmente para que los `delay()` no bloqueen el test.

---

## Cargar imágenes desde URL: Coil

Para mostrar imágenes descargadas de Internet usamos **Coil** (*Coroutine Image Loader*). Gestiona automáticamente la descarga, el caché en disco y en memoria, y la visualización.

### Dependencia

```kotlin
implementation("io.coil-kt:coil-compose:2.4.0")
```

### `AsyncImage` — el composable principal de Coil

```kotlin
@Composable
fun MarsPhotoCard(photo: MarsPhoto, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photo.imgSrc)        // URL de la imagen
                .crossfade(true)            // animación de fundido al aparecer
                .build(),
            contentDescription = stringResource(R.string.mars_photo),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
```

`AsyncImage` descarga la imagen en un hilo de fondo y la muestra cuando está lista, sin bloquear la UI. Mientras descarga muestra un espacio vacío o un placeholder si lo configuras:

```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(photo.imgSrc)
        .crossfade(true)
        .build(),
    placeholder = painterResource(R.drawable.loading_img),   // mientras carga
    error = painterResource(R.drawable.ic_broken_image),      // si falla
    contentDescription = null
)
```

### Mostrar la cuadrícula de fotos

```kotlin
@Composable
fun PhotosGridScreen(photos: List<MarsPhoto>, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(
            items = photos,
            key   = { photo -> photo.id }   // clave única para optimizar recomposición
        ) { photo ->
            MarsPhotoCard(
                photo    = photo,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f)    // hace que cada celda sea cuadrada
            )
        }
    }
}
```

---

## Resumen de la Unidad 5

| Concepto | Lo esencial |
|----------|------------|
| Hilo principal | Dibuja la UI. No puede bloquearse. |
| Corrutinas | Código asíncrono que parece síncrono. `suspend` + `launch`. |
| `viewModelScope` | Lanza corrutinas vinculadas al ViewModel. Se cancelan solas. |
| Retrofit | Cliente HTTP. Define la API como interfaz Kotlin. |
| `@GET`, `@POST`... | Anotaciones para el método HTTP y la ruta. |
| `GsonConverterFactory` | Convierte JSON ↔ data class automáticamente. |
| `sealed interface` | Para modelar estados: Loading, Success, Error. |
| Repositorio | Capa entre ViewModel y fuente de datos. Abstrae el origen. |
| DI manual | Application crea el contenedor, el Factory lo inyecta al ViewModel. |
| Coil | Carga imágenes desde URL. `AsyncImage` con placeholder y error. |

---

**→ Continúa con [[../U6-Persistencia/00 - Unidad 6 Resumen|Unidad 6 — Persistencia de datos]]**
