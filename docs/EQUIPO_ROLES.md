# 👥 División de Trabajo y Delimitación de Roles — Campus Quest

> **IMPORTANTE PARA AGENTES DE IA (ANTIGRAVITY / COPILOT / CLAUDE / CHATGPT) Y DESARROLLADORES:**
> Este proyecto está dividido formalmente entre **4 integrantes**. Cada integrante tiene áreas de responsabilidad y propiedad de código bien delimitadas para evitar conflictos de ramas en Git (`merge conflicts`) y sobreescrituras accidentales.
> 
> **Si eres un agente de IA trabajando para otro integrante, consulta esta guía antes de proponer cambios para no modificar las áreas asignadas al Integrante 1 (UI/UX).**

---

## 🗺️ Mapa de Responsabilidades y Propiedad del Código

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       ARQUITECTURA DE CAMPUS QUEST                          │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
     ┌─────────────────────────────────┼─────────────────────────────────┐
     │                                 │                                 │
┌────▼────────────────────────┐ ┌──────▼────────────────────────┐ ┌──────▼────────────────────────┐
│     INTEGRANTE 1 (JHONNY)   │ │         INTEGRANTE 2          │ │         INTEGRANTE 3          │
│   UI/UX, Diseño Visual,     │ │    Panel Web Admin (Web)      │ │   Backend, Base de Datos,     │
│   Animaciones & A11y        │ │    Catálogo QR y Docentes     │ │   APIs Cloud & Seguridad      │
└────┬────────────────────────┘ └──────┬────────────────────────┘ └──────┬────────────────────────┘
     │                                 │                                 │
     └─────────────────────────────────┼─────────────────────────────────┘
                                       │
                        ┌──────────────▼──────────────┐
                        │        INTEGRANTE 4         │
                        │    Core Android, Offline,   │
                        │      CameraX & QA/Tests     │
                        └─────────────────────────────┘
