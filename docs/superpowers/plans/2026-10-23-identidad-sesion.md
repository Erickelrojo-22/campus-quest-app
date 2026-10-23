# Campus Quest: identidad y sesión Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Corregir los tres hallazgos validados preservando el progreso local, los controles del servidor y una navegación de sesión determinista.

**Architecture:** Separar DTO remotos y entidades Room; traducir la identidad en un adaptador transaccional y usar claves locales en UI/FK. Compartir la clasificación de errores y publicar el estado de sesión desde un coordinador comprobable sin Android. La navegación observa ese estado y la sincronización de catálogo se ejecuta después de resolver la identidad.

**Tech Stack:** Kotlin 2.2.10, Compose, Room 2.8.5, DataStore 1.2.1, Coroutines 1.10.2, Navigation Compose 2.10.1, JUnit 4 y pruebas instrumentadas Android.

**Spec:** [Diseño y criterios de aceptación](../specs/2026-10-23-identidad-sesion-design.md).

## Base y modo de ejecución

- Plan sobre `main` en `5ba2b88e322856599de73e466f5fd40cb7b81671`; `git ls-remote` confirmó esa misma punta al preparar el plan.
- Antes de implementar: comprobar otra vez `main`, leer las instrucciones del checkout y reutilizar o crear un worktree aislado mediante las herramientas de Codex. Revalidar este plan si cambiaron las áreas afectadas.
- Recomendación: ejecución nativa en esta sesión, por tareas; los cambios comparten contratos de identidad y sesión. Una revisión independiente al finalizar puede hacerse con `agy` por extractos, contrastando sus conclusiones.
- Cada tarea termina compilable y con sus pruebas. Preparar un commit por tarea en la rama de trabajo, sin publicar ni fusionar automáticamente.

## Global Constraints

- Alcance: solo los tres hallazgos de la última revisión validados en el diseño.
- minSdk 29, compileSdk/targetSdk 37; JDK 21 para Gradle/CI.
- Mantener las versiones del catálogo; nuevas dependencias solo de pruebas y alineadas con Room/Navigation existentes.
- Ninguna prueba usa credenciales, cuentas o endpoints de producción.
- No borrar bases, no usar `fallbackToDestructiveMigration` y no fusionar historial antiguo por inferencia.
- Las instrucciones de equipo en `docs/EQUIPO_ROLES.md` se consultan al ejecutar; conservar diseño visual y cambios de UI limitados al estado de sesión y a la identidad del ranking.

## Review Focus

1. Filas v2 ambiguas: conservarlas sin convertir su número local en identidad remota — tarea 2.
2. Login remoto nuevo durante un refresh anterior: el resultado viejo no modifica la sesión nueva — tareas 2 y 3.
3. HTTP 429/500 y errores de JSON: no deben convertirse en visitante offline — tarea 1.
4. Restauración con caché, sin caché o con un token antiguo no vinculado: salida explícita y correcta — tarea 3.
5. Expiración con una pantalla privada abierta y pulsación Atrás: no recuperar rutas de la sesión anterior — tarea 4.

## Mapa de archivos

Las rutas de las tareas son relativas a la raíz del repositorio. `src` significa `app/src/main/java/com/example/gamequest`; `test` significa `app/src/test/java/com/example/gamequest`; `androidTest` significa `app/src/androidTest/java/com/example/gamequest`.

| Área | Archivos | Responsabilidad |
| --- | --- | --- |
| Frontera remota | `src/data/remote/CampusGateway.kt` (nuevo), `CampusApi.kt` | Contrato sustituible en pruebas y llamadas HTTP |
| Sesión persistida | `src/data/remote/TokenStore.kt` (nuevo), `SessionTokenStore.kt` | Token y vínculo local coherentes; compatibilidad de lectura |
| Errores | `src/data/remote/OfflineFallbackPolicy.kt` (nuevo), `src/data/repository/AuthRepository.kt` | Clasificación de transporte y rechazo |
| Identidad | `src/data/remote/RemoteModels.kt`, `LocalIdentityStore.kt` (nuevos), entidades y DAOs de usuario/progreso | Traducir IDs sin colisiones |
| Base de datos | `src/data/local/AppDatabase.kt`, `app/schemas/com.example.gamequest.data.local.AppDatabase/3.json` | Migración 2→3 y esquema exportado |
| Integración de datos | `RemoteCampusRepository.kt`, `src/data/repository/CampusRepository.kt`, `AppContainer.kt` | IDs correctos en caché, red y consumidores |
| Clasificación | `src/data/model/RankingEntry.kt` (nuevo), `ProfileViewModel.kt`, `ProfileScreen.kt`, `LeaderboardDialog.kt` | Identidad estable en ranking sin confundir ID local/remoto |
| Coordinación | `src/data/session/SessionState.kt`, `SessionCoordinator.kt` (nuevos), `SessionViewModel.kt` | Restaurar, invalidar y publicar una sesión |
| Navegación | `CampusQuestNavHost.kt`, `SplashScreen.kt` | Rutas derivadas del estado de sesión |
| Calidad | Pruebas nombradas abajo, catálogo Gradle, `app/build.gradle.kts`, `.github/workflows/android-ci.yml` | Casos de regresión y gates de entrega |

