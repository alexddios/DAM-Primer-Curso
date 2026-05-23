# Ruta 2 — Configurar Android Studio

**← [[01 - Introducción a Kotlin|Ruta 1]]** | **→ [[03 - Primer diseño con Compose|Ruta 3]]**

---

## ¿Qué es Android Studio?

Android Studio es el **IDE** (entorno de desarrollo integrado) oficial para desarrollar apps Android. Es como un editor de texto muy avanzado que incluye:

- Editor de código con autocompletado y detección de errores en tiempo real
- Herramientas para compilar y ejecutar la app
- Un **emulador** de Android (un móvil virtual en tu ordenador)
- Herramientas de depuración (*debugging*)
- Vista previa del diseño de la interfaz

Se puede descargar gratis desde: https://developer.android.com/studio

---

## Instalación y requisitos

Antes de instalar, comprueba que tu ordenador cumple los **requisitos mínimos** (en la web oficial están actualizados, los más relevantes son):
- Windows, macOS o Linux
- Al menos 8 GB de RAM (recomendado 16 GB)
- 8 GB de espacio en disco disponible para el IDE + emulador
- Pantalla de al menos 1280×800 píxeles

La instalación es como cualquier otro programa: descargas el instalador, lo ejecutas y sigues los pasos. Android Studio incluye el SDK de Android (las herramientas que necesitas para compilar apps).

---

## Crear tu primer proyecto

Una vez instalado, al abrir Android Studio:

1. Pulsa **New Project** (o *File → New → New Project* si ya tienes un proyecto abierto)
2. Selecciona la plantilla **Empty Activity** — es la más básica y la que usa Compose
3. Rellena los campos:
   - **Name**: el nombre de la app (ej: `Happy Birthday`)
   - **Package name**: identificador único de la app, en formato de dominio invertido (ej: `com.example.happybirthday`). Cuando publicas en Google Play, este nombre es permanente
   - **Save location**: dónde se guarda el proyecto en tu disco
   - **Minimum SDK**: la versión mínima de Android que necesita el dispositivo para usar la app. A mayor mínimo, más funciones disponibles pero menos dispositivos compatibles. Para empezar, **API 24** (Android 7.0) cubre más del 90% de dispositivos activos
4. Pulsa **Finish**

Android Studio genera automáticamente la estructura básica del proyecto.

---

## Estructura de un proyecto Android

```
MiApp/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/example/miapp/
│   │       │       └── MainActivity.kt    ← código principal
│   │       ├── res/                       ← recursos (imágenes, strings...)
│   │       │   ├── drawable/              ← imágenes
│   │       │   ├── mipmap/                ← iconos de la app
│   │       │   └── values/
│   │       │       ├── strings.xml        ← textos de la app
│   │       │       ├── colors.xml         ← colores
│   │       │       └── themes.xml         ← temas/estilos
│   │       └── AndroidManifest.xml        ← configuración de la app
│   └── build.gradle.kts                   ← dependencias del módulo
└── build.gradle.kts                       ← configuración del proyecto
```

Los archivos más importantes al principio:
- **`MainActivity.kt`**: el código de la pantalla principal
- **`AndroidManifest.xml`**: dice al sistema qué actividades tiene la app, qué permisos necesita, cuál es el icono, etc.
- **`build.gradle.kts`**: define qué versión de Android usas y qué librerías externas necesitas

---

## El archivo `MainActivity.kt`

Cuando creas un proyecto nuevo con la plantilla *Empty Activity*, Android Studio genera este código:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
```

Aunque hay muchas cosas que aún no conoces, las partes clave son:
- `MainActivity` es la **actividad** principal — una actividad es básicamente una pantalla de la app
- `setContent { }` es donde defines la interfaz de usuario con Compose
- `Greeting` es un **composable** (una función que describe parte de la UI)

No te preocupes si no entiendes todo ahora. Se irá explicando en la Ruta 3 y en las unidades siguientes.

---

## Ejecutar la app en el emulador

El **emulador** simula un dispositivo Android en tu ordenador. Para configurarlo:

1. En Android Studio, abre **Device Manager** (icono del teléfono en la barra lateral o *Tools → Device Manager*)
2. Pulsa **Create Device**
3. Elige un modelo de teléfono (ej: *Pixel 7*)
4. Elige la versión del sistema operativo (elige la más reciente disponible)
5. Pulsa **Finish**

Para ejecutar la app:
1. Selecciona el emulador en el menú desplegable de la barra de herramientas
2. Pulsa el botón ▶ (Run)
3. Android Studio compila el proyecto y lo instala en el emulador

La primera vez tarda bastante (el emulador tiene que arrancar). Las siguientes veces es más rápido.

### Ejecutar en un dispositivo físico

Si tienes un móvil Android, puedes ejecutar la app directamente en él:

1. En el móvil, ve a *Ajustes → Acerca del teléfono* y pulsa **7 veces** sobre "Número de compilación" — esto activa el **modo desarrollador**
2. Ve a *Ajustes → Opciones de desarrollador* y activa **Depuración USB**
3. Conecta el teléfono al ordenador con un cable USB
4. Acepta el permiso de depuración que aparece en el teléfono
5. En Android Studio, selecciona tu dispositivo en el menú desplegable y pulsa ▶

---

## Editar el código y ver el resultado

Para entender cómo funciona el flujo de trabajo, modifica el texto que muestra la app:

En `MainActivity.kt`, busca la función `Greeting` y cambia el texto:

```kotlin
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
```

Cambia `"Hello $name!"` por `"¡Hola, $name!"` y pulsa ▶ para ejecutar. Verás el cambio en el emulador.

---

## Errores de compilación vs errores en tiempo de ejecución

Hay dos tipos de errores que encontrarás:

**Errores de compilación**: el código tiene algo mal y Android Studio no puede compilar. Aparecen subrayados en rojo en el editor y el botón ▶ no funciona hasta que los corrijas. Ejemplo: falta un paréntesis, usas una variable que no existe.

**Errores en tiempo de ejecución (*crashes*)**: el código compila bien pero la app falla mientras se ejecuta. En el panel **Logcat** de Android Studio aparecen los mensajes de error (*stack trace*) que te dicen qué ha fallado y en qué línea.

---

**→ Continúa con [[03 - Primer diseño con Compose|Ruta 3 — Primer diseño con Compose]]**
