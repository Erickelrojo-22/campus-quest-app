# Backend para Gamequest — propuesta

## Contexto

Hoy la app es 100% local: no hay ninguna dependencia de red (no hay
Retrofit/OkHttp/Ktor en `app/build.gradle.kts`), todo vive en Room
(`AppDatabase`, ver `AppContainer.kt`). Este documento propone un
servicio backend que la app consumiría por HTTP, dejando Room como
caché local (offline-first) en vez de única fuente de verdad.

**Esto es una propuesta para revisar, no algo ya decidido ni
implementado.**

## Entidades actuales (Room) que el backend debería exponer

Mapeando directo de `app/src/main/java/com/example/gamequest/data/local/entity/`:

- `UsuarioEntity` → usuarios/cuentas. Rol actual: `estudiante` / `tutor`
  (`Rol.kt`).

  **Decisión (2026-09-23)**: el modelo pasa a 3 roles a nivel de
  negocio, pero repartidos en 2 clientes distintos sobre el mismo
  backend:
  - App móvil (este repo): solo ve `estudiante` y `visitante`.
  - Panel web (a crear, fuera de este repo): rol `admin`/gestor, ve
    la gestión de misiones/puntos/premios que hoy vive en
    `ui/crud/` (`MissionManagementScreen`, `MissionFormScreen`).

  Un solo backend sirve a los dos clientes; el rol `tutor` actual en
  `Rol.kt` se retira de la app y las pantallas de `ui/crud/` quedarían
  sin uso en el cliente móvil (a mover al panel web cuando exista).
- `PuntoInteresEntity` → puntos de interés del campus (posición,
  categoría, código QR).
- `MisionEntity` → misiones asociadas a un punto (dificultad, puntos,
  tiempo estimado, insignia).
- `ProgresoMisionEntity` → historial de misiones completadas por
  usuario (`fechaHora`, `puntosObtenidos`, `codigoQrValidado`). Esta
  tabla ya es, en esencia, el "timeline de dónde has ganado puntos"
  que se propuso para Inicio.
- **Nueva**: `Premio` (no existe todavía) — para poder "asignar
  premio" hace falta una tabla que registre el catálogo de premios
  (nombre, puntos requeridos, stock/cupo) y otra de canjes
  (usuarioId, premioId, fechaHora, estado: pendiente/entregado).

  **Pendiente de definir**: "asignar premio" puede ser (a) insignia
  digital — ya existe vía `insigniaNombre`/`insigniaEmoji` en
  `MisionEntity`, no requiere tabla nueva — o (b) premio canjeable con
  puntos acumulados, que sí requiere la tabla `Premio` de arriba.
  Confirmado por el usuario que aún puede ser cualquiera de las dos, o
  ambas: no implementar hasta definirlo.

## Endpoints propuestos (REST, v1)

| Método | Ruta | Descripción | Reemplaza |
|---|---|---|---|
| POST | `/auth/login` | Entrar con nombre (o credenciales reales si se agregan) | `AuthRepository.entrarConNombre` |
| GET | `/puntos` | Listar puntos de interés | `PuntoInteresDao.observarTodos` |
| GET | `/misiones` | Listar misiones | `MisionDao.observarTodas` |
| GET | `/usuarios/{id}/progreso` | Historial de misiones completadas (timeline) | `ProgresoMisionDao.observarPorUsuario` |
| POST | `/usuarios/{id}/progreso` | Validar código QR y registrar misión completada | `CampusRepository.validarCodigo` |
| GET | `/premios` | Catálogo de premios disponibles | — (nuevo) |
| POST | `/usuarios/{id}/premios/{premioId}/canjear` | Canjear un premio con los puntos acumulados | — (nuevo) |
| POST/PUT/DELETE | `/misiones` (admin) | CRUD de misiones/puntos | `MissionManagementViewModel`, `MissionFormViewModel` |

El último bloque (CRUD) solo debería quedar disponible para el rol con
permiso de gestión — sea cual sea el nombre final de ese rol.

## Integración en la app Android

1. Agregar Retrofit + OkHttp (o Ktor client) al `app/build.gradle.kts`.
2. Nueva capa `data/remote/` con los DTOs y el `ApiService`.
3. Los repositorios actuales (`CampusRepository`, `AuthRepository`)
   pasarían a coordinar red + Room: piden a la API, guardan en Room, y
   la UI sigue leyendo de Room (Flow) como hoy — así se conserva el
   RF-20 (funcionamiento offline) mostrando el último dato cacheado
   cuando no hay conexión.
4. `AppContainer.kt` agregaría el cliente HTTP y lo inyectaría a los
   repositorios.

## Pendiente de evaluar

- **Costos**: hosting del backend (y si se reutiliza algún proveedor
  ya usado en otros proyectos), base de datos administrada, dominio.
- **Autenticación real**: hoy el login es solo con nombre, sin
  contraseña real (`contrasenaHash` existe en el modelo pero no se usa
  para validar). Si hay backend, probablemente haga falta login real.
- **Modelo de roles**: ver pregunta abierta sobre `estudiante` /
  `visitante` y quién gestiona misiones y premios.
- **Sincronización offline**: qué pasa si un `visitante`/`estudiante`
  completa una misión sin conexión — cola de reintento al recuperar
  señal.

Nada de esto está implementado; este documento es el punto de partida
para decidir alcance antes de escribir código del backend.
