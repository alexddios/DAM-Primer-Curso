# Ruta 3 — Estado en Compose

**← [[02 - Botones e Interactividad|Ruta 2]]** | **→ [[U3-Listas-Material|Unidad 3]]**

---

## ¿Qué es el estado?

El **estado** de una app es cualquier valor que puede cambiar con el tiempo. Esta definición es amplia a propósito:

- El texto que el usuario ha escrito en un campo de texto → estado
- Si un checkbox está marcado o no → estado
- El número que salió al lanzar un dado → estado
- El scroll actual de una lista → estado
- Los mensajes cargados desde el servidor → estado

**La UI es una función del estado.** Dicho de otro modo: lo que se muestra en pantalla es el resultado de "pintar" el estado actual. Cuando el estado cambia, la pantalla debe actualizarse.

---

## `remember` y `mutableStateOf`

Para que Compose "recuerde" un valor entre recomposiciones (redibujados) y para que actualice la UI cuando ese valor cambia, necesitas dos cosas:

- **`mutableStateOf(valor)`**: crea un objeto de estado observable. Cuando su valor cambia, Compose sabe que tiene que volver a dibujar los composables que lo leen
- **`remember { }`**: hace que el valor persista entre recomposiciones. Sin `remember`, cada vez que Compose redibuja la función, la variable se reiniciaría a su valor inicial

```kotlin
@Composable
fun Contador() {
    var cuenta by remember { mutableStateOf(0) }

    Column {
        Text(text = "Cuenta: $cuenta")
        Button(onClick = { cuenta++ }) {
            Text("Sumar uno")
        }
    }
}
```

> El `by` en `var cuenta by remember { ... }` es la sintaxis de **delegación de propiedades** en Kotlin. Permite leer y escribir `cuenta` directamente como si fuera una variable normal, sin tener que escribir `cuenta.value` cada vez.

### ¿Qué es la recomposición?

Cuando el estado cambia, Compose vuelve a ejecutar (*recompone*) los composables que dependen de ese estado. Solo se actualizan las partes afectadas, no toda la pantalla.

Ejemplo con el contador: cuando `cuenta` cambia al pulsar el botón, Compose sabe que `Contador()` usa `cuenta`, así que lo vuelve a ejecutar y el `Text` muestra el nuevo valor.

---

## La app Tip Time (calculadora de propinas)

Es el proyecto principal de esta ruta. Calcula la propina en función del importe de la factura que introduce el usuario.

### `TextField` — campo de texto editable

```kotlin
@Composable
fun CampoImporte(
    valor: String,
    alCambiar: (String) -> Unit
) {
    TextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text("Importe de la factura") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}
```

- `value`: el texto actual que muestra el campo
- `onValueChange`: lambda que se llama cada vez que el usuario escribe o borra algo. Recibe el nuevo texto como parámetro
- `label`: texto descriptivo que aparece dentro del campo (se mueve arriba al escribir)
- `keyboardOptions`: configura el tipo de teclado. `KeyboardType.Number` muestra el teclado numérico
- `singleLine = true`: impide que el campo crezca a múltiples líneas

---

## State hoisting — Elevación del estado

El **state hoisting** (elevación del estado) es el patrón recomendado en Compose para gestionar el estado. La idea es simple:

> **Mueve el estado hacia arriba en el árbol de composables**, al composable padre que lo necesite, y pasa el valor y las funciones para cambiarlo hacia abajo como parámetros.

**¿Por qué?** Porque si el estado está dentro del composable que lo muestra (*stateful*), ese composable no se puede reutilizar ni probar fácilmente. Si el estado está fuera (*stateless*), el composable solo muestra lo que le das y llama a las funciones que le pasas.

### Composable stateful vs stateless

**Stateful** (tiene estado interno):
```kotlin
@Composable
fun ContadorConEstado() {
    var cuenta by remember { mutableStateOf(0) }
    ContadorUI(cuenta = cuenta, alPulsar = { cuenta++ })
}
```

**Stateless** (sin estado interno, recibe todo como parámetros):
```kotlin
@Composable
fun ContadorUI(cuenta: Int, alPulsar: () -> Unit) {
    Column {
        Text("Cuenta: $cuenta")
        Button(onClick = alPulsar) { Text("Sumar") }
    }
}
```

`ContadorUI` es reutilizable y fácil de probar porque no tiene estado propio. `ContadorConEstado` es el que "sabe" el valor actual y lo gestiona.

### Ejemplo completo: Tip Time

