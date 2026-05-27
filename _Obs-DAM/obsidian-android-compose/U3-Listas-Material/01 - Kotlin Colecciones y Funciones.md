# Ruta 1 — Kotlin: Colecciones y funciones de orden superior

**← [[U3-Listas-Material|Unidad 3]]** | **→ [[02 - Listas Desplazables|Ruta 2]]**

---

## Colecciones en Kotlin

Las **colecciones** son objetos que agrupan varios valores. En Kotlin hay tres tipos principales: `List`, `Set` y `Map`.

### `List` — lista ordenada

```kotlin
// Lista inmutable: no se puede modificar después de crearse
val frutas = listOf("manzana", "pera", "uva")
println(frutas[0])        // manzana  (acceso por índice, empieza en 0)
println(frutas.size)      // 3
println(frutas.first())   // manzana
println(frutas.last())    // uva

// Lista mutable: sí se puede modificar
val numeros = mutableListOf(1, 2, 3)
numeros.add(4)            // [1, 2, 3, 4]
numeros.removeAt(0)       // [2, 3, 4]
numeros[0] = 99           // [99, 3, 4]
numeros.remove(3)         // elimina la primera ocurrencia del valor 3 → [99, 4]
```

> Regla general: usa la versión inmutable (`listOf`) siempre que puedas. Solo usa `mutableListOf` si necesitas modificar la lista después de crearla.

### `Set` — colección sin duplicados

Un `Set` no permite elementos repetidos. Si intentas añadir uno que ya existe, simplemente no se añade.

```kotlin
val conjunto = setOf(1, 2, 3, 2, 1)   // {1, 2, 3} — los duplicados se ignoran
println(conjunto.contains(2))          // true
println(2 in conjunto)                 // true (sintaxis alternativa)

val mutableSet = mutableSetOf("a", "b")
mutableSet.add("c")
mutableSet.add("a")  // no se añade, ya existe — no da error, simplemente no hace nada
println(mutableSet)  // [a, b, c]
```

Cuándo usar `Set` en vez de `List`: cuando los duplicados no tienen sentido, por ejemplo una colección de IDs únicos, países visitados, o etiquetas.

### `Map` — pares clave-valor

Un `Map` asocia una **clave** a un **valor**. Cada clave es única.

```kotlin
val capitales = mapOf(
    "España" to "Madrid",
    "Francia" to "París",
    "Italia" to "Roma"
)

println(capitales["España"])            // Madrid
println(capitales.getOrDefault("Japón", "Desconocida"))  // Desconocida
println(capitales.keys)                 // [España, Francia, Italia]
println(capitales.values)              // [Madrid, París, Roma]
println(capitales.containsKey("Francia"))  // true

// Iterar
for ((pais, capital) in capitales) {
    println("La capital de $pais es $capital")
}

val mutableMap = mutableMapOf("uno" to 1, "dos" to 2)
mutableMap["tres"] = 3
mutableMap.remove("uno")
```

---

## Funciones de orden superior con colecciones

Estas funciones reciben una lambda y aplican su lógica a cada elemento de la colección. Son la forma idiomática (natural) de trabajar con colecciones en Kotlin.

Usaremos esta lista de galletas como ejemplo:

```kotlin
data class Cookie(
    val nombre: String,
    val softBaked: Boolean,
    val inclusSprinkles: Boolean,
    val precio: Double
)

val galletas = listOf(
    Cookie("Choco Chip",    softBaked = false, inclusSprinkles = false, precio = 1.69),
    Cookie("Banana Walnut", softBaked = true,  inclusSprinkles = false, precio = 1.49),
    Cookie("Snickerdoodle", softBaked = true,  inclusSprinkles = false, precio = 1.49),
    Cookie("Sprinkles",     softBaked = false, inclusSprinkles = true,  precio = 1.99)
)
```

### `forEach` — iterar sobre cada elemento

Ejecuta la lambda para cada elemento. No devuelve nada.

```kotlin
galletas.forEach { println("${it.nombre}: ${it.precio}€") }
// Choco Chip: 1.69€
// Banana Walnut: 1.49€
// Snickerdoodle: 1.49€
// Sprinkles: 1.99€
```

### `map` — transformar cada elemento

Devuelve una **nueva lista** donde cada elemento ha sido transformado por la lambda. La lista original no cambia.

```kotlin
// Lista de nombres
val nombres = galletas.map { it.nombre }
// ["Choco Chip", "Banana Walnut", "Snickerdoodle", "Sprinkles"]

// Lista de precios con IVA
val preciosConIva = galletas.map { it.precio * 1.21 }
// [2.044, 1.803, 1.803, 2.409]

// Lista de strings descriptivos
val descripciones = galletas.map { "${it.nombre} - ${it.precio}€" }
```

### `filter` — filtrar elementos

