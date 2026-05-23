# Ruta 1 — Kotlin Intermedio

**← [[U2-IU-App]]** | **→ [[02 - Botones e Interactividad|Ruta 2]]**

---

## Condicionales

Los condicionales permiten que el programa tome decisiones: ejecuta unas instrucciones u otras dependiendo de si se cumple una condición.

### Expresiones booleanas

Antes de los condicionales hay que entender las expresiones booleanas. Una expresión booleana se evalúa como `true` o `false`. Se construyen con operadores de comparación:

| Operador | Significado | Ejemplo |
|----------|-------------|---------|
| `==` | igual a | `5 == 5` → `true` |
| `!=` | distinto de | `5 != 3` → `true` |
| `>` | mayor que | `5 > 3` → `true` |
| `<` | menor que | `3 < 5` → `true` |
| `>=` | mayor o igual | `5 >= 5` → `true` |
| `<=` | menor o igual | `3 <= 5` → `true` |

Y operadores lógicos para combinar condiciones:

| Operador | Significado | Ejemplo |
|----------|-------------|---------|
| `&&` | AND (y) | `true && false` → `false` |
| `\|\|` | OR (o) | `true \|\| false` → `true` |
| `!` | NOT (no) | `!true` → `false` |

### `if` / `else`

```kotlin
val semaforo = "rojo"

if (semaforo == "rojo") {
    println("Para el coche")
} else if (semaforo == "amarillo") {
    println("Prepárate")
} else {
    println("Puedes pasar")
}
```

En Kotlin, `if/else` también puede usarse como **expresión** (devuelve un valor, sin necesidad de `return`):

```kotlin
val mensaje = if (semaforo == "rojo") "Para" else "Sigue"
println(mensaje)  // Para
```

Esto es muy útil para asignar valores dependiendo de una condición en una sola línea.

### `when` — el switch de Kotlin

`when` es el equivalente mejorado del `switch` de Java. Sirve para comparar un valor contra múltiples casos:

```kotlin
val dia = 3

when (dia) {
    1 -> println("Lunes")
    2 -> println("Martes")
    3 -> println("Miércoles")
    4 -> println("Jueves")
    5 -> println("Viernes")
    6, 7 -> println("Fin de semana")  // varios valores en el mismo caso
    else -> println("Día inválido")   // caso por defecto (obligatorio si no se cubren todos)
}
```

`when` también puede usarse como expresión:

```kotlin
val nombreDia = when (dia) {
    1 -> "Lunes"
    2 -> "Martes"
    3 -> "Miércoles"
    else -> "Otro día"
}
```

Además puede evaluar condiciones (sin valor entre paréntesis):

```kotlin
val temperatura = 25

when {
    temperatura < 10 -> println("Frío")
    temperatura in 10..20 -> println("Fresco")
    temperatura > 20 -> println("Calor")
}
```

---

## Nulabilidad

### El problema del `null`

En Java (y muchos otros lenguajes), cualquier variable puede ser `null` (vacía, sin valor). Esto causa el famoso `NullPointerException` — uno de los errores en tiempo de ejecución más comunes en la historia del software.

**Kotlin soluciona esto a nivel de lenguaje**: por defecto, ninguna variable puede ser `null`. Si intentas asignarle `null`, el compilador da error.

```kotlin
var nombre: String = "Alex"
nombre = null  // ❌ Error de compilación: String no acepta null
```

### Tipos nullable

Si necesitas que una variable pueda ser `null`, tienes que declararlo explícitamente añadiendo `?` al tipo:

```kotlin
var actorFavorito: String? = null   // ✅ String? acepta null
actorFavorito = "Tom Hanks"         // también puede tener valor
```

La diferencia es importante:
- `String` — nunca puede ser null. El compilador lo garantiza.
- `String?` — puede ser null o tener un valor.

### Acceder a propiedades de tipos nullable

Si tienes un `String?`, no puedes acceder directamente a `.length` porque podría ser null:

```kotlin
var nombre: String? = "Alex"
println(nombre.length)  // ❌ Error: podría ser null
```

Tienes varias opciones:

**Operador de llamada segura `?.`** — solo accede si no es null, si es null devuelve null:
```kotlin
println(nombre?.length)  // Si nombre es null, imprime null; si no, imprime la longitud
```

**Operador Elvis `?:`** — proporciona un valor por defecto si es null:
```kotlin
val longitud = nombre?.length ?: 0  // si nombre es null, longitud = 0
```

**Operador de aserción no-null `!!`** — fuerza el acceso y lanza una excepción si es null (úsalo solo si estás 100% seguro de que no es null):
```kotlin
println(nombre!!.length)  // si nombre es null → NullPointerException
```
> Evita `!!` siempre que puedas. Derrota el propósito de la seguridad de null de Kotlin.

**Comprobación con `if`:**
```kotlin
if (nombre != null) {
    println(nombre.length)  // aquí Kotlin sabe que no es null
}
```

---

## Clases y Programación Orientada a Objetos

### ¿Qué es una clase?

Una **clase** es una plantilla para crear objetos. Define qué **propiedades** (datos) y **métodos** (comportamientos) tienen los objetos de ese tipo.

Analogía: una clase `Coche` es el plano de un coche. A partir de ese plano puedes crear múltiples coches concretos (objetos), cada uno con su propio color, modelo y velocidad.

### Definir una clase

```kotlin
class DispositivoInteligente(val nombre: String, val categoria: String) {
    var estadoDispositivo: Int = 0   // propiedad con valor por defecto

    fun encender() {
        estadoDispositivo = 1
        println("$nombre está encendido")
    }

    fun apagar() {
        estadoDispositivo = 0
        println("$nombre está apagado")
    }
}
```

### Crear un objeto (instancia)

```kotlin
val televisor = DispositivoInteligente("Mi TV", "Entretenimiento")
println(televisor.nombre)      // Mi TV
televisor.encender()           // Mi TV está encendido
println(televisor.estadoDispositivo)  // 1
```

Crear un objeto de una clase se llama **instanciar** la clase. El objeto es una **instancia**.

### Constructores

El constructor es el mecanismo para inicializar un objeto. En Kotlin hay dos tipos:

**Constructor primario** — va en la cabecera de la clase:
```kotlin
class Persona(val nombre: String, var edad: Int)
```

**Constructor secundario** — usando `constructor` dentro del cuerpo:
```kotlin
class Persona(val nombre: String) {
    var edad: Int = 0

    constructor(nombre: String, edad: Int) : this(nombre) {
        this.edad = edad
    }
}
```

### Herencia

Una clase puede **heredar** de otra, tomando sus propiedades y métodos y pudiendo añadir o modificar los suyos propios.

```kotlin
// Clase padre (debe ser open para permitir herencia)
open class DispositivoInteligente(val nombre: String) {
    open fun encender() {
        println("Encendiendo...")
    }
}

// Clase hija
class TelevisorInteligente(nombre: String, val resolucion: String)
    : DispositivoInteligente(nombre) {

    override fun encender() {    // sobreescribe el método del padre
        super.encender()          // llama al método del padre
        println("Mostrando resolución $resolucion")
    }
}
```

### Visibilidad: `public`, `private`, `protected`

Los **modificadores de visibilidad** controlan desde dónde se puede acceder a una propiedad o método:

| Modificador | Accesible desde |
|-------------|----------------|
| `public` (por defecto) | Cualquier lugar |
| `private` | Solo dentro de la clase |
| `protected` | Dentro de la clase y sus subclases |
| `internal` | Dentro del mismo módulo |

```kotlin
class CuentaBancaria {
    private var saldo: Double = 0.0   // solo accesible desde dentro

    fun ingresar(cantidad: Double) {
        if (cantidad > 0) saldo += cantidad
    }

    fun getSaldo(): Double = saldo  // método público para leer el saldo
}
```

### `data class`