Los archivos ya existentes se encuentran en los paquetes vistos durante la revisión; los nombres sin directorio repiten el paquete indicado en su fila. No introducir una reorganización general del proyecto.

---

### Tarea 1: Preservar el rechazo del servidor en el acceso de visitante

**Files:** crear `src/data/remote/CampusGateway.kt`, `TokenStore.kt`, `OfflineFallbackPolicy.kt`; modificar `CampusApi.kt`, `SessionTokenStore.kt`, `RemoteCampusRepository.kt`, `src/data/repository/AuthRepository.kt`, `AppContainer.kt`; modificar `test/data/repository/AuthRepositoryTest.kt`, `test/data/remote/OfflineFallbackPolicyTest.kt`; crear `test/data/remote/FakeCampusGateway.kt` y `FakeTokenStore.kt`.

**Interfaces:**
- `CampusGateway`: inicialmente reproduce las operaciones públicas actuales de CampusApi (`authenticate`, `me`, `logout`, `puntos`, `misiones`, `progreso`, `completar`, `ranking`), `tokens: TokenStore` y `onUnauthorized`. CampusApi lo implementa; los repositorios dependen de la interfaz.
- `TokenStore`: `val token: String?`, `fun save(value: String)`, `fun clear()`. Mantener inicialmente el contrato actual; la tarea 2 lo amplía para el vínculo local.
- Mover, sin duplicar, `internal fun Throwable.permiteFallbackOffline(): Boolean` a `OfflineFallbackPolicy.kt`.
- Conservar `AuthResult.Exito(usuario)` y `AuthResult.Error(mensaje)` como contrato de UI.

- [ ] Escribir los casos de regresión antes de cambiar la decisión de fallback:

| Test | Preparación | Afirmaciones |
| --- | --- | --- |
| `visitante_rechazoHttp_noCreaSesionLocal` | Fake lanza ApiException para 400/401/403/422/429/500 | Error; 0 inserciones; 0 escrituras de token offline |
| `visitante_errorTransporte_permitaFallback` | Fake lanza IOException | Exito local; visitante sin credenciales; token offline de su ID local |
| `visitante_cancelacion_noCreaSesionLocal` | Fake lanza CancellationException | Propagar cancelación; ninguna escritura |
| `visitante_respuestaInvalida_noCreaSesionLocal` | Fake lanza error de parseo | Error; ninguna escritura |
| `visitante_exitoRemoto_noCreaVisitante` | Fake autentica | Solo persiste la cuenta remota; sin fallback |

- [ ] Ejecutar `./gradlew.bat :app:testDebugUnitTest --tests '*AuthRepositoryTest' --tests '*OfflineFallbackPolicyTest'`; comprobar que el caso HTTP falla por el acceso local que reproduce el hallazgo, no por un error de compilación.
- [ ] Introducir las interfaces y adaptar los fakes existentes. Separar autenticación remota de decisión de fallback: la operación remota conserva la excepción hasta clasificarla; la conversión a `AuthResult.Error` ocurre después de decidir. Un error de Room o de almacenamiento del token tampoco es un fallo de transporte autenticando.
- [ ] Repetir las pruebas dirigidas y ejecutar `:app:compileDebugKotlin`; todos los casos pasan, se conserva la cancelación y no hay cambios de conducta del login institucional.
- [ ] Revisar el diff y crear un commit: `fix: respect visitor authentication rejections`.

### Tarea 2: Separar claves locales y remotas con migración conservadora

**Files:** crear `src/data/remote/RemoteModels.kt`, `LocalIdentityStore.kt`, `src/data/model/RankingEntry.kt`; modificar `src/data/local/entity/UsuarioEntity.kt`, `ProgresoMisionEntity.kt`, `src/data/local/dao/UsuarioDao.kt`, `ProgresoMisionDao.kt`, `src/data/local/AppDatabase.kt`, API/gateway, AuthRepository, RemoteCampusRepository, CampusRepository, TokenStore/SessionTokenStore y AppContainer. Adaptar `src/ui/profile/{ProfileViewModel,ProfileScreen,LeaderboardDialog}.kt` y los fakes afectados. Crear `androidTest/data/local/IdentityMigrationTest.kt`, `androidTest/data/remote/LocalIdentityStoreTest.kt`, `test/data/remote/RemoteIdentityRoutingTest.kt`.

**Interfaces y decisiones:**
- Añadir al final de los constructores de `UsuarioEntity` y `ProgresoMisionEntity` `remoteId: Int? = null`, con índice único por tabla. `id` sigue siendo PK local autogenerada.
- DTO: `RemoteUserDto(remoteId: Int, nombres: String, correoInstitucional: String, carrera: String, puntajeAcumulado: Int, nivel: Int, rol: String)`; `RemoteProgressDto(remoteId: Int, remoteUserId: Int, misionId: Int, fechaHora: Long, puntosObtenidos: Int, codigoQrValidado: String, estado: String)`; `AuthPayload(accessToken: String, user: RemoteUserDto)`. Mantener las claves JSON actuales y el hash local vacío al mapear usuarios remotos.
- `CampusGateway.me(): RemoteUserDto`, `authenticate(path, body): AuthPayload`, `progreso(remoteUserId): List<RemoteProgressDto>`, `completar(remoteUserId, misionId, codigo): RemoteProgressDto`.
- `LocalIdentityStore.guardarUsuario(dto: RemoteUserDto): UsuarioEntity` y `guardarProgreso(dto: RemoteProgressDto, localUserId: Int): ProgresoMisionEntity`, ambas suspend. Resolver por `remoteId`, conservar PK local existente y comprobar la FK remota antes de insertar. La operación de snapshot completa debe ser transaccional y verificar que no cambió la sesión.
- `RankingEntry(remoteUserId: Int?, localUserId: Int?, nombres: String, puntajeAcumulado: Int, nivel: Int)`: remoto se identifica por `remoteUserId`; fallback local, por `localUserId`. UI recibe ambas identidades del usuario actual y no cruza los espacios. No introducir usuarios ficticios en Room solo para llenar el ranking.
- Persistir un `SessionSnapshot(token: String?, localUserId: Int?)` coherente. `TokenStore` expone `val snapshot`; `token` deriva de ese snapshot, y `save(value: String, localUserId: Int? = null)` guarda ambos en una escritura. Leer los tokens cifrados antiguos sin vínculo como `localUserId = null`.
- `CampusApi.authenticate` devuelve el payload sin guardar el token. AuthRepository resuelve primero la identidad en Room y luego guarda token + ID local; la persistencia fallida no concede una sesión.

- [ ] Añadir las regresiones de identidad con valores concretos:

| Test | Escenario | Resultado esperado |
| --- | --- | --- |
| `visitanteYRemoto_mismoNumero_noSeMezclan` | Visitante local 51 con progreso; DTO remoto 51 | 2 filas de usuario; visitante conserva historial; ID local remoto distinto de 51; remoto no hereda ese progreso |
| `eventoLocalYRemoto_mismoNumero_noSeSobrescriben` | Evento local 1 y remoto 1 de otra identidad | PK locales distintas; ninguna FK cambia de dueño |
| `snapshotRepetido_noDuplicaCuentaNiEvento` | Guardar dos veces mismo remoto | Mismos IDs locales y mismos recuentos |
| `solicitudProgreso_usaRemoteId` | Cuenta local 8 vinculada al remoto 51 | Gateway recibe 51; Room/UI consultan 8 |
| `progresoDeOtroUsuario_seRechaza` | DTO trae remoteUserId de otra cuenta | Sin escrituras; error explícito |
| `ranking_resaltaCuentaConIdsDistintos` | Usuario local 8/remoto 51 | Solo la entrada remota 51 es la actual |
| `tokenNoPersistido_noAutentica` | Fallo guardando el snapshot | No se publica login exitoso ni se reutiliza un token de otra cuenta |

