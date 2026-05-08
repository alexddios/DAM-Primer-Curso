# 🧭 Espacios de nombres en XML (Namespaces)

## Vocabularios XML

- Un vocabulario XML es el conjunto de etiquetas usadas en un determinado ámbito.
- Cada aplicación o sistema puede definir sus propias etiquetas.
- Las etiquetas solo tienen significado dentro de su vocabulario.

Ejemplo de dos vocabularios distintos con la misma etiqueta:

```Xml
<precio>15.50</precio>          <!-- tienda -->
<precio>2026-04-25</precio>     <!-- facturación -->
```

---

## Colisiones de nombres

- Se produce una colisión de nombres cuando dos vocabularios usan la misma etiqueta con distinto significado.
- El problema aparece al combinar datos de diferentes ámbitos en un mismo XML.
- Sin una forma de diferenciarlos, el documento resulta ambiguo.

---

## ¿Qué son los espacios de nombres?

- Los espacios de nombres (namespaces) permiten distinguir etiquetas con el mismo nombre.
- Hacen posible combinar varios vocabularios en un mismo documento.
- Son la solución estándar al problema de las colisiones de nombres.

---

## Declaración de un espacio de nombres

- Se declaran con el atributo `xmlns`.
- Asocian un prefijo a un identificador (URI).
- El prefijo se usa delante del nombre de la etiqueta.

```Xml
<tienda xmlns:t="http://ejemplo.com/tienda">
  <t:producto>
    <t:precio>15.50</t:precio>
  </t:producto>
</tienda>
```

- `t` es el prefijo
- La URI identifica el vocabulario (no tiene por qué existir como web)

---

## Uso de prefijos

- El prefijo identifica a qué vocabulario pertenece una etiqueta.
- Dos etiquetas con el mismo nombre pueden coexistir si tienen distinto prefijo.

```Xml
<doc xmlns:a="http://ejemplo.com/a" xmlns:b="http://ejemplo.com/b">
  <a:precio>15.50</a:precio>
  <b:precio>2026-04-25</b:precio>
</doc>
```

---

## Espacio de nombres por defecto

- También se puede definir un namespace por defecto.
- En ese caso, las etiquetas no necesitan prefijo.

```Xml
<libros xmlns="http://ejemplo.com/libros">
  <libro>
    <titulo>XML básico</titulo>
  </libro>
</libros>
```

Todas las etiquetas pertenecen al mismo vocabulario.

---

## Documentos con varios vocabularios

- Un documento XML puede mezclar varios vocabularios.
- Cada uno queda identificado por su prefijo o namespace.

Esto es habitual cuando:

- se combinan estándares distintos
- se integran datos de varias fuentes
- un esquema externo lo exige

---

## ¿Cuándo usar espacios de nombres?

✅ Úsalos cuando:

- el documento combina vocabularios distintos
- existe riesgo de colisiones
- un estándar (XSD, SOAP, SVG...) lo exige

❌ No son necesarios cuando:

- todo el documento pertenece a un único ámbito
- no hay ambigüedad

---

🔗 Relacionado:

- [[Jerarquía y buenas prácticas en XML]]
- [[Documentos bien formados y válidos]]
- [[Validación XML DTD y XSD]]

#xml #namespaces #vocabularios