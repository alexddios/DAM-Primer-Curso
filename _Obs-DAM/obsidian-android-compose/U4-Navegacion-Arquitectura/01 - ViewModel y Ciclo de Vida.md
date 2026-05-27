# Ruta 1 — ViewModel, ciclo de vida y arquitectura

**← [[U4-Navegacion-Arquitectura|Unidad 4]]** | **→ [[02 - Navigation Compose|Ruta 2]]**

---

## El ciclo de vida de una Activity

Una **Activity** es básicamente una pantalla de la app. Android gestiona su existencia a través de una serie de métodos del ciclo de vida que se invocan en distintos momentos:

```
         onCreate()    ← la activity se crea. Aquí inicializas todo (UI, ViewModel...)
              ↓
         onStart()     ← la activity se hace visible al usuario
              ↓
         onResume()    ← está en primer plano, el usuario puede interactuar
              ↓
    [ App activa y visible ]
              ↓
         onPause()     ← pierde el foco (otra app aparece encima, o dialog)
              ↓
         onStop()      ← ya no es visible (el usuario fue a otra app o pulsó Home)
              ↓
         onDestroy()   ← la activity se destruye definitivamente
```

Si el usuario vuelve (pulsa "atrás" de otra app, o vuelve desde el historial):
```
onStop → onRestart → onStart → onResume
```

### El problema de la rotación

Cuando el usuario **rota el dispositivo**, Android destruye y vuelve a crear la Activity completa (`onDestroy` + `onCreate`). Esto significa que cualquier variable declarada en la Activity **se pierde**.

Ejemplo del problema:
```kotlin
class MainActivity : ComponentActivity() {
    var puntuacion = 0  // ← SE PIERDE al rotar

    override fun onCreate(...) {
        puntuacion++  // empieza siempre en 1 al rotar
    }
}
```

La solución a esto es el **ViewModel**.

---

## Arquitectura MVVM

El patrón **MVVM** (Model-View-ViewModel) es la arquitectura recomendada por Google para apps Android. Divide el código en capas con responsabilidades bien separadas:

```
┌──────────────────────────────────────┐
│           UI Layer (Vista)            │
│   Composables / Activity             │
│   Solo dibuja la UI y reenvía        │
│   eventos al ViewModel               │
├──────────────────────────────────────┤
│          ViewModel                    │
│   Guarda el estado de la UI          │
│   Contiene la lógica de presentación │
│   Sobrevive a cambios de config.     │
├──────────────────────────────────────┤
│          Data Layer (Modelo)          │
│   Repositorios                       │
│   Fuentes de datos (red, BD, etc.)   │
└──────────────────────────────────────┘
```

**Regla de oro**: las dependencias solo van hacia abajo. La UI depende del ViewModel, el ViewModel depende del Repositorio. Nunca al revés.

### Flujo Unidireccional de Datos (UDF)

El **Unidirectional Data Flow** es el patrón que usa Compose para comunicar la UI con el ViewModel:

```
ViewModel ──── estado ────► Composable
    ▲                           │
    └─────── eventos ───────────┘
```

- El **estado** fluye hacia abajo: el ViewModel expone el estado, el composable lo lee y lo muestra
- Los **eventos** fluyen hacia arriba: el usuario interactúa, el composable llama al ViewModel, el ViewModel actualiza el estado

Ventaja: el estado solo tiene un origen de verdad (el ViewModel) y solo se modifica desde ahí. Es fácil de depurar porque sabes exactamente de dónde viene cada cambio.

---

## ViewModel en la práctica

### Crear un ViewModel

```kotlin
class UnscrambleViewModel : ViewModel() {
    // Estado privado y mutable: solo el ViewModel puede cambiarlo
    private val _uiState = MutableStateFlow(GameUiState())

    // Estado público e inmutable: la UI lo puede leer pero no modificar
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // Función que la UI llama cuando el usuario hace algo
    fun checkUserGuess(userGuess: String) {
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            _uiState.update { currentState ->
                currentState.copy(
                    score = currentState.score + SCORE_INCREASE,
                    isGuessedWordWrong = false
                )
            }
        } else {
            _uiState.update { it.copy(isGuessedWordWrong = true) }
        }
    }
}
```

