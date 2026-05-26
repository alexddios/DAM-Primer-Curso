# Ruta 3 — Preferences DataStore

**← [[02 - Room|Ruta 2]]** | **→ [[../U7-WorkManager/00 - Unidad 7 Resumen|Unidad 7]]**

---

## ¿Cuándo usar DataStore y cuándo Room?

Ambas persisten datos localmente, pero para casos de uso distintos:

| | Room | Preferences DataStore |
|--|------|-----------------------|
| **Tipo de datos** | Datos estructurados con relaciones (tablas) | Pares clave-valor simples |
| **Cuándo usarlo** | Lista de tareas, inventario, historial | Preferencias del usuario, configuración |
| **Ejemplo real** | "Lista de favoritos del usuario" | "El usuario prefiere la vista en cuadrícula" |
| **API** | DAO con consultas SQL | Editor con clave-valor |
| **Asíncrono** | `Flow` + `suspend` | `Flow` + `suspend` |

**Preferences DataStore** es el reemplazo moderno de `SharedPreferences`. Sus ventajas sobre SharedPreferences:
- Totalmente asíncrono (usa `Flow` y corrutinas). SharedPreferences podía bloquear el hilo principal.
- Maneja errores con excepciones en lugar de fallos silenciosos
- Operaciones transaccionales (o se guardan todos los cambios o ninguno)

La app con la que se practica es **Dessert Release**: una lista de versiones de Android donde el usuario puede cambiar entre vista lineal y cuadrícula. Esa preferencia se guarda con DataStore.

---

## Dependencia

```kotlin
implementation("androidx.datastore:datastore-preferences:1.0.0")
```

---

## Crear la instancia de DataStore

Se crea como una **extensión de `Context`** a nivel de archivo (fuera de cualquier clase), para que sea un singleton de toda la app:

```kotlin
// En el mismo archivo que UserPreferencesRepository, fuera de la clase
private const val LAYOUT_PREFERENCE_NAME = "layout_preferences"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = LAYOUT_PREFERENCE_NAME
)
```

`LAYOUT_PREFERENCE_NAME` es el nombre del archivo donde se guardarán las preferencias en el dispositivo.

---

## Definir las claves

Cada preferencia se identifica por una **clave**. El tipo de la clave debe coincidir con el tipo del valor:

```kotlin
private object PreferencesKeys {
    val IS_LINEAR_LAYOUT  = booleanPreferencesKey("is_linear_layout")
    val USERNAME          = stringPreferencesKey("username")
    val FONT_SIZE         = intPreferencesKey("font_size")
    val LAST_SYNC_TIME    = longPreferencesKey("last_sync_time")
}
```

Tipos de clave disponibles:
- `booleanPreferencesKey` → `Boolean`
- `stringPreferencesKey` → `String`
- `intPreferencesKey` → `Int`
- `floatPreferencesKey` → `Float`
- `doublePreferencesKey` → `Double`
- `longPreferencesKey` → `Long`
- `stringSetPreferencesKey` → `Set<String>`

---

## El Repositorio de preferencias

```kotlin
class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {

    // Leer la preferencia como Flow
    val isLinearLayout: Flow<Boolean> = dataStore.data
        .catch { exception ->
            // Si hay un error al leer (archivo corrupto, etc.), emite preferencias vacías
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception   // relanza otros errores
            }
        }
        .map { preferences ->
            // Si la clave no existe todavía, devuelve true como valor por defecto
            preferences[PreferencesKeys.IS_LINEAR_LAYOUT] ?: true
        }

    // Guardar la preferencia
    suspend fun saveLayoutPreference(isLinearLayout: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LINEAR_LAYOUT] = isLinearLayout
        }
    }
}
```

**`dataStore.data`**: es un `Flow<Preferences>` que emite el estado actual de todas las preferencias. Cuando cualquier preferencia cambia, emite un nuevo valor.

**`dataStore.edit { }`**: es una función `suspend` que abre una transacción para modificar las preferencias. Si algo falla durante la escritura, los cambios no se aplican (atomicidad).

---

## Integrar con el ViewModel

```kotlin
class DessertReleaseViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<DessertReleaseUiState> =
        userPreferencesRepository.isLinearLayout
            .map { isLinearLayout ->
                DessertReleaseUiState(isLinearLayout = isLinearLayout)
            }
            .stateIn(
                scope        = viewModelScope,
                started      = SharingStarted.WhileSubscribed(5_000),
                initialValue = DessertReleaseUiState()
            )

    fun selectLayout(isLinearLayout: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveLayoutPreference(isLinearLayout)
        }
    }
}

data class DessertReleaseUiState(
    val isLinearLayout: Boolean = true
)
```

El flujo completo cuando el usuario cambia la preferencia:
1. Usuario pulsa el botón de cambiar layout
2. Composable llama a `viewModel.selectLayout(false)`
3. ViewModel llama a `userPreferencesRepository.saveLayoutPreference(false)` (suspend)
4. DataStore guarda el valor en disco y emite el nuevo valor por el Flow
5. El `StateFlow` del ViewModel emite un nuevo `DessertReleaseUiState(isLinearLayout = false)`
6. El composable recoge el nuevo estado con `collectAsState()` y redibuja la UI

---

## Inicializar en la Application

```kotlin
class DessertReleaseApplication : Application() {
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        userPreferencesRepository = UserPreferencesRepository(
            dataStore   // accedemos a través de la extensión de Context
        )
    }
}
```

Y el ViewModelFactory:
```kotlin
companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val application = this[APPLICATION_KEY] as DessertReleaseApplication
            DessertReleaseViewModel(application.userPreferencesRepository)
        }
    }
}
```

---

## Resumen: cuándo usar qué para persistencia

```
¿Qué tipo de datos quieres guardar?
        │
        ├── Datos estructurados, listas, múltiples registros
        │   → Room (SQLite)
        │
        ├── Preferencias simples, configuración, flags
        │   → Preferences DataStore
        │
        └── Archivos grandes (imágenes, documentos, audio)
            → Sistema de archivos (File API) o MediaStore
```

---

**→ Continúa con [[../U7-WorkManager/00 - Unidad 7 Resumen|Unidad 7 — WorkManager]]**
