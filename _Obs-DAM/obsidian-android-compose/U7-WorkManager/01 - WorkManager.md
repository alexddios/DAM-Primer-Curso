# Ruta 1 — Programar tareas con WorkManager

**← [[U7-WorkManager|Unidad 7]]** | **→ [[U8-Views-Compose|Unidad 8]]**

---

## ¿Qué es WorkManager?

WorkManager es la API de Jetpack para programar **trabajo en segundo plano garantizado**: tareas que deben ejecutarse aunque el usuario salga de la app, cierre la app, o incluso reinicie el dispositivo.

La palabra clave es **garantizado**. WorkManager persiste el trabajo pendiente en una base de datos interna y lo reanuda si la app o el dispositivo se interrumpen antes de que termine.

---

## ¿Cuándo usar WorkManager?

La elección de la herramienta correcta depende de dos factores: si el trabajo es inmediato o diferible, y si debe ser garantizado.

| Herramienta | Cuándo usarla |
|-------------|--------------|
| **Corrutinas** | Trabajo inmediato mientras el usuario está en la app |
| **WorkManager** | Trabajo diferible y garantizado, puede ejecutarse más tarde |
| **AlarmManager** | Trabajo con hora exacta (tipo alarma de reloj) |
| **Foreground Service** | Trabajo largo que el usuario debe ver (descarga activa, música) |

✅ **Usa WorkManager para:**
- Subir fotos al servidor cuando haya WiFi
- Sincronizar datos con el servidor periódicamente
- Procesar imágenes (comprimir, aplicar filtros) antes de guardar
- Hacer copias de seguridad de los datos
- Enviar analíticas o logs en segundo plano

❌ **No uses WorkManager para:**
- Responder inmediatamente a la interacción del usuario → corrutinas
- Ejecutar algo exactamente a las 8:00 → AlarmManager
- Reproducir música en segundo plano → Foreground Service

La app con la que se practica es **Blur-O-Matic**: aplica un efecto de difuminado a una imagen. El proceso tiene tres pasos encadenados: limpiar temporales → difuminar → guardar resultado.

---

## Dependencias

```kotlin
implementation("androidx.work:work-runtime-ktx:2.9.0")
```

---

## `CoroutineWorker` — el Worker que usarás

Un `Worker` contiene la lógica del trabajo en segundo plano. El curso usa `CoroutineWorker` (en lugar de la clase base `Worker`) porque es compatible con corrutinas y `suspend`.

```kotlin
class BlurWorker(context: Context, params: WorkerParameters)
    : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Leer los datos de entrada
        val resourceUri = inputData.getString(KEY_IMAGE_URI)

        return try {
            // Hacer el trabajo real
            val resolver = applicationContext.contentResolver
            val bitmap = BitmapFactory.decodeStream(
                resolver.openInputStream(Uri.parse(resourceUri))
            )
            val output = blurBitmap(bitmap, applicationContext)
            val outputUri = writeBitmapToFile(applicationContext, output)

            // Devolver éxito con datos de salida
            val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())
            Result.success(outputData)

        } catch (throwable: Throwable) {
            Log.e(TAG, "Error al difuminar imagen", throwable)
            Result.failure()
        }
    }
}
```

### Valores de retorno de `doWork()`

| Resultado | Cuándo usarlo |
|-----------|--------------|
| `Result.success()` | El trabajo terminó correctamente |
| `Result.failure()` | El trabajo falló de forma irrecuperable |
| `Result.retry()` | El trabajo falló pero puede reintentarse (WorkManager lo reintentará con backoff exponencial) |

---

## Pasar datos entre Workers: `Data`

Los Workers se comunican a través del objeto `Data`, que es básicamente un mapa clave-valor que solo acepta tipos primitivos.

### Enviar datos de entrada al encolar

```kotlin
val blurRequest = OneTimeWorkRequestBuilder<BlurWorker>()
    .setInputData(
        workDataOf(KEY_IMAGE_URI to imageUri.toString())
    )
    .build()
```

### Leer datos de entrada dentro del Worker

```kotlin
val imageUri = inputData.getString(KEY_IMAGE_URI)
val blurLevel = inputData.getInt(KEY_BLUR_LEVEL, 1)  // 1 es el valor por defecto
```

### Devolver datos de salida

```kotlin
val outputData = workDataOf(KEY_OUTPUT_URI to outputUri.toString())
Result.success(outputData)
```

En una cadena de Workers, los datos de salida de uno se convierten en los datos de entrada del siguiente.

---

## `WorkRequest` — definir el trabajo

### Trabajo que se ejecuta una sola vez

```kotlin
val blurRequest = OneTimeWorkRequestBuilder<BlurWorker>()
    .setInputData(workDataOf(KEY_IMAGE_URI to imageUri))
    .addTag("blur")     // etiqueta opcional para filtrar/cancelar después
    .build()
```

### Trabajo periódico

```kotlin
val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
    repeatInterval     = 1,
    repeatIntervalTimeUnit = TimeUnit.HOURS
).build()
```

Restricción: el intervalo mínimo es **15 minutos** (limitación de Android para preservar batería).

---

## Restricciones (`Constraints`)

Las restricciones le dicen a WorkManager que solo ejecute el trabajo cuando se cumplan ciertas condiciones del dispositivo:

```kotlin
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)  // necesita Internet
    .setRequiresCharging(true)                       // solo si está cargando
    .setRequiresBatteryNotLow(true)                  // batería no baja
    .setRequiresStorageNotLow(true)                  // almacenamiento suficiente
    .build()

val uploadRequest = OneTimeWorkRequestBuilder<UploadWorker>()
    .setConstraints(constraints)
    .setInputData(workDataOf(KEY_IMAGE_URI to imageUri))
    .build()
```

Si las condiciones no se cumplen cuando se encola el trabajo, WorkManager lo guarda y espera pacientemente hasta que sí se cumplan. Si dejan de cumplirse mientras el trabajo se ejecuta, WorkManager lo pausa y lo reanuda cuando vuelvan.

---

## Encolar el trabajo

```kotlin
val workManager = WorkManager.getInstance(applicationContext)

// Trabajo simple
workManager.enqueue(blurRequest)

// Trabajo único con nombre (evita que se duplique si se encola dos veces)
workManager.enqueueUniqueWork(
    "blur_work",
    ExistingWorkPolicy.REPLACE,   // si ya hay uno con este nombre: REPLACE / KEEP / APPEND
    blurRequest
)

// Trabajo periódico único
workManager.enqueueUniquePeriodicWork(
    "daily_sync",
    ExistingPeriodicWorkPolicy.KEEP,   // mantiene el existente si ya hay uno
    syncRequest
)
```

---

## Encadenar Workers

La gran ventaja de WorkManager es poder encadenar Workers. El resultado de uno es la entrada del siguiente:

```kotlin
workManager
    .beginUniqueWork(
        IMAGE_MANIPULATION_WORK_NAME,
        ExistingWorkPolicy.REPLACE,
        OneTimeWorkRequestBuilder<CleanupWorker>().build()   // paso 1: limpia temp
    )
    .then(blurRequest)                                        // paso 2: difumina
    .then(
        OneTimeWorkRequestBuilder<SaveImageWorker>().build() // paso 3: guarda
    )
    .enqueue()
```

Para ejecutar en paralelo (y esperar a que terminen todos antes de continuar):
```kotlin
workManager
    .beginWith(listOf(trabajoA, trabajoB))  // A y B en paralelo
    .then(trabajoC)                          // C empieza cuando A y B terminan
    .enqueue()
```

---

## Observar el estado del trabajo

Desde el ViewModel puedes observar el estado del trabajo como un `Flow`:

```kotlin
val outputWorkInfo: Flow<WorkInfo?> =
    workManager
        .getWorkInfosForUniqueWorkFlow(IMAGE_MANIPULATION_WORK_NAME)
        .map { workInfoList -> workInfoList.firstOrNull() }
```

En el composable, recoges ese Flow y actúas según el estado:

```kotlin
val workInfo by viewModel.outputWorkInfo.collectAsState()

when (workInfo?.state) {
    WorkInfo.State.ENQUEUED  -> Text("Trabajo en cola...")
    WorkInfo.State.RUNNING   -> CircularProgressIndicator()
    WorkInfo.State.SUCCEEDED -> {
        val outputUri = workInfo?.outputData?.getString(KEY_OUTPUT_URI)
        // Mostrar el resultado
        Text("¡Hecho!")
    }
    WorkInfo.State.FAILED    -> Text("Error al procesar")
    WorkInfo.State.CANCELLED -> Text("Cancelado")
    else -> {}
}
```

### Estados posibles de un Worker

```
ENQUEUED → RUNNING → SUCCEEDED
                  ↘ FAILED
                  ↘ CANCELLED
BLOCKED  (esperando que termine otro Worker anterior en la cadena)
```

---

## Cancelar trabajo

```kotlin
// Cancelar por nombre único
workManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME)

// Cancelar por ID específico
workManager.cancelWorkById(request.id)

// Cancelar por etiqueta (todos los Workers con esa tag)
workManager.cancelAllWorkByTag("blur")

// Cancelar absolutamente todo (con cuidado)
workManager.cancelAllWork()
```

---

## El Inspector de tareas en segundo plano

Android Studio incluye el **Background Task Inspector** para monitorizar los Workers en tiempo real:

*View → Tool Windows → App Inspection → Background Task Inspector*

Desde ahí puedes ver:
- Todos los Workers activos y su estado
- Las restricciones de cada Worker
- Cuándo se ejecutó cada uno y cuánto tardó
- Los datos de entrada y salida

---

## Resumen visual del flujo completo de Blur-O-Matic

```
Usuario pulsa "Difuminar"
         │
         ▼
viewModel.applyBlur(blurLevel)
         │
         ▼
WorkManager encola la cadena:
┌─────────────────────────────────────────────────────┐
│ CleanupWorker → BlurWorker → SaveImageToFileWorker  │
│ (borra temp)    (difumina)    (guarda en galería)   │
└─────────────────────────────────────────────────────┘
         │
         ▼
Flow<WorkInfo> emite cambios de estado
         │
    RUNNING → CircularProgressIndicator()
         │
    SUCCEEDED → mostrar botón "Ver imagen"
                outputData["KEY_OUTPUT_URI"] = "content://..."
```

---

**→ Continúa con [[../U8-Views-Compose/00 - Unidad 8 Resumen|Unidad 8 — Views y Compose]]**
