# Unidad 3 — Listas y Material Design

**← [[../U2-IU-App/00 - Unidad 2 Resumen|Unidad 2]]** | **→ [[../U4-Navegacion-Arquitectura/00 - Unidad 4 Resumen|Unidad 4]]**

Duración: ~15 horas | 3 rutas de aprendizaje

---

## Ruta 1 — Kotlin: Colecciones y funciones de orden superior

### Colecciones en Kotlin

Las **colecciones** son objetos que agrupan varios valores. En Kotlin hay tres tipos principales:

#### `List` — lista ordenada

```kotlin
// Lista inmutable
val frutas = listOf("manzana", "pera", "uva")
println(frutas[0])        // manzana (acceso por índice)
println(frutas.size)      // 3

// Lista mutable
val numeros = mutableListOf(1, 2, 3)
numeros.add(4)            // [1, 2, 3, 4]
numeros.removeAt(0)       // [2, 3, 4]
numeros[0] = 99           // [99, 3, 4]
```

#### `Set` — colección sin duplicados

```kotlin
val conjunto = setOf(1, 2, 3, 2, 1)   // {1, 2, 3}  — los duplicados se eliminan
println(conjunto.contains(2))          // true

val mutableSet = mutableSetOf("a", "b")
mutableSet.add("c")
mutableSet.add("a")  // no se añade, ya existe
```

#### `Map` — pares clave-valor

```kotlin
val capitales = mapOf(
    "España" to "Madrid",
    "Francia" to "París",
    "Italia" to "Roma"
)

println(capitales["España"])   // Madrid
println(capitales.keys)        // [España, Francia, Italia]
println(capitales.values)      // [Madrid, París, Roma]

val mutableMap = mutableMapOf("uno" to 1, "dos" to 2)
mutableMap["tres"] = 3
```

---

### Funciones de orden superior con colecciones

Estas son las funciones más usadas en la práctica. Todas reciben una lambda y trabajan sobre la colección.

#### `forEach` — iterar sobre cada elemento

```kotlin
val numeros = listOf(1, 2, 3, 4, 5)
numeros.forEach { println(it) }
// Imprime 1, 2, 3, 4, 5 (cada uno en su línea)
```

#### `map` — transformar cada elemento

Devuelve una nueva lista donde cada elemento ha sido transformado por la lambda.

```kotlin
val dobles = numeros.map { it * 2 }       // [2, 4, 6, 8, 10]
val cadenas = numeros.map { "Número $it" } // ["Número 1", "Número 2", ...]
```

#### `filter` — filtrar elementos

Devuelve una nueva lista con solo los elementos para los que la lambda devuelve `true`.

```kotlin
val pares = numeros.filter { it % 2 == 0 }  // [2, 4]
val mayores3 = numeros.filter { it > 3 }     // [4, 5]
```

#### `groupBy` — agrupar por criterio

Devuelve un `Map` donde las claves son los distintos valores que devuelve la lambda, y los valores son listas de elementos que producen esa clave.

```kotlin
data class Cookie(val nombre: String, val softBaked: Boolean, val precio: Double)

val galletas = listOf(
    Cookie("Choco Chip", softBaked = false, precio = 1.69),
    Cookie("Banana Walnut", softBaked = true, precio = 1.49),
    Cookie("Snickerdoodle", softBaked = true, precio = 1.49)
)

val porTipo = galletas.groupBy { it.softBaked }
// {false: [Choco Chip], true: [Banana Walnut, Snickerdoodle]}
```

#### `fold` — acumular un resultado

Reduce toda la colección a un único valor. Necesita un valor inicial y una lambda que combina el acumulador con cada elemento.

```kotlin
val suma = numeros.fold(0) { acumulador, elemento -> acumulador + elemento }
// 0 + 1 + 2 + 3 + 4 + 5 = 15

val precioTotal = galletas.fold(0.0) { total, galleta -> total + galleta.precio }
```

> Nota: `fold` se llama `reduce` en otros lenguajes (JS, Python...). En Kotlin `reduce()` también existe, pero el acumulador empieza con el primer elemento en vez de un valor inicial que tú eliges.

#### `sortedBy` — ordenar por un campo

```kotlin
val ordenadas = galletas.sortedBy { it.precio }       // de menor a mayor
val ordenNombre = galletas.sortedBy { it.nombre }      // alfabético
```