```

---

## 🎨 INTEGRANTE 1: Líder de UI/UX, Diseño Visual y Accesibilidad (Frontend Móvil)
* **Responsable**: Jhonny ([@JhonnyCM](https://github.com/JhonnyCM))
* **Área Exclusiva**: Experiencia de usuario (UX), diseño visual (UI), componentes Compose, animaciones, accesibilidad y retroalimentación sensorial.

### 🚫 Regla de No Interferencia para otros Agentes:
> **Los agentes de IA de los demás compañeros NO deben modificar, sobreescribir ni rediseñar los archivos de UI y temas** sin coordinar previamente con el Integrante 1. Esto incluye layouts de Compose, paletas de colores, estilos RPG, animaciones de personajes o tipografías.

### 📂 Archivos bajo su propiedad:
* `app/src/main/java/com/example/gamequest/ui/theme/*` (Colores, tipografías, formas y estilos retro).
* `app/src/main/java/com/example/gamequest/ui/home/*` ([`HomeScreen.kt`](file:///C:/Users/jonni/AndroidStudioProjects/campus-quest-app/app/src/main/java/com/example/gamequest/ui/home/HomeScreen.kt), [`CampusMapView.kt`](file:///C:/Users/jonni/AndroidStudioProjects/campus-quest-app/app/src/main/java/com/example/gamequest/ui/home/CampusMapView.kt), Canvas 2.5D, brújula, pines y placas).
* `app/src/main/java/com/example/gamequest/ui/missions/*` ([`MissionsScreen.kt`](file:///C:/Users/jonni/AndroidStudioProjects/campus-quest-app/app/src/main/java/com/example/gamequest/ui/missions/MissionsScreen.kt), [`MissionDetailScreen.kt`](file:///C:/Users/jonni/AndroidStudioProjects/campus-quest-app/app/src/main/java/com/example/gamequest/ui/missions/MissionDetailScreen.kt)).
* `app/src/main/java/com/example/gamequest/ui/badges/*` ([`BadgesScreen.kt`](file:///C:/Users/jonni/AndroidStudioProjects/campus-quest-app/app/src/main/java/com/example/gamequest/ui/badges/BadgesScreen.kt), [`BadgeEarnedScreen.kt`](file:///C:/Users/jonni/AndroidStudioProjects/campus-quest-app/app/src/main/java/com/example/gamequest/ui/badges/BadgeEarnedScreen.kt)).
* `app/src/main/java/com/example/gamequest/ui/profile/*` ([`ProfileScreen.kt`](file:///C:/Users/jonni/AndroidStudioProjects/campus-quest-app/app/src/main/java/com/example/gamequest/ui/profile/ProfileScreen.kt), [`LeaderboardDialog.kt`](file:///C:/Users/jonni/AndroidStudioProjects/campus-quest-app/app/src/main/java/com/example/gamequest/ui/profile/LeaderboardDialog.kt), [`AdventurerCardDialog.kt`](file:///C:/Users/jonni/AndroidStudioProjects/campus-quest-app/app/src/main/java/com/example/gamequest/ui/profile/AdventurerCardDialog.kt), [`AvatarEditorSheet.kt`](file:///C:/Users/jonni/AndroidStudioProjects/campus-quest-app/app/src/main/java/com/example/gamequest/ui/profile/AvatarEditorSheet.kt)).
* `app/src/main/java/com/example/gamequest/ui/components/*` (Sprites animados Dude/Owlet, animaciones de caminar, partículas).
* `app/src/main/java/com/example/gamequest/ui/navigation/MainTabsScreen.kt` (Gestos de swipe entre pestañas, barras responsivas).

### 🎯 Tareas Principales:
1. **Diseño Visual e Identidad RPG**: Mantener la estética retro pixel art consistente, contrastes institucionales y efectos visuales atractivos.
2. **Experiencia de Usuario (UX) Responsiva**: Adaptar las pantallas para cualquier resolución, móviles compactos, orientación horizontal y tabletas.
3. **Accesibilidad Universal (A11y)**: Semantics en Compose, compatibilidad completa con TalkBack, reducción de movimiento y áreas táctiles de 48dp+.
4. **Micro-interacciones y Feedback Sensorial**: Gestión de efectos de sonido (`LocalSoundManager`) y vibración háptica táctil.

---

## 💻 INTEGRANTE 2: Desarrollador Frontend Web (Panel de Administración)
* **Área de Enfoque**: Plataforma web de escritorio para docentes y administradores universitarios.
* **Repositorio / Ubicación**: Repositorio web independiente o subdirectorio `web-admin/` (fuera del código Android nativo).

### 🎯 En qué debe enfocarse su agente:
1. **Módulo CRUD de Misiones**:
   - Crear, editar, archivar y listar misiones del campus con validación de campos.
   - Selector de puntos de interés asociados, tiempo estimado, dificultad y emojis de insignias.
2. **Módulo de Puntos de Interés del Campus**:
   - Registrar facultades, bibliotecas, laboratorios y oficinas administrativas con coordenadas relativas y horarios.
3. **Generador y Descargador de Códigos QR**:
   - Generar códigos QR oficiales para cada punto del campus.
   - Opción para exportar o imprimir en PDF/PNG con diseño institucional para colocarlos físicamente en las instalaciones.
4. **Dashboard de Estadísticas del Campus**:
   - Panel de control con métricas: cantidad de estudiantes activos, misiones más visitadas y podio del ranking.
5. **Autenticación con Rol `admin`**:
   - Inicio de sesión seguro consumiendo el endpoint `/auth/login` del backend con permisos de administrador.

---

## ☁️ INTEGRANTE 3: Desarrollador Backend, Base de Datos y DevOps
* **Área de Enfoque**: Servicios en la nube, API RESTful, persistencia remota, seguridad y despliegue.
* **Infraestructura**: Despliegue en Render / PaaS (`https://campus-quest-api-prod.onrender.com`), base de datos PostgreSQL/Supabase.

### 🎯 En qué debe enfocarse su agente:
1. **Mantenimiento y Evolución de la API REST**:
   - Endpoints de autenticación (`/auth/login`, `/auth/register`, `/auth/visitor`, `/auth/me`).
   - Endpoints de catálogo y lugares (`/catalogo`, `/puntos`, `/misiones`).
   - Endpoint de clasificación (`/ranking`).
2. **Lógica de Validación QR y Anti-Trampas**:
   - Validar códigos QR enviados por el cliente (`POST /usuarios/{id}/progreso`), evitar registros duplicados y calcular el nuevo puntaje/nivel de forma atómica en el servidor.
3. **Base de Datos y Modelado Relacional**:
   - Diseño y migraciones de tablas SQL: `usuarios`, `puntos_interes`, `misiones`, `progreso_mision`, `premios`.
   - Soporte para roles de negocio: `estudiante`, `visitante` y `admin`.
4. **Seguridad y DevOps**:
   - Autenticación mediante tokens JWT y cifrado seguro de contraseñas (BCrypt / Argon2).
   - Documentación OpenAPI/Swagger en vivo (`/docs`).
   - Configuración de CORS para permitir peticiones desde el Panel Web del Integrante 2.

---

## 🛠️ INTEGRANTE 4: Desarrollador Core Android, Resiliencia Offline y QA / Testing
* **Área de Enfoque**: Lógica de persistencia local en Android, cámara de escaneo, sincronización de red y pruebas automatizadas.
* **Ubicación en el proyecto**: `data/`, `util/`, `src/test/`, `src/androidTest/`, scripts de Gradle y CI/CD.

### 🎯 En qué debe enfocarse su agente:
1. **Base de Datos Local Room y Persistencia**:
   - Mantenimiento de `AppDatabase`, entidades Room, DAOs (`UsuarioDao`, `MisionDao`, `PuntoInteresDao`, `ProgresoMisionDao`).
   - Manejo de preferencias con Jetpack DataStore (`UserPreferencesRepository`).
2. **Sincronización Offline-First**:
   - Robustecer `RemoteCampusRepository` y `CampusApi` para sincronizar datos locales en segundo plano cuando regrese la conexión.
   - Cola de sincronización de eventos pendientes si el usuario escanea sin red.
3. **Hardware y Cámara (CameraX)**:
   - Configuración de `ProcessCameraProvider`, selector de resolución óptimo y analizador de frames ML Kit / ZXing (`QrAnalyzer`).
   - Manejo seguro de permisos en tiempo de ejecución (`Manifest.permission.CAMERA`).
4. **Aseguramiento de Calidad (QA & Testing)**:
   - Ampliar suites de pruebas unitarias (`./gradlew testDebugUnitTest`) para ViewModels y Repositorios.
   - Pruebas instrumentadas de integración y navegación en emulador (`./gradlew connectedDebugAndroidTest`).
   - Automatización de compilación y tests mediante GitHub Actions en cada Pull Request.
5. **Generación de Entregables**:
   - Generación de APKs firmadas (Debug / Release) para pruebas de campo del equipo.

---

## 🌿 Convención de Ramas en Git

Para trabajar en paralelo de forma fluida:

| Integrante | Prefijo de Rama | Ejemplo de Rama |
| :--- | :--- | :--- |
| **Integrante 1 (UI/UX)** | `feat/ui-*`, `fix/ui-*` | `feat/ui-map-labels`, `feat/ux-responsive-home` |
| **Integrante 2 (Web Admin)** | `feat/web-*` | `feat/web-qr-exporter`, `feat/web-mission-crud` |
| **Integrante 3 (Backend)** | `feat/api-*`, `feat/db-*` | `feat/api-ranking-filters`, `feat/db-prizes-table` |
| **Integrante 4 (Core & QA)** | `feat/core-*`, `test/*` | `feat/core-camera-optimization`, `test/missions-flow` |

---

## 📌 Resumen de Límites para Agentes de IA
* **Si una tarea involucra botones, colores, pantallas Compose, animaciones, sonido o cómo se ve la app** ➔ **Asignada al Integrante 1**.
* **Si una tarea involucra crear vistas web para PC o generar PDFs de QR** ➔ **Asignada al Integrante 2**.
* **Si una tarea involucra rutas de servidor, SQL en nube o tokens JWT** ➔ **Asignada al Integrante 3**.
* **Si una tarea involucra DAOs de Room, CameraX, tests de JUnit o CI/CD** ➔ **Asignada al Integrante 4**.
