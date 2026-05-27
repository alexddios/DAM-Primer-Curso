# Ruta 1 — Introducción a Kotlin

**← [[U1-Primera-App|Unidad 1]]** | **→ [[02 - Configurar Android Studio|Ruta 2]]**

---

## ¿Por qué Kotlin?

Kotlin es el lenguaje recomendado por Google para desarrollar apps Android. Hay varias razones para usarlo en lugar de Java u otros lenguajes:

- **Es más conciso**: la misma funcionalidad requiere menos líneas de código
- **Es más seguro**: reduce los errores de tipo NullPointerException (uno de los errores más comunes en Java)
- **Es moderno**: tiene características como lambdas, corrutinas y extension functions que hacen el código más legible
- **Tiene interoperabilidad total con Java**: puedes usar librerías Java existentes sin problema

---

## Tu primer programa

Para practicar Kotlin sin instalar nada, se usa el **Playground de Kotlin**, que es un editor online en el navegador: https://developer.android.com/training/kotlinplayground

Cuando abres el Playground, ya hay un programa de ejemplo:

```kotlin
fun main() {
    println("Hello, world!")
}
```

Si pulsas el botón ▶ (Ejecutar), el programa se **compila** (se traduce a algo que el ordenador entiende) y después se **ejecuta**. En el panel de resultados aparece:

```
Hello, world!
```

**Importante:** El proceso de transformar el código Kotlin que escribes a instrucciones que puede ejecutar el ordenador se llama **compilación**. Si el código tiene errores, el compilador no puede hacer su trabajo y te muestra un mensaje de error en lugar del resultado.

---

## Funciones

### ¿Qué es una función?

Una función es un bloque de código con nombre que realiza una tarea concreta. La idea es la misma que una receta de cocina: defines los pasos una vez y puedes "ejecutar la receta" cuando quieras.

Hay dos acciones distintas con las funciones:
- **Definir** una función: escribir el código con las instrucciones
- **Llamar** a una función: hacer que esas instrucciones se ejecuten

### Estructura de una función

```kotlin
fun nombreDeLaFuncion() {
    // instrucciones aquí
}
```

Partes:
- `fun` — palabra clave obligatoria que indica que estás definiendo una función
- `nombreDeLaFuncion` — el nombre que eliges (sigue el formato *camelCase*: primera letra minúscula, cada nueva palabra en mayúscula)
- `()` — paréntesis donde van los parámetros (si no hay ninguno, se deja vacío)
- `{ }` — llaves que encierran el cuerpo de la función (las instrucciones)

### La función `main`

Todo programa en Kotlin **necesita** una función `main`. Es el punto de entrada: cuando ejecutas el programa, el compilador empieza a ejecutar lo que hay dentro de `main`.

```kotlin
fun main() {
    println("Hello, world!")
}
```

Aquí `println` es una función del lenguaje Kotlin que ya viene hecha. Imprime una línea de texto en el resultado. El texto que le pasas entre paréntesis y entre comillas es lo que mostrará.

### Definir tus propias funciones

Puedes (y debes) crear tus propias funciones para organizar el código en lugar de meterlo todo en `main`.

```kotlin
fun main() {
    birthdayGreeting()   // aquí LLAMAS a la función
}

fun birthdayGreeting() {  // aquí DEFINES la función
    println("Happy Birthday, Rover!")
    println("You are now 5 years old!")
}
```

Resultado:
```
Happy Birthday, Rover!
You are now 5 years old!
```

**¿Por qué crear funciones separadas?**
- **Reutilización**: si necesitas hacer lo mismo varias veces, llamas a la función en vez de copiar y pegar código
- **Legibilidad**: el nombre de la función describe qué hace, lo que facilita entender el programa a simple vista

### Funciones con parámetros

Un parámetro es una variable que le pasas a la función cuando la llamas. Así la función puede usar ese valor dentro de su cuerpo.

Sintaxis:
```kotlin
fun nombreFuncion(nombreParametro: TipoDato) {
    // usar nombreParametro aquí
}
```

Ejemplo — queremos que el saludo funcione para cualquier nombre, no solo para Rover:

```kotlin
fun main() {
    birthdayGreeting("Rover")
    birthdayGreeting("Rex")
}

fun birthdayGreeting(name: String) {
    println("Happy Birthday, $name!")
}
```

Resultado:
```
Happy Birthday, Rover!
Happy Birthday, Rex!
```

El `$name` dentro de las comillas es una **plantilla de cadena** (*string template*): Kotlin sustituye `$name` por el valor real de la variable `name` al ejecutarse.

### Funciones con varios parámetros

Puedes poner tantos parámetros como necesites, separados por comas:

```kotlin
fun birthdayGreeting(name: String, age: Int): String {
    val nameGreeting = "Happy Birthday, $name!"
    val ageGreeting = "You are now $age years old!"
    return "$nameGreeting\n$ageGreeting"
}
```

Llamarla:
```kotlin
println(birthdayGreeting("Rover", 5))
println(birthdayGreeting("Rex", 2))
```

Resultado:
```
Happy Birthday, Rover!
You are now 5 years old!
Happy Birthday, Rex!
You are now 2 years old!
```

El `\n` dentro de una cadena es un carácter especial que representa un **salto de línea**.

### Valor de retorno de una función

Las funciones pueden devolver un valor con `return`. El tipo de dato que devuelven se indica después de los paréntesis con `:`.

```kotlin
fun birthdayGreeting(name: String, age: Int): String {
    return "Happy Birthday, $name! You are now $age years old!"
}
```

Si la función no devuelve nada, el tipo de retorno es `Unit` (equivale a `void` en Java). Normalmente no se escribe porque es el valor por defecto:

```kotlin
fun imprimirAlgo(): Unit {  // se puede escribir así...
    println("hola")
}

fun imprimirAlgo() {        // ...o así, es lo mismo
    println("hola")
}
```

### Argumentos con nombre

Cuando llamas a una función puedes escribir el nombre del parámetro explícitamente. Esto se llama **argumento con nombre**:

```kotlin
println(birthdayGreeting(name = "Rex", age = 2))
```

La ventaja es que puedes **reordenar los argumentos** sin afectar el resultado, porque Kotlin sabe a qué parámetro corresponde cada valor:

```kotlin
println(birthdayGreeting(age = 2, name = "Rex"))  // mismo resultado
```

### Argumentos predeterminados

Puedes asignar un valor por defecto a un parámetro. Si al llamar la función no pasas ese argumento, se usa el valor por defecto:

```kotlin
fun birthdayGreeting(name: String = "Rover", age: Int): String {
    return "Happy Birthday, $name! You are now $age years old!"
}

// No hace falta pasar el nombre, usa "Rover" por defecto
println(birthdayGreeting(age = 5))
// Resultado: Happy Birthday, Rover! You are now 5 years old!
```

> Nota: cuando se omite un argumento con valor por defecto que no es el último, hay que usar argumentos con nombre para los que sí se pasan, para evitar ambigüedad.

### Resumen: parámetro vs argumento

| Término | Qué es | Ejemplo |
|---------|--------|---------|
| **Parámetro** | La variable definida en la función | `name: String` en la firma |
| **Argumento** | El valor que pasas al llamarla | `"Rover"` en la llamada |

---

## Variables

### ¿Por qué necesitamos variables?

Imagina una app de noticias. El título, la fecha y el autor de cada artículo cambian constantemente. No puedes escribir el código para un artículo concreto porque la app tendría que funcionar para cualquier artículo. Ahí entran las variables: son contenedores con nombre que pueden guardar un valor que puede cambiar.

Analogía: una variable es como una caja con una etiqueta. La etiqueta es el nombre, y dentro de la caja está el valor.

### Declarar una variable

```kotlin
val count: Int = 2
```

Partes:
- `val` — palabra clave que indica que es una variable (inmutable, no se puede cambiar)
- `count` — nombre de la variable (sigue camelCase igual que los nombres de función)
- `: Int` — tipo de dato (el dos puntos separa el nombre del tipo)
- `= 2` — valor inicial (el `=` es el *operador de asignación*, asigna el valor a la variable)

### `val` vs `var`

Hay dos formas de declarar variables:

| Palabra clave | Significado | ¿Se puede cambiar? |
|---|---|---|
| `val` | valor (value) | ❌ No (inmutable) |
| `var` | variable | ✅ Sí (mutable) |

```kotlin
val nombre = "Android"   // no puedo cambiar nombre después
var puntuacion = 0       // puedo cambiar puntuacion más tarde
puntuacion = 10          // esto funciona con var
// nombre = "Kotlin"     // esto daría ERROR con val
```

La regla general es: **usa `val` siempre que puedas**. Solo usa `var` cuando realmente necesites cambiar el valor.

### Tipos de datos básicos en Kotlin

