# Ruta 3 — Diseños adaptables para múltiples pantallas

**← [[02 - Navigation Compose|Ruta 2]]** | **→ [[U5-Internet|Unidad 5]]**

---

## El problema: un diseño no sirve para todos los tamaños

Las apps Android pueden ejecutarse en dispositivos muy distintos: teléfonos de 5", tablets de 10", dispositivos plegables... Un layout pensado solo para móvil queda fatal en una tablet (texto diminuto con márgenes inmensos, o columna única cuando hay espacio para dos).

La solución es el **diseño adaptable**: la app detecta el espacio disponible y cambia su layout en consecuencia.

La app con la que se practica es **Reply**, un cliente de correo.

---

## `WindowSizeClass` — clasificar el tamaño de pantalla

`WindowSizeClass` es la API de Jetpack que clasifica el tamaño de la ventana en tres categorías:

| Clase | Ancho típico | Dispositivo típico |
|-------|-----------|--------------------|
| `Compact` | < 600dp | Teléfonos en vertical |
| `Medium` | 600–840dp | Tablets pequeñas en vertical, teléfonos en horizontal |
| `Expanded` | > 840dp | Tablets en horizontal, dispositivos plegables abiertos |

### Dependencia

```kotlin
implementation("androidx.compose.material3:material3-window-size-class")
```

### Obtener el WindowSizeClass

