# Unidad 6 — Persistencia de datos

**← [[../U5-Internet/00 - Unidad 5 Resumen|Unidad 5]]** | **→ [[../U7-WorkManager/00 - Unidad 7 Resumen|Unidad 7]]**

Duración: ~10 horas | 3 rutas de aprendizaje

---

## Ruta 1 — SQL

### ¿Qué es una base de datos relacional?

Una **base de datos relacional** organiza los datos en **tablas**. Cada tabla tiene:
- **Columnas**: los campos o atributos (como las columnas de una hoja de cálculo)
- **Filas**: cada registro individual

Ejemplo de tabla `items`:

| id | nombre | precio | cantidad |
|----|--------|--------|---------|
| 1 | Manzana | 0.50 | 50 |
| 2 | Pera | 0.80 | 30 |
| 3 | Uva | 1.20 | 20 |

### SQL — el lenguaje de las bases de datos

SQL (*Structured Query Language*) es el lenguaje estándar para interactuar con bases de datos relacionales. Android usa **SQLite**, una base de datos que funciona directamente en el dispositivo sin servidor.

#### Crear una tabla: `CREATE TABLE`

```sql
CREATE TABLE items (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,  -- clave primaria, se incrementa sola
    nombre   TEXT NOT NULL,                        -- texto, obligatorio
    precio   REAL NOT NULL DEFAULT 0.0,            -- número decimal, por defecto 0
    cantidad INTEGER NOT NULL DEFAULT 0            -- entero, por defecto 0
);
```

- `PRIMARY KEY`: identifica de forma única cada fila
- `AUTOINCREMENT`: el valor se asigna automáticamente (1, 2, 3...)
- `NOT NULL`: el campo no puede estar vacío
- `DEFAULT`: valor por defecto si no se especifica

#### Insertar datos: `INSERT`

```sql
INSERT INTO items (nombre, precio, cantidad)
VALUES ('Manzana', 0.50, 50);
-- No incluimos id porque es AUTOINCREMENT
```

#### Leer datos: `SELECT`

```sql
-- Todos los registros y todas las columnas
SELECT * FROM items;

-- Solo algunas columnas
SELECT nombre, precio FROM items;

-- Con condición
SELECT * FROM items WHERE cantidad > 20;

-- Ordenado
SELECT * FROM items ORDER BY precio ASC;   -- ascendente
SELECT * FROM items ORDER BY precio DESC;  -- descendente

-- Varios filtros combinados
SELECT * FROM items WHERE precio < 1.0 AND cantidad > 10;
```

#### Actualizar datos: `UPDATE`

```sql
-- Siempre usar WHERE, si no actualiza TODAS las filas
UPDATE items SET cantidad = 45 WHERE id = 1;

UPDATE items SET precio = 0.60, cantidad = 40 WHERE nombre = 'Manzana';
```

⚠️ **Error común**: olvidar el `WHERE` en un `UPDATE` o `DELETE`. Sin `WHERE`, la operación afecta a **todas** las filas de la tabla.

#### Eliminar datos: `DELETE`

```sql
DELETE FROM items WHERE id = 3;

-- ¡Esto borra TODAS las filas!
DELETE FROM items;
```

---

## Ruta 2 — Room: base de datos local en Android

Trabajar directamente con SQLite en Android requiere mucho código repetitivo y propenso a errores. **Room** es una librería de Jetpack que actúa como capa de abstracción sobre SQLite, permitiendo trabajar con objetos Kotlin en lugar de escribir SQL a mano.

### Los tres componentes de Room

```
┌──────────────────────────────────────────────────────┐
│                     Room Database                     │
│  ┌────────────┐    ┌──────────────┐    ┌──────────┐  │
│  │  @Entity   │    │    @Dao      │    │@Database │  │
│  │            │    │              │    │          │  │
│  │ Representa │    │ Define las   │    │ Punto de │  │
│  │ una tabla  │    │ operaciones  │    │ acceso   │  │
│  │ (una fila) │    │ SQL como     │    │ a la BD  │  │
│  │            │    │ funciones    │    │          │  │
│  └────────────┘    └──────────────┘    └──────────┘  │
└──────────────────────────────────────────────────────┘
```

### Dependencias

```kotlin
// build.gradle.kts
val room_version = "2.6.1"
implementation("androidx.room:room-runtime:$room_version")
implementation("androidx.room:room-ktx:$room_version")      // soporte de corrutinas
ksp("androidx.room:room-compiler:$room_version")             // procesador de anotaciones

// También necesitas KSP en plugins{}
plugins {
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
}
```

### `@Entity` — definir una tabla

Cada `@Entity` corresponde a una tabla en SQLite. La clase define las columnas.

```kotlin
@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val price: Double,
    val quantity: Int
)
```

- `@Entity(tableName = "items")`: el nombre de la tabla en la BD
- `@PrimaryKey(autoGenerate = true)`: clave primaria con autoincremento
- Cada propiedad del data class se convierte en una columna

Si el nombre de la columna en la BD debe ser diferente al nombre de la propiedad en Kotlin:
```kotlin
@ColumnInfo(name = "item_price")
val price: Double
```

### `@Dao` — definir las operaciones

El DAO (*Data Access Object*) es una interfaz donde defines qué operaciones puedes hacer con la tabla. Room genera automáticamente el código SQL necesario.

```kotlin
@Dao
interface ItemDao {

    // Operaciones básicas con anotaciones de conveniencia
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Item)

    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)

    // Consultas personalizadas con @Query
    @Query("SELECT * FROM items ORDER BY name ASC")
    fun getAllItems(): Flow<List<Item>>   // ← Flow, no suspend

    @Query("SELECT * FROM items WHERE id = :itemId")
    fun getItem(itemId: Int): Flow<Item>

    @Query("SELECT * FROM items WHERE price < :maxPrice")
    fun getItemsUnderPrice(maxPrice: Double): Flow<List<Item>>
}
```

**Importante**: observa que `getAllItems()` devuelve `Flow<List<Item>>`, no `suspend fun`. Las consultas que devuelven `Flow` no son funciones suspend porque el Flow emite nuevos valores automáticamente cada vez que los datos de la tabla cambian. Las operaciones de escritura (`insert`, `update`, `delete`) sí son `suspend` porque son operaciones puntuales.

`OnConflictStrategy.IGNORE`: si intentas insertar un elemento con un `id` que ya existe, lo ignora. Otras opciones: `REPLACE` (sobreescribe), `ABORT` (falla con error).

### `@Database` — el punto de acceso

```kotlin
@Database(entities = [Item::class], version = 1, exportSchema = false)
abstract class InventoryDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao   // Room implementa esto automáticamente

    companion object {
        @Volatile  // garantiza que todos los hilos ven el mismo valor
        private var Instance: InventoryDatabase? = null

        fun getDatabase(context: Context): InventoryDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, InventoryDatabase::class.java, "item_database")
                    .fallbackToDestructiveMigration()  // borra y recrea la BD si cambia la versión
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
```

- `@Database(entities = [...])`: lista de todas las tablas (`@Entity`) de la BD
- `version = 1`: cada vez que cambias el esquema (añades columna, cambias tabla...) debes incrementar la versión y gestionar la migración
- El patrón `synchronized` garantiza que solo se crea una instancia de la BD aunque varios hilos intenten acceder al mismo tiempo (*Singleton*)

### Conectar Room con el Repositorio

```kotlin
class OfflineItemsRepository(private val itemDao: ItemDao) : ItemsRepository {
    override fun getAllItemsStream(): Flow<List<Item>> = itemDao.getAllItems()
    override fun getItemStream(id: Int): Flow<Item?> = itemDao.getItem(id)
    override suspend fun insertItem(item: Item) = itemDao.insert(item)
    override suspend fun deleteItem(item: Item) = itemDao.delete(item)
    override suspend fun updateItem(item: Item) = itemDao.update(item)
}
```

### Usar `Flow` en el ViewModel

