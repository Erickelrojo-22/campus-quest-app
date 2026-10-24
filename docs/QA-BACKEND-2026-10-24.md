# Validación del backend real — 2026-10-24

Código Android: `f53de6e`. API: `https://campus-quest-api-prod.onrender.com/api/v1/`.
No se modificó código de producción durante esta validación.

## API autenticada: 30 comprobaciones correctas

Evidencia: [resultados sin tokens ni contraseñas](qa/identity-session-2026-10-23/backend-live-2026-10-24.json).

- Salud HTTP 200; sesión, catálogo, puntos y ranking sin token: HTTP 401.
- Dos visitantes QA distintos (IDs remotos 20/21), sesión y perfil consistentes.
- Contratos de datos compatibles con `CampusApi`: 8 puntos, 8 misiones y ranking.
- Progreso propio accesible; consultar el progreso de la otra cuenta QA o el
  directorio de usuarios: HTTP 403.
- QR incorrecto: HTTP 422 y ningún evento registrado.
- Completar una misión propia: HTTP 201, evento e incremento de puntaje correctos.
- Repetir el completado: HTTP 200 con el mismo evento; ni otro evento ni puntos extra.
- El historial de la otra cuenta permanece vacío.
- Logout de ambas sesiones y posterior `/auth/me`: HTTP 401.

La comprobación inicial esperaba HTTP 409 para duplicados. El servidor tiene
semántica idempotente y devuelve el evento existente con HTTP 200. La pasada
final comprueba su identidad, historial y puntaje antes/después. También se
observó que entrar como visitante crea una identidad nueva aunque se repita el
nombre; restaurar una sesión conserva la identidad mediante su token.

## APK real en Android 35

Se instaló el APK debug ya verificado y se recorrió la app completa:

1. Sin resolución DNS hacia Render, el visitante `QA-Codex-UI24` obtuvo solo una
   identidad local (`id=1`, `remoteId=NULL`).
2. Con conectividad, `QA-Online24` inició una sesión remota: `id=2`,
   `remoteId=22`. La fila local anterior se conservó separada.
3. Se descargaron los 8 puntos y 8 misiones. Se abrió Misiones y el detalle de
   Biblioteca sin `FATAL EXCEPTION`.
4. Se detuvo y relanzó el proceso sin acceso al dominio: la identidad remota
   vinculada y las 8 misiones se recuperaron desde caché.
5. Se restauró de nuevo con conexión y se cerró la sesión desde Perfil; apareció
   el login. No se observaron errores `AndroidRuntime`.

Evidencia de UI/Room:
[Misiones](qa/identity-session-2026-10-23/api35/live-missions-layout.json),
[detalle](qa/identity-session-2026-10-23/api35/live-detail-layout.json),
[identidades y catálogo](qa/identity-session-2026-10-23/api35/live-cache-proof.json),
[caché offline](qa/identity-session-2026-10-23/api35/live-offline-cache-layout.json),
[restauración](qa/identity-session-2026-10-23/api35/live-restored-layout.json),
[logout](qa/identity-session-2026-10-23/api35/live-logout-layout.json).

### Condición de red del emulador

El host resolvía Render y accedía a HTTPS, pero Android devolvía
`UnknownHostException / EAI_NODATA`, incluso tras cambiar sus resolutores. Para
la pasada online se usó un túnel CONNECT en loopback, restringido a ese dominio
y puerto 443. El APK conservó la URL HTTPS y la validación normal del certificado;
no se interceptó TLS ni se cambió la seguridad de la app. Al retirar el proxy se
reprodujo el fallo de transporte y se verificó la caché. El proxy se retiró y
el proceso auxiliar y emulador se cerraron al terminar.

La captura de pantalla del CLI resultó negra en esta imagen ATD; se descartó
como evidencia y se conservaron los árboles accesibles y las consultas de Room.

## Límites y registros de prueba

No se certifican registro/login institucional, privilegios de administrador,
escaneo por cámara ni teléfono físico. El QR/progreso real se comprobó por API;
las regresiones del cliente siguen cubiertas por las 146 ejecuciones de la
[matriz automatizada](QA-identidad-sesion-2026-10-23.md).

Los visitantes QA 17–22 y los eventos de las cuentas de prueba permanecen en el
backend con nombres QA. No se alteraron catálogo ni cuentas reales. Los tokens
de las sesiones completadas se revocaron; el intento exploratorio 19 no completó
el cierre y queda sujeto a su expiración normal. No se publican credenciales.

El issue general de backend y el reporte de Misiones permanecen abiertos:
esta pasada amplía la evidencia a visitante real/API 35, sin completar toda la
matriz institucional, administrativa y física del equipo.
