# 🌐 Tema 5 - Sistemas Informáticos en Red - Resumen para Estudiar

---

## 📋 Índice



---

## 1. Redes Informáticas

### ¿Qué es una Red Informática?
Conjunto de **equipos o nodos** interconectados mediante medios de transmisión para compartir recursos e información.

### Medios de Transmisión

#### Cableados (Guiados)
| Medio | Características |
|-------|-----------------|
| **Par Trenzado** | El más usado en LAN. Conector RJ45. Tipos: UTP (sin apantallar), FTP (apantallamiento global), STP (cada par apantallado). Categorías: Cat5e, Cat6, Cat6a, Cat7 |
| **Coaxial** | Núcleo de cobre, uso antiguo. En desuso en LAN |
| **Fibra Óptica** | Pulsos de luz. Muy rápida, gran distancia, inmune a interferencias. Tipos: Monomodo (más distancia), Multimodo. FTTH (hasta el hogar), FTTP (hasta instalaciones) |

#### Inalámbricos (No guiados)
| Medio | Descripción | Ejemplo |
|-------|-------------|---------|
| **Radiofrecuencia** | Ondas de radio | WiFi, Bluetooth |
| **Microondas** | Frecuencias altas | Satélites |
| **Infrarrojos** | Corto alcance, línea vista | Mandos |

### Dispositivos de Red
| Dispositivo | Capa OSI | Función |
|-------------|----------|---------|
| **Tarjeta de Red (NIC)** | 2 | Permite conectar el equipo. Tiene MAC única (48 bits) |
| **Hub** | 1 | Reenvía a todos los puertos (en desuso) |
| **Switch** | 2 | Reenvía solo al puerto destino (usa tabla MAC) |
| **Router** | 3 | Conecta redes diferentes, decide rutas |
| **Punto de Acceso (AP)** | 2 | Conecta dispositivos WiFi a red cableada |
| **Módem** | - | Convierte digital ↔ analógico (ADSL/Fibra) |
| **Repetidor** | 1 | Amplifica señal para mayor distancia |
| **Bridge** | 2 | Conecta dos segmentos de red |
| **Firewall** | - | Filtra tráfico, protege la red |

### Dirección MAC
- **48 bits** representados en hexadecimal: `08:00:27:9B:AC:B3`
- Primeros 24 bits: fabricante (OUI)
- Últimos 24 bits: dispositivo

---

## 2. Protocolos y Estándares

### Definiciones
- **Protocolo**: Conjunto de reglas para comunicaciones entre dispositivos
- **Estándar**: Formalización normalizada de un protocolo

### Organismos de Normalización
| Sigla | Nombre completo | Descripción |
|-------|----------------|-------------|
| **IEEE** | Institute of Electrical and Electronics Engineers | Instituto de ingenieros eléctricos y electrónicos |
| **ISO** | International Organization for Standardization | Del griego *isos* = "igual" |
| **EIA** | Electronic Industries Alliance | Alianza de industrias electrónicas |
| **TIA** | Telecommunications Industry Association | Asociación de la industria de las telecomunicaciones |
| **ETSI** | European Telecommunications Standards Institute | Instituto europeo de normas de telecomunicaciones |
| **ANSI** | American National Standards Institute | Instituto americano de estándares nacionales |


## Estándares Principales

### IEEE 802.3 — Ethernet

- Estándar para redes **LAN cableadas**
- Variantes: Fast Ethernet, Gigabit Ethernet, 100 Gigabit Ethernet
- Control de acceso al medio: **CSMA/CD**
- Con switches y conexiones dúplex se resuelve el problema del acceso al medio

### IEEE 802.11 — WiFi

- Estándar para **redes inalámbricas**
- Control de acceso al medio: **CSMA/CA** (evita colisiones)

### IEEE 802.15 — WPAN

- Redes inalámbricas de **área personal**
- **802.15.1** → Bluetooth
- **802.15.4** → ZigBee (domótica, ondas de radio de baja frecuencia)

### EIA/TIA T568A y T568B

- Normas para **cables de redes LAN**

### Protocolos en desuso

- **IEEE 802.5** → Token Ring (paso de testigo en anillo)
- **IEEE 802.4** → Token Bus (paso de testigo en bus)

---

## 3. Tipos de Redes

### Clasificación por Dirección de Datos
| Tipo | Descripción |
|------|-------------|
| **Símplex / Unidireccional** | Los datos van solo en una dirección |
| **Half-duplex / Semidúplex** | Ambas direcciones, pero NO simultáneo |
| **Full-duplex / Dúplex** | Ambas direcciones de forma simultánea |

