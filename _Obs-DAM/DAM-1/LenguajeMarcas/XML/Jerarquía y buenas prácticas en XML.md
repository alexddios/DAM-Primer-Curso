# 🌳 Jerarquía y buenas prácticas en XML
## Jerarquía de elementos

- Los documentos XML organizan la información de forma jerárquica.
- Esta jerarquía se puede representar como un árbol.
- Cada elemento puede contener otros elementos.

Ejemplo:

```Xml
<alumnos>
  <alumno>
    <nombre>Ana</nombre>
  </alumno>
  <alumno>
    <nombre>Luis</nombre>
  </alumno>
</alumnos>
```

---

## Relaciones entre nodos

En un documento XML, los nodos se relacionan entre sí:

- Nodo padre: elemento que contiene a otros.
- Nodo hijo: elemento contenido dentro de otro.
- Nodos hermanos: elementos que comparten el mismo padre.

Ejemplo:

```Xml
<alumno>
  <nombre>Ana</nombre>
  <edad>20</edad>
</alumno>
```

- `alumno` → padre
- `nombre` y `edad` → hijos
- `nombre` y `edad` → hermanos

---

## Colecciones de elementos

Es muy habitual encontrar colecciones de elementos del mismo tipo.

- Se usa un elemento contenedor.
- Dentro se repite el mismo tipo de elemento.

Ejemplo:

```Xml
<libros>
  <libro>
    <titulo>XML básico</titulo>
  </libro>
  <libro>
    <titulo>XML avanzado</titulo>
  </libro>
</libros>
```

Este patrón es muy común en:

- listas
- catálogos
- registros

---

## Coherencia estructural 

- Los elementos del mismo tipo deben tener la misma estructura.
- Esto facilita:
    - el procesamiento automático
    - la validación
    - la comprensión del documento

❌ Mala práctica:

```Xml
<alumno><nombre>Ana</nombre></alumno>
<alumno><edad>20</edad></alumno>
```

✅ Buena práctica:

```Xml
<alumno>
  <nombre>Ana</nombre>
  <edad>20</edad>
</alumno>
```

---

## Estructura lógica del documento

Un documento XML puede ser:

- sintácticamente correcto pero
- lógicamente mal diseñado

Buenas prácticas de diseño:

- Reflejar relaciones reales entre los datos.
- No crear jerarquías innecesarias.
- Usar contenedores cuando haya listas.

---

## Buenas prácticas generales

- Usar nombres de etiquetas claros y consistentes.
- Mantener estructuras regulares.
- Evitar mezclar distintos significados en un mismo elemento.
- Pensar en cómo se procesará el XML (lectura, validación, consultas).

---

🔗 Relacionado:

- [[Elementos, atributos y texto en XML]]
- [[Espacios de nombres en XML]]
- [[Documentos bien formados y válidos]]

#xml #jerarquia #buenas-practicas