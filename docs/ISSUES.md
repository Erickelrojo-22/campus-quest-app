# Issues locales

Seguimiento local de QA/decisiones de producto, hasta que el repo
tenga remoto en GitHub. Formato pensado para migrarse 1 a 1 con
`gh issue create --title "..." --body "..."` cuando exista el fetch.

## Resueltos

- [x] **QR crasheaba al gestionar otra ventana mientras escaneaba.**
  Causa: `CamaraPreview` (`ScannerScreen.kt`) desligaba y volvía a
  ligar toda la cámara en cada cambio de estado, con un `.get()`
  bloqueante sin `try/catch`. Corregido: la cámara se liga una sola
  vez; solo se activa/desactiva el analizador de frames.
- [x] **Cámara sin enfoque.** No se guardaba la referencia a `Camera`
  ni se disparaba `startFocusAndMetering`. Corregido: enfoque continuo
  al centro + toque para reenfocar.
- [x] **"Worker" no permitía gestionar otras ventanas al mismo
  tiempo.** Mismo root cause que el crash de QR (rebind bloqueando el
  hilo principal). Auditado el resto del código (`runBlocking`, `.get()`
  bloqueante, `DisposableEffect`, uso de CameraX): no aparece en
  ningún otro archivo.
- [x] **Textbox "Buscar lugar o misión" con bordes poco redondeados.**
  `HomeScreen.kt`: `shape` del `TextField` cambiado de 4dp a 24dp.
- [x] **Puntos/"actividades" muy pequeños en el mapa de Inicio.**
  `CampusMapView.kt`: se incrementó el radio táctil a 65f para garantizar
  usabilidad en pantallas móviles y se añadieron anillos y detalles centrales
  con alto contraste visual.
- [x] **Registro de usuario institucional y pantalla de registro (RF-02).**
  Implementada `RegisterScreen`, `RegisterViewModel` y métodos de registro
  y login con contraseña en `AuthRepository` con validación de dominio
  institucional (@live.uleam.edu.ec) y pruebas unitarias.
- [x] **Visualización de carrera y correo en el perfil.**
  `ProfileScreen.kt`: ahora muestra el correo institucional, carrera,
  nivel del usuario y puntaje acumulado con estilo retro.
- [x] **Configuración de correo institucional en Git.**
  Actualizada la configuración local y global de Git con `e1312842246@live.uleam.edu.ec` y usuario `JhonnyCM` para vincular correctamente los commits en GitHub.
- [x] **Historial y timeline de actividades y puntos ganados (RF-15).**
  Implementada la función `observarHistorialProgreso` en `CampusRepository` y DAOs con orden cronológico (`ORDER BY fechaHora DESC`), integrando un modal bottom sheet en `ProfileScreen.kt` con fecha/hora formateada, emojis, lugar del campus y puntos ganados (`+X pts`).
- [x] **Movimiento fluido e independiente de NPCs en el mapa.**
  `CampusMapView.kt`: corregida la memoización en Compose para calcular las posiciones animadas de los NPCs dinámicamente sin congelarse cuando el jugador se detiene.
- [x] **Optimización de visualización de insignias y dificultad en misiones.**
  `BadgesScreen.kt`: eliminado el `LazyVerticalGrid` anidado para evitar problemas de altura fija; `MissionsScreen.kt`: agregadas etiquetas cromáticas para todas las dificultades (Baja, Media, Alta).
- [x] **Cobertura de pruebas unitarias (Repository y ViewModels).**
  Implementadas suites de tests para `CampusRepositoryTest`, `MissionsViewModelTest` y `HomeViewModelTest` con verificación de filtrado reactivo, búsquedas y progreso.
- [x] **Buscador reactivo en tiempo real en la pantalla de Misiones.**
  `MissionsScreen.kt` y `MissionsViewModel.kt`: implementado filtrado reactivo por título, lugar, dificultad y pistas con TextField estilizado retro, botón de borrado rápido y tests unitarios.
- [x] **Vitrina interactiva de insignias con modal de inspección retro.**
  `BadgesScreen.kt`: las insignias obtenidas y bloqueadas ahora son interactivas, abriendo un modal detallado con animación de resplandor, puntos, lugar del campus, pistas secretas y opción para compartir logros.
- [x] **Brújula y radar de navegación hacia la misión activa en el mapa.**
  `CampusMapView.kt` y `HomeScreen.kt`: indicador flotante tipo brújula 🧭 que orienta hacia el punto objetivo de la misión sugerida, con animación de pulso sobre el pin y caminata/centrado automático suave.