```kotlin
class InventoryViewModel(private val itemsRepository: ItemsRepository) : ViewModel() {

    val homeUiState: StateFlow<HomeUiState> =
        itemsRepository.getAllItemsStream()
            .map { HomeUiState(it) }   // convierte List<Item> en HomeUiState
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = HomeUiState()
            )
}

data class HomeUiState(val itemList: List<Item> = listOf())
```

`stateIn()` convierte el `Flow` en un `StateFlow` que la UI puede recoger con `collectAsState()`. El `SharingStarted.WhileSubscribed(5_000)` mantiene el Flow activo 5 segundos después de que la UI deja de observarlo (útil para cambios de configuración).

### Para operaciones de escritura en el ViewModel

```kotlin
fun saveItem() {
    viewModelScope.launch {
        itemsRepository.insertItem(uiState.value.itemDetails.toItem())
    }
}

fun deleteItem() {
    viewModelScope.launch {
        itemsRepository.deleteItem(uiState.value.itemDetails.toItem())
    }
}
```

### El Inspector de bases de datos

Android Studio incluye el **Database Inspector** para ver y consultar la base de datos de la app en tiempo real mientras se ejecuta en el emulador:

*View → Tool Windows → App Inspection → Database Inspector*

Desde ahí puedes:
- Ver el contenido de cada tabla
- Ejecutar consultas SQL manualmente
- Ver los cambios en tiempo real mientras usas la app

---

## Ruta 3 — Preferences DataStore

### ¿Cuándo usar DataStore en vez de Room?

| | Room | Preferences DataStore |
|--|------|----------------------|
| Tipo de datos | Estructurados, tablas, relaciones | Pares clave-valor simples |
| Cuándo usarlo | Lista de elementos, historial, catálogos | Preferencias del usuario, configuración |
| Ejemplo | Lista de tareas, inventario | Si el usuario prefiere modo oscuro, último filtro seleccionado |

**Preferences DataStore** es la alternativa moderna a `SharedPreferences`. Es asíncrono (usa `Flow` y corrutinas) y más seguro que `SharedPreferences`.

### Dependencia

```kotlin
implementation("androidx.datastore:datastore-preferences:1.0.0")
```

### Crear el DataStore

```kotlin
// Se crea a nivel de archivo (fuera de cualquier clase), como una extensión de Context
private const val LAYOUT_PREFERENCE_NAME = "layout_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = LAYOUT_PREFERENCE_NAME
)
```

### Definir las claves

```kotlin
object UserPreferencesKeys {
    val IS_LINEAR_LAYOUT = booleanPreferencesKey("is_linear_layout")
    val USERNAME = stringPreferencesKey("username")
    val FONT_SIZE = intPreferencesKey("font_size")
}
```

Tipos de clave disponibles: `booleanPreferencesKey`, `stringPreferencesKey`, `intPreferencesKey`, `floatPreferencesKey`, `doublePreferencesKey`, `longPreferencesKey`, `stringSetPreferencesKey`.

### Leer preferencias (como Flow)

```kotlin
class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    val isLinearLayout: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[UserPreferencesKeys.IS_LINEAR_LAYOUT] ?: true  // true por defecto
        }
}
```

### Escribir preferencias

```kotlin
suspend fun saveLayoutPreference(isLinearLayout: Boolean) {
    dataStore.edit { preferences ->
        preferences[UserPreferencesKeys.IS_LINEAR_LAYOUT] = isLinearLayout
    }
}
```

`dataStore.edit {}` es una función `suspend` que abre una transacción y aplica los cambios de forma atómica.

### Usar en el ViewModel

```kotlin
class DessertViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<DessertUiState> =
        userPreferencesRepository.isLinearLayout.map { isLinearLayout ->
            DessertUiState(isLinearLayout)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DessertUiState()
        )

    fun selectLayout(isLinearLayout: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveLayoutPreference(isLinearLayout)
        }
    }
}
```

El Flow de DataStore emite un nuevo valor cada vez que la preferencia cambia, igual que Room.

---

**→ Continúa con [[../U7-WorkManager/00 - Unidad 7 Resumen|Unidad 7 — WorkManager]]**
