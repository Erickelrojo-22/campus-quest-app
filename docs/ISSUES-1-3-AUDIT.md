# Verificación de issues #1 y #3 — 2026-10-04

Rama: `prueba/integracion-backend-web-android`. La revisión incluye los tres
microcommits de ubicaciones hasta `e952d1f` y las pruebas añadidas en esta auditoría.

## #1 — Backend

Estado: implementación real y desplegada, verificación incompleta; mantener abierta.

Comprobaciones realizadas contra `https://campus-quest-api-prod.onrender.com`:

- `GET /api/v1/health`: HTTP 200, `{"status":"ok"}`.
- OpenAPI disponible: rutas de login, registro, visitante, sesión, puntos,
  misiones, catálogo, usuarios, ranking y progreso.
- Sin credenciales, puntos, misiones, catálogo, sesión, usuarios y progreso
  responden HTTP 401.
- Login con credenciales inexistentes: HTTP 401.
- Registro con campos vacíos/contraseña corta: HTTP 422; no se creó una cuenta.

En Android, `AppContainer` utiliza `CampusApi` y `RemoteCampusRepository`.
El catálogo, usuario y progreso se descargan y guardan en una transacción Room
sobre `campus_quest_remote.db`. La validación QR solicita al servidor registrar
el progreso y vuelve a consultar el puntaje. La sesión se cifra con Android
Keystore. No es correcto describir el backend como «nada implementado».

Diferencias frente a la propuesta de la issue: se usa `HttpURLConnection`, no
Retrofit/Ktor; la app obtiene misiones desde `/catalogo`, aunque el servidor
también publica `/misiones`. El cliente conserva código legado de rol tutor.
Estas diferencias deben revisarse junto con el alcance acordado, no confundirse
con ausencia de backend.

Falta verificar de extremo a extremo con cuentas de prueba autorizadas:
registro y login institucional exitosos, roles estudiante/visitante/admin,
restricciones entre usuarios, lectura autenticada del catálogo, validación QR,
puntaje y rechazo de duplicados, restauración de sesión y lectura de la caché
sin red. No se probaron escrituras de progreso ni se alteró el catálogo remoto.
La entidad Premio/canjes está indicada como futura en la issue; no se considera
por sí sola un bloqueo del backend inicial.

## #3 — Cierre al abrir Misiones

Estado: no reproducido en las pruebas de pantallas en API 36; mantener abierta
hasta completar la matriz y probar la sesión/navegación de la app completa.

Entorno: emulador `Pixel_10_Pro_XL`, Android 16, API 36, x86_64.

Resultados:

- `./gradlew testDebugUnitTest assembleDebug connectedDebugAndroidTest --no-configuration-cache`:
  compilación y 56 pruebas unitarias pasan; la prueba instrumentada inicial pasa.
- Se añadieron tres pruebas en `MissionsNavigationTest`: entrada desde la barra
  inferior real con catálogo vacío; entrada con misiones activas/completadas y
  cambio de filtros; entrada desde el botón real «SIGUIENTE MISIÓN» de
  `BadgeEarnedScreen` con catálogo completado.
- Ejecución final de `connectedDebugAndroidTest`: 4 pruebas pasan (las tres
  nuevas y la prueba de contexto existente), sin fallos.
- Las pruebas usan las pantallas y ViewModels reales con un repositorio de
  datos de prueba en un NavHost aislado. No ejecutan el `CampusQuestNavHost`
  completo, autenticación ni el escaneo QR real. El destino Inicio del host
  de prueba contiene la barra inferior, no el mapa/HomeScreen completo.
- No se identificó un fallo de aplicación ni se cambió su lógica para estas
  pruebas. Los fallos iniciales eran del test (acción de scroll sobre un
  contenedor no desplazable), corregidos en el propio test.

Limitaciones: no hay imagen API 29 instalada ni dispositivo físico conectado.
Faltan instalación con datos previos, sesión real, completar un QR y continuar
hacia Misiones en la app completa. La ejecución instrumentada de Gradle
instala/desinstala el paquete en el emulador; su sesión previa no se conserva.

Los reportes locales quedan en `app/build/reports/tests/testDebugUnitTest/` y
`app/build/reports/androidTests/connected/debug/`. Los logcat por prueba están
bajo `app/build/outputs/androidTest-results/connected/debug/`.
El CI reciente de main pasó, pero no contiene las nuevas pruebas de esta rama:
https://github.com/Erickelrojo-22/campus-quest-app/actions/runs/37080493301

## Decisión

No cerrar #1 ni #3 con esta evidencia parcial. La #1 ya tiene backend funcional
integrado, pendiente de validación autenticada; la #3 tiene cobertura de pantalla
que antes no existía, pendiente de la matriz de ejecución completa.
