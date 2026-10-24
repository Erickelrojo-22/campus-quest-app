# QA: identidad, fallback y sesión — 2026-10-23

## Alcance

Corrección de tres hallazgos sobre `5ba2b88e322856599de73e466f5fd40cb7b81671`:
colisiones de IDs locales/remotos, fallback de visitante tras rechazos HTTP y
navegación antes de resolver la sesión. Rama: `codex/campus-quest-identity-session`.

## Entorno

Windows, JDK 21.0.11, Gradle 9.5.0, AGP 9.3.3, Kotlin 2.2.10, Room 2.8.5,
Navigation 2.10.1. minSdk 29, compileSdk/targetSdk 37. Plataforma SDK real
`android-37.0`, revisión 2, `AndroidVersion.ApiLevel=37.0`; build-tools 36.0.0.
No se usó una plataforma 35 renombrada como 37.

Los emuladores usan imágenes oficiales x86_64: API 29 `default` y API 35
`aosp_atd`. Pruebas con Room real, API simulada y tokens ficticios. Ningún test
necesita cuentas, secretos ni solicitudes al backend de producción.

## Resultados

Verificación final ejecutada el 2026-10-24, después de corregir los dos
hallazgos adicionales de la [revisión final](REVISION-identidad-sesion.md).

| Gate | Resultado | Evidencia |
|---|---|---|
| JVM | 90 pruebas, 0 fallos/errores/omisiones | [XML consolidado](qa/identity-session-2026-10-23/unit-tests.xml) |
| Lint debug | 0 errores, 49 advertencias | [SARIF](qa/identity-session-2026-10-23/lint.sarif) |
| Debug y androidTest | Compilación y APK correctos | [Log de build](qa/identity-session-2026-10-23/build.log) |
| Release con R8 | APK sin firmar generado correctamente | [Hashes de APK](qa/identity-session-2026-10-23/apks.sha256) |
| Android API 29 | 28 pruebas, 0 fallos/errores/omisiones | [XML](qa/identity-session-2026-10-23/api29/tests.xml), [log](qa/identity-session-2026-10-23/api29/gradle-connected.log) |
| Android API 35 | 28 pruebas, 0 fallos/errores/omisiones | [XML](qa/identity-session-2026-10-23/api35/tests.xml), [log](qa/identity-session-2026-10-23/api35/gradle-connected.log) |

Las advertencias de Lint se conservan en el reporte: recursos sin uso,
actualizaciones de dependencias, estilo de Modifier y recomendaciones KTX/TOML.
No se declaran resueltas por esta corrección.

Se ejecutó además el caso integrado de forma dirigida para capturar el
[login tras el 401](qa/identity-session-2026-10-23/api35/session-expired.png).
Esta ejecución visual adicional no se suma a las 56 de la matriz requerida.
Gradle retira los APK al terminar sus pruebas, por eso la captura se obtuvo
con una ejecución instrumentada dirigida que conserva la instalación.

## Reproducción

Con JDK 21 y SDK 37 reales:

```powershell
./gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:compileDebugAndroidTestKotlin :app:assembleDebug :app:assembleDebugAndroidTest :app:assembleRelease --stacktrace
./gradlew.bat :app:connectedDebugAndroidTest --stacktrace
```

Ejecutar el segundo comando por separado en API 29 y API 35. La compilación local
usa un trabajador, compilador Kotlin en el proceso de Gradle y un heap de 2 GB
para limitar el consumo simultáneo de memoria. Para las pruebas ya compiladas
se reduce el heap de Gradle y se conecta un solo emulador por ejecución.

## Qué demuestran las regresiones

- Room: el usuario local 51 y el remoto 51 conservan perfiles e historial
  distintos. El evento remoto 1 no sobrescribe el evento local 1.
- La misma cuenta y evento conservan sus PK al repetirse el snapshot; un evento
  que declara otra cuenta se rechaza sin cambiar FK.
- La migración v2→v3 conserva filas e integridad referencial. El camino
  v1→v2→v3 usa un fixture sin duplicados; no modifica la depuración de
  duplicados que ya realizaba v1→v2.
- Los HTTP 400/401/403/422/429/500 del visitante no conceden modo offline;
  transporte, JSON, persistencia y cancelación tienen casos separados.
- El grafo de producción permanecía mal en login durante `Restoring` y mantenía
  Main al recibir `SignedOut`. Ambas regresiones se observaron fallar en API 35
  antes de corregirlo.
- El coordinador espera las preferencias, publica la identidad antes del catálogo,
  acepta solo caché vinculada y descarta resultados de una sesión anterior.
- La navegación comprueba recuperación, Main/detalle, cambio de cuenta,
  conservación del detalle de la misma cuenta y recreación de estado guardado.
- Una misión confirmada conserva su evento y recompensa si falla el transporte
  de la consulta posterior del perfil; repetir el QR no repite la mutación.
- La transición `Restoring` con recreación conserva el detalle de la misma
  cuenta y descarta el de otra. Ambos fallos nuevos se observaron antes de la
  corrección ([RED](qa/identity-session-2026-10-23/final-review-red.log)) y los
  tres casos posteriores pasan en ambas API.
- El recorrido integrado parte de una base v2 con un visitante y progreso;
  migra a v3, restaura sin red, inicia un remoto con ID coincidente, restaura
  el vínculo, rechaza 403 y elimina la navegación privada ante 401.

## CI

Las pruebas instrumentadas se configuran para PR, push a main y ejecución manual,
con API 29 y 35. Los tres jobs instalan y comprueban una plataforma 37 real; se
eliminó el enlace que simulaba SDK 37 usando SDK 35. El YAML se valida localmente;
la ejecución alojada en GitHub requiere publicar la rama y no se acredita aquí.

## Límites

El historial antiguo ambiguo se conserva separado: no se puede reconstruir
su autoría automáticamente. No se añadieron outbox, reconciliación de puntos,
actualización del ranking tras recompensas, cambios del analizador QR ni backend.
Las pruebas del backend autenticado y en teléfono físico siguen pendientes.
El issue del backend y el reporte general del crash de Misiones permanecen abiertos.
La prueba de recreación usa `StateRestorationTester`; no representa una prueba
manual de rotación en un teléfono físico.