| Tipo | Qué guarda | Ejemplos |
|------|-----------|---------|
| `String` | Texto | `"Hola"`, `"Android"`, `"123"` |
| `Int` | Número entero | `32`, `-5`, `1000000` |
| `Double` | Número decimal (mayor precisión) | `3.14`, `2.0`, `-0.001` |
| `Float` | Número decimal (menor precisión) | `3.14f`, `2.0F` |
| `Boolean` | Verdadero o falso | `true`, `false` |

> Nota: `Float` lleva una `f` o `F` al final del número para distinguirlo de `Double`.

Ejemplos prácticos de cuándo usar cada tipo:
- Nombre de un restaurante → `String`
- Calificación con estrellas (ej: 4.2) → `Double`
- Número de opiniones → `Int`
- Si el usuario ha guardado en favoritos → `Boolean`
- Dirección → `String`

### Inferencia de tipos

Kotlin puede deducir el tipo de dato por sí solo si le das un valor inicial. No hace falta escribirlo siempre:

```kotlin
val count: Int = 2    // con tipo explícito
val count = 2         // sin tipo, Kotlin infiere que es Int

val nombre: String = "Kotlin"
val nombre = "Kotlin"  // Kotlin infiere que es String
```

Ambas formas son válidas. La segunda es más común en la práctica.

### Comentarios en el código

Los comentarios son anotaciones para el programador que el compilador ignora completamente. Sirven para explicar qué hace el código:

```kotlin
// Esto es un comentario de una línea
// Todo lo que sigue al // en esa línea se ignora

/* Esto es un comentario
   de varias líneas */

fun main() {
    val total = 10 * 20  // calcular el total (comentario en línea)
}
```

---

## Guía de estilo de Kotlin

Google tiene una guía de estilo que define cómo debe verse el código Kotlin para que sea consistente entre desarrolladores. Estas son las normas más importantes para lo que hemos visto:

- Los **nombres de funciones y variables** siguen *camelCase*: primera letra minúscula, cada nueva palabra empieza en mayúscula. Ejemplos: `calculateTip`, `numberOfEmails`
- Cada instrucción va en **su propia línea**
- La **llave de apertura** `{` va al final de la línea donde empieza la función, con un espacio antes
- El **cuerpo de la función** tiene una sangría de **4 espacios** (no tabulaciones)
- La **llave de cierre** `}` va sola en su propia línea, alineada con la palabra `fun`

```kotlin
// ✅ Correcto
fun saludar(nombre: String) {
    println("Hola, $nombre")
}

// ❌ Incorrecto
fun saludar(nombre:String){
println("Hola, $nombre")
}
```

---

## Errores comunes y cómo arreglarlos

Cometer errores es normal cuando aprendes a programar. El compilador de Kotlin muestra mensajes de error cuando algo está mal.

**Error: comilla sin cerrar**
```kotlin
// ❌ Error
println("Today is sunny!)   // falta la comilla de cierre

// ✅ Correcto
println("Today is sunny!")
```

**Error: función mal escrita**
```kotlin
// ❌ Error — printLine no existe
printLine("There is a chance of snow")

// ✅ Correcto — es println (todo minúsculas)
println("There is a chance of snow")
```

**Error: llave sin cerrar**
```kotlin
// ❌ Error
fun main() {
    println("Cloudy")
    // falta la llave de cierre

// ✅ Correcto
fun main() {
    println("Cloudy")
}
```

> Consejo: cuando el compilador dice "Expecting..." (Esperando...) significa que falta algo. Lee el mensaje de error y fíjate en el número de línea que indica.

---

## Ejercicios de práctica

Intenta resolver estos antes de ver la solución:

**1.** ¿Qué imprime este programa?
```kotlin
fun main() {
    println("1")
    println("2")
    println("3")
}
```

**2.** Escribe un programa que imprima exactamente esto:
```
I'm
learning
Kotlin!
```

**3.** Escribe una función `suma` que reciba dos números enteros y devuelva su suma. Llámala desde `main` e imprime el resultado.

**Soluciones:**

```kotlin
// Ejercicio 1: imprime 1, 2, 3 (cada uno en su línea)

// Ejercicio 2:
fun main() {
    println("I'm")
    println("learning")
    println("Kotlin!")
}

// Ejercicio 3:
fun main() {
    println(suma(3, 5))  // imprime 8
}

fun suma(a: Int, b: Int): Int {
    return a + b
}
```

---

**→ Continúa con [[02 - Configurar Android Studio|Ruta 2 — Configurar Android Studio]]**