### El UiState como data class

Toda la información que la UI necesita para dibujarse se agrupa en una `data class`:

```kotlin
data class GameUiState(
    val currentScrambledWord: String = "",
    val currentWordCount: Int = 1,
    val score: Int = 0,
    val isGuessedWordWrong: Boolean = false,
    val isGameOver: Boolean = false
)
```

Usar `data class` es clave porque tiene el método `copy()`, que crea una nueva copia cambiando solo los campos que especificas:
```kotlin
// Solo cambia score, el resto se mantiene igual
_uiState.update { it.copy(score = it.score + 10) }
```

### `MutableStateFlow` vs `StateFlow`

- **`MutableStateFlow`**: es mutable. El ViewModel lo tiene `private` y lo modifica internamente con `.update { }` o asignando `.value`
- **`StateFlow`**: es inmutable. Es lo que se expone públicamente. La UI lo lee pero no puede escribir en él

Este patrón de tener una versión mutable privada y una inmutable pública se usa mucho en Android:

```kotlin
private val _uiState = MutableStateFlow(GameUiState())  // privado, mutable
val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()  // público, inmutable
```

### Conectar el ViewModel al Composable

```kotlin
@Composable
fun GameScreen(
    gameViewModel: UnscrambleViewModel = viewModel()  // viewModel() lo crea o recupera el existente
) {
    // collectAsState() convierte el StateFlow en un State de Compose
    val gameUiState by gameViewModel.uiState.collectAsState()

    // Usar los datos del estado en la UI
    Text(text = gameUiState.currentScrambledWord)
    Text(text = "Puntuación: ${gameUiState.score}")

    // Enviar eventos al ViewModel
    Button(onClick = { gameViewModel.checkUserGuess(userInput) }) {
        Text("Comprobar")
    }
}
```

`viewModel()` hace dos cosas importantes:
1. Si el ViewModel no existe, lo crea
2. Si ya existe (por ejemplo, la Activity se está recreando por rotación), devuelve el mismo — **por eso el estado sobrevive a la rotación**

### Dependencia necesaria

```kotlin
// build.gradle.kts (módulo app)
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
```

---

## Pruebas del ViewModel

Las pruebas de unidad del ViewModel son fáciles de escribir porque no dependen de Android (no necesitas emulador, se ejecutan directamente en la JVM):

```kotlin
class UnscrambleViewModelTest {

    // Instanciamos el ViewModel directamente, sin contexto de Android
    private val viewModel = UnscrambleViewModel()

    @Test
    fun gameViewModel_Initialization_FirstWordLoaded() {
        val gameUiState = viewModel.uiState.value
        assertFalse(
            "El juego no debería haber terminado al inicio",
            gameUiState.isGameOver
        )
        assertNotEquals(
            "La palabra inicial no debería estar vacía",
            "",
            gameUiState.currentScrambledWord
        )
    }

    @Test
    fun gameViewModel_CorrectGuess_ScoreIncreasesAndErrorFlagClears() {
        val initialState = viewModel.uiState.value
        val correctWord = getUnscrambledWord(initialState.currentScrambledWord)

        viewModel.updateUserGuess(correctWord)
        viewModel.checkUserGuess()

        val updatedState = viewModel.uiState.value
        assertFalse(updatedState.isGuessedWordWrong)
        assertEquals(SCORE_AFTER_FIRST_CORRECT, updatedState.score)
    }

    @Test
    fun gameViewModel_WrongGuess_ErrorFlagIsSet() {
        viewModel.updateUserGuess("respuestaIncorrecta")
        viewModel.checkUserGuess()

        assertTrue(viewModel.uiState.value.isGuessedWordWrong)
    }
}
```

Convención de nombres para pruebas: `nombreMetodo_condicion_resultadoEsperado`

---

**→ Continúa con [[02 - Navigation Compose|Ruta 2 — Navigation Compose]]**