Se obtiene en la `Activity` (no en un composable) y se pasa como parámetro:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ReplyTheme {
                val windowSize = calculateWindowSizeClass(this)
                ReplyApp(windowSize = windowSize.widthSizeClass)
            }
        }
    }
}
```

### Adaptar el layout

```kotlin
@Composable
fun ReplyApp(windowSize: WindowWidthSizeClass) {
    when (windowSize) {
        WindowWidthSizeClass.Compact  -> ReplyAppCompact()
        WindowWidthSizeClass.Medium   -> ReplyAppMedium()
        WindowWidthSizeClass.Expanded -> ReplyAppExpanded()
        else                          -> ReplyAppCompact()
    }
}
```

---

## Tipos de navegación según el tamaño

Google recomienda distintos patrones de navegación dependiendo del espacio disponible:

### Compact — `NavigationBar` (barra inferior)

```kotlin
NavigationBar {
    items.forEach { item ->
        NavigationBarItem(
            icon    = { Icon(item.icon, contentDescription = null) },
            label   = { Text(stringResource(item.labelResId)) },
            selected = currentDestination == item.destination,
            onClick  = { onTabPressed(item.destination) }
        )
    }
}
```

La barra inferior tiene como máximo 5 destinos. Es el patrón más habitual en teléfonos porque es fácil de alcanzar con el pulgar.

### Medium — `NavigationRail` (barra lateral compacta)

```kotlin
NavigationRail(modifier = Modifier.padding(top = 8.dp)) {
    items.forEach { item ->
        NavigationRailItem(
            icon    = { Icon(item.icon, contentDescription = null) },
            label   = { Text(stringResource(item.labelResId)) },
            selected = currentDestination == item.destination,
            onClick  = { onTabPressed(item.destination) }
        )
    }
}
```

El `NavigationRail` va a la izquierda de la pantalla y deja el área principal libre.

### Expanded — `PermanentNavigationDrawer` (cajón permanente)

```kotlin
PermanentNavigationDrawer(
    drawerContent = {
        PermanentDrawerSheet(modifier = Modifier.width(240.dp)) {
            Spacer(Modifier.height(12.dp))
            items.forEach { item ->
                NavigationDrawerItem(
                    icon     = { Icon(item.icon, contentDescription = null) },
                    label    = { Text(stringResource(item.labelResId)) },
                    selected = currentDestination == item.destination,
                    onClick  = { onTabPressed(item.destination) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
) {
    // contenido principal a la derecha del drawer
    ReplyContent(...)
}
```

---

## Layout de dos paneles (master-detail)

En tablets en horizontal hay espacio para mostrar la lista y el detalle al mismo tiempo:

```kotlin
@Composable
fun ReplyAppExpanded(
    replyUiState: ReplyUiState,
    onEmailCardPressed: (Email) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Panel izquierdo: lista de emails
        ReplyEmailList(
            emails       = replyUiState.currentMailboxEmails,
            onEmailCardPressed = onEmailCardPressed,
            modifier     = Modifier
                .weight(1f)     // ocupa la mitad del espacio
                .padding(end = 8.dp)
        )

        // Panel derecho: detalle del email seleccionado
        ReplyEmailDetail(
            selectedEmail = replyUiState.currentSelectedEmail,
            modifier      = Modifier
                .weight(1f)     // ocupa la otra mitad
                .padding(start = 8.dp)
        )
    }
}
```

El `Modifier.weight(1f)` hace que cada panel ocupe el 50% del espacio disponible. Si quisieras 40/60:
```kotlin
Modifier.weight(0.4f)  // panel izquierdo: 40%
Modifier.weight(0.6f)  // panel derecho: 60%
```

---

## Probar diseños adaptables

### Previsualizaciones con tamaños específicos

```kotlin
@Preview(name = "Compacto", widthDp = 400)
@Preview(name = "Mediano", widthDp = 700)
@Preview(name = "Expandido", widthDp = 1000)
@Composable
fun ReplyAppPreview() {
    ReplyTheme {
        ReplyApp(windowSize = WindowWidthSizeClass.Compact)
    }
}
```

Para previsualizar cada variante directamente:
```kotlin
@Preview(widthDp = 400,  name = "Compact")
@Composable
fun ReplyCompactPreview() {
    ReplyTheme { ReplyAppCompact() }
}

@Preview(widthDp = 700,  name = "Medium")
@Composable
fun ReplyMediumPreview() {
    ReplyTheme { ReplyAppMedium() }
}

@Preview(widthDp = 1000, name = "Expanded")
@Composable
fun ReplyExpandedPreview() {
    ReplyTheme { ReplyAppExpanded() }
}
```

### Pruebas instrumentadas por tamaño

```kotlin
@RunWith(AndroidJUnit4::class)
class ReplyAppTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun compactDevice_verifyBottomNavigation() {
        composeTestRule.setContent {
            ReplyTheme {
                ReplyApp(windowSize = WindowWidthSizeClass.Compact)
            }
        }
        // La barra inferior debe estar visible en Compact
        composeTestRule.onNodeWithTagForStringId(R.string.navigation_bottom)
            .assertExists()
    }

    @Test
    fun expandedDevice_verifyNavigationDrawer() {
        composeTestRule.setContent {
            ReplyTheme {
                ReplyApp(windowSize = WindowWidthSizeClass.Expanded)
            }
        }
        // El drawer permanente debe estar visible en Expanded
        composeTestRule.onNodeWithTagForStringId(R.string.navigation_drawer)
            .assertExists()
    }
}
```

---

## Resumen de la Unidad 4

| Tema | Lo esencial |
|------|------------|
| Ciclo de vida | `onCreate → onStart → onResume → onPause → onStop → onDestroy` |
| Problema de la rotación | La Activity se destruye y recrea, las variables se pierden |
| ViewModel | Sobrevive a cambios de configuración. Guarda el UiState |
| MutableStateFlow | Mutable y privado (solo ViewModel lo modifica) |
| StateFlow | Inmutable y público (la UI lo lee con `collectAsState()`) |
| UDF | Estado baja ↓, eventos suben ↑ |
| NavController | Gestiona el back stack y la navegación |
| NavHost | Define las pantallas disponibles y sus rutas |
| WindowSizeClass | Compact / Medium / Expanded según el ancho de pantalla |
| NavigationBar | Para Compact (barra inferior) |
| NavigationRail | Para Medium (barra lateral compacta) |
| NavigationDrawer | Para Expanded (panel lateral permanente) |

---

**→ Continúa con [[../U5-Internet/00 - Unidad 5 Resumen|Unidad 5 — Conectarse a Internet]]**