- [ ] Probar primero la colisión contra el comportamiento actual mediante un test Room con gateway fake. Confirmar el fallo antes del cambio; usar Android para Room real, no solo un DAO falso.
- [ ] Implementar `MIGRATION_2_3`: añadir columnas e índices, dejando `remoteId = NULL` en filas anteriores; conservar todos los IDs y FK. Registrar la migración en ambos builders (`campus_quest.db` y `campus_quest_remote.db`). Mantener `MIGRATION_1_2` y exportar `app/schemas/com.example.gamequest.data.local.AppDatabase/3.json`.
- [ ] Añadir `androidx.room:room-testing` con la versión `libs.versions.room` y exponer `app/schemas` como assets de androidTest. `IdentityMigrationTest` valida esquema 2→3, contenido, FK e integridad; añadir el camino 1→2→3 con fixture sin duplicados, respetando la migración previa. Verificar también creación limpia de v3.
- [ ] Implementar los DTO, adaptador, DAO por remoteId y traducción de llamadas. En progreso existente de la identidad canónica, resolver por remoteId o por `(localUserId, misionId)` antes de actualizar; nunca conservar un ID remoto como PK local ni adoptar una fila perteneciente a otra identidad.
- [ ] No marcar las filas ambiguas antiguas como remotas por coincidencia de ID/nombre/correo. Un remoto validado obtiene una fila canónica y recupera su snapshot del servidor; el historial anterior se conserva bajo la fila original. Una sesión offline antigua sigue usando su ID local y solo puede reutilizar perfiles sin credenciales y sin remoteId.
- [ ] Adaptar el ranking y las firmas de los consumidores/fakes en la misma tarea. Mantener las pantallas y las rutas de UI usando IDs locales. No añadir aquí la mejora independiente de refrescar puntajes del ranking.
- [ ] Ejecutar `:app:testDebugUnitTest --tests '*RemoteIdentityRoutingTest' --tests '*AuthRepositoryTest' --tests '*ProfileViewModelTest'`, `:app:compileDebugAndroidTestKotlin` y las pruebas instrumentadas `IdentityMigrationTest`/`LocalIdentityStoreTest`. Esperar 0 fallos y ninguna pérdida o reasignación de filas del fixture.
- [ ] Crear un commit: `fix: isolate local and remote identities`.

### Tarea 3: Resolver y publicar el estado de sesión

**Files:** crear `src/data/session/SessionState.kt`, `SessionCoordinator.kt`, `SessionCommitter.kt` y `SessionAccess.kt`; modificar `src/ui/common/SessionViewModel.kt`, TokenStore/SessionTokenStore, RemoteCampusRepository, AuthRepository, CampusApi y AppContainer. Crear `test/data/session/SessionCoordinatorTest.kt` y ampliar fakes.

**Interfaces:**
- `SessionState`: `Restoring`; `SignedOut(reason: String? = null)`; `Authenticated(localUserId: Int, source: SessionSource, offline: Boolean)`; `RecoveryRequired(message: String)`.
- `SessionSource`: `REMOTE` o `LOCAL_VISITOR`.
- `SessionCoordinator.state: StateFlow<SessionState>`; `suspend fun restore()`; `suspend fun acceptLogin(localUserId: Int)`; `suspend fun logout()`; `suspend fun retryRestore()`. Sesión inicial en Restoring.
- `SessionCommitter.commitAuthenticated(token: String, localUserId: Int)` es suspend; SessionCoordinator implementa esta interfaz. Al terminar la autenticación y persistencia de usuario, AuthRepository solicita este commit en vez de escribir el token directamente. El coordinador serializa todas las mutaciones de sesión, incluida la preferencia de usuario activo, mediante el mismo Mutex. `acceptLogin` confirma de forma idempotente el usuario ya vinculado y no repite solicitudes.
- `SessionAccess`: `suspend fun resolverIdentidad(): UsuarioEntity` y `suspend fun sincronizarCatalogoYProgreso(localUserId: Int)`. RemoteCampusRepository lo implementa separando identidad de la descarga completa; AppContainer lo inyecta.
- `TokenStore.clearIfCurrent(expectedToken: String): Boolean` compara e invalida atómicamente. El callback `CampusGateway.onUnauthorized: (String) -> Unit` incluye el token rechazado. CampusApi solo notifica; SessionCoordinator es el único propietario de la invalidación, usa ese compare-and-clear y verifica una generación de sesión antes de modificar preferencias/estado tras una suspensión. Así no se invalida dos veces ni se comprueba un token que otro componente ya borró.
- SessionViewModel conserva los métodos públicos que llama la UI y delega el ciclo de sesión al coordinador; no mantiene otro estado de autenticación contradictorio en `ready` y `usuarioActual`.

- [ ] Escribir `SessionCoordinatorTest` con scheduler de Coroutines y fakes: `sinToken_esperaLimpiezaAntesDeSignedOut`; `preferenciasLentas_noAutenticaPorIdObsoleto`; `tokenValido_resuelveAntesDelCatalogo`; `tokenAntiguo_sinVinculo_sinRed_requiereRecuperacion`; `visitanteAntiguo_conservaIdLocal`; `sinRed_conVinculoValido_usaCache`; `sinRed_sinCache_requiereRecuperacion`; `http401_invalidaSesion`; `resultadoTardio_noCambiaLoginNuevo`; `cancelacion_noPublicaErrorDeRed`; `logout_limpiaLocalAntesDeRevocarConTokenCapturado`.
- [ ] Para cada prueba afirmar el estado y sus IDs, número de escrituras y ausencia de llamadas con identidad ajena. Antes de implementar, comprobar al menos la regresión de arranque o 401 con un caso equivalente del flujo actual; las nuevas interfaces no sustituyen la reproducción del defecto.
- [ ] Implementar el coordinador: cargar preferencias reales, resolver token/vínculo, publicar identidad, después iniciar sincronización de datos bajo la misma generación de sesión. En error de transporte, usar solo la caché de una identidad vinculada; una fila antigua sin vínculo no basta para autenticar.
- [ ] Implementar invalidación y logout sin restaurar resultados tardíos. Un token o sesión nuevo vuelve obsoletos los refresh anteriores. Conservar el cierre local inmediato y el aviso si no se confirma revocación remota.
- [ ] Ejecutar `:app:testDebugUnitTest --tests '*SessionCoordinatorTest' --tests '*AuthViewModelTest' --tests '*RegisterViewModelTest'` y `:app:compileDebugKotlin`; todos pasan.
- [ ] Crear un commit: `fix: make session restoration explicit`.

### Tarea 4: Navegar según sesión y comprobar el NavHost real

**Files:** modificar `src/ui/navigation/CampusQuestNavHost.kt`, `src/ui/splash/SplashScreen.kt`, `src/ui/common/SessionViewModel.kt`, AppContainer y `app/build.gradle.kts`; crear `src/CampusQuestDependencies.kt`, `androidTest/SessionNavigationTest.kt` y `androidTest/FakeCampusQuestDependencies.kt`; conservar `MissionsNavigationTest.kt` funcionando.

**Interfaces:**
- Extraer el grafo comprobable como `CampusQuestNavGraph(navController: NavHostController, sessionState: SessionState, container: CampusQuestDependencies, onLogin: (Int) -> Unit, onLogout: () -> Unit, onRetryRestore: () -> Unit)`. El wrapper actual sigue construyendo el controller y observando el ViewModel; los callbacks invocan sus métodos.
- `CampusQuestDependencies` expone `authRepository: AuthRepository`, `campusRepository: CampusRepository`, `preferencesRepository: UserPreferencesRepository`, `soundEffectManager: SoundEffectManager` y `sessionCoordinator: SessionCoordinator`. AppContainer lo implementa. El grafo no inicia el coordinador; los tests le pasan directamente un estado controlado y un contenedor con Room de prueba y gateway fake. Probar el mismo grafo de producción, no crear otro en el test.
- Splash deja de recibir `onTimeout` como autoridad de autenticación. Restoring conserva splash/carga; RecoveryRequired presenta «Reintentar» y «Volver al inicio de sesión»; SignedOut lleva al login; Authenticated entra a MAIN si se está en una ruta pública.

- [ ] Escribir pruebas del grafo de producción: `restoring_masDe1100ms_noAbreMainNiLogin`; `authenticated_abreMainUnaVez`; `signedOut_abreLogin`; `recoveryRequired_muestraReintento`; `errorSinCache_puedeVolverAlLogin`; `http401EnMain_limpiaBackStackPrivado`; `http401EnDetalle_limpiaBackStackPrivado`; `rotacion_noDuplicaNavegacion`; `loginNuevo_noRecuperaPantallasDeLaCuentaAnterior`.
- [ ] Reproducir el arranque basado en tiempo y el 401 sin redirección antes de arreglar el grafo. No cubrirlo solo con un NavHost de juguete como el del test de Misiones actual.
- [ ] Añadir `androidx.navigation:navigation-testing` alineado con `libs.versions.navigationCompose` para inyectar `TestNavHostController`. Los destinos mantienen callbacks de navegación y sus pantallas visuales.
- [ ] Implementar navegación por estado y sesión con un efecto estable. Limpiar rutas privadas al pasar a SignedOut; impedir navegación duplicada al recomponer. Un cambio de cuenta reinicia el contexto privado aunque ambos estados sean Authenticated; avanzar entre destinos de la misma cuenta no devuelve al usuario a MAIN.
- [ ] Ejecutar `:app:compileDebugAndroidTestKotlin`, las instrumentadas `SessionNavigationTest` y `MissionsNavigationTest`; con MAIN o detalle abierto, un 401 muestra login y Atrás no muestra datos privados.
- [ ] Crear un commit: `fix: drive navigation from resolved session`.