Una `data class` es especial para clases que solo sirven para almacenar datos. Genera automáticamente `equals()`, `hashCode()`, `toString()` y `copy()`:

```kotlin
data class Punto(val x: Int, val y: Int)

val p1 = Punto(1, 2)
val p2 = p1.copy(y = 5)  // crea una copia cambiando solo y
println(p2)               // Punto(x=1, y=5)
println(p1 == p2)         // false (compara por valor gracias a equals())
```

---

## Lambdas y tipos de función

### Funciones como tipos de datos

En Kotlin, **las funciones son ciudadanos de primera clase**: puedes almacenarlas en variables, pasarlas como argumentos a otras funciones, o devolverlas como resultado. Esto es lo que hace que Compose funcione.

```kotlin
val truco: () -> Unit = {
    println("¡Abracadabra!")
}

// Llamar la función almacenada en la variable
truco()  // imprime: ¡Abracadabra!
```

### Tipo de función

La sintaxis del tipo de una función es: `(tiposDeParámetros) -> TipoDeRetorno`

```kotlin
val suma: (Int, Int) -> Int = { a, b -> a + b }
val saludo: (String) -> Unit = { nombre -> println("Hola, $nombre") }
val constante: () -> String = { "Hola" }
```

### Expresiones lambda

Una **lambda** es una función sin nombre, definida en el lugar donde se usa:

```kotlin
{ parámetro1: Tipo1, parámetro2: Tipo2 -> 
    // cuerpo de la función
    expresionDeRetorno  // última expresión = valor de retorno
}
```

Ejemplos:
```kotlin
val doblar = { n: Int -> n * 2 }
doblar(5)  // 10

// Si Kotlin puede inferir el tipo, no hace falta declararlo
val lista = listOf(1, 2, 3, 4, 5)
lista.filter { n -> n > 2 }   // [3, 4, 5]
lista.map { n -> n * 2 }      // [2, 4, 6, 8, 10]
```

### `it` — el parámetro implícito

Cuando una lambda tiene un solo parámetro, puedes omitir su nombre y usar `it`:

```kotlin
lista.filter { it > 2 }    // equivalente a { n -> n > 2 }
lista.map { it * 2 }       // equivalente a { n -> n * 2 }
```

### Trailing lambda

Si el último parámetro de una función es una lambda, puedes escribirla fuera de los paréntesis. Esto es muy común en Compose:

```kotlin
// Estas dos formas son equivalentes:
Button(onClick = { println("click") })  // lambda dentro
Button(onClick = { println("click") }) {}

// O en Compose:
Column(modifier = Modifier.padding(16.dp)) {   // la lambda va fuera
    Text("Hola")
    Text("Mundo")
}
```

### Funciones de orden superior

Una función que **recibe otra función como parámetro** o **devuelve una función** se llama **función de orden superior**:

```kotlin
fun repetir(veces: Int, accion: () -> Unit) {
    for (i in 1..veces) {
        accion()
    }
}

repetir(3) { println("¡Hola!") }
// ¡Hola!
// ¡Hola!
// ¡Hola!
```

---

## Ejercicios de práctica

**1.** Escribe un programa que imprima el resumen de notificaciones:
- Si hay menos de 100 notificaciones: "Tienes X notificaciones"
- Si hay 100 o más: "Tienes 99+ notificaciones"

```kotlin
fun main() {
    val mañana = 51
    val tarde = 135
    imprimirResumen(mañana)
    imprimirResumen(tarde)
}

fun imprimirResumen(numero: Int) {
    if (numero < 100) {
        println("Tienes $numero notificaciones")
    } else {
        println("Tienes 99+ notificaciones")
    }
}
```

**2.** ¿Qué imprime esto?
```kotlin
var nombre: String? = null
println(nombre?.length ?: -1)
```
Respuesta: `-1` — porque `nombre` es null, `nombre?.length` devuelve null, y `?: -1` devuelve -1 como valor por defecto.

---

**→ Continúa con [[02 - Botones e Interactividad|Ruta 2 — Botones e Interactividad]]**
