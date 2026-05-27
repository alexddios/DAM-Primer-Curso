# Ruta 1 — El sistema de Views de Android

**← [[U8-Views-Compose|Unidad 8]]** | **→ [[02 - Interoperabilidad Views y Compose|Ruta 2]]**

---

## ¿Por qué aprender el sistema de Views?

Antes de Jetpack Compose (que se estabilizó en 2021), **todas** las apps Android se construían con el sistema de Views. Eso significa que hay millones de apps activas escritas así, y si vas a trabajar en Android es muy probable que te encuentres con código legacy que usa Views. Esta ruta te da las bases para entenderlo.

---

## Views vs Compose: las diferencias fundamentales

| Sistema de Views (legacy) | Jetpack Compose (moderno) |
|--------------------------|--------------------------|
| UI definida en archivos **XML** | UI definida en código **Kotlin** |
| Dos archivos separados (XML + código) | Un único archivo Kotlin |
| **Imperativo**: describes *cómo* construir la UI paso a paso | **Declarativo**: describes *qué* debe mostrar la UI |
| Las Views son **mutables**: se actualizan manualmente (`view.text = "..."`) | Los composables son **inmutables**: el estado cambia y Compose redibuja |
| Jerarquía de Views en memoria (objetos pesados) | Árbol de composables (más ligero y eficiente) |

---

## Componentes principales del sistema de Views

### Views individuales

Cada elemento visual es un objeto de la clase `View` o una subclase:

| View | Equivalente en Compose |
|------|----------------------|
| `TextView` | `Text` |
| `ImageView` | `Image` |
| `Button` | `Button` |
| `EditText` | `TextField` |
| `CheckBox` | `Checkbox` |
| `RecyclerView` | `LazyColumn` / `LazyGrid` |
| `ScrollView` | `Column` + `verticalScroll` |

### Contenedores (ViewGroups)

| ViewGroup | Equivalente en Compose |
|-----------|----------------------|
| `LinearLayout` (vertical) | `Column` |
| `LinearLayout` (horizontal) | `Row` |
| `FrameLayout` | `Box` |
| `ConstraintLayout` | `ConstraintLayout` (también existe en Compose) |

---

## Definir la UI en XML

```xml
<!-- res/layout/activity_main.xml -->
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <ImageView
        android:id="@+id/logo"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:src="@drawable/ic_launcher" />

    <TextView
        android:id="@+id/titleText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Mi app"
        android:textSize="24sp"
        android:textStyle="bold" />

    <Button
        android:id="@+id/actionButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Pulsar"
        android:layout_marginTop="16dp" />

</LinearLayout>
```

Atributos de tamaño:
- `match_parent`: ocupa todo el espacio disponible del padre (como `fillMaxSize()`)
- `wrap_content`: ocupa solo el espacio que necesita el contenido (como el comportamiento por defecto en Compose)

---

## Conectar el XML con el código Kotlin

### `setContentView` y `findViewById`

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // infla el XML y lo pone en pantalla

        // Busca la View por su id y la castea al tipo correcto
        val titleText  = findViewById<TextView>(R.id.titleText)
        val actionButton = findViewById<Button>(R.id.actionButton)

        // Modificar las Views desde código
        titleText.text = "Título actualizado"

        actionButton.setOnClickListener {
            titleText.text = "¡Botón pulsado!"
        }
    }
}
```

`findViewById` devuelve `View?` (nullable). Si escribes mal el id o usas el tipo incorrecto, el error no se detecta hasta que se ejecuta la app.

### ViewBinding — la forma moderna (y segura)

**ViewBinding** genera automáticamente una clase Kotlin con referencias type-safe a todas las Views del layout, eliminando los errores de `findViewById`.

```kotlin
// 1. Activar en build.gradle.kts
android {
    buildFeatures {
        viewBinding = true
    }
}
```

```kotlin
// 2. Usar en la Activity
class MainActivity : AppCompatActivity() {

    // La clase se genera automáticamente con el nombre del layout en PascalCase + "Binding"
    // activity_main.xml → ActivityMainBinding
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Infla el layout con el binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Accede a las Views directamente, sin findViewById
        // Los nombres son camelCase del id del XML (title_text → titleText)
        binding.titleText.text = "Título actualizado"

        binding.actionButton.setOnClickListener {
            binding.titleText.text = "¡Botón pulsado!"
        }
    }
}
```

Si renombras un id en el XML o lo eliminas, el código de ViewBinding falla al **compilar** (no en ejecución), lo que es mucho mejor.

---

## Fragments — pantallas reutilizables

En el sistema de Views, las pantallas no siempre son Activities completas. Un **Fragment** es un módulo de UI reutilizable que vive dentro de una Activity. Ventajas:
- Se puede reutilizar en distintas Activities
- En tablets, varias pantallas pueden mostrarse simultáneamente
- Tiene su propio ciclo de vida

```kotlin
class HomeFragment : Fragment() {

    // _binding puede ser null fuera del ciclo de vida del Fragment
    private var _binding: FragmentHomeBinding? = null
    // binding solo se accede cuando _binding no es null
    private val binding get() = _binding!!

    // Equivalent a setContentView en la Activity
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Aquí interactúas con las Views (equivale al resto de onCreate en Activity)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.titleText.text = "Hola desde Fragment"
        binding.actionButton.setOnClickListener {
            // navegar, actualizar UI, etc.
        }
    }

    // IMPORTANTE: liberar el binding cuando la Vista se destruye
    // Evita fugas de memoria (memory leaks)
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

### Ciclo de vida del Fragment

```
onAttach → onCreate → onCreateView → onViewCreated → onStart → onResume
                                                              ↓
                                               onPause → onStop → onDestroyView → onDestroy → onDetach
```

Lo más importante a recordar: libera el binding en `onDestroyView()`, no en `onDestroy()`.

---

## RecyclerView — listas eficientes en Views

El equivalente al `LazyColumn` de Compose. Requiere bastante más código:

```kotlin
// 1. Adapter: sabe cómo mostrar cada elemento
class FruitAdapter(private val fruits: List<String>) :
    RecyclerView.Adapter<FruitAdapter.FruitViewHolder>() {

    // ViewHolder: guarda referencias a las Views de cada elemento
    class FruitViewHolder(private val binding: ItemFruitBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(fruit: String) {
            binding.fruitName.text = fruit
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FruitViewHolder {
        val binding = ItemFruitBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FruitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FruitViewHolder, position: Int) {
        holder.bind(fruits[position])
    }

    override fun getItemCount() = fruits.size
}

// 2. Configurar en el Fragment
binding.recyclerView.adapter = FruitAdapter(listOf("Manzana", "Pera", "Uva"))
binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
```

Compara esto con `LazyColumn` en Compose y notarás por qué Compose es mucho más conciso.

---

**→ Continúa con [[02 - Interoperabilidad Views y Compose|Ruta 2 — Interoperabilidad]]**
