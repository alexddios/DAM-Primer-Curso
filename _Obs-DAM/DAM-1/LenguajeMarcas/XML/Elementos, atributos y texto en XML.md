# 🧩 Elementos, atributos y texto en XML

## Elementos XML

- Los elementos representan las partes principales de la información.
- Se definen mediante etiquetas.
- Suelen reflejar conceptos importantes del dominio de datos.

Ejemplo:

```Xml
<alumno>
  <nombre>Ana</nombre>
  <edad>20</edad>
</alumno>
```

En este caso, `alumno`, `nombre` y `edad` son elementos.

---

## Etiquetas de apertura, cierre y vacías

### Etiquetas de apertura y cierre

La mayoría de los elementos tienen:

- una etiqueta de apertura `<elemento>`
- una etiqueta de cierre `</elemento>`

```Xml
<titulo>XML básico</titulo>
```

### Etiquetas vacías

Si un elemento no tiene contenido, puede escribirse como etiqueta vacía:

```Xml
<linea />
```

---

## Atributos XML

- Los atributos añaden información adicional a un elemento.
- Se escriben dentro de la etiqueta de apertura.
- Suelen usarse para datos:
    - cortos
    - no estructurados
    - identificativos (id, tipo, unidad…)

Ejemplo:

```Xml
<libro isbn="9781234567890">
  <titulo>XML práctico</titulo>
</libro>
```

Aquí, `isbn` es un atributo del elemento `libro`.

### ¿Elemento o atributo?

✅ Usar elementos cuando:

- la información tiene estructura
- puede crecer
- puede contener otros datos

✅ Usar atributos cuando:

- es un identificador
- es información breve
- no tiene estructura interna

---

## Contenido textual

- El texto es el valor contenido dentro de un elemento.
- Representa el dato propiamente dicho.

Ejemplo:

```Xml
<precio>19.99</precio>
```

Aquí, `19.99` es contenido textual.

---

## Combinación de elementos, atributos y texto

XML permite combinar los tres mecanismos en un mismo documento.

```Xml
<producto id="A1">
  <nombre>Ratón</nombre>
  <precio moneda="EUR">15.50</precio>
</producto>
```

---

## Buenas prácticas

- No mezclar información importante en atributos sin necesidad.
- Mantener coherencia: elementos del mismo tipo con la misma estructura.
- Usar nombres de etiquetas claros y significativos.

---

🔗 Relacionado:

- [[Estructura de un documento XML]]
- [[Jerarquía y buenas prácticas en XML]]

#xml #elementos #atributos #texto