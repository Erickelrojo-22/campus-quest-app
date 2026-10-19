# Backend para Gamequest — estado actual y evolución

## Contexto

La app ya consume la API REST desplegada en Render mediante `CampusApi`
(`HttpURLConnection`) y utiliza `campus_quest_remote.db` como caché Room.
`RemoteCampusRepository` sincroniza usuario, puntos, misiones, progreso y
ranking; `SessionTokenStore` cifra el token con Android Keystore.

La integración existe, pero todavía no constituye un offline-first completo:
falta una cola persistente para enviar progreso creado sin red, reconciliación
explícita de conflictos y una validación E2E autenticada de todos los roles.

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

## Endpoints REST v1

| Método | Ruta | Descripción | Estado Android |
|---|---|---|---|
| POST | `/auth/register` | Registrar cuenta institucional | Integrado |
| POST | `/auth/login` | Iniciar sesión con correo y contraseña | Integrado |
| POST | `/auth/visitor` | Crear o recuperar visitante de prueba | Integrado con fallback local |
| GET | `/auth/me` | Restaurar y validar la sesión | Integrado |
| GET | `/puntos` | Descargar puntos de interés | Integrado y cacheado |
| GET | `/catalogo` | Descargar misiones activas | Integrado y cacheado |
| GET | `/ranking` | Descargar clasificación | Integrado |
| GET | `/usuarios/{id}/progreso` | Descargar historial del usuario | Integrado y cacheado |
| POST | `/usuarios/{id}/progreso` | Validar QR y otorgar puntos | Integrado; falta outbox offline |
| GET/POST | `/premios` y canjes | Catálogo de premios canjeables | Pendiente de definición |
| POST/PUT/DELETE | `/misiones` (admin) | CRUD desde panel administrativo | Pendiente en panel web |

El último bloque (CRUD) solo debería quedar disponible para el rol con
permiso de gestión — sea cual sea el nombre final de ese rol.

## Integración en la app Android

1. `AppContainer` construye `CampusApi`, `AuthRepository` y
   `RemoteCampusRepository` sobre una única base de caché.
2. `CampusApi` ejecuta HTTPS fuera del hilo principal y adjunta el token.
3. `RemoteCampusRepository` actualiza Room dentro de una transacción y la UI
   continúa observando `Flow`, sin depender directamente de la red.
4. Ante una interrupción real de transporte, la validación puede utilizar la
   copia local. Un rechazo HTTP del servidor no debe otorgar puntos locales.
5. Al recuperar conexión todavía falta enviar automáticamente los eventos
   pendientes mediante una outbox persistente y `WorkManager`.

## Pendiente de evaluar

- **Disponibilidad/costos**: el backend está en Render y puede presentar
  arranque en frío; falta acordar el nivel de servicio esperado.
- **Autenticación federada**: correo/contraseña ya funciona; Google OIDC
  permanece abierto en el issue #2.
- **Modelo de roles**: ver pregunta abierta sobre `estudiante` /
  `visitante` y quién gestiona misiones y premios.
- **Sincronización offline**: cola de reintento, idempotencia, estado visible
  de sincronización y política de conflictos.
- **Migración local**: definir cómo trasladar cuentas y progreso de
  `campus_quest.db` a la caché remota sin perder datos.
