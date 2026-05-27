# Ruta 1 — Introducción a SQL

**← [[U6-Persistencia|Unidad 6]]** | **→ [[02 - Room|Ruta 2]]**

---

## ¿Qué es una base de datos relacional?

Una **base de datos relacional** organiza los datos en **tablas**. Cada tabla tiene:
- **Columnas**: los atributos o campos (como las columnas de una hoja de cálculo)
- **Filas**: cada registro individual

Ejemplo de tabla `items` de una app de inventario:

| id | name | price | quantity |
|----|------|-------|---------|
| 1  | Cuaderno | 2.50 | 30 |
| 2  | Bolígrafo | 0.80 | 120 |
| 3  | Goma | 0.50 | 55 |

La columna `id` es la **clave primaria** (*primary key*): identifica de forma única cada fila. Ninguna fila puede tener el mismo `id`.

Android usa **SQLite**, una base de datos relacional que funciona directamente en el dispositivo, sin necesidad de servidor externo. Es un único archivo en el almacenamiento del teléfono.

---

## SQL — el lenguaje de las bases de datos

SQL (*Structured Query Language*) es el lenguaje estándar para interactuar con bases de datos relacionales. Aunque Room te abstrae de escribir SQL directamente (lo verás en la Ruta 2), es importante entender qué hace Room por debajo.

### Crear una tabla: `CREATE TABLE`

```sql
CREATE TABLE items (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    name     TEXT    NOT NULL,
    price    REAL    NOT NULL DEFAULT 0.0,
    quantity INTEGER NOT NULL DEFAULT 0
);
```

Restricciones más comunes:
- `PRIMARY KEY`: clave primaria, identifica cada fila
- `AUTOINCREMENT`: el valor del `id` se asigna automáticamente y se incrementa (1, 2, 3...)
- `NOT NULL`: el campo no puede estar vacío
- `DEFAULT valor`: valor que se usa si no se especifica al insertar
- `UNIQUE`: no puede haber dos filas con el mismo valor en esta columna

Tipos de datos en SQLite:

| Tipo SQLite | Equivalente en Kotlin |
|------------|----------------------|
| `INTEGER` | `Int`, `Long`, `Boolean` |
| `REAL` | `Double`, `Float` |
| `TEXT` | `String` |
| `BLOB` | `ByteArray` |

### Insertar datos: `INSERT INTO`

```sql
INSERT INTO items (name, price, quantity)
VALUES ('Cuaderno', 2.50, 30);

-- No incluimos id porque es AUTOINCREMENT, se asigna solo
```

### Leer datos: `SELECT`

```sql
-- Todos los campos de todas las filas
SELECT * FROM items;

-- Solo algunos campos
SELECT name, price FROM items;

-- Con filtro (WHERE)
SELECT * FROM items WHERE quantity > 20;

-- Varios filtros combinados
SELECT * FROM items WHERE price < 1.0 AND quantity > 50;

-- Ordenar el resultado
SELECT * FROM items ORDER BY price ASC;    -- ascendente (de menor a mayor)
SELECT * FROM items ORDER BY name DESC;    -- descendente (de mayor a menor)

-- Limitar el número de resultados
SELECT * FROM items LIMIT 5;

-- Buscar texto que contiene algo (LIKE)
SELECT * FROM items WHERE name LIKE '%bolí%';  -- % es comodín
```

### Actualizar datos: `UPDATE`

```sql
UPDATE items
SET quantity = 100
WHERE id = 2;

-- Actualizar varios campos a la vez
UPDATE items
SET price = 0.90, quantity = 150
WHERE name = 'Bolígrafo';
```

⚠️ **Advertencia importante**: si olvidas el `WHERE`, el `UPDATE` modifica **todas las filas** de la tabla:
```sql
UPDATE items SET quantity = 0;   -- PELIGRO: pone a 0 el stock de TODO
```

### Eliminar datos: `DELETE`

```sql
DELETE FROM items WHERE id = 3;

-- También afecta a todas las filas si no pones WHERE
DELETE FROM items;   -- PELIGRO: vacía la tabla entera
```

### Funciones de agregación

```sql
-- Contar filas
SELECT COUNT(*) FROM items;
SELECT COUNT(*) FROM items WHERE price < 1.0;

-- Suma
SELECT SUM(quantity) FROM items;

-- Media
SELECT AVG(price) FROM items;

-- Máximo y mínimo
SELECT MAX(price) FROM items;
SELECT MIN(quantity) FROM items;
```

### Filtrar grupos: `GROUP BY`

```sql
-- Cuántos items hay por rango de precio
SELECT
    CASE
        WHEN price < 1.0 THEN 'Barato'
        WHEN price < 3.0 THEN 'Medio'
        ELSE 'Caro'
    END AS categoria,
    COUNT(*) AS total
FROM items
GROUP BY categoria;
```

---

## Ejercicios de práctica de SQL

Imagina esta tabla `schedule` de una app de horarios de autobús:

| id | stop_name | arrival_time | destination |
|----|-----------|-------------|-------------|
| 1 | Estación Central | 08:00 | Norte |
| 2 | Plaza Mayor | 08:15 | Norte |
| 3 | Estación Central | 10:00 | Sur |
| 4 | Universidad | 10:30 | Sur |

**1.** Obtén todas las paradas con destino "Norte":
```sql
SELECT * FROM schedule WHERE destination = 'Norte';
```

**2.** Obtén los nombres de las paradas ordenados alfabéticamente:
```sql
SELECT stop_name FROM schedule ORDER BY stop_name ASC;
```

**3.** ¿Cuántas paradas hay en total?
```sql
SELECT COUNT(*) FROM schedule;
```

**4.** Obtén todas las paradas que lleguen antes de las 10:00:
```sql
SELECT * FROM schedule WHERE arrival_time < '10:00';
```

---

**→ Continúa con [[02 - Room|Ruta 2 — Room: base de datos local]]**
