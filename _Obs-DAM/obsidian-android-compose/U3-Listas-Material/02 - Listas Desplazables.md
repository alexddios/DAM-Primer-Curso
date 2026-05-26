# Ruta 2 — Listas desplazables con LazyColumn

**← [[01 - Kotlin Colecciones y Funciones|Ruta 1]]** | **→ [[03 - Material Design|Ruta 3]]**

---

## ¿Por qué no usar `Column` para listas largas?

Si tienes una lista con muchos elementos y usas `Column`, Compose intenta renderizar **todos** los elementos a la vez aunque solo sean visibles unos pocos en pantalla. Con 10 elementos no se nota, pero con 100 o 1000 la app se vuelve lenta o se queda sin memoria.

`LazyColumn` (y `LazyRow` para listas horizontales) solo renderiza los elementos **visibles** en ese momento. Cuando el usuario hace scroll, descarga los que salen del área visible y renderiza los que entran. Es el equivalente al `RecyclerView` del sistema antiguo de Views, pero mucho más sencillo de usar.

```
Column:     [●][●][●][●][●][●][●][●][●][●]   ← todos en memoria
LazyColumn: [●][●][●][●][●]..............     ← solo los visibles
```

---

## La app Affirmations

Es la app con la que se practica en esta ruta. Muestra una lista de afirmaciones positivas, cada una con una imagen y un texto.

### Estructura de datos

```kotlin
// Modelo: representa una afirmación
data class Affirmation(
    @StringRes val stringResourceId: Int,   // ID del texto en strings.xml
    @DrawableRes val imageResourceId: Int    // ID de la imagen en res/drawable/
)
```

Las anotaciones `@StringRes` y `@DrawableRes` son opcionales pero recomendadas: indican que ese `Int` es un recurso de texto o imagen. Android Studio puede detectar si pasas el tipo de recurso incorrecto.

```kotlin
// Fuente de datos estática
object DatosEjemplo {
    val affirmations = listOf(
        Affirmation(R.string.affirmation1, R.drawable.image1),
        Affirmation(R.string.affirmation2, R.drawable.image2),
        Affirmation(R.string.affirmation3, R.drawable.image3),
        // ...
    )
}
```

En esta app los datos son estáticos (un objeto que siempre devuelve la misma lista). En apps reales vendrían de una base de datos o de Internet.

---

## `LazyColumn` — lista vertical

```kotlin
@Composable
fun AffirmationList(
    affirmationList: List<Affirmation>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(affirmationList) { affirmation ->
            AffirmationCard(
                affirmation = affirmation,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
```

El bloque `items(lista) { elemento -> ... }` itera sobre la lista y llama a la lambda para cada elemento visible.

Puedes mezclar elementos individuales con listas:
```kotlin
LazyColumn {
    item {
        Text("Cabecera", style = MaterialTheme.typography.headlineMedium)
    }
    items(miLista) { elemento ->
        TarjetaElemento(elemento)
    }
    item {
        Text("Fin de la lista")
    }
}
```

### `LazyRow` — lista horizontal

Exactamente igual pero en horizontal:
```kotlin
LazyRow {
    items(miLista) { elemento ->
        TarjetaElemento(elemento)
    }
}
```

---

## Composable de la tarjeta

Cada elemento de la lista es una `Card` (tarjeta) con imagen y texto:

```kotlin
@Composable
fun AffirmationCard(
    affirmation: Affirmation,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column {
            Image(
                painter = painterResource(affirmation.imageResourceId),
                contentDescription = stringResource(affirmation.stringResourceId),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(194.dp),
                contentScale = ContentScale.Crop   // recorta para rellenar sin deformar
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

**`Card`**: es un composable de Material Design que dibuja una superficie elevada con esquinas redondeadas y sombra. Es el patrón más común para mostrar un elemento de lista.

---

## `LazyVerticalGrid` — cuadrícula

Para mostrar los elementos en cuadrícula en lugar de lista:

```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(2),          // exactamente 2 columnas
    modifier = Modifier.padding(4.dp)
) {
    items(miLista) { elemento ->
        TarjetaElemento(elemento, modifier = Modifier.padding(4.dp))
    }
}
```

Con columnas adaptables (tantas como quepan según el ancho mínimo):
```kotlin
columns = GridCells.Adaptive(minSize = 150.dp)
```

Esto es útil para diseños adaptables: en un móvil puede salir 2 columnas, en una tablet 4.

---

## El icono de la app

Cada app Android tiene un icono que aparece en el lanzador. Puedes personalizarlo desde Android Studio:

1. En el panel del proyecto, clic derecho sobre `res/` → *New → Image Asset*
2. Sube tu imagen (PNG o SVG)
3. Android Studio genera automáticamente todas las versiones necesarias para cada densidad de pantalla (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi) y las guarda en las carpetas `mipmap-*`

Los iconos adaptativos (desde Android 8.0) se componen de dos capas: una de fondo (`ic_launcher_background`) y una de primer plano (`ic_launcher_foreground`). Esto permite que el sistema aplique distintas formas según el dispositivo (circular, cuadrado con esquinas redondeadas, etc.).

---

## Buenas prácticas con listas

**Siempre proporciona el parámetro `key`** cuando los elementos de la lista pueden reordenarse o cambiar:

```kotlin
LazyColumn {
    items(
        items = miLista,
        key = { it.id }   // identificador único de cada elemento
    ) { elemento ->
        TarjetaElemento(elemento)
    }
}
```

Sin `key`, si insertas un elemento al principio de la lista, Compose tiene que redibujar toda la lista. Con `key`, solo redibuja el elemento nuevo y desplaza los demás.

**Usa `contentPadding`** para añadir espacio al principio y al final de la lista sin que afecte al scroll:

```kotlin
LazyColumn(
    contentPadding = PaddingValues(vertical = 8.dp)
) {
    items(miLista) { ... }
}
```

---

**→ Continúa con [[03 - Material Design|Ruta 3 — Material Design con Compose]]**
