# Ruta 2 — Botones e Interactividad

**← [[01 - Kotlin Intermedio|Ruta 1]]** | **→ [[03 - Estado en Compose|Ruta 3]]**

---

## La app Dice Roller

En esta ruta construyes la app **Dice Roller**: un dado virtual. El usuario pulsa un botón y ve la imagen del número que salió. Es la primera app realmente interactiva del curso.

Objetivo final: una imagen de dado que cambia al pulsar un botón "Roll".

---

## El composable `Button`

```kotlin
Button(onClick = { /* acción al pulsar */ }) {
    Text("Lanzar dado")
}
```

- El parámetro `onClick` recibe una lambda que se ejecuta cuando el usuario pulsa el botón
- El contenido del botón (lo que se muestra dentro) va en el bloque `{ }` al final (trailing lambda)
- Dentro del botón normalmente va un `Text`, pero podría ir cualquier composable

---

## Responder a eventos con `onClick`

La forma de hacer que algo cambie al pulsar un botón es:
1. Almacenar algún dato en el **estado** (lo verás en profundidad en la Ruta 3)
2. En `onClick`, cambiar ese estado
3. Compose detecta el cambio y vuelve a dibujar la pantalla automáticamente

Ejemplo completo de Dice Roller:

```kotlin
@Composable
fun DiceRollerApp() {
    var resultado by remember { mutableStateOf(1) }  // estado: número del dado

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Imagen del dado según el resultado
        val imagenId = when (resultado) {
            1 -> R.drawable.dice_1
            2 -> R.drawable.dice_2
            3 -> R.drawable.dice_3
            4 -> R.drawable.dice_4
            5 -> R.drawable.dice_5
            else -> R.drawable.dice_6
        }

        Image(
            painter = painterResource(id = imagenId),
            contentDescription = resultado.toString()
        )

        Spacer(modifier = Modifier.height(16.dp))  // espacio entre elementos

        Button(onClick = { resultado = (1..6).random() }) {  // número aleatorio entre 1 y 6
            Text(text = "Roll")
        }
    }
}
```

Cosas nuevas aquí:
- `(1..6).random()`: un **rango** en Kotlin (`..`) y `.random()` elige un número aleatorio dentro de él
- `Spacer`: un composable que simplemente añade espacio vacío entre elementos
- `remember` y `mutableStateOf`: esto se explica en detalle en la Ruta 3

---

## Strings en recursos

En Android, los textos que se muestran al usuario no deberían estar escritos directamente en el código ("hardcodeados"). En su lugar, se guardan en `res/values/strings.xml`:

```xml
<!-- res/values/strings.xml -->
<resources>
    <string name="app_name">Dice Roller</string>
    <string name="roll">Roll</string>
</resources>
```

Y en el código se acceden con `stringResource()`:

```kotlin
Text(text = stringResource(R.string.roll))
```

**¿Por qué hacerlo así?** Porque facilita la **traducción** de la app a otros idiomas: solo tienes que crear un archivo `strings.xml` diferente para cada idioma (con los mismos nombres pero los textos traducidos) y Android elige el correcto automáticamente.

---

## El depurador de Android Studio

El **depurador** te permite pausar la ejecución del programa en un punto concreto y examinar el valor de las variables en ese momento. Es esencial para encontrar bugs.

### Cómo usar el depurador

1. Haz clic en el margen izquierdo de una línea de código para poner un **breakpoint** (punto de parada) — aparece un punto rojo
2. Ejecuta la app pulsando el icono del bicho 🐛 (en vez del triángulo ▶)
3. Cuando la ejecución llega al breakpoint, la app se pausa
4. En el panel **Debug** de Android Studio puedes ver:
   - El valor de todas las variables locales
   - La pila de llamadas (*call stack*): qué funciones llevaron hasta ese punto
5. Controles del depurador:
   - **Step Over** (F8): ejecuta la línea actual y para en la siguiente
   - **Step Into** (F7): si la línea llama a una función, entra dentro de ella
   - **Step Out** (Shift+F8): sale de la función actual
   - **Resume** (F9): continúa la ejecución hasta el siguiente breakpoint o el final

### Cuándo usar el depurador

- Cuando la app no se comporta como esperas y no sabes por qué
- Para verificar que una variable tiene el valor que crees que tiene
- Para entender el flujo de ejecución de código complejo

---

**→ Continúa con [[03 - Estado en Compose|Ruta 3 — Estado en Compose]]**