### Clasificación por Destinatarios
| Tipo | Descripción |
|------|-------------|
| **Unicast** (unidifusión) | Un usuario → otro usuario |
| **Multicast** (multidifusión) | Un usuario → varios usuarios |
| **Broadcast** (difusión) | Un usuario → todos los usuarios |

### Clasificación por Dimensión
| Tipo | Alcance | Estándar |
|------|---------|----------|
| **PAN** | ~10m (Bluetooth) | IEEE 802.15 |
| **LAN** | ~100m (hogar/empresa) | IEEE 802.3 / 802.11 |
| **MAN** | ~km (ciudad) | - |
| **WAN** | Continental | - |

### Clasificación por Privacidad
- **Pública**: Acceso libre (internet)
- **Privada**: Solo usuarios autorizados
- **VPN**: Red privada virtual sobre internet (túnel cifrado)

### Topologías de Red
| Topología | Descripción | Fallo |
|-----------|-------------|-------|
| **Bus** | Un bus común | Falla el bus = falla toda la red |
| **Estrella** | Todos al centro (switch) | Falla un nodo ≠ centro = red sigue |
| **Anillo** | Cada nodo conecta a 2 cercanos | Falla un nodo, red puede seguir |
| **Árbol** | Jerárquica | Falla nodo central = falla red |
| **Malla** | Varios caminos posibles | Muy robusta |
| **Totalmente conectada** | Todos con todos | Muy costosa |

---

## 4. Mapas Físicos y Lógicos

| Aspecto | Mapa Lógico | Mapa Físico |
|---------|-------------|-------------|
| **Muestra** | Flujo de datos, IPs | Ubicación real dispositivos |
| **Incluye** | IPs, subredes, hosts | Edificios, distancias, cableado |
| **Herramientas** | Packet Tracer, GNS3 | Visio, Draw, SmartDraw |

---

## 5. Modelos de Referencia

### Modelo OSI (7 capas)
| Capa | Nombre | Función | Protocolos | Dispositivo |
|------|--------|---------|------------|-------------|
| 7 | Aplicación | Interfaz usuario-aplicación | HTTP, FTP, DNS, DHCP | - |
| 6 | Presentación | Cifrado, compresión, formato | SSL/TLS, JPEG | - |
| 5 | Sesión | Gestión de sesiones | NetBIOS | - |
| 4 | Transporte | Conexión extremo a extremo | TCP, UDP, SCTP | - |
| 3 | Red | Enrutamiento, direcciones IP | IP, ICMP, ARP | **Router** |
| 2 | Enlace | Tramas, direcciones MAC | Ethernet, WiFi | **Switch, Bridge** |
| 1 | Física | Transmisión de bits | - | **Hub, Repetidor, Cable** |

**Nemónico** (de abajo arriba): **F**ísica, **E**nlace, **R**ed, **T**ransporte, **S**esión, **P**resentación, **A**plicación → "**Fer Tres Semanas Para Aprender**"

### Modelo TCP/IP (4 capas)
| Capa | Función | Protocolos |
|------|---------|------------|
| **Aplicación** | Datos del usuario | DNS, DHCP, HTTP, FTP, SSH, SMTP... |
| **Transporte** | Segmentación, confiabilidad | TCP, UDP, SCTP, TLS/SSL |
| **Internet** | Enrutamiento | IP, ARP, ICMP, IGMP |
| **Acceso a Red** | Transmisión física | Ethernet, WiFi, PPP |

### Comparación OSI vs TCP/IP
| TCP/IP | OSI |
|--------|-----|
| 4 capas (práctico) | 7 capas (teórico) |
| Aplicación = capas 7+6+5 de OSI | Capas separadas para presentación y sesión |
| Acceso a Red = capas 2+1 de OSI | Capas física y enlace separadas |

### Puertos de Red (16 bits: 0-65535)
| Rango | Tipo |
|-------|------|
| 0-1023 | Bien conocidos (reservados) |
| 1024-49151 | Registrados |
| 49152-65535 | Efímeros/dinámicos |

### Puertos Principales
| Puerto | Protocolo | Servicio |
|--------|-----------|---------|
| 21 | FTP | Transferencia archivos |
| 22 | SSH/SFTP | Acceso remoto seguro |
| 23 | Telnet | Acceso remoto (inseguro) |
| 25/587 | SMTP | Envío correo |
| 53 | DNS | Resolución nombres |
| 67/68 | DHCP | Asignación IP dinámica |
| 80 | HTTP | Web |
| 110 | POP3 | Recepción correo (descarga) |
| 143 | IMAP | Correo en servidor |
| 443 | HTTPS | Web segura |
| 445 | SMB | Compartir archivos |
| 3389 | RDP | Escritorio remoto Windows |
| 5900 | VNC | Escritorio remoto libre |

### Protocolos Importantes
| Protocolo | Puerto | Función |
|-----------|--------|---------|
| **TCP** | - | Orientado a conexión, fiable |
| **UDP** | - | Sin conexión, rápido |
| **HTTP/HTTPS** | 80/443 | Web (HTTPS = HTTP + SSL/TLS) |
| **FTP/SFTP/SCP** | 21/22 | Transferencia archivos |
| **SMTP/POP3/IMAP** | 25/110/143 | Correo electrónico |
| **DNS** | 53 | Traduce nombres a IP |
| **DHCP** | 67/68 | Asigna IPs automáticamente |
| **ARP** | - | IP → MAC |
| **ICMP** | - | Ping, mensajes control |

---

## 6. Direccionamiento IP

### IPv4 (32 bits)
- 4 octetos en decimal (0-255): `192.168.0.1`
- Total: ~4.300 millones de direcciones

### IPv6 (128 bits)
- 8 grupos de 4 hex: `15ba::20ef:2020:2200`
- Reglas abreviación: omitir ceros iniciales, `::` una sola vez

### Loopback
| Versión | Dirección |
|---------|-----------|
| IPv4 | `127.0.0.1` (rango `127.0.0.0/8`) |
| IPv6 | `::1` |
| Nombre | `localhost` |

### IPs Públicas vs Privadas
| Tipo | Descripción | Rango |
|------|-------------|-------|
| **Pública** | Visible en internet (ISP asigna) | Resto de rangos |
| **Privada** | Solo en LAN local | `10.x.x.x`, `172.16-31.x.x`, `192.168.x.x` |

**NAT**: Traduce IPs privadas a una pública compartida (evita agotamiento IPv4).

### Clases IPv4
| Clase | Intervalo | Bits Red | Bits Host | Máscara | Hosts |
|-------|-----------|----------|-----------|---------|-------|
| **A** | 0-127 | 8 | 24 | 255.0.0.0 (/8) | 16.777.214 |
| **B** | 128-191 | 16 | 16 | 255.255.0.0 (/16) | 65.534 |
| **C** | 192-223 | 24 | 8 | 255.255.255.0 (/24) | 254 |
| **D** | 224-239 | - | - | - | Multicast |
| **E** | 240-255 | - | - | - | Experimental |

### Máscara de Subred
Indica qué parte es red (bits `1`) y qué parte es host (bits `0`).

**Notaciones equivalentes:**
- Decimal: `255.255.255.0`
- CIDR: `/24`

**Calcular dirección de red (AND bit a bit):**
```
IP:       192.168.0.32   → 11000000.10101000.00000000.00100000
Máscara:  255.255.255.0  → 11111111.11111111.11111111.00000000
─────────────────────────────────────────────────────────────────
Red:      192.168.0.0    → 11000000.10101000.00000000.00000000
```

### Número de Hosts Válidos
```
Hosts = 2^n - 2   (n = bits de host)
```
Se restan 2: dirección de red (todo 0s) y broadcast (todo 1s).

### Subnetting (División en Subredes)
Tomar bits de host y añadirlos a red para crear más subredes.

**Ejemplo: Dividir 192.168.0.0/24 en 4 subredes (2 bits prestados → /26)**

| Subred | Red | Rango Válido | Broadcast |
|--------|-----|--------------|-----------|
| 1 | 192.168.0.0/26 | .1 - .62 | .63 |
| 2 | 192.168.0.64/26 | .65 - .126 | .127 |
| 3 | 192.168.0.128/26 | .129 - .190 | .191 |
| 4 | 192.168.0.192/26 | .193 - .254 | .255 |

### Puerta de Enlace (Gateway)
- IP del router que da salida a internet
- En domésticas suele ser `192.168.0.1` o `192.168.1.1`

---

## 7. DHCP y DNS

### DHCP (Dynamic Host Configuration Protocol)
Asigna **automáticamente** IPs, máscara, gateway y DNS a los equipos.

| Situación | Recomendación |
|-----------|---------------|
| Servidores, impresoras | IP estática |
| PCs usuario, móviles | DHCP |

### DNS (Domain Name System)
Traduce nombres de dominio (`google.com`) a direcciones IP (`142.250.185.46`).

**DNS públicos conocidos:**
- Google: `8.8.8.8` / `8.8.4.4`
- Cloudflare: `1.1.1.1` / `1.0.0.1`

---

## 8. Conexión

### Ethernet (IEEE 802.3)
| Estándar | Velocidad |
|----------|-----------|
| Ethernet | 10 Mbps |
| Fast Ethernet | 100 Mbps |
| Gigabit Ethernet | 1 Gbps |
| 10 Gigabit Ethernet | 10 Gbps |

**Nomenclatura:** `VelocidadBase-TipoCable` (T/TX = par trenzado, FX/LX = fibra)

### WiFi (IEEE 802.11)
| Estándar | Nombre | Banda | Velocidad |
|----------|---------|-------|-----------|
| 802.11a | - | 5 GHz | 54 Mbps |
| 802.11b | - | 2.4 GHz | 11 Mbps |
| 802.11g | - | 2.4 GHz | 54 Mbps |
| 802.11n | WiFi 4 | 2.4/5 GHz | 450 Mbps |
| 802.11ac | WiFi 5 | 5 GHz | 3.5 Gbps |
| 802.11ax | WiFi 6/6E | 2.4/5/6 GHz | 9.6 Gbps |

### Seguridad WiFi
| Protocolo | Seguridad |
|-----------|-----------|
| **WEP** | ❌ Débil (clave estática) |
| **WPA** | ✅ Buena (clave dinámica 256 bits) |
| **WPA2** | ✅✅ Mejor (cifrado AES) |
| **WPA3** | ✅✅✅ Recomendado (cifrado por dispositivo) |

**Medidas adicionales:** Filtrado MAC, ocultar SSID, cambiar credenciales por defecto, desactivar WPS.

### Conexión a Internet
| Tipo | Descripción |
|------|-------------|
| **Fibra óptica** | La más usada actualmente |
| **ADSL/DSL** | En desuso, siendo reemplazada |
| **Móvil (4G/5G)** | 4G: ~150 Mbps, 5G: ~10 Gbps |
| **WiMAX** | Inalámbrico para zonas sin fibra |

### WAN (Wide Area Network)
Redes de gran alcance (ciudades, países, continentes).
- Protocolos actuales: **MPLS**, **Ethernet por fibra**
- Obsoletos: X.25, Frame Relay, ATM

### Proxy
Intermediario entre clientes e internet.
- **Funciones:** anonimato, control acceso, filtrado, caché
- **Tipos:** Web, Caché, Inverso, NAT, Transparente

---

## 9. Ejercicios Resueltos

### Ejercicio 1: Calcular dirección de red
**Dato:** IP = `192.168.56.1`, Máscara = `255.255.255.0`
```
   192.168.56.1   → 11000000.10101000.00111000.00000001
AND
   255.255.255.0  → 11111111.11111111.11111111.00000000
─────────────────────────────────────────────────────────
   192.168.56.0   → 11000000.10101000.00111000.00000000
```
**Resultado:** Red = `192.168.56.0/24`

### Ejercicio 2: Rango y broadcast de 172.16.0.0/16
```
Primer host: 172.16.0.1
Último host: 172.16.255.254
Broadcast:   172.16.255.255
```

### Ejercicio 3: ¿Pertenece a la red 192.168.20.0/22?
Máscara /22 = `255.255.252.0` → Rango: `192.168.20.1` - `192.168.23.254`

| IP | ¿Pertenece? |
|----|-------------|
| `192.160.20.5` | ❌ No |
| `192.168.22.5` | ✅ Sí |
| `192.168.24.5` | ❌ No |

### Ejercicio 4: Subnetting 192.168.10.0/24 para 60 hosts
**Necesita:** 2^6 = 64 ≥ 60+2 → 6 bits host → /26 (máscara 255.255.255.192)

| Subred | Red | Rango | Broadcast |
|--------|-----|-------|-----------|
| 1 | 192.168.10.0/26 | .1-.62 | .63 |
| 2 | 192.168.10.64/26 | .65-.126 | .127 |
| 3 | 192.168.10.128/26 | .129-.190 | .191 |
| 4 | 192.168.10.192/26 | .193-.254 | .255 |

---

## 📌 Conceptos Clave para Recordar
- **Protocolo** = reglas de comunicación
- **Estándar** = normalización de un protocolo
- **MAC** = dirección física única (48 bits)
- **IP** = dirección lógica (32 bits IPv4 / 128 bits IPv6)
- **Máscara** = separa red de host (AND para calcular red)
- **Gateway** = puerta de salida a internet
- **DHCP** = asigna IPs automáticamente
- **DNS** = traduce nombres a IPs
- **Subnetting** = dividir red en subredes (bits prestados)
- **OSI** = 7 capas teóricas | **TCP/IP** = 4 capas prácticas

---
*Resumen generado para estudio e impresión - Tema 5 Sistemas Informáticos en Red*