```kotlin
@Composable
fun TipTimeLayout() {
    // Estado elevado al composable raíz
    var importeInput by remember { mutableStateOf("") }
    var porcentajeTipInput by remember { mutableStateOf("") }
    var redondear by remember { mutableStateOf(false) }

    // Cálculos derivados del estado
    val importe = importeInput.toDoubleOrNull() ?: 0.0
    val porcentajeTip = porcentajeTipInput.toDoubleOrNull() ?: 0.0
    val propina = calcularPropina(importe, porcentajeTip, redondear)

    Column(modifier = Modifier.padding(40.dp)) {
        Text("Calculadora de Propinas", modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(Modifier.height(16.dp))

        CampoEditable(
            label = R.string.bill_amount,
            valor = importeInput,
            alCambiar = { importeInput = it }  // "it" es el nuevo texto
        )

        CampoEditable(
            label = R.string.how_was_the_service,
            valor = porcentajeTipInput,
            alCambiar = { porcentajeTipInput = it }
        )

        // Switch para redondear la propina
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Redondear propina")
            Spacer(Modifier.weight(1f))  // empuja el Switch al extremo derecho
            Switch(checked = redondear, onCheckedChange = { redondear = it })
        }

        Text(
            text = "Propina: ${NumberFormat.getCurrencyInstance().format(propina)}",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

fun calcularPropina(importe: Double, porcentaje: Double, redondear: Boolean): Double {
    var propina = importe * porcentaje / 100
    if (redondear) propina = kotlin.math.ceil(propina)
    return propina
}
```

Cosas nuevas:
- `.toDoubleOrNull()`: convierte un String a Double, o devuelve null si no se puede (ej: si el campo está vacío). El `?: 0.0` lo convierte a 0.0 en ese caso
- `Switch`: composable para un interruptor on/off
- `Modifier.weight(1f)`: dentro de un `Row`, hace que ese elemento ocupe todo el espacio sobrante, empujando los demás hacia los lados
- `NumberFormat.getCurrencyInstance()`: formatea un número como moneda según la configuración regional del dispositivo

---

## Pruebas automatizadas

Las pruebas automatizadas son código que verifica que tu código funciona correctamente. Son fundamentales porque:

- Detectan bugs inmediatamente cuando cambias algo
- Sirven como documentación: explican qué debe hacer el código
- Dan confianza para refactorizar sin romper nada

### Tipos de pruebas en Android

| Tipo | Dónde se ejecutan | Velocidad |
|------|-------------------|-----------|
| **Pruebas de unidad** (*unit tests*) | En el ordenador (JVM) | Muy rápido |
| **Pruebas de instrumentación** | En el dispositivo/emulador | Más lento |
| **Pruebas de UI** | En el dispositivo/emulador | Más lento |

Las pruebas de unidad prueban funciones individuales de forma aislada. Son las más rápidas y las más fáciles de escribir.

### Estructura de una prueba de unidad

```kotlin
// En src/test/java/com/example/miapp/
class CalculadoraPropinasTest {

    @Test
    fun calcularPropina_conImporte100_y20porciento_devuelve20() {
        val resultado = calcularPropina(importe = 100.0, porcentaje = 20.0, redondear = false)
        assertEquals(20.0, resultado, 0.01)  // valor esperado, valor real, tolerancia
    }

    @Test
    fun calcularPropina_conRedondeo_redondea_hacia_arriba() {
        val resultado = calcularPropina(importe = 100.0, porcentaje = 15.0, redondear = true)
        assertEquals(15.0, resultado, 0.01)  // 15% de 100 ya es exacto
    }
}
```

- `@Test`: anotación que marca una función como prueba
- `assertEquals(esperado, real, tolerancia)`: comprueba que dos valores son iguales
- Convención de nombres: `nombreMetodo_condición_resultadoEsperado`

Para ejecutar las pruebas: clic derecho sobre la clase de prueba → "Run tests".

---

## Resumen de la Unidad 2

- `if/else` y `when` para condicionales en Kotlin
- Tipos nullable (`String?`) y operadores de null safety (`?.`, `?:`, `!!`)
- Clases, constructores, herencia, visibilidad
- Lambdas y funciones de orden superior
- `Button` con `onClick` para interactividad
- **Estado** (`mutableStateOf` + `remember`) — el concepto central de Compose
- **Recomposición**: Compose redibuja la UI cuando el estado cambia
- **State hoisting**: el estado sube al padre, los hijos son stateless
- `TextField` para entrada de texto del usuario
- `Switch` para toggles booleanos
- Pruebas de unidad con JUnit

---

**→ Continúa con [[../U3-Listas-Material/00 - Unidad 3 Resumen|Unidad 3 — Listas y Material Design]]**
