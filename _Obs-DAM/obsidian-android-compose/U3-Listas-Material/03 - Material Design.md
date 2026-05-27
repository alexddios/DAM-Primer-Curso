# Ruta 3 — Material Design con Compose

**← [[02 - Listas Desplazables|Ruta 2]]** | **→ [[U4-Navegacion-Arquitectura|Unidad 4]]**

---

## ¿Qué es Material Design?

Material Design es el **sistema de diseño** de Google. Define un conjunto de reglas, componentes y guías que hacen que las apps tengan un aspecto consistente, accesible y profesional. La versión actual es **Material 3** (M3).

Material Design se basa en tres pilares:
- **Color**: paleta de colores coherente con roles definidos
- **Tipografía**: jerarquía tipográfica clara
- **Formas**: esquinas y bordes que dan personalidad visual

La app con la que se practica en esta ruta es **Woof**, una app de lista de perros.

---

## La estructura de archivos del tema

Cuando creas un proyecto con la plantilla *Empty Activity*, Android Studio genera automáticamente:

```
ui/theme/
├── Color.kt     ← todos los colores de la paleta
├── Theme.kt     ← aplica los colores, tipografía y formas al MaterialTheme
└── Type.kt      ← define los estilos de texto
```

Y en `MainActivity.kt` la app entera se envuelve en el tema:

```kotlin
setContent {
    WoofTheme {      // ← aplica el tema a toda la app
        Surface {
            WoofApp()
        }
    }
}
```

---

## Sistema de colores en Material 3

### Roles de color

Material 3 no trabaja con colores individuales sino con **roles**: cada color tiene una función específica en la UI. Los más importantes:

| Rol | Para qué se usa |
|-----|----------------|
| `primary` | Botones principales, FAB, elementos de acción clave |
| `onPrimary` | Texto/iconos encima del color `primary` |
| `primaryContainer` | Fondos de chips, tarjetas destacadas |
| `onPrimaryContainer` | Texto encima de `primaryContainer` |
| `secondary` | Elementos secundarios, chips |
| `background` | Fondo general de la pantalla |
| `surface` | Fondo de tarjetas, sheets, dialogs |
| `onSurface` | Texto/iconos sobre `surface` |
| `error` | Mensajes de error, campos inválidos |

### Definir la paleta en `Color.kt`

```kotlin
// Color.kt
val Verde80 = Color(0xFF1B5E20)
val VerdeClaro80 = Color(0xFF388E3C)
val VerdeContainer80 = Color(0xFFA5D6A7)
val OnVerdeContainer80 = Color(0xFF1B5E20)

val Naranja40 = Color(0xFFE65100)
val NaranjaContainer40 = Color(0xFFFFCCBC)
```

Los colores se definen como `Color(0xFFRRGGBB)`: `FF` es la opacidad (FF = completamente opaco), seguido del código hexadecimal RGB.

### Asignar roles en `Theme.kt`

```kotlin
private val LightColorScheme = lightColorScheme(
    primary = Verde80,
    onPrimary = Color.White,
    primaryContainer = VerdeContainer80,
    onPrimaryContainer = OnVerdeContainer80,
    secondary = Naranja40,
    // ...
)

private val DarkColorScheme = darkColorScheme(
    primary = VerdeClaro80,
    onPrimary = Color.Black,
    // ...
)

@Composable
fun WoofTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

`isSystemInDarkTheme()` devuelve `true` si el usuario tiene el modo oscuro activado en el sistema. El tema oscuro se aplica automáticamente.

### Usar los colores del tema en los composables

Accede siempre a los colores a través de `MaterialTheme.colorScheme`, nunca poniendo colores directos (*hardcoded*). Así el tema oscuro funciona automáticamente:

```kotlin
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    )
) { ... }

Text(
    text = "Hola",
    color = MaterialTheme.colorScheme.onSurface
)
```

---

## Material Theme Builder

Para crear paletas de colores personalizadas visualmente, Google ofrece la herramienta **Material Theme Builder**: https://m3.material.io/theme-builder

Introduces tu color principal y genera automáticamente toda la paleta de roles (primario, secundario, terciario, error, fondo, superficie...) tanto para tema claro como oscuro, con los valores hexadecimales listos para copiar en `Color.kt`.

---

## Tipografía en Material 3

### Escala tipográfica

Material 3 define una escala con 15 estilos de texto organizados en 5 familias:

| Familia | Estilos | Cuándo usar |
|---------|---------|-------------|
| `display` | Large, Medium, Small | Textos muy grandes, hero sections |
| `headline` | Large, Medium, Small | Títulos de pantalla, secciones |
| `title` | Large, Medium, Small | Títulos de componentes, app bar |
| `body` | Large, Medium, Small | Texto de contenido, párrafos |
| `label` | Large, Medium, Small | Etiquetas, texto en botones, chips |

### Usar fuentes personalizadas

1. Descarga los archivos de fuente (`.ttf` o `.otf`) de https://fonts.google.com
2. Crea la carpeta `res/font/` y copia los archivos allí
3. Define la `FontFamily` en `Type.kt`:

```kotlin
// Type.kt
val Nunito = FontFamily(
    Font(R.font.nunito_regular),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold)
)

