# Unidad 8 — Views y Compose

**← [[../U7-WorkManager/00 - Unidad 7 Resumen|Unidad 7]]** | **← [[../000 - Índice del Curso|Volver al índice]]**

Duración: ~4 horas | 2 rutas de aprendizaje

---

## ¿Por qué existe esta unidad?

Aunque este curso enseña Compose desde cero, la realidad del mundo laboral es que hay millones de apps Android en producción escritas con el **sistema de Views** (el sistema anterior a Compose, basado en XML). Si vas a trabajar en Android, es probable que te encuentres con apps legadas que usan Views.

Esta unidad tiene dos objetivos:
1. Entender cómo funciona el sistema de Views
2. Saber cómo **mezclar Views y Compose** en la misma app (interoperabilidad)

---

## Ruta 1 — El sistema de Views

### ¿Qué son las Views?

Las **Views** son los componentes de UI del sistema antiguo de Android. Cada elemento visual es un objeto de la clase `View` o una subclase:

- `TextView` → muestra texto (≈ `Text` en Compose)
- `ImageView` → muestra una imagen (≈ `Image`)
- `Button` → botón (≈ `Button`)
- `EditText` → campo de texto editable (≈ `TextField`)
- `RecyclerView` → lista desplazable eficiente (≈ `LazyColumn`)
- `ConstraintLayout`, `LinearLayout`, `FrameLayout` → contenedores (≈ `Column`, `Row`, `Box`)

### Diferencias fundamentales con Compose

| Views (sistema antiguo) | Compose (sistema nuevo) |
|------------------------|------------------------|
| UI definida en archivos XML | UI definida en código Kotlin |
| Dos archivos separados (XML + código) | Un único archivo Kotlin |
| **Imperativo**: dices *cómo* construir la UI paso a paso | **Declarativo**: dices *qué* debe mostrar la UI |
| Las Views son mutables (se actualizan manualmente) | Los composables son inmutables (se recrean cuando el estado cambia) |
| `view.text = "nuevo texto"` | Cambia el estado → Compose redibuja |
| Hierarchy de Views en memoria | Árbol de composables (más ligero) |

### Cómo se define la UI en XML

```xml
<!-- res/layout/activity_main.xml -->
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:id="@+id/titleText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Mi título"
        android:textSize="24sp" />

    <Button
        android:id="@+id/myButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Pulsar" />

</LinearLayout>
```

En el código Kotlin, conectas el XML con la lógica:

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // infla el XML

        // Encontrar las Views por su id
        val titleText = findViewById<TextView>(R.id.titleText)
        val myButton = findViewById<Button>(R.id.myButton)

        // Modificarlas desde código
        titleText.text = "Texto cambiado"

        myButton.setOnClickListener {
            titleText.text = "Botón pulsado"
        }
    }
}
```

### ViewBinding — la forma moderna de acceder a Views

`findViewById` es propenso a errores (puedes escribir mal el id, o usar el tipo de View incorrecto). **ViewBinding** genera automáticamente una clase Kotlin con referencias type-safe a todas las Views del layout:

```kotlin
// build.gradle.kts — activar ViewBinding
android {
    buildFeatures {
        viewBinding = true
    }
}
```

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding  // clase generada automáticamente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Acceder a las Views directamente sin findViewById
        binding.titleText.text = "Texto cambiado"
        binding.myButton.setOnClickListener {
            binding.titleText.text = "Botón pulsado"
        }
    }
}
```

### Fragment — la pantalla reutilizable

En el sistema de Views, las pantallas no siempre son Activities enteras. Un **Fragment** es una parte de la UI que puede reutilizarse en diferentes Activities, o combinar varios en la misma pantalla (muy útil en tablets).

```kotlin
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.myButton.setOnClickListener { /* ... */ }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // evitar memory leaks
    }
}
```

---

## Ruta 2 — Interoperabilidad: mezclar Views y Compose

### Caso 1: Añadir Compose a una app de Views

Si tienes una app existente en Views y quieres empezar a usar Compose en partes nuevas (sin reescribir todo):

**En el layout XML**, añade un `ComposeView` donde quieres que vaya el composable:

```xml
<LinearLayout ...>
    <TextView ... />

    <!-- Aquí irá contenido de Compose -->
    <androidx.compose.ui.platform.ComposeView
        android:id="@+id/compose_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
</LinearLayout>
```

**En el Fragment o Activity**, configura el contenido del `ComposeView`:

```kotlin
binding.composeView.setContent {
    // Aquí puedes usar cualquier composable
    MaterialTheme {
        MiComposable()
    }
}
```

### Caso 2: Usar una View dentro de un Composable (`AndroidView`)

A veces necesitas usar un componente que solo existe como View y aún no tiene equivalente en Compose: un mapa, un reproductor de video, un editor de texto enriquecido...

El composable `AndroidView` actúa como puente:

```kotlin
@Composable
fun MiWebView(url: String) {
    AndroidView(
        factory = { context ->
            // Se llama UNA VEZ al crear la View
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
            }
        },
        update = { webView ->
            // Se llama cada vez que cambia url u otro estado de Compose
            webView.loadUrl(url)
        },
        modifier = Modifier.fillMaxSize()
    )
}
```

- `factory`: lambda que crea e inicializa la View. Se ejecuta una sola vez
- `update`: lambda que actualiza la View cuando el estado de Compose cambia. Se ejecuta en la recomposición

### Caso 3: `AndroidViewBinding` — usar un layout XML completo en Compose

Si tienes un layout XML complejo que quieres reutilizar dentro de Compose sin reescribirlo:

```kotlin
@Composable
fun MiLayoutXmlEnCompose() {
    AndroidViewBinding(MiLayoutBinding::inflate) {
        // this = la instancia del binding
        miTexto.text = "Hola desde XML"
        miBoton.setOnClickListener { /* ... */ }
    }
}
```

### ¿Cuándo usar qué?

| Situación | Solución recomendada |
|-----------|---------------------|
| App nueva desde cero | Compose puro |
| App existente en Views, nueva funcionalidad | Añadir Compose con `ComposeView` |
| Composable que necesita un widget sin equivalente | `AndroidView` |
| Reescritura gradual de pantalla por pantalla | `ComposeView` en Activities/Fragments existentes |
| El widget solo existe como XML | `AndroidViewBinding` |

### Estrategia de migración gradual

Si tienes una app grande en Views y quieres migrar a Compose, Google recomienda hacerlo de forma incremental:

1. **No reescribas todo de golpe**: es un riesgo enorme y tarda muchísimo
2. **Empieza por pantallas nuevas**: escríbelas en Compose directamente
3. **Migra pantallas existentes de afuera hacia adentro**: primero los elementos de UI más simples (textos, botones), luego los contenedores
4. **Usa el ViewModel**: si ya usas ViewModel + StateFlow, la migración de la capa de UI es más sencilla porque la lógica no cambia

---

## 🎓 ¡Fin del curso!

Has completado los **Android Basics with Compose**. Ahora sabes:

| Tema | Qué aprendiste |
|------|---------------|
| **Kotlin** | Variables, funciones, clases, lambdas, corrutinas, Flow |
| **Compose** | Composables, estado, recomposición, layouts, listas, animaciones |
| **Arquitectura** | MVVM, ViewModel, UDF, capa de datos, Repositorio |
| **Navegación** | Navigation Compose, back stack, argumentos entre pantallas |
| **Red** | Retrofit, JSON, Coil, inyección de dependencias |
| **Persistencia** | Room (SQLite), DataStore |
| **Segundo plano** | WorkManager, Workers, restricciones, cadenas |
| **Diseño** | Material Design 3, temas, colores, tipografía, diseños adaptables |
| **Legacy** | Sistema de Views, ViewBinding, interoperabilidad Views↔Compose |

### Próximos pasos sugeridos

- **Hilt**: inyección de dependencias automatizada (alternativa al DI manual del curso)
- **Paging 3**: cargar listas grandes de forma eficiente con paginación
- **Jetpack Compose avanzado**: animaciones complejas, Canvas, gestos
- **Testing avanzado**: pruebas de UI con Espresso y Compose Testing
- **Firebase**: autenticación, base de datos en tiempo real, notificaciones push
- **Google Play**: cómo publicar tu primera app
