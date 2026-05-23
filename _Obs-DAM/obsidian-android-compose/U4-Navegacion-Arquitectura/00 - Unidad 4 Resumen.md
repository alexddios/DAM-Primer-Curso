# Unidad 4 — Navegación y Arquitectura de la App

**← [[../U3-Listas-Material/00 - Unidad 3 Resumen|Unidad 3]]** | **→ [[../U5-Internet/00 - Unidad 5 Resumen|Unidad 5]]**

Duración: ~28 horas | 3 rutas de aprendizaje

---

## Ruta 1 — Componentes de Arquitectura: ViewModel y Ciclo de Vida

### El ciclo de vida de una Activity

Una **Activity** es una pantalla de la app. Android gestiona su ciclo de vida mediante una serie de métodos que se llaman en distintos momentos:

```
Creada
  ↓ onCreate()   ← se llama al crear. Aquí inicializas la UI
  ↓ onStart()    ← la activity se hace visible
  ↓ onResume()   ← está en primer plano, el usuario interactúa
  
[el usuario sale o llega otra app]
  ↓ onPause()    ← pierde el foco (otra app encima)
  ↓ onStop()     ← ya no es visible
  
[el usuario vuelve]
  ↑ onRestart() → onStart() → onResume()
  
[la activity se cierra o el sistema la mata]
  ↓ onDestroy()
```

**Problema importante:** cuando el usuario rota el dispositivo, Android destruye y vuelve a crear la Activity (`onDestroy` + `onCreate`). Esto significa que **cualquier variable que hayas declarado en la Activity se pierde**. Si tenías datos (puntuación de un juego, texto escrito, resultados cargados...), desaparecen.

La solución es el **ViewModel**.

---

### ¿Qué es el ViewModel?

El `ViewModel` es una clase de Jetpack que guarda el estado y la lógica de presentación de una pantalla. Su ciclo de vida está ligado al de la pantalla, no al de la Activity, así que **sobrevive a los cambios de configuración** como la rotación.

```
Activity/Composable          ViewModel
┌─────────────────┐         ┌──────────────────────────────┐
│ Solo UI:        │  lee    │ Guarda el estado (UiState)   │
│ muestra datos   │◄────────│ Contiene la lógica           │
│ envía eventos   │─────────► Procesa eventos del usuario  │
└─────────────────┘ eventos └──────────────────────────────┘
```

Regla general: **todo lo que no sea dibujar la UI debería estar en el ViewModel**. La Activity/Composable solo dibuja y reenvía eventos al ViewModel.

### Arquitectura en capas

El curso sigue la arquitectura recomendada por Google, que divide la app en capas con responsabilidades bien separadas:

```
┌────────────────────────────────────┐
│           UI Layer                 │
│  Composables / Activity            │
│  Solo muestran datos y envían      │
│  eventos al ViewModel              │
├────────────────────────────────────┤
│        Domain Layer (opcional)     │
│  Lógica de negocio compleja        │
│  Use Cases / Interactors           │
├────────────────────────────────────┤
│          Data Layer                │
│  Repositorios                      │
│  Fuentes de datos (red, BD...)     │
└────────────────────────────────────┘
```

Las dependencias solo van hacia abajo: la UI depende del ViewModel, el ViewModel depende del Repositorio, nunca al revés.

### Unidirectional Data Flow (UDF)

El **flujo unidireccional de datos** es el patrón que usa Compose:

- El estado fluye **hacia abajo**: el ViewModel expone el estado, los composables lo leen y muestran
- Los eventos fluyen **hacia arriba**: el usuario interactúa, el composable llama a una función del ViewModel, el ViewModel actualiza el estado

```
ViewModel ──estado──► Composable
    ▲                     │
    └──────eventos─────────┘
```

Esto hace el código más predecible y fácil de depurar: el estado solo cambia desde el ViewModel, nunca directamente desde la UI.

### `StateFlow` y `MutableStateFlow`

Para exponer el estado desde el ViewModel, se usa `StateFlow`:

- **`MutableStateFlow`**: mutable, solo el ViewModel lo modifica (es privado)
- **`StateFlow`**: inmutable, lo que expone al exterior para que la UI lo lea

```kotlin
class UnscrambleViewModel : ViewModel() {

    // Estado privado y mutable (solo el ViewModel lo cambia)
    private val _uiState = MutableStateFlow(GameUiState())

    // Estado público e inmutable (la UI lo lee)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun checkUserGuess(userGuess: String) {
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            val updatedScore = _uiState.value.score.inc()
            updateGameState(updatedScore)
        } else {
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
    }

    private fun updateGameState(updatedScore: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                isGuessedWordWrong = false,
                currentScrambledWord = pickRandomWordAndShuffle(),
                score = updatedScore
            )
        }
    }
}
```

### El `UiState` como data class

El estado de la UI se modela como una `data class`. Contiene **todo lo que la UI necesita para dibujarse**:

```kotlin
data class GameUiState(
    val currentScrambledWord: String = "",
    val isGuessedWordWrong: Boolean = false,
    val score: Int = 0,
    val currentWordCount: Int = 1,
    val isGameOver: Boolean = false
)
```