Devuelve una **nueva lista** con solo los elementos para los que la lambda devuelve `true`.

```kotlin
val blandas = galletas.filter { it.softBaked }
// [Banana Walnut, Snickerdoodle]

val baratas = galletas.filter { it.precio < 1.60 }
// [Banana Walnut, Snickerdoodle]

val blandasyBaratas = galletas.filter { it.softBaked && it.precio < 1.60 }
// [Banana Walnut, Snickerdoodle]
```

### `groupBy` — agrupar por criterio

Devuelve un `Map` donde las claves son los distintos valores que produce la lambda, y los valores son listas de elementos que generan esa clave.

```kotlin
val porTipo = galletas.groupBy { it.softBaked }
// {
//   false: [Choco Chip, Sprinkles],
//   true:  [Banana Walnut, Snickerdoodle]
// }

println(porTipo[true]?.size)   // 2

// Agrupar por precio
val porPrecio = galletas.groupBy { it.precio }
// {1.69: [Choco Chip], 1.49: [Banana Walnut, Snickerdoodle], 1.99: [Sprinkles]}
```

### `fold` — acumular un resultado

Reduce la colección completa a un **único valor**. Necesita un valor inicial y una lambda que combina el acumulador con cada elemento.

```kotlin
// Suma de todos los precios
val total = galletas.fold(0.0) { acumulador, galleta ->
    acumulador + galleta.precio
}
// 0.0 + 1.69 + 1.49 + 1.49 + 1.99 = 6.66

// Construir un string con todos los nombres
val listaNombres = galletas.fold("") { acc, galleta ->
    if (acc.isEmpty()) galleta.nombre else "$acc, ${galleta.nombre}"
}
// "Choco Chip, Banana Walnut, Snickerdoodle, Sprinkles"
```

> En otros lenguajes (JavaScript, Python, Swift) esto se llama `reduce`. En Kotlin también existe `reduce()`, pero el acumulador empieza con el primer elemento en lugar de un valor inicial que tú eliges. `fold` es más flexible.

### `sortedBy` — ordenar por un campo

Devuelve una nueva lista ordenada según el criterio que le das. No modifica la original.

```kotlin
val ordenPorPrecio = galletas.sortedBy { it.precio }
// [Banana Walnut (1.49), Snickerdoodle (1.49), Choco Chip (1.69), Sprinkles (1.99)]

val ordenAlfabetico = galletas.sortedBy { it.nombre }
// [Banana Walnut, Choco Chip, Snickerdoodle, Sprinkles]
```

Para orden descendente usa `sortedByDescending`:
```kotlin
val masCaroPrimero = galletas.sortedByDescending { it.precio }
```

### Encadenar funciones

Puedes combinarlas en cadena. El resultado de una pasa como entrada a la siguiente:

```kotlin
val resultado = galletas
    .filter { it.softBaked }                        // solo blandas
    .sortedBy { it.precio }                          // ordenadas por precio
    .map { "${it.nombre}: ${it.precio}€" }           // convertidas a texto

// resultado: ["Banana Walnut: 1.49€", "Snickerdoodle: 1.49€"]
```

Esto es mucho más legible que hacer lo mismo con bucles `for` anidados.

---

## Funciones de extensión

Las **funciones de extensión** te permiten añadir nuevas funciones a clases existentes sin modificar su código fuente. La sintaxis es `fun NombreClase.nuevaFuncion()`:

```kotlin
fun String.esPalindromo(): Boolean {
    return this == this.reversed()
}

"radar".esPalindromo()    // true
"kotlin".esPalindromo()   // false
```

El `this` dentro de la función de extensión es la instancia sobre la que se llama. Desde fuera, se usa exactamente igual que si fuera un método de la clase.

Otro ejemplo: añadir una función a `Int` que devuelve si el número es primo:
```kotlin
fun Int.esPrimo(): Boolean {
    if (this < 2) return false
    for (i in 2 until this) {
        if (this % i == 0) return false
    }
    return true
}

7.esPrimo()   // true
10.esPrimo()  // false
```

---

## Ejercicios de práctica

**1.** Dada la lista de galletas de arriba, obtén el precio total de las galletas blandas (`softBaked = true`).

```kotlin
val totalBlandas = galletas
    .filter { it.softBaked }
    .fold(0.0) { acc, galleta -> acc + galleta.precio }
// 1.49 + 1.49 = 2.98
```

**2.** ¿Cuántas galletas tienen precio menor de 1.60€?
```kotlin
val baratas = galletas.count { it.precio < 1.60 }  // 2
```

**3.** ¿Cuál es la galleta más cara?
```kotlin
val masCara = galletas.maxByOrNull { it.precio }  // Sprinkles (1.99)
```

---

**→ Continúa con [[02 - Listas Desplazables|Ruta 2 — Listas desplazables con LazyColumn]]**
