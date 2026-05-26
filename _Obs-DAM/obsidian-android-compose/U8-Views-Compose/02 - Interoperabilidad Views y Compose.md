# Ruta 2 — Interoperabilidad entre Views y Compose

**← [[01 - Sistema de Views|Ruta 1]]** | **[[../000 - Índice del Curso|Volver al índice]]**

---

## ¿Por qué mezclar Views y Compose?

En la realidad, las apps no nacen y mueren de golpe. La mayoría de equipos tienen:
- Una app existente grande escrita en Views que no pueden reescribir desde cero
- Funcionalidades nuevas que quieren escribir en Compose
- Componentes de terceros que solo existen como Views

Compose fue diseñado desde el principio para **coexistir con Views**. La interoperabilidad puede funcionar en ambas direcciones:
- **Compose dentro de Views**: añadir composables en una pantalla de Views
- **Views dentro de Compose**: usar un componente de Views dentro de un composable

---

## Caso 1: Compose dentro de Views (`ComposeView`)

Este es el caso más común: tienes una app existente en Views y quieres añadir una pantalla o sección nueva en Compose sin reescribir todo.

### En el layout XML

Añade un `ComposeView` en el lugar donde quieres que aparezca el composable:

```xml
<!-- res/layout/fragment_home.xml -->
<LinearLayout ...>

    <TextView
        android:id="@+id/legacyTitle"
        android:text="Título heredado"
        ... />

    <!-- Aquí irá el contenido de Compose -->
    <androidx.compose.ui.platform.ComposeView
        android:id="@+id/compose_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />

</LinearLayout>
```

### En el Fragment o Activity

Configura el contenido del `ComposeView`:

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.composeView.apply {
        // Importante: define la estrategia de ciclo de vida del Composition
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        setContent {
            // Aquí puedes usar cualquier composable
            MiAppTheme {
                MiComposable()
            }
        }
    }
}
```

**`setViewCompositionStrategy`**: define cuándo se destruye la Composition (el árbol de composables). La opción `DisposeOnViewTreeLifecycleDestroyed` es la recomendada para Fragments: destruye la Composition cuando se destruye la vista del Fragment, evitando fugas de memoria.

---

## Caso 2: Views dentro de Compose (`AndroidView`)

Cuando necesitas un componente que solo existe como View y no tiene equivalente en Compose: `WebView`, `MapView`, `SurfaceView`, reproductores de video, widgets de terceros...

```kotlin
@Composable
fun MiWebView(url: String, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            // Se ejecuta UNA SOLA VEZ al crear el composable
            // Aquí creas e inicializas la View
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
            }
        },
        update = { webView ->
            // Se ejecuta cada vez que el composable se recompone
            // Aquí actualizas la View con el nuevo estado
            webView.loadUrl(url)
        },
        modifier = modifier
    )
}
```

Uso:
```kotlin
@Composable
fun PantallaDocumentacion() {
    var currentUrl by remember { mutableStateOf("https://developer.android.com") }

    Column {
        Button(onClick = { currentUrl = "https://kotlinlang.org" }) {
            Text("Ir a Kotlin")
        }
        MiWebView(
            url = currentUrl,
            modifier = Modifier.fillMaxSize()
        )
    }
}
```

Cuando `currentUrl` cambia, Compose llama al bloque `update` con la nueva URL y el `WebView` la carga.

### `AndroidViewBinding` — layout XML completo en Compose

Si tienes un layout XML más complejo (con varios elementos) que quieres reutilizar en Compose:

```kotlin
@Composable
fun MiLayoutXml(modifier: Modifier = Modifier) {
    AndroidViewBinding(
        factory = MiLayoutBinding::inflate,
        modifier = modifier
    ) {
        // "this" es la instancia del binding
        // Aquí configuras las Views como en un Fragment normal
        miTextoView.text = "Texto desde Compose"
        miBoton.setOnClickListener {
            // acción
        }
    }
}
```

---

## Estrategia de migración gradual

Si tienes una app grande en Views y quieres migrar a Compose, Google recomienda hacerlo de forma gradual. Intentar reescribir todo a la vez es arriesgado y lleva meses o años.

**Estrategia recomendada — de afuera hacia adentro:**

```
Pantalla en Views
├── TopAppBar         → migrar primero (simple)
├── Lista de items    → migrar con LazyColumn
│   └── Tarjeta item  → migrar a Card composable
└── BottomBar         → migrar (simple)
```

**Pasos prácticos:**

1. **Empieza por las pantallas nuevas**: escríbelas directamente en Compose
2. **Elementos de UI reutilizables**: crea componentes Compose para los nuevos botones, tarjetas, etc.
3. **Pantallas existentes simples primero**: migra las pantallas con menos lógica
4. **Usa el ViewModel durante la migración**: si ya usas ViewModel + StateFlow/LiveData, la capa de lógica no cambia al migrar la UI

---

## Resumen: qué usar en cada situación

| Situación | Solución |
|-----------|---------|
| App nueva desde cero | Compose puro |
| App en Views, nueva pantalla | Compose en la nueva pantalla, `ComposeView` si hay código XML de por medio |
| App en Views, nuevo componente pequeño | `ComposeView` dentro del layout XML |
| Composable necesita un widget sin equivalente | `AndroidView` |
| Reutilizar layout XML complejo en Compose | `AndroidViewBinding` |
| Migración gradual de pantalla existente | `ComposeView` para ir sustituyendo partes |

---

## 🎓 ¡Has terminado el curso!

Has completado **Android Basics with Compose**. Aquí tienes un resumen de todo lo que has aprendido:

| Unidad | Habilidades |
|--------|------------|
| 1 | Kotlin básico, Android Studio, Compose básico, composables, layouts |
| 2 | Kotlin intermedio (clases, lambdas), botones, estado, recomposición, state hoisting |
| 3 | Colecciones, funciones de orden superior, LazyColumn, Material Design 3 |
| 4 | ViewModel, ciclo de vida, MVVM, Navigation Compose, diseños adaptables |
| 5 | Corrutinas, Retrofit, REST/JSON, capa de datos, inyección de dependencias, Coil |
| 6 | SQL, Room (Entity/DAO/Database), Flow, Preferences DataStore |
| 7 | WorkManager, Workers, restricciones, cadenas de trabajo |
| 8 | Sistema de Views, ViewBinding, Fragments, interoperabilidad Views↔Compose |

### Qué aprender después

- **Hilt**: librería de Google para inyección de dependencias automatizada (más potente que el DI manual del curso)
- **Paging 3**: cargar listas enormes de forma eficiente con paginación
- **Jetpack Compose avanzado**: animaciones complejas, Canvas, gestos personalizados
- **Testing avanzado**: Espresso, Compose Testing, pruebas de integración
- **Firebase**: autenticación de usuarios, base de datos en tiempo real, notificaciones push
- **Publicar en Google Play**: firma de la app, store listing, política de privacidad
