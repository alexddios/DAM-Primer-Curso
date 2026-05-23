# Unidad 7 — WorkManager

**← [[../U6-Persistencia/00 - Unidad 6 Resumen|Unidad 6]]** | **→ [[../U8-Views-Compose/00 - Unidad 8 Resumen|Unidad 8]]**

Duración: ~3 horas | 1 ruta de aprendizaje

---

## ¿Qué es WorkManager y para qué sirve?

WorkManager es la API recomendada de Jetpack para programar **trabajo en segundo plano garantizado**: tareas que deben ejecutarse aunque el usuario salga de la app, cierre la app, o incluso reinicie el dispositivo.

### ¿Cuándo usar WorkManager?

La clave está en la palabra **garantizado**. WorkManager asegura que el trabajo se ejecutará, aunque no necesariamente de inmediato.

✅ **Úsalo para:**
- Subir fotos al servidor cuando haya WiFi disponible
- Hacer una copia de seguridad periódica de los datos
- Sincronizar datos con el servidor en segundo plano
- Procesar imágenes (comprimir, filtrar, redimensionar) antes de guardar
- Enviar analíticas o logs cuando haya conexión

❌ **No uses WorkManager para:**
- Trabajo que debe ejecutarse *ahora mismo* mientras el usuario está en la app → usa **corrutinas**
- Trabajo con hora exacta (ej: alarma a las 8:00) → usa **AlarmManager**
- Trabajo que el usuario debe ver en tiempo real (notificaciones push) → usa **Firebase Cloud Messaging**

### La app del curso: Blur-O-Matic

La app con la que se practica difumina (*blur*) una imagen. El proceso tiene varios pasos:
1. Limpiar archivos temporales antiguos
2. Difuminar la imagen (puede ser un proceso lento)
3. Guardar el resultado final

Estos pasos se encadenan en Workers de WorkManager.

---

## Conceptos clave de WorkManager

### `Worker` / `CoroutineWorker`

Un `Worker` es la unidad de trabajo. Contiene el código que se ejecuta en segundo plano. El método `doWork()` es donde va la lógica:

```kotlin
class BlurWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val resourceUri = inputData.getString(KEY_IMAGE_URI)

        return try {
            // hacer el trabajo real
            val bitmap = BitmapFactory.decodeStream(
                applicationContext.contentResolver.openInputStream(Uri.parse(resourceUri))
            )
            val output = blurBitmap(bitmap, applicationContext)
            val outputUri = writeBitmapToFile(applicationContext, output)

            // devolver éxito con datos de salida
            val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())
            Result.success(outputData)

        } catch (throwable: Throwable) {
            Result.failure()  // el trabajo falló
        }
    }
}
```

Valores de retorno de `doWork()`:
- `Result.success()` — el trabajo terminó correctamente
- `Result.failure()` — el trabajo falló de forma irrecuperable
- `Result.retry()` — el trabajo falló, reintentarlo más tarde

> Usamos `CoroutineWorker` (en lugar de `Worker`) porque es compatible con corrutinas y permite usar funciones `suspend` dentro de `doWork()`.

### `WorkRequest` — definir el trabajo a encolar

```kotlin
// Trabajo que se ejecuta UNA vez
val blurRequest = OneTimeWorkRequestBuilder<BlurWorker>()
    .setInputData(workDataOf(KEY_IMAGE_URI to imageUri))  // datos de entrada
    .build()

// Trabajo PERIÓDICO (se repite)
val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
    repeatInterval = 1,
    repeatIntervalTimeUnit = TimeUnit.HOURS  // cada hora
).build()
```

### Pasar datos entre Workers: `Data`

Los Workers pueden recibir datos de entrada y producir datos de salida mediante el objeto `Data`:

```kotlin
// Crear datos de entrada al encolar
val inputData = workDataOf(
    KEY_IMAGE_URI to "content://media/...",
    KEY_BLUR_LEVEL to 3
)

// En el Worker, leer los datos de entrada
val imageUri = inputData.getString(KEY_IMAGE_URI)
val blurLevel = inputData.getInt(KEY_BLUR_LEVEL, 1)  // 1 es el valor por defecto

// Devolver datos de salida
Result.success(workDataOf(KEY_OUTPUT_URI to outputUri.toString()))
```

Limitación: `Data` solo acepta tipos primitivos y sus arrays. No puedes pasar objetos complejos directamente.

### Restricciones (`Constraints`)

Puedes indicarle a WorkManager que solo ejecute el trabajo si se cumplen ciertas condiciones:

```kotlin
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)  // necesita conexión a Internet
    .setRequiresCharging(true)                       // solo si está cargando
    .setRequiresBatteryNotLow(true)                  // batería no baja
    .setRequiresStorageNotLow(true)                  // almacenamiento suficiente
    .build()

val uploadRequest = OneTimeWorkRequestBuilder<UploadWorker>()
    .setConstraints(constraints)
    .build()
```

Si las condiciones no se cumplen cuando se encola el trabajo, WorkManager espera hasta que sí se cumplan. Si se deja de cumplir mientras se ejecuta, WorkManager pausa y reanuda cuando vuelven a cumplirse.

### Encolar el trabajo

```kotlin
// Trabajo simple
WorkManager.getInstance(context).enqueue(blurRequest)

// Trabajo único con nombre (evita duplicados)
WorkManager.getInstance(context).enqueueUniqueWork(
    "blur_work",                           // nombre único
    ExistingWorkPolicy.REPLACE,            // qué hacer si ya existe: REPLACE, KEEP, APPEND
    blurRequest
)

// Trabajo periódico único
WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "sync_work",
    ExistingPeriodicWorkPolicy.KEEP,       // mantiene el existente si ya hay uno
    syncRequest
)
```

### Encadenar Workers

WorkManager permite ejecutar Workers en **secuencia** o en **paralelo**:

```kotlin
WorkManager.getInstance(context)
    // Paso 1: limpiar archivos temporales (trabajo único)
    .beginUniqueWork(
        IMAGE_MANIPULATION_WORK_NAME,
        ExistingWorkPolicy.REPLACE,
        OneTimeWorkRequestBuilder<CleanupWorker>().build()
    )
    // Paso 2: difuminar la imagen (en secuencia)
    .then(blurBuilder.build())
    // Paso 3: guardar el resultado (en secuencia)
    .then(OneTimeWorkRequestBuilder<SaveImageToFileWorker>().build())
    .enqueue()
```

Para ejecutar en paralelo:
```kotlin
WorkManager.getInstance(context)
    .beginWith(listOf(trabajoA, trabajoB))  // A y B se ejecutan a la vez
    .then(trabajoC)                          // C empieza cuando A y B terminan
    .enqueue()
```

### Observar el estado del trabajo

```kotlin
// En el ViewModel
val outputWorkInfo: Flow<WorkInfo?> =
    workManager.getWorkInfosForUniqueWorkFlow(IMAGE_MANIPULATION_WORK_NAME)
        .map { workInfos -> workInfos.firstOrNull() }

// En el Composable
val workInfo by viewModel.outputWorkInfo.collectAsState()

when (workInfo?.state) {
    WorkInfo.State.RUNNING    -> CircularProgressIndicator()
    WorkInfo.State.SUCCEEDED  -> {
        val outputUri = workInfo?.outputData?.getString(KEY_IMAGE_URI)
        // mostrar la imagen resultado
    }
    WorkInfo.State.FAILED     -> Text("Error al procesar la imagen")
    else -> { /* ENQUEUED, BLOCKED, CANCELLED */ }
}
```

### Cancelar trabajo

```kotlin
// Cancelar por nombre único
workManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME)

// Cancelar por ID
workManager.cancelWorkById(workRequest.id)

// Cancelar todo
workManager.cancelAllWork()
```

### El Inspector de tareas en segundo plano

Android Studio tiene el **Background Task Inspector** para ver el estado de los Workers en tiempo real:

*View → Tool Windows → App Inspection → Background Task Inspector*

Desde ahí puedes ver:
- Todos los Workers encolados y su estado
- Las restricciones de cada Worker
- El historial de ejecuciones

---

## Resumen visual del flujo completo

```
Usuario pulsa "Difuminar"
        │
        ▼
  ViewModel llama a
  WorkManager.beginUniqueWork()
        │
        ▼
  ┌─────────────────────────────────────────────┐
  │              WorkManager                     │
  │                                              │
  │  CleanupWorker ──► BlurWorker ──► SaveWorker │
  │  (limpia temp)    (difumina)     (guarda)    │
  └─────────────────────────────────────────────┘
        │
        ▼
  WorkInfo.State.SUCCEEDED
  outputData["KEY_IMAGE_URI"] = "content://..."
        │
        ▼
  UI muestra el resultado
```

---

**→ Continúa con [[../U8-Views-Compose/00 - Unidad 8 Resumen|Unidad 8 — Views y Compose]]**