val Typography = Typography(
    displayLarge  = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 57.sp),
    displayMedium = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 45.sp),
    headlineLarge = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 32.sp),
    headlineSmall = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold,   fontSize = 24.sp),
    bodyLarge     = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    labelSmall    = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold,   fontSize = 11.sp),
)
```

Usar los estilos en composables:
```kotlin
Text(
    text = nombre,
    style = MaterialTheme.typography.headlineSmall
)
Text(
    text = descripcion,
    style = MaterialTheme.typography.bodyMedium
)
```

---

## Formas en Material 3

Las formas controlan el redondeado de las esquinas de los componentes. Se definen en un archivo `Shape.kt`:

```kotlin
val Shapes = Shapes(
    small  = RoundedCornerShape(50.dp),   // completamente redondo (para chips, badges)
    medium = RoundedCornerShape(16.dp),   // redondeado moderado (tarjetas)
    large  = RoundedCornerShape(0.dp)     // esquinas rectas
)
```

Y se pasan al `MaterialTheme`:
```kotlin
MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = Shapes,      // ← aquí
    content = content
)
```

Los componentes de Material Design usan automáticamente la forma correspondiente a su tamaño. Una `Card` usa `medium`, un `Button` pequeño usa `small`, etc. También puedes sobrescribir manualmente:

```kotlin
Card(
    shape = RoundedCornerShape(24.dp)  // sobrescribe la forma por defecto
) { ... }
```

---

## `Scaffold` y `TopAppBar`

`Scaffold` es el composable que proporciona la **estructura básica de una pantalla** según Material Design:

```kotlin
Scaffold(
    topBar = { WoofTopAppBar() }
) { innerPadding ->
    // El innerPadding asegura que el contenido no quede tapado por la TopAppBar
    LazyColumn(contentPadding = innerPadding) {
        items(dogs) { dog -> DogItem(dog) }
    }
}
```

La `TopAppBar`:
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
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displayLarge
                )
            }
        },
        modifier = modifier
    )
}
```

Tipos de TopAppBar disponibles:
- `TopAppBar`: título alineado a la izquierda
- `CenterAlignedTopAppBar`: título centrado
- `MediumTopAppBar` / `LargeTopAppBar`: versiones más grandes con más espacio

---

## Animaciones sencillas

### `animateContentSize` — animar cambio de tamaño

Cuando un composable cambia de tamaño (por ejemplo al expandirse), en vez de cambiar de golpe puedes animarlo suavemente:

```kotlin
@Composable
fun DogItem(dog: Dog) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.padding(8.dp)) {
        Column(
            modifier = Modifier.animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,  // sin rebote
                    stiffness    = Spring.StiffnessMedium         // velocidad media
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                DogIcon(dog.imageResourceId)
                DogInformation(dog.name, dog.age)
                Spacer(Modifier.weight(1f))
                DogItemButton(
                    expanded = expanded,
                    onClick  = { expanded = !expanded }
                )
            }
            if (expanded) {
                DogHobby(dog.hobbies)
            }
        }
    }
}
```

### El botón de expandir/contraer

```kotlin
@Composable
fun DogItemButton(expanded: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = stringResource(
                if (expanded) R.string.collapse_button else R.string.expand_button
            ),
            tint = MaterialTheme.colorScheme.secondary
        )
    }
}
```

`Icons.Filled.ExpandLess` y `Icons.Filled.ExpandMore` son iconos incluidos en Material Design. Para usarlos:
```kotlin
implementation("androidx.compose.material:material-icons-extended")
```

---

## Tema oscuro y accesibilidad

### Probar el tema oscuro en las previsualizaciones

```kotlin
@Preview(name = "Tema claro")
@Preview(name = "Tema oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun WoofPreview() {
    WoofTheme { WoofApp() }
}
```

### Accesibilidad básica

- **`contentDescription`**: siempre en imágenes que aportan información. Ponlo a `null` en las puramente decorativas.
- **Colores con buen contraste**: usa `MaterialTheme.colorScheme` en vez de colores fijos, el sistema de Material garantiza contraste mínimo.
- **Tamaño mínimo de área táctil**: Material recomienda mínimo 48.dp × 48.dp para cualquier elemento interactivo.
- **Prueba con TalkBack**: actívalo en *Ajustes del dispositivo → Accesibilidad → TalkBack* y navega tu app solo con gestos y audio.

---

**→ Continúa con [[U4-Navegacion-Arquitectura|Unidad 4 — Navegación y Arquitectura]]**
