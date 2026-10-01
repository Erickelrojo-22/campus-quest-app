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

## Pendientes

- [ ] **Crash al abrir la sección de Misiones.** Revisados
  `MissionsScreen.kt`, `MissionsViewModel.kt`, `CampusRepository`,
  los 3 DAOs y el `NavHost` — compila limpio y suite de tests pasa al 100%. **Bloqueado**: falta el logcat
  (`FATAL EXCEPTION`/`AndroidRuntime`) o un dispositivo/emulador para
  reproducirlo en caso de que aún persista.
- [ ] **Mapa en alta definición / estilo Google Maps 3D.** Propuesta
  en `ROADMAP.md`, pendiente de evaluar costos (API key, facturación)
  e integración (conexión a internet rompe el offline-first actual).
- [ ] **Servicio backend dedicado / BaaS (Railway / Supabase / PocketBase / PaaS).** [Issue #1 en GitHub](https://github.com/Erickelrojo-22/campus-quest-app/issues/1). Propuesta detallada en `BACKEND.md` (entidades, endpoints, autenticación institucional y sincronización con Room). Nada implementado todavía.
- [ ] **Modelo de roles `estudiante`/`visitante` en la app + rol
  `admin` en panel web separado (mismo backend).** Decisión tomada
  (ver `BACKEND.md`), falta implementar: retirar `Rol.TUTOR` del
  cliente móvil y sacar `ui/crud/` de la app hacia el futuro panel
  web.
- [ ] **Sistema de premios ("asignar premio").** Sin definir si es
  insignia digital (ya existe), premio canjeable con puntos (requiere
  tabla nueva), o ambos. No implementar hasta confirmarlo.
