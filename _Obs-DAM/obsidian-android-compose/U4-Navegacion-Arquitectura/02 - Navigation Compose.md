# Ruta 2 — Navigation Compose

**← [[01 - ViewModel y Ciclo de Vida|Ruta 1]]** | **→ [[03 - Diseños Adaptables|Ruta 3]]**

---

## ¿Qué es el componente Navigation?

Navigation es una librería de Jetpack que gestiona la navegación entre pantallas de la app. En lugar de lanzar Intents a mano o mantener el "historial de pantallas" manualmente, defines un **grafo de navegación** y Navigation gestiona todo el resto:

- El back stack (historial de pantallas)
- El botón "Atrás"
- El paso de datos entre pantallas
- Las animaciones de transición

La app con la que se practica es **Cupcake**: una app para pedir cupcakes donde el usuario pasa por varias pantallas (cantidad → sabor → fecha de recogida → resumen).

### Dependencia

```kotlin
implementation("androidx.navigation:navigation-compose:2.7.7")
```

---

## Los tres elementos clave

### 1. `NavController`

El `NavController` es el objeto que gestiona la navegación. Lo creas con `rememberNavController()` en el composable raíz y lo pasas a donde lo necesites:

```kotlin
@Composable
fun CupcakeApp() {
    val navController = rememberNavController()
    // ...
}
```

Métodos principales:
- `navController.navigate("ruta")` — navega a esa pantalla (la añade al back stack)
- `navController.navigateUp()` — vuelve a la pantalla anterior (equivale al botón "Atrás")
- `navController.popBackStack("ruta", inclusive = false)` — vuelve a una pantalla concreta del historial

### 2. `NavHost`

El `NavHost` define todas las pantallas disponibles y sus rutas. Cada `composable(route = "ruta")` registra una pantalla:

```kotlin
NavHost(
    navController = navController,
    startDestination = "inicio"    // pantalla que se muestra al abrir la app
) {
    composable(route = "inicio") {
        PantallaInicio(
            onSiguiente = { navController.navigate("sabor") }
        )
    }
    composable(route = "sabor") {
        PantallaSabor(
            onSiguiente = { navController.navigate("fecha") },
            onVolver    = { navController.navigateUp() }
        )
    }
    composable(route = "fecha") {
        PantallaFecha(
            onSiguiente = { navController.navigate("resumen") },
            onVolver    = { navController.navigateUp() }
        )
    }
    composable(route = "resumen") {
        PantallaResumen(
            onCancelar = { navController.popBackStack("inicio", inclusive = false) }
        )
    }
}
```

### 3. Rutas con enum

En vez de strings sueltos (donde un error tipográfico causa un crash), es mejor práctica usar un `enum`:

```kotlin
enum class CupcakeScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Flavor(title = R.string.choose_flavor),
    Pickup(title = R.string.choose_pickup_date),
    Summary(title = R.string.order_summary)
}
```

Uso:
```kotlin
NavHost(
    navController = navController,
    startDestination = CupcakeScreen.Start.name  // ".name" devuelve el String "Start"
) {
    composable(route = CupcakeScreen.Start.name) { ... }
    composable(route = CupcakeScreen.Flavor.name) { ... }
    // ...
}

// Navegar
navController.navigate(CupcakeScreen.Flavor.name)
```

---

## Pasar argumentos entre pantallas

Cuando necesitas pasar un dato de una pantalla a otra (por ejemplo, el ID de un elemento seleccionado):

```kotlin
// 1. Definir la ruta con un parámetro entre llaves {}
composable(
    route = "detalle/{productoId}",
    arguments = listOf(
        navArgument("productoId") { type = NavType.IntType }
    )
) { backStackEntry ->
    // 2. Recuperar el argumento del backStackEntry
    val productoId = backStackEntry.arguments?.getInt("productoId")
    PantallaDetalle(productoId = productoId ?: 0)
}

// 3. Navegar pasando el valor en la URL
navController.navigate("detalle/42")
```

Para strings:
```kotlin
composable("perfil/{nombre}") { backStackEntry ->
    val nombre = backStackEntry.arguments?.getString("nombre")
    PantallaPerfil(nombre = nombre ?: "")
}
navController.navigate("perfil/Alex")
```

---

## Compartir estado entre pantallas con ViewModel

En la app Cupcake, el pedido se construye en varias pantallas (cantidad, sabor, fecha). La forma más sencilla de compartir esos datos es usar un **ViewModel compartido** a nivel del `NavHost`:

```kotlin
@Composable
fun CupcakeApp() {
    val navController = rememberNavController()
    val viewModel: OrderViewModel = viewModel()  // ← mismo ViewModel para todas las pantallas

    NavHost(navController = navController, startDestination = CupcakeScreen.Start.name) {
        composable(CupcakeScreen.Start.name) {
            StartOrderScreen(
                onNextButtonClicked = { cantidad ->
                    viewModel.setQuantity(cantidad)
                    navController.navigate(CupcakeScreen.Flavor.name)
                }
            )
        }
        composable(CupcakeScreen.Flavor.name) {
            // El mismo viewModel tiene el estado del pedido
            OrderSummaryScreen(
                orderUiState = viewModel.uiState.value,
                onNextButtonClicked = { navController.navigate(CupcakeScreen.Pickup.name) },
                onCancelButtonClicked = { cancelAndNavigateToStart(viewModel, navController) }
            )
        }
        // ...
    }
}

fun cancelAndNavigateToStart(viewModel: OrderViewModel, navController: NavController) {
    viewModel.resetOrder()
    navController.popBackStack(CupcakeScreen.Start.name, inclusive = false)
}
```

---

## `TopAppBar` con botón "Atrás"

Es habitual añadir una barra superior que muestre el título de la pantalla actual y un botón para volver:

```kotlin
@Composable
fun CupcakeAppBar(
    currentScreen: CupcakeScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        },
        modifier = modifier
    )
}
```

Para saber en qué pantalla estás y si puedes volver:
```kotlin
val backStackEntry by navController.currentBackStackEntryAsState()
val currentScreen = CupcakeScreen.valueOf(
    backStackEntry?.destination?.route ?: CupcakeScreen.Start.name
)
val canNavigateBack = navController.previousBackStackEntry != null
```

---

## Probar la navegación

Las pruebas de navegación requieren el contexto de Android (se ejecutan en el emulador), pero la API de testing de Compose las hace bastante sencillas:

```kotlin
@RunWith(AndroidJUnit4::class)
class CupcakeScreenNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun setupCupcakeNavHost() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            CupcakeApp(navController = navController)
        }
    }

    @Test
    fun cupcakeNavHost_verifyStartDestination() {
        navController.assertCurrentRouteName(CupcakeScreen.Start.name)
    }

    @Test
    fun cupcakeNavHost_clickNextOnStartScreen_navigatesToFlavorScreen() {
        composeTestRule.onNodeWithStringId(R.string.one_cupcake).performClick()
        navController.assertCurrentRouteName(CupcakeScreen.Flavor.name)
    }
}
```

---

**→ Continúa con [[03 - Diseños Adaptables|Ruta 3 — Diseños adaptables]]**
