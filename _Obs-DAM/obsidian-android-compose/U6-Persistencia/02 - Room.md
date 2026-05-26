# Ruta 2 — Room: base de datos local con SQLite

**← [[01 - SQL|Ruta 1]]** | **→ [[03 - DataStore|Ruta 3]]**

---

## ¿Por qué Room y no SQLite directamente?

Trabajar con SQLite en Android sin abstracción requiere escribir mucho código repetitivo: abrir la conexión, crear los cursores, mapear las columnas a objetos Kotlin, cerrar la conexión, gestionar errores... Es propenso a fallos y tedioso.

**Room** es una librería de Jetpack que actúa como capa de abstracción sobre SQLite. Sus ventajas:
- Convierte automáticamente filas de la BD en objetos Kotlin (y viceversa)
- Verifica las consultas SQL en **tiempo de compilación** (si tu SQL tiene un error, falla al compilar, no en el móvil del usuario)
- Se integra con `Flow` para que la UI se actualice automáticamente cuando cambian los datos
- Reduce enormemente el código repetitivo

La app con la que se practica es **Inventory**: un inventario de productos con operaciones CRUD completas (Create, Read, Update, Delete).

---

## Los tres componentes de Room

```
@Entity          @Dao            @Database
   │                │                │
   │  una tabla     │  las           │  punto de
   │  de la BD      │  operaciones   │  entrada,
   │  (una fila     │  disponibles   │  Singleton
   │  = un objeto)  │  (funciones    │
   │                │  Kotlin)       │
   └────────────────┴────────────────┘
```

### Dependencias

```kotlin
// build.gradle.kts (módulo app)
val room_version = "2.6.1"
implementation("androidx.room:room-runtime:$room_version")
implementation("androidx.room:room-ktx:$room_version")       // soporte de corrutinas y Flow
ksp("androidx.room:room-compiler:$room_version")              // genera código en compilación

// En el bloque plugins {}
plugins {
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
}
```

---

## `@Entity` — definir la tabla

Cada clase anotada con `@Entity` corresponde a una tabla en la base de datos. Sus propiedades son las columnas.

```kotlin
@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,       // clave primaria, autoincremental
    val name: String,
    val price: Double,
    val quantity: Int
)
```

Si el nombre de una columna en la BD debe ser diferente al nombre de la propiedad en Kotlin:
```kotlin
@ColumnInfo(name = "item_name")
val name: String
```

Si quieres que Room ignore una propiedad y no la guarde en la BD:
```kotlin
@Ignore
val temporaryNote: String = ""
```

---

## `@Dao` — definir las operaciones

El **DAO** (*Data Access Object*) es una interfaz donde defines qué operaciones puedes hacer con la tabla. Room genera automáticamente la implementación concreta.

```kotlin
@Dao
interface ItemDao {

    // INSERT: inserta un objeto. Si ya existe uno con el mismo id:
    // IGNORE → lo ignora y no hace nada
    // REPLACE → sobreescribe el existente
    // ABORT   → lanza una excepción
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Item)

    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)

    // Consulta personalizada: todos los items ordenados por nombre
    @Query("SELECT * FROM items ORDER BY name ASC")
    fun getAllItems(): Flow<List<Item>>   // ← Flow, NO suspend

    // Consulta por id
    @Query("SELECT * FROM items WHERE id = :itemId")
    fun getItem(itemId: Int): Flow<Item>
}
```

### `suspend` vs `Flow` en el DAO

- Las operaciones de **escritura** (`insert`, `update`, `delete`) son `suspend`: son operaciones puntuales que terminan y devuelven un resultado.
- Las operaciones de **lectura** que devuelven `Flow` **no son** `suspend`: el `Flow` emite un nuevo valor automáticamente cada vez que los datos de la tabla cambian. La UI se actualiza sola sin hacer nada más.

Esto es muy potente: si alguien añade un item, el `Flow` de `getAllItems()` emite la lista actualizada automáticamente, y la UI la muestra sin que hayas escrito ningún código extra para eso.

---

## `@Database` — el punto de entrada

```kotlin
@Database(
    entities = [Item::class],   // lista de todas las tablas
    version  = 1,               // versión del esquema
    exportSchema = false        // no exportar el esquema a un archivo (útil en producción, no en aprendizaje)
)
abstract class InventoryDatabase : RoomDatabase() {

    // Room implementa este método automáticamente
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile   // garantiza que todos los hilos ven el mismo valor
        private var Instance: InventoryDatabase? = null

        fun getDatabase(context: Context): InventoryDatabase {
            // Si ya existe una instancia, devuélvela
            return Instance ?: synchronized(this) {
                // synchronized garantiza que solo un hilo puede crear la instancia
                Room.databaseBuilder(
                    context,
                    InventoryDatabase::class.java,
                    "item_database"        // nombre del archivo en el dispositivo
                )
                    .fallbackToDestructiveMigration()  // si cambia la versión, borra y recrea
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
```