#### Encadenar funciones

Puedes combinarlas en cadena, el resultado de una pasa a la siguiente:

```kotlin
val resultado = galletas
    .filter { !it.softBaked }       // solo las crujientes
    .sortedBy { it.precio }          // ordenadas por precio
    .map { "${it.nombre}: ${it.precio}€" }  // convertidas a texto
```

---

## Ruta 2 — Listas desplazables con `LazyColumn`

### ¿Por qué `LazyColumn` y no `Column`?

Si tienes una lista con muchos elementos y usas `Column`, Compose intenta dibujar **todos** los elementos a la vez, aunque solo unos pocos sean visibles en pantalla. Esto es ineficiente y puede hacer la app lenta.

`LazyColumn` (y `LazyRow` para listas horizontales) solo renderiza los elementos que son visibles en la pantalla en ese momento. Cuando el usuario hace scroll, descarga los elementos que salen del área visible y renderiza los que entran. Es exactamente como `RecyclerView` en el sistema antiguo de Views.

### Sintaxis de `LazyColumn`

```kotlin
@Composable
fun ListaAffirmations(affirmations: List<Affirmation>) {
    LazyColumn {
        items(affirmations) { affirmation ->
            TarjetaAffirmation(affirmation)
        }
    }
}
```

- `items(lista) { elemento -> ... }`: itera sobre la lista y llama a la lambda para cada elemento
- También puedes añadir elementos individuales con `item { ... }`

```kotlin
LazyColumn {
    item { Text("Cabecera") }           // elemento único
    items(miLista) { elemento ->         // varios elementos de la lista
        TarjetaElemento(elemento)
    }
    item { Text("Pie de página") }
}
```

### Estructura de la app Affirmations

La app muestra una lista de afirmaciones, cada una con una imagen y un texto. La estructura de datos es:

```kotlin
// Modelo de datos
data class Affirmation(
    @StringRes val stringResourceId: Int,   // ID del texto en strings.xml
    @DrawableRes val imageResourceId: Int    // ID de la imagen en drawable/
)

// Fuente de datos
object DatosEjemplo {
    val affirmations = listOf(
        Affirmation(R.string.affirmation1, R.drawable.image1),
        Affirmation(R.string.affirmation2, R.drawable.image2),
        // ...
    )
}
```

Las anotaciones `@StringRes` y `@DrawableRes` son opcionales pero buena práctica: indican que ese `Int` es un ID de recurso de tipo texto o imagen respectivamente, lo que ayuda a Android Studio a detectar errores.

### Composable de cada tarjeta

```kotlin
@Composable
fun TarjetaAffirmation(affirmation: Affirmation, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column {
            Image(
                painter = painterResource(affirmation.imageResourceId),
                contentDescription = stringResource(affirmation.stringResourceId),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(194.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = stringResource(affirmation.stringResourceId),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
```

### `LazyVerticalGrid` — cuadrícula

Para mostrar elementos en cuadrícula:

```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(2),    // 2 columnas fijas
    modifier = Modifier.padding(4.dp)
) {
    items(miLista) { elemento ->
        TarjetaElemento(elemento)
    }
}
```

O con columnas adaptables según el ancho:
```kotlin
columns = GridCells.Adaptive(minSize = 150.dp)  // tantas columnas como quepan de al menos 150dp
```

---

## Ruta 3 — Material Design con Compose

Material Design es el sistema de diseño de Google. Define colores, tipografía, formas, componentes y patrones de interacción para que las apps tengan un aspecto coherente y profesional.

La app con la que se practica en esta ruta es **Woof**, una app de lista de perros.

### El sistema de temas en Compose (Material 3)

Cuando creas un proyecto con la plantilla Empty Activity, Android Studio genera automáticamente los archivos del tema:

```
ui/theme/
├── Color.kt      ← define los colores
├── Theme.kt      ← aplica el esquema de colores
└── Type.kt       ← define la tipografía
```

El tema se aplica envolviendo toda la app en `MaterialTheme`:

```kotlin
@Composable
fun WoofTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

### Definir colores personalizados

En `Color.kt` defines tus colores:

```kotlin
val Verde80 = Color(0xFF1B5E20)
val VerdeClaroContainer80 = Color(0xFF4CAF50)
// ...
```

En `Theme.kt` los asignas a los roles del esquema de colores:

```kotlin
private val LightColorScheme = lightColorScheme(
    primary = Verde80,
    onPrimary = Color.White,
    primaryContainer = VerdeClaroContainer80,
    // ...
)
```

Los **roles de color** de Material 3 son:
- `primary` — color principal de la app (botones, FAB...)
- `onPrimary` — color del contenido encima del primary (texto en botón)
- `primaryContainer` — versión más suave del primary (para contenedores)
- `secondary`, `tertiary` — colores complementarios
- `background` — fondo general
- `surface` — fondo de tarjetas y hojas

Para acceder a los colores del tema en un composable:
```kotlin
Text(
    text = "Hola",
    color = MaterialTheme.colorScheme.primary
)
```

### Tipografía personalizada

Puedes añadir fuentes propias a la app (descargándolas de fonts.google.com o incluyendo los archivos `.ttf` en `res/font/`):

```kotlin
// En Type.kt
val MiFuente = FontFamily(
    Font(R.font.nunito_regular),
    Font(R.font.nunito_bold, FontWeight.Bold)
)

val Typography = Typography(
    bodyLarge = TextStyle(fontFamily = MiFuente, fontSize = 16.sp),
    headlineSmall = TextStyle(fontFamily = MiFuente, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    // ...
)
```

Para usar los estilos de tipografía:
```kotlin
Text(text = "Título", style = MaterialTheme.typography.headlineSmall)
Text(text = "Cuerpo", style = MaterialTheme.typography.bodyMedium)
```

### Formas (Shapes)

Material 3 permite personalizar la forma de los componentes (redondeado de esquinas):

```kotlin
// En Shape.kt
val Shapes = Shapes(
    small = RoundedCornerShape(50.dp),   // completamente redondo
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(0.dp)     // esquinas rectas
)
```

### `Card` — componente tarjeta

```kotlin
Card(
    modifier = Modifier.padding(8.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)  // sombra
) {
    // contenido de la tarjeta
}
```

### `TopAppBar` — barra superior

```kotlin
@Composable
fun WoofTopAppBar(modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.ic_woof_logo),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).padding(8.dp)
                )
                Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.displayLarge)
            }
        },
        modifier = modifier
    )
}
```

Para incluirla en la pantalla, usa `Scaffold`:

```kotlin
Scaffold(
    topBar = { WoofTopAppBar() }
) { innerPadding ->
    LazyColumn(contentPadding = innerPadding) {
        items(dogs) { dog -> DogItem(dog) }
    }
}
```

`Scaffold` es un composable de Material que proporciona la estructura básica de una pantalla: barra superior, barra inferior, botón flotante, y el área de contenido central.

### Animaciones sencillas

Puedes animar cambios con `animateContentSize()` y `animateDpAsState()`:

```kotlin
@Composable
fun TarjetaPerro(perro: Perro) {
    var expandido by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .animateContentSize(   // anima el cambio de tamaño automáticamente
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
    ) {
        Column {
            Row(modifier = Modifier.padding(8.dp)) {
                Image(painter = painterResource(perro.imagenId), contentDescription = null)
                Text(text = stringResource(perro.nombreId))
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { expandido = !expandido }) {
                    Icon(
                        imageVector = if (expandido) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null
                    )
                }
            }
            if (expandido) {
                Text(
                    text = stringResource(perro.infoId),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
```

El `animateContentSize()` hace que la tarjeta se expanda/contraiga suavemente en lugar de cambiar de golpe.

### Tema oscuro

Compose gestiona el tema oscuro automáticamente. Solo tienes que definir los colores del tema oscuro en `Theme.kt`:

```kotlin
private val DarkColorScheme = darkColorScheme(
    primary = Verde80,
    onPrimary = Color.Black,
    // ...
)
```

Y en el `WoofTheme` se selecciona automáticamente según la configuración del sistema:
```kotlin
val colorScheme = if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
```

### Accesibilidad básica

- Añade siempre `contentDescription` a las imágenes que aportan información
- Usa `MaterialTheme.colorScheme` en vez de colores directos para que el contraste sea correcto en tema oscuro
- Prueba la app con **TalkBack** (el lector de pantalla de Android) para verificar que es usable sin vista

---

**→ Continúa con [[../U4-Navegacion-Arquitectura/00 - Unidad 4 Resumen|Unidad 4 — Navegación y Arquitectura]]**