Usar `data class` es conveniente porque tiene el método `copy()`, que permite crear una copia cambiando solo los campos que quieres:

```kotlin
_uiState.update { it.copy(score = it.score + 1) }
```

### Conectar el ViewModel a los Composables

En el composable se obtiene el ViewModel con `viewModel()` y se recoge el estado con `collectAsState()`:

```kotlin
@Composable
fun GameScreen(
    gameViewModel: UnscrambleViewModel = viewModel()  // inyección automática
) {
    // Recoge el StateFlow como State de Compose
    val gameUiState by gameViewModel.uiState.collectAsState()

    // Usar los datos del estado
    Text(text = gameUiState.currentScrambledWord)
    Text(text = "Puntuación: ${gameUiState.score}")

    // Enviar eventos al ViewModel
    Button(onClick = { gameViewModel.checkUserGuess(userInput) }) {
        Text("Comprobar")
    }
}
```

`collectAsState()` convierte el `StateFlow` en un `State` de Compose, de modo que cuando el `StateFlow` emite un nuevo valor, Compose lo detecta y recompone la UI automáticamente.

### Dependencia necesaria

```kotlin
// build.gradle.kts (módulo app)
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
```

### Pruebas del ViewModel

Las pruebas de unidad del ViewModel son muy sencillas porque no dependen de Android:

```kotlin
class UnscrambleViewModelTest {

    private val viewModel = UnscrambleViewModel()

    @Test
    fun gameViewModel_Initialization_FirstWordLoaded() {
        val gameUiState = viewModel.uiState.value
        assertFalse(gameUiState.isGameOver)
        assertNotEquals("", gameUiState.currentScrambledWord)
    }

    @Test
    fun gameViewModel_CorrectWordGuessed_ScoreUpdatedAndErrorFlagUnset() {
        var currentGameUiState = viewModel.uiState.value
        val correctPlayerWord = getUnscrambledWord(currentGameUiState.currentScrambledWord)

        viewModel.updateUserGuess(correctPlayerWord)
        viewModel.checkUserGuess()

        currentGameUiState = viewModel.uiState.value
        assertFalse(currentGameUiState.isGuessedWordWrong)
        assertEquals(SCORE_AFTER_FIRST_CORRECT_ANSWER, currentGameUiState.score)
    }
}
```

---

## Ruta 2 — Navigation Compose

### ¿Qué es el componente Navigation?

Navigation es una librería de Jetpack que gestiona la navegación entre pantallas (composables) de forma declarativa. En lugar de lanzar Intents manualmente o gestionar el back stack a mano, defines un **grafo de navegación** y Navigation se encarga del resto.

Ventajas:
- Gestión automática del botón "Atrás"
- Paso de argumentos entre pantallas de forma segura
- Soporte para deep links
- Fácil de probar

### Dependencia

```kotlin
implementation("androidx.navigation:navigation-compose:2.7.7")
```

### Configurar el NavController y el NavHost

El `NavController` es el objeto central que gestiona la navegación. El `NavHost` define las pantallas disponibles y sus rutas.

```kotlin
@Composable
fun CupcakeApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "inicio"  // pantalla inicial
    ) {
        // Cada composable() define una pantalla y su ruta
        composable(route = "inicio") {
            PantallaInicio(
                onSiguiente = { navController.navigate("sabor") }
            )
        }
        composable(route = "sabor") {
            PantallaSabor(
                onSiguiente = { navController.navigate("resumen") },
                onVolver = { navController.navigateUp() }
            )
        }
        composable(route = "resumen") {
            PantallaResumen(
                onVolver = { navController.navigateUp() }
            )
        }
    }
}
```

- `navController.navigate("ruta")`: navega a esa pantalla (la añade al back stack)
- `navController.navigateUp()`: equivale al botón "Atrás" (saca del back stack)

### Usar enums para las rutas

En vez de strings sueltos, es mejor práctica usar un enum:

```kotlin
enum class CupcakeScreen {
    Start,
    Flavor,
    Pickup,
    Summary
}

// En el NavHost
composable(route = CupcakeScreen.Start.name) { ... }
composable(route = CupcakeScreen.Flavor.name) { ... }

// Al navegar
navController.navigate(CupcakeScreen.Flavor.name)
```

Así el compilador detecta errores tipográficos en los nombres de las rutas.

### Pasar argumentos entre pantallas

```kotlin
// Definir la ruta con un argumento
composable(
    route = "detalle/{productoId}",
    arguments = listOf(navArgument("productoId") { type = NavType.IntType })
) { backStackEntry ->
    val id = backStackEntry.arguments?.getInt("productoId")
    PantallaDetalle(productoId = id)
}

// Navegar pasando el argumento
navController.navigate("detalle/42")
```

### Compartir datos entre pantallas con el ViewModel

En la app Cupcake, el pedido se va construyendo en varias pantallas. La forma recomendada es usar un **ViewModel compartido** en el NavHost:

```kotlin
@Composable
fun CupcakeApp() {
    val navController = rememberNavController()
    val viewModel: OrderViewModel = viewModel()  // se comparte entre todas las pantallas

    NavHost(...) {
        composable(CupcakeScreen.Flavor.name) {
            PantallaSabor(
                viewModel = viewModel,
                onSiguiente = { navController.navigate(CupcakeScreen.Pickup.name) }
            )
        }
        // ...
    }
}
```

### Navegar de vuelta a una pantalla concreta (popBackStack)

`navigateUp()` siempre vuelve a la pantalla anterior. Si quieres ir a una pantalla concreta del back stack:

```kotlin
navController.popBackStack(CupcakeScreen.Start.name, inclusive = false)
// inclusive = false: va a Start pero lo mantiene en el stack
// inclusive = true: va a Start y lo saca del stack
```

---

## Ruta 3 — Diseños adaptables para múltiples pantallas

### El problema: distintos tamaños de dispositivo

Una app Android puede ejecutarse en teléfonos de 5", tablets de 10", o dispositivos plegables. El mismo layout que funciona bien en un móvil queda horrible en una tablet (con mucho espacio desaprovechado).

La solución es un diseño **adaptable**: la app detecta el tamaño de pantalla disponible y cambia su layout en consecuencia.

### `WindowSizeClass`

`WindowSizeClass` es la API de Jetpack que clasifica el tamaño de la ventana en categorías:

| Categoría | Ancho típico | Dispositivo típico |
|-----------|-------------|-------------------|
| `Compact` | < 600dp | Móviles en vertical |
| `Medium` | 600–840dp | Tablets en vertical, móviles en horizontal |
| `Expanded` | > 840dp | Tablets en horizontal, dispositivos plegables abiertos |

```kotlin
// En MainActivity (no en un composable)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            ReplyApp(windowSize = windowSizeClass.widthSizeClass)
        }
    }
}
```

Dependencia necesaria:
```kotlin
implementation("androidx.compose.material3:material3-window-size-class")
```

### Adaptar el layout según el tamaño

```kotlin
@Composable
fun ReplyApp(windowSize: WindowWidthSizeClass) {
    when (windowSize) {
        WindowWidthSizeClass.Compact -> {
            // Móvil: navigation bar abajo
            ReplyAppCompact()
        }
        WindowWidthSizeClass.Medium -> {
            // Tablet vertical: navigation rail a la izquierda
            ReplyAppMedium()
        }
        WindowWidthSizeClass.Expanded -> {
            // Tablet horizontal: navigation drawer permanente + dos paneles
            ReplyAppExpanded()
        }
    }
}
```

### Tipos de navegación según el tamaño

Google recomienda diferentes patrones de navegación según el espacio disponible:

**Compact — Bottom Navigation Bar (barra inferior)**
```kotlin
NavigationBar {
    items.forEach { item ->
        NavigationBarItem(
            icon = { Icon(item.icon, contentDescription = null) },
            label = { Text(item.label) },
            selected = currentDestination == item.destination,
            onClick = { navController.navigate(item.destination) }
        )
    }
}
```

**Medium — Navigation Rail (barra lateral compacta)**
```kotlin
NavigationRail {
    items.forEach { item ->
        NavigationRailItem(
            icon = { Icon(item.icon, contentDescription = null) },
            label = { Text(item.label) },
            selected = currentDestination == item.destination,
            onClick = { navController.navigate(item.destination) }
        )
    }
}
```

**Expanded — Navigation Drawer (panel lateral permanente)**
```kotlin
PermanentNavigationDrawer(
    drawerContent = {
        PermanentDrawerSheet {
            items.forEach { item ->
                NavigationDrawerItem(
                    icon = { Icon(item.icon, contentDescription = null) },
                    label = { Text(item.label) },
                    selected = currentDestination == item.destination,
                    onClick = { navController.navigate(item.destination) }
                )
            }
        }
    }
) {
    // contenido principal
}
```

### Layout de dos paneles (master-detail)

En tablets en horizontal suele haber espacio para mostrar lista y detalle al mismo tiempo:

```kotlin
@Composable
fun ReplyAppExpanded(viewModel: ReplyViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Row(modifier = Modifier.fillMaxSize()) {
        // Panel izquierdo: lista
        ReplyEmailList(
            emails = uiState.emails,
            onEmailClick = { viewModel.selectEmail(it) },
            modifier = Modifier.weight(1f)
        )

        // Panel derecho: detalle
        ReplyEmailDetail(
            email = uiState.selectedEmail,
            modifier = Modifier.weight(1f)
        )
    }
}
```

### Probar diseños adaptables

En el emulador puedes cambiar el tamaño de la ventana para probar distintos layouts sin cambiar de dispositivo. También puedes usar la **vista previa** de Android Studio con distintos tamaños:

```kotlin
@Preview(widthDp = 700)
@Composable
fun ReplyAppMediumPreview() {
    ReplyApp(windowSize = WindowWidthSizeClass.Medium)
}

@Preview(widthDp = 1000)
@Composable
fun ReplyAppExpandedPreview() {
    ReplyApp(windowSize = WindowWidthSizeClass.Expanded)
}
```

---

**→ Continúa con [[../U5-Internet/00 - Unidad 5 Resumen|Unidad 5 — Conectarse a Internet]]**