### Tarea 5: Validación integrada y entrega

**Files:** modificar `.github/workflows/android-ci.yml`, `docs/SECURITY-E2E.md`, `docs/ISSUES.md`; añadir evidencia de QA en un archivo dentro de `docs/`. Reutilizar los tests de tareas 1–4, sin crear otra suite que copie su lógica.

- [ ] Ejecutar la suite completa: `./gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:compileDebugAndroidTestKotlin :app:assembleDebug :app:assembleRelease --stacktrace`. Revisar reportes, no solo el código de salida.
- [ ] Ejecutar `./gradlew.bat :app:connectedDebugAndroidTest` en un emulador API 29 y en uno API 35 o superior. Incluir migraciones, identidad y navegación; guardar XML y capturas/logs de los escenarios afectados. Si no hay emulador o una plataforma real del SDK, documentar el bloqueo y no declarar el gate superado.
- [ ] Ampliar el job instrumentado de CI para que las regresiones de migración/sesión se ejecuten también en PR, manteniendo la compilación de todos los androidTest. Usar fixtures/fakes sin secretos. No dar por válida una compilación contra una plataforma 35 enlazada bajo el nombre 37; comprobar el SDK real y registrar esa limitación si el CI actual sigue usándolo. Resolver disponibilidad del SDK como gate de ejecución, sin mezclar una actualización general de herramientas en estos fixes.
- [ ] Hacer el recorrido integrado en una base de prueba v2: crear visitante, guardar progreso, actualizar a v3, abrir sin red, iniciar sesión remota simulada con ID coincidente, restaurar sesión, forzar 403 y después 401. Verificar que no se mezcla progreso, 403 no abre modo offline y 401 lleva al login.
- [ ] Actualizar SECURITY-E2E para reflejar la arquitectura con API; registrar versión/esquema, pasos y evidencia en ISSUES. No cerrar el issue general del backend o el crash de Misiones basándose solo en estos cambios.
- [ ] Revisar el diff completo por integridad de datos, traducción de IDs y resultados tardíos. Si se usa agy, enviar partes pequeñas y exigir escenarios/rutas; contrastar cada propuesta con código y pruebas.
- [ ] Preparar commit final de pruebas/documentación y una descripción de PR con: tres defectos, decisión de migración, pruebas ejecutadas, límites y tratamiento de datos antiguos. Crear o publicar el PR únicamente dentro de la ejecución que el usuario autorice; no fusionar automáticamente.

## Criterio de finalización

Los nueve requisitos verificables del diseño están cubiertos por las tareas anteriores; todas las pruebas y builds requeridos tienen evidencia; los datos antiguos permanecen almacenados sin reasignación silenciosa; y la navegación de sesión funciona tanto al arrancar como al expirar con la app abierta. Un build correcto sin los tests de Room y navegación no basta.

## Riesgos que deben figurar en la entrega

- La migración conserva historial ambiguo, pero no puede reparar automáticamente autorías ya mezcladas. Las cuentas remotas pueden ver una caché reconstruida tras validarse; las filas antiguas se conservan separadas.
- El cambio de IDs requiere adaptar también ranking, progreso y endpoints; traducir solo UsuarioEntity dejaría un arreglo incompleto.
- Las pruebas propuestas todavía no se han escrito ni ejecutado. La revisión previa fue estática.

## Referencias de pruebas

- [Room: migrar y probar migraciones](https://developer.android.com/training/data-storage/room/migrating-db-versions): usar migraciones registradas, conservar esquemas y validar la actualización con datos.
- [Navigation Compose: probar el NavHost](https://developer.android.com/guide/navigation/testing/compose): comprobar el grafo real y sus acciones con un controller inyectable.
