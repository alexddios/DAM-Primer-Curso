# Ruta 3 — Primer diseño con Compose

**← [[02 - Configurar Android Studio|Ruta 2]]** | **→ [[U2-IU-App|Unidad 2]]**

---

## ¿Qué es Jetpack Compose?

Jetpack Compose es el **kit de herramientas moderno de Google para construir interfaces de usuario en Android**. Es el sistema que se usa en este curso en lugar del antiguo sistema basado en XML.

### El enfoque antiguo (XML) vs Compose

**Antes (XML + código):**
- Diseñabas la pantalla en un archivo XML
- Luego escribías código Kotlin para conectar los elementos del XML con la lógica
- Eran dos archivos separados que había que mantener sincronizados

**Ahora (Compose):**
- La interfaz se describe directamente en código Kotlin
- No hay XML
- Es **declarativo**: dices *qué* quieres mostrar, no *cómo* construirlo paso a paso

Analogía: es como la diferencia entre dar instrucciones exactas ("coge el tornillo, ponlo en el agujero, gira 3 veces...") y simplemente decir qué quieres ("una estantería de 3 niveles").

---

## Composables: el bloque fundamental de Compose

Un **composable** es una función que describe una parte de la interfaz de usuario. Se identifica con la anotación `@Composable` antes del nombre de la función.

```kotlin
@Composable
fun Saludo(nombre: String) {
    Text(text = "¡Hola, $nombre!")
}
```

Características de los composables:
- **Describen la UI**, no son instrucciones de qué hacer paso a paso
- **No devuelven nada** (tipo de retorno `Unit`)
- Toman datos de entrada (parámetros) y los convierten en elementos visuales
- Se pueden **anidar** unos dentro de otros para construir interfaces más complejas

### La anotación `@Composable`

Las anotaciones en Kotlin (empiezan por `@`) añaden información extra al código. `@Composable` le dice al compilador de Compose que esa función va a construir parte de la UI. Sin esta anotación, no puedes usar funciones de Compose dentro de ella.

```kotlin
@Composable          // ← esta anotación es obligatoria
fun MiTexto() {
    Text("Hola")     // Text es un composable de la librería de Compose
}
```

---

## El composable `Text`

`Text` es el composable más básico: muestra texto en pantalla.

```kotlin
@Composable
fun Greeting() {
    Text(text = "¡Happy Birthday!")
}
```

Parámetros útiles de `Text`:
```kotlin
Text(
    text = "Hola",
    fontSize = 24.sp,      // tamaño de fuente (sp = scaled pixels)
    color = Color.Blue,    // color del texto
    fontWeight = FontWeight.Bold  // negrita
)
```

---

## Layouts: Column, Row y Box

Para organizar varios elementos en pantalla necesitas **layouts** (contenedores). Los tres más básicos de Compose son:

### `Column` — elementos apilados verticalmente

```kotlin
@Composable
fun TarjetaCumpleanos() {
    Column {
        Text(text = "Happy Birthday, Sam!")
        Text(text = "From, Alex")
    }
}
```

Resultado visual:
```
Happy Birthday, Sam!
From, Alex
```

### `Row` — elementos alineados horizontalmente

```kotlin
@Composable
fun FilaDatos() {
    Row {
        Text(text = "Temperatura:")
        Text(text = "22°C")
    }
}
```

Resultado visual: `Temperatura:  22°C`

### `Box` — elementos superpuestos

`Box` coloca los elementos unos encima de otros. Útil para colocar texto sobre una imagen, por ejemplo.

```kotlin
@Composable
fun ImagenConTexto() {
    Box {
        Image(...)  // va detrás
        Text("Título encima de la imagen")  // va delante
    }
}
```

---

## El `Modifier`

El `Modifier` es un objeto que permite **personalizar cómo se muestra un composable**: su tamaño, márgenes, relleno, alineación, etc. Se pasa como parámetro a casi cualquier composable.

```kotlin
@Composable
fun MiTexto() {
    Text(
        text = "Hola",
        modifier = Modifier
            .padding(16.dp)         // espacio interno (dp = density-independent pixels)
            .fillMaxWidth()          // ocupa todo el ancho disponible
    )
}
```

Los modificadores se encadenan con `.`: cada uno añade algo al anterior.

Modificadores más comunes:

| Modificador | Efecto |
|-------------|--------|
| `.padding(16.dp)` | Espacio alrededor del contenido (por dentro) |
| `.fillMaxWidth()` | Ocupa todo el ancho disponible |
| `.fillMaxSize()` | Ocupa todo el espacio disponible |
| `.size(100.dp)` | Tamaño fijo |
| `.background(Color.Blue)` | Color de fondo |
| `.align(Alignment.Center)` | Alineación dentro del padre |

### `dp` y `sp`: unidades de medida

- **`dp`** (density-independent pixels): unidad para tamaños, márgenes, padding. Se escala automáticamente según la densidad de pantalla del dispositivo, así que `16.dp` se ve igual de grande en todos los teléfonos
- **`sp`** (scale-independent pixels): igual que `dp` pero también respeta el tamaño de fuente que el usuario ha configurado en el sistema. Se usa para texto

---

## Mostrar imágenes con `Image`

Para añadir una imagen a la app:

1. Guarda la imagen en `res/drawable/` (arrastra el archivo a esa carpeta en Android Studio)
2. Úsala en un composable con `painterResource`:

```kotlin
@Composable
fun TarjetaCumpleanos() {
    Box {
        Image(
            painter = painterResource(id = R.drawable.androidparty),
            contentDescription = null,   // null si es decorativa; pon texto descriptivo si es informativa
            contentScale = ContentScale.Crop,  // cómo escalar la imagen
            modifier = Modifier.fillMaxSize()
        )
        Column {
            Text(text = "Happy Birthday, Sam!")
            Text(text = "From, Alex")
        }
    }
}
```

**`R.drawable.nombre`**: así se referencian los recursos en Android. `R` es una clase generada automáticamente que contiene los IDs de todos los recursos de la app.

**`contentDescription`**: descripción de la imagen para la accesibilidad (lectores de pantalla). Si la imagen es solo decorativa y no aporta información, ponla como `null`.

**`ContentScale`**: controla cómo se escala la imagen si no cabe exactamente:
- `ContentScale.Crop`: recorta para llenar el espacio sin deformar
- `ContentScale.Fit`: encaja completa sin recortar (pueden quedar franjas)
- `ContentScale.FillBounds`: estira para llenar exactamente (puede deformar)

---

## Alineación dentro de Column y Row

Puedes controlar cómo se alinean los elementos dentro de un `Column` o `Row`:

```kotlin
Column(
    horizontalAlignment = Alignment.CenterHorizontally,  // centra horizontalmente
    verticalArrangement = Arrangement.Center              // centra verticalmente
) {
    Text("Línea 1")
    Text("Línea 2")
}
```

Para `Row` es al revés:
```kotlin
Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween  // espacio entre elementos
) {
    Text("Izquierda")
    Text("Derecha")
}
```

---

## Vista previa con `@Preview`

Compose tiene una función muy útil: puedes ver cómo quedará la interfaz **sin tener que ejecutar la app en el emulador**. Para eso añades `@Preview`:

```kotlin
@Preview(showBackground = true)
@Composable
fun TarjetaCumpleanosPreview() {
    MiAppTheme {
        TarjetaCumpleanos()
    }
}
```

`showBackground = true` muestra un fondo blanco para que puedas ver mejor el diseño. En Android Studio, en la pestaña **Split** o **Design** del editor, aparece la vista previa en tiempo real.

**Importante**: la función de `@Preview` es solo para desarrollo. No forma parte de la app final.

---

## Proyecto final de la Unidad 1: App de tarjeta de presentación

El proyecto con el que termina la unidad consiste en crear una app que muestre una tarjeta de presentación personal con:
- Tu nombre (texto grande)
- Tu título o profesión (texto pequeño)
- Un icono o logo
- Tus datos de contacto (nombre de usuario de redes, teléfono, correo)

Para conseguirlo usarás todo lo aprendido: `Column`, `Row`, `Text`, `Image` y `Modifier`.

Esquema de la estructura:

```kotlin
@Composable
fun TarjetaPresentacion() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF003A6B))  // color de fondo personalizado
            .padding(8.dp)
    ) {
        // Sección superior: logo + nombre + título
        Image(...)
        Text(text = "Full Name", fontSize = 36.sp)
        Text(text = "Android Developer", color = Color(0xFF3DDC84))
        
        // Sección inferior: contacto
        Column(modifier = Modifier.padding(top = 48.dp)) {
            Row { Icon(...); Text("+34 600 000 000") }
            Row { Icon(...); Text("@username") }
            Row { Icon(...); Text("email@example.com") }
        }
    }
}
```

---

## Resumen de lo aprendido en la Unidad 1

- **Kotlin**: lenguaje de programación de Android. Programas básicos, funciones, variables, tipos de datos
- **Android Studio**: IDE oficial. Crear proyectos, emulador, estructura de archivos
- **Jetpack Compose**: sistema declarativo para construir UIs
- **Composables**: funciones anotadas con `@Composable` que describen partes de la UI
- **Layouts básicos**: `Column`, `Row`, `Box`
- **Modifier**: para personalizar el aspecto y comportamiento de los composables
- **Unidades**: `dp` para tamaños, `sp` para texto
- **Imágenes**: `Image` + `painterResource`
- **Vista previa**: `@Preview`

---

**→ Continúa con [[../U2-IU-App/00 - Unidad 2 Resumen|Unidad 2 — Compila la IU de una app]]**
