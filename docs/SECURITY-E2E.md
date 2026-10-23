# Seguridad y pruebas E2E

## Arquitectura vigente

Android utiliza una API REST sobre HTTPS y Room como caché local. El APK no
incluye un proveedor OAuth. La autorización de las operaciones remotas corresponde
al servidor; estas pruebas usan una API simulada y no certifican el backend de
producción. El issue de validación autenticada del backend sigue abierto.

El token se cifra con AES-GCM y una clave de Android Keystore. El token cifrado y
su ID local se escriben juntos en SharedPreferences. Las preferencias de interfaz
usan DataStore y no están cifradas. Las cuentas del repositorio local conservan
su verificación PBKDF2; no se almacena la contraseña recibida de la API.

## Identidad y actualización de datos

- Room v3 separa las claves locales autogeneradas de `remoteId`, único y nullable,
  tanto para usuarios como para eventos de progreso.
- La UI, las FK y la preferencia de usuario activo usan IDs locales. Los endpoints
  reciben IDs remotos. El ranking distingue ambos espacios de identidad.
- La migración v2→v3 conserva filas, PK y FK y deja `remoteId = NULL` en los
  registros antiguos. Se mantiene la migración v1→v2 existente y su depuración previa. No vincula cuentas por nombre, correo ni coincidencia numérica.
- Una cuenta remota validada obtiene su identidad canónica y su caché del servidor.
  El historial antiguo permanece separado. Esto no repara autorías que ya estuvieran
  mezcladas antes de actualizar.
- Un token heredado de visitante solo reutiliza su perfil local sin credenciales,
  correo, rol tutor ni vínculo remoto. Un token remoto sin vínculo requiere validación
  en línea antes de seleccionar una cuenta local.

## Fallback y ciclo de sesión

Solo un fallo de transporte permite crear o reutilizar un visitante offline.
Cualquier rechazo HTTP, incluidos 4xx y 5xx, conserva el error. Cancelaciones,
errores de JSON y fallos al persistir el login no conceden una sesión offline.

`SessionCoordinator` publica `Restoring`, `SignedOut`, `Authenticated` o
`RecoveryRequired` y serializa las escrituras de sesión. El temporizador visual
del splash no decide el acceso. La identidad se resuelve antes de descargar
catálogo, ranking y progreso. Sin red solo se utiliza una identidad remota
previamente vinculada y almacenada; sin esa caché se ofrecen reintento y login.

Un 401 vigente invalida la sesión y elimina las rutas privadas. El callback
incluye el token rechazado: una respuesta antigua no cierra una sesión nueva.
El logout limpia primero la sesión local y después intenta revocar el token
capturado. Cambiar de cuenta reinicia la navegación privada.

El modo local no dispone de outbox ni de reconciliación automática de recompensas.
La autorización remota de QR, roles y administración debe comprobarse también en
el backend. En la configuración remota, el CRUD se deriva al panel web.

## Pruebas y evidencia

Consultar [QA de identidad y sesión](QA-identidad-sesion-2026-10-23.md) para el
entorno, comandos, resultados, logs y limitaciones de esta corrección.

| Área | Verificación automatizada |
|---|---|
| Identidad | Colisiones usuario 51 y evento 1; snapshots repetidos; FK ajenas rechazadas; endpoint con ID remoto |
| Migración | v1→v2→v3, v2→v3 y creación limpia; conservación de filas y FK |
| Login | HTTP 400/401/403/422/429/500, transporte, JSON, persistencia y cancelación |
| Sesión | Preferencias lentas, tokens heredados, caché válida/ausente, 401, logout y respuestas tardías |
| Navegación | Grafo real, más de 1100 ms en restauración, Main/detalle, recuperación, cambio de cuenta y recreación de estado |
| Persistencia | Snapshot cifrado tras nueva instancia; invalidación condicional; token antiguo sin vínculo |
| Integración | Base v2 con historial, visitante offline, remoto con ID coincidente, restauración, 403 y 401 |

Las pruebas no sustituyen un recorrido autenticado contra el backend ni una
validación en un teléfono físico. Tampoco cierran el reporte general de Misiones.