**`@Volatile`**: hace que el valor de `Instance` sea siempre visible para todos los hilos. Sin esto, en un escenario multi-hilo podría crearse más de una instancia.

**`synchronized(this)`**: solo permite que un hilo a la vez ejecute ese bloque. Evita la *race condition* de que dos hilos creen dos instancias distintas.

**`fallbackToDestructiveMigration()`**: si incrementas la `version` sin escribir una migración, Room borra toda la base de datos y la recrea desde cero. Es lo más sencillo mientras desarrollas. En producción usarías migraciones para no perder los datos de los usuarios.

---

## Integrar Room con el repositorio y el ViewModel

### Repositorio

```kotlin
interface ItemsRepository {
    fun getAllItemsStream(): Flow<List<Item>>
    fun getItemStream(id: Int): Flow<Item?>
    suspend fun insertItem(item: Item)
    suspend fun deleteItem(item: Item)
    suspend fun updateItem(item: Item)
}

class OfflineItemsRepository(private val itemDao: ItemDao) : ItemsRepository {
    override fun getAllItemsStream(): Flow<List<Item>> = itemDao.getAllItems()
    override fun getItemStream(id: Int): Flow<Item?> = itemDao.getItem(id)
    override suspend fun insertItem(item: Item) = itemDao.insert(item)
    override suspend fun deleteItem(item: Item) = itemDao.delete(item)
    override suspend fun updateItem(item: Item) = itemDao.update(item)
}
```

### Application con el contenedor de dependencias

```kotlin
class InventoryApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}

interface AppContainer {
    val itemsRepository: ItemsRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val itemsRepository: ItemsRepository by lazy {
        OfflineItemsRepository(InventoryDatabase.getDatabase(context).itemDao())
    }
}
```

### ViewModel

```kotlin
class HomeViewModel(itemsRepository: ItemsRepository) : ViewModel() {

    // Convierte el Flow del repositorio en un StateFlow para la UI
    val homeUiState: StateFlow<HomeUiState> =
        itemsRepository.getAllItemsStream()
            .map { HomeUiState(itemList = it) }
            .stateIn(
                scope   = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = HomeUiState()
            )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class HomeUiState(val itemList: List<Item> = listOf())
```

`SharingStarted.WhileSubscribed(5_000L)`: mantiene el Flow activo durante 5 segundos después de que la UI deja de observarlo. Esto evita que al rotar el dispositivo (la Activity se recrea en ~1-2 segundos) se reinicie el Flow innecesariamente.

### ViewModel para añadir/editar items

```kotlin
class ItemEntryViewModel(private val itemsRepository: ItemsRepository) : ViewModel() {

    var itemUiState by mutableStateOf(ItemUiState())
        private set

    fun updateUiState(itemDetails: ItemDetails) {
        itemUiState = ItemUiState(
            itemDetails = itemDetails,
            isEntryValid = validateInput(itemDetails)
        )
    }

    suspend fun saveItem() {
        if (validateInput()) {
            itemsRepository.insertItem(itemUiState.itemDetails.toItem())
        }
    }

    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return with(uiState) {
            name.isNotBlank() && price.isNotBlank() && quantity.isNotBlank()
        }
    }
}

// Datos del formulario
data class ItemDetails(val id: Int = 0, val name: String = "", val price: String = "", val quantity: String = "")
data class ItemUiState(val itemDetails: ItemDetails = ItemDetails(), val isEntryValid: Boolean = false)

// Conversión entre ItemDetails y Item
fun ItemDetails.toItem() = Item(id = id, name = name, price = price.toDoubleOrNull() ?: 0.0, quantity = quantity.toIntOrNull() ?: 0)
```

---

## Versiones y migraciones

Cada vez que cambias el esquema de la base de datos (añades una columna, renombras una tabla, añades una nueva tabla...) debes:

1. **Incrementar el número de versión** en `@Database(version = X)`
2. **Proporcionar una migración** para transformar los datos existentes al nuevo esquema

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // SQL para transformar el esquema de v1 a v2
        database.execSQL("ALTER TABLE items ADD COLUMN description TEXT DEFAULT ''")
    }
}

Room.databaseBuilder(...)
    .addMigrations(MIGRATION_1_2)
    .build()
```

En desarrollo, si no te importa perder datos, `fallbackToDestructiveMigration()` es suficiente.

---

## El Database Inspector

Android Studio incluye el **Database Inspector** para inspeccionar la base de datos de la app mientras se ejecuta:

*View → Tool Windows → App Inspection → Database Inspector*

Desde ahí puedes:
- Ver el contenido de cada tabla en tiempo real
- Ejecutar consultas SQL manualmente para probar
- Ver cómo cambian los datos mientras usas la app

---

**→ Continúa con [[03 - DataStore|Ruta 3 — Preferences DataStore]]**
