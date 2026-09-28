<div align="center">

# 🎮 Campus Quest

### Explora tu campus como si fuera un RPG

Aplicación Android que **gamifica el reconocimiento del campus universitario**: recorre puntos de interés, escanea códigos QR, completa misiones, gana puntos e insignias y sube de nivel.

<br>
<br>

![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Room](https://img.shields.io/badge/Room-Offline%20First-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![minSdk](https://img.shields.io/badge/minSdk-29-orange?style=for-the-badge)
![Arquitectura](https://img.shields.io/badge/Arquitectura-MVVM-blueviolet?style=for-the-badge)

</div>

---

## 🎓 Contexto académico

> **Materia:** Aplicaciones Móviles Nativas
> **Periodo:** 2026 · 2
> **Plataforma:** Android nativo (Kotlin + Jetpack Compose)
> **Modalidad:** Proyecto de aplicación funcional con persistencia local

Campus Quest es un proyecto desarrollado para la asignatura de **Aplicaciones Móviles Nativas**, orientado a demostrar el uso de las herramientas modernas del ecosistema Android: interfaz declarativa con Compose, persistencia con Room, cámara con CameraX y una arquitectura limpia basada en MVVM.

---

## ✨ ¿Qué hace la app?

Campus Quest convierte el recorrido por la universidad en una experiencia de juego con estética **pixel art / retro 8 bits**:

| Módulo | Descripción |
|---|---|
| 🗺️ **Mapa del campus** | Mapa estilizado (sin Google Maps ni GPS) con los puntos de interés señalizados por categoría. |
| 🎯 **Misiones** | Retos asociados a cada punto de interés, con dificultad, puntos y tiempo estimado. Filtrables por *activas / completadas / todas*. |
| 📷 **Escáner QR** | Validación de llegada a un punto escaneando su código QR con la cámara (o ingresándolo a mano). |
| 🏅 **Insignias** | Cada misión completada otorga una insignia; pantalla de celebración al ganarla. |
| ⭐ **Progreso y nivel** | El puntaje acumulado sube el nivel del usuario y alimenta un ranking. |
| 👤 **Perfil y ajustes** | Datos del usuario, tema claro/oscuro y cierre de sesión. |
| 🛠️ **Gestión de misiones (CRUD)** | Rol *tutor*: crear, editar y eliminar misiones y puntos de interés. |

### 🔌 Funciona sin conexión

Toda la información (usuarios, puntos, misiones y progreso) se sirve desde **Room**. El escaneo de QR usa **ZXing en local** (sin ML Kit ni descarga de modelos), por lo que la app funciona **100 % offline**.

---

## 🧭 Flujo de pantallas

```
Splash ──▶ Login ──▶ Home (mapa) ──▶ Detalle de misión ──▶ Escáner QR ──▶ Insignia obtenida
             │          │
             ├─▶ Registro├─▶ Misiones (lista + filtros)
                        ├─▶ Insignias
                        └─▶ Perfil ──▶ Ajustes
                                   └─▶ Gestión de misiones (CRUD) ──▶ Formulario
```


---

## 🛠️ Tecnologías

| Categoría | Herramienta |
|---|---|
| **Lenguaje** | Kotlin 2.2.10 |
| **UI** | Jetpack Compose + Material 3 |
| **Navegación** | Navigation Compose |
| **Persistencia** | Room (SQLite) + esquema exportado |
| **Preferencias** | DataStore Preferences |
| **Cámara** | CameraX (core, camera2, lifecycle, view) |
| **Lectura de QR** | ZXing (core) |
| **Asincronía** | Kotlin Coroutines + Flow |
| **Arquitectura** | MVVM + repositorios + inyección de dependencias manual |
| **Tipografía** | Press Start 2P (títulos) · VT323 (cuerpo) |

---

## 🏛️ Arquitectura

Patrón **MVVM** con separación por capas y un contenedor de dependencias manual (`AppContainer`):

```
UI (Compose)  ──▶  ViewModel (StateFlow)  ──▶  Repository  ──▶  DAO (Room) / DataStore
   Screens            lógica de estado          reglas de negocio      fuentes de datos
```

- **UI:** pantallas `@Composable` sin estado propio; observan `StateFlow` del ViewModel.
- **ViewModel:** expone el estado con `stateIn` / `StateFlow` y sobrevive a rotaciones.
- **Repository:** `CampusRepository` y `AuthRepository` combinan y transforman los datos.
- **Data:** entidades, DAOs y `AppDatabase` (Room) + `UserPreferencesRepository` (DataStore).

### 📂 Estructura del proyecto

```
app/src/main/java/com/example/gamequest/
├── MainActivity.kt / CampusQuestApp.kt / AppContainer.kt   # núcleo y DI
├── data/
│   ├── local/            # Room: entidades, DAOs, AppDatabase, SeedData
│   ├── preferences/      # DataStore (tema, sesión)
│   └── repository/       # CampusRepository, AuthRepository
├── ui/
│   ├── navigation/       # Routes + NavHost
│   ├── theme/            # Color, Type (pixel art), Theme
│   ├── common/           # ViewModelFactory, SessionViewModel, BottomBar
│   ├── splash / auth / home / missions / scanner /
│   │   badges / profile / crud / settings           # pantallas + ViewModels
│   └── ...
└── util/                 # PasswordHasher, QrGenerator, QrAnalyzer
```

---

## 🗃️ Modelo de datos (Room)

| Entidad | Rol | Relaciones |
|---|---|---|
| `UsuarioEntity` | Cuentas de la app (estudiante / tutor) | 1 : N con progreso |
| `PuntoInteresEntity` | Lugares del campus (posición en el mapa, código QR) | 1 : N con misiones |
| `MisionEntity` | Retos asociados a un punto de interés | FK → punto de interés |
| `ProgresoMisionEntity` | Registro de misiones completadas por usuario | FK → usuario y misión |

Las claves foráneas usan `ON DELETE CASCADE` e índices en las columnas de relación.

---

## 👥 Roles

| Rol | Puede |
|---|---|
| 🧑‍🎓 **Estudiante** | Explorar el mapa, completar misiones, escanear QR, ganar insignias y ver su progreso. |
| 🧑‍🏫 **Tutor** | Todo lo anterior + **gestión CRUD** de misiones y puntos de interés. |

---

## 🚀 Cómo ejecutar

### Requisitos
- Android Studio (versión reciente con soporte para AGP 9)
- JDK 11+
- Un dispositivo o emulador con **Android 10 (API 29)** o superior
- Cámara (para el escáner QR; la app degrada de forma segura si no hay)

### Pasos

```bash
# 1. Clonar
git clone https://github.com/Erickelrojo-22/campus-quest-app.git
cd campus-quest-app

# 2. Compilar
./gradlew assembleDebug

# 3. Instalar en un dispositivo/emulador conectado
./gradlew installDebug
```

O ábrelo directamente en Android Studio y pulsa **Run ▶️**.

---

## ⚡ Optimizaciones de rendimiento

El proyecto incluye un paso de análisis y optimización de rendimiento:

- **R8 activado en release** (`isMinifyEnabled` + `isShrinkResources`) con reglas `keep` para Room, ZXing y CameraX → APK más liviano y arranque más rápido.
- **Menos recomposiciones en Compose**: memoización de colecciones derivadas (`remember`) y eliminación de escrituras de estado durante la fase de dibujo del mapa.
- **Flujos derivados eficientes**: se reemplazaron `combine` innecesarios por `map` en los ViewModels para no recalcular datos en cada pulsación del buscador o cambio de filtro.
- **Escáner QR parametrizado para gama baja**: CameraX limita el análisis a 640 × 480,
  conserva únicamente el último frame y ZXing procesa como máximo un frame cada 120 ms.
- **Listas con coste lineal**: el ranking usa el índice entregado por `itemsIndexed` en
  lugar de buscar la posición de cada usuario repetidamente durante la composición.

> ⚠️ Al activar R8 por primera vez, se recomienda validar un build de release (`./gradlew assembleRelease`) en dispositivo antes de publicar.

---

## 📚 Documentación adicional

En la carpeta [`docs/`](docs/) encontrarás:

- [`ROADMAP.md`](docs/ROADMAP.md) — plan de evolución del proyecto.
- [`BACKEND.md`](docs/BACKEND.md) — propuesta de backend para una futura sincronización en la nube.
- [`ISSUES.md`](docs/ISSUES.md) — incidencias de QA registradas.

---

<div align="center">

**Campus Quest** · Aplicaciones Móviles Nativas · Periodo 2026

Hecho con 💚 y Kotlin

</div>