- [x] **Feedback háptico y pantalla de Nivel Alcanzado (Level Up).**
  `ScannerScreen.kt` y `BadgeEarnedScreen.kt`: vibración háptica táctil en escaneo exitoso/erróneo, banner dinámico de nuevo nivel y compartir victoria.
- [x] **Prólogo y tutorial de bienvenida interactivo (Onboarding RPG).**
  `OnboardingDialog.kt`, `UserPreferencesRepository.kt` y `HomeScreen.kt`: tutorial interactivo en 3 pasos con persistencia DataStore (`tutorialVisto`), accesible también desde el perfil.
- [x] **Carné de Aventurero RPG con copia rápida de credencial y feedback sensorial.**
  `AdventurerCardDialog.kt`: credencial oficial con sprite personalizado, copia de ID al portapapeles con confirmación visual (Toast) y sonidos/vibración háptica.
- [x] **Supresión de advertencias de Gradle en compilación.**
  `gradle.properties`: añadida propiedad recomendada para suprimir advertencias experimentales de AGP.
- [x] **Resiliencia offline en Modo Prueba y validación QR (RF-20).**
  `AuthRepository.kt` y `RemoteCampusRepository.kt`: implementado fallback automático a Room local si Render está en reposo o no hay internet; generación de tokens locales offline y sincronización transparente.
- [x] **Tabla de Clasificación y Ranking de Exploradores RPG (RF-18).**
  `LeaderboardDialog.kt`, `ProfileViewModel.kt` y `ProfileScreen.kt`: modal interactivo con podio Top 3 (🥇, 🥈, 🥉), lista completa de aventureros con niveles y puntos, posición destacada del usuario actual y tests unitarios.
- [x] **Etiquetas de Puntos de Interés y filtro de misiones en el Mapa.**
  `CampusMapView.kt`: placas retro con nombres de edificios en cada pin (`drawText`), filtro flotante para ver "Solo pendientes" vs "Todos los lugares", feedback de sonido y vibración háptica al llegar a destino.
- [x] **Navegación "Ver ruta en el mapa" con auto-enfoque y caminata guiada.**
  `CampusQuestNavHost.kt` y `HomeScreen.kt`: al pulsar "Ver ruta en el mapa" desde el detalle de misión, la app regresa al mapa, enfoca el punto objetivo e inicia la caminata guiada del avatar.

## Pendientes

- [ ] **Crash al abrir la sección de Misiones.** Revisados
  `MissionsScreen.kt`, `MissionsViewModel.kt`, `CampusRepository`,
  los 3 DAOs y el `NavHost`. Auditoría del 2026-10-04: compilación,
  56 pruebas unitarias y 4 instrumentadas pasan en Android 16/API 36.
  No reproducido con catálogo vacío, activo/completado y entrada desde
  insignia en un host de navegación de prueba. Falta comprobar la app
  completa con sesión real, datos previos, API 29 y dispositivo físico.
  Evidencia y límites en [`ISSUES-1-3-AUDIT.md`](ISSUES-1-3-AUDIT.md).
- [ ] **Mapa en alta definición / estilo Google Maps 3D.** Propuesta
  en `ROADMAP.md`, pendiente de evaluar costos (API key, facturación)
  e integración (conexión a internet rompe el offline-first actual).
- [ ] **Servicio backend dedicado / BaaS (Railway / Supabase / PocketBase / PaaS).** [Issue #1 en GitHub](https://github.com/Erickelrojo-22/campus-quest-app/issues/1). API desplegada en Render e integrada en Android con caché Room y sesión cifrada. Salud HTTP 200 y controles sin sesión HTTP 401 verificados; falta validación autenticada de registro, roles, QR/progreso y funcionamiento sin red. Ver [`ISSUES-1-3-AUDIT.md`](ISSUES-1-3-AUDIT.md).
- [ ] **Modelo de roles `estudiante`/`visitante` en la app + rol
  `admin` en panel web separado (mismo backend).** Decisión tomada
  (ver `BACKEND.md`), falta implementar: retirar `Rol.TUTOR` del
  cliente móvil y sacar `ui/crud/` de la app hacia el futuro panel
  web.
- [ ] **Sistema de premios ("asignar premio").** Sin definir si es
  insignia digital (ya existe), premio canjeable con puntos (requiere
  tabla nueva), o ambos. No implementar hasta confirmarlo.
