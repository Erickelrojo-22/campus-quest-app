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

## Pendientes

- [ ] **Crash al abrir la sección de Misiones.** Revisados
  `MissionsScreen.kt`, `MissionsViewModel.kt`, `CampusRepository`,
  los 3 DAOs y el `NavHost` — compila limpio, no se encontró la causa
  por lectura de código. **Bloqueado**: falta el logcat
  (`FATAL EXCEPTION`/`AndroidRuntime`) o un dispositivo/emulador para
  reproducirlo.
- [ ] **Mapa en alta definición / estilo Google Maps 3D.** Propuesta
  en `ROADMAP.md`, pendiente de evaluar costos (API key, facturación)
  e integración (conexión a internet rompe el offline-first actual).

- [ ] **Menú ☰ + timeline de dónde se ganaron puntos.** Evaluado como
  factible (el dato ya existe en `ProgresoMisionEntity.fechaHora` /
  `puntosObtenidos`). Falta decidir si se implementa ahora.
- [ ] **Servicio backend dedicado / BaaS (Railway / Supabase / PocketBase / PaaS).** [Issue #1 en GitHub](https://github.com/Erickelrojo-22/campus-quest-app/issues/1). Propuesta detallada en `BACKEND.md` (entidades, endpoints, autenticación institucional y sincronización con Room). Nada implementado todavía.
- [ ] **Modelo de roles `estudiante`/`visitante` en la app + rol
  `admin` en panel web separado (mismo backend).** Decisión tomada
  (ver `BACKEND.md`), falta implementar: retirar `Rol.TUTOR` del
  cliente móvil y sacar `ui/crud/` de la app hacia el futuro panel
  web.
- [ ] **Sistema de premios ("asignar premio").** Sin definir si es
  insignia digital (ya existe), premio canjeable con puntos (requiere
  tabla nueva), o ambos. No implementar hasta confirmarlo.
