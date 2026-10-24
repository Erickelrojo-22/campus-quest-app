# Campus Quest: corrección de identidad, fallback y arranque

## Objetivo y alcance

Corregir los tres hallazgos aceptados en la revisión de `agy` sobre `main`, commit `5ba2b88e322856599de73e466f5fd40cb7b81671`: colisión de identidad local/remota, fallback de visitante tras un rechazo HTTP y navegación antes de resolver la sesión. El resultado debe poder probarse sin utilizar cuentas ni datos del backend de producción.

El usuario pidió un plan. Este documento describe la propuesta; no supone que la implementación o las pruebas ya se hayan realizado.

## Decisiones propuestas

### 1. Identidad local independiente

Mantener `UsuarioEntity.id` como clave local autogenerada y añadir `remoteId: Int?` con índice único. Un visitante nuevo tiene `remoteId = null`; un usuario autenticado se busca por `remoteId`, nunca por una coincidencia con la clave local. Hacer lo mismo con `ProgresoMisionEntity`, porque sus IDs remotos también pueden coincidir con IDs de eventos locales.

Los DTO de usuario y progreso deben distinguir explícitamente los IDs recibidos del servidor. Un adaptador transaccional resuelve la identidad local y traduce las FK antes de persistir. La UI, DataStore y los ViewModels conservan IDs locales; los endpoints reciben IDs remotos. Los IDs del catálogo siguen siendo los del servidor, ya que esta app no crea catálogo local en su configuración remota actual.

Alternativa descartada: reservar IDs negativos para visitantes. Reduce el cambio inicial, pero mantiene el acoplamiento del resto del código a IDs del servidor y no resuelve de forma clara las filas antiguas ambiguas. Separar las claves requiere actualizar más llamadas, pero hace explícita la identidad y permite probarla.

### 2. Migración conservadora a Room v3

Añadir las columnas e índices conservando claves primarias, FK y contenido existentes. Para filas antiguas, `remoteId` comienza en `NULL`: la versión 2 no guarda procedencia suficiente para inferirlo con seguridad. No deducir una identidad remota por nombre, correo ni coincidencia numérica de IDs.

En el primer login o restauración remota validada, crear/resolver una identidad canónica por `remoteId` y descargar su progreso. Los registros antiguos continúan almacenados bajo su identidad local original. La interfaz no los debe fusionar automáticamente con una cuenta remota. Un token antiguo `offline-visitor-N` puede seguir apuntando a la fila local N; un token remoto antiguo sin vínculo local persistido debe validarse antes de usar una identidad canónica. Si no hay conexión, mostrar un estado recuperable; no adivinar la cuenta.

Esto preserva los datos, pero no permite reconstruir automáticamente la autoría de progreso que ya hubiera sido mezclado antes de actualizar. Esa recuperación, si fuera necesaria, requiere otra tarea y evidencia adicional. No utilizar migración destructiva ni borrar bases de datos.

### 3. Fallback solo ante fallos de transporte

Preservar la causa de un fallo remoto hasta decidir el fallback de visitante. Cualquier `ApiException`, incluidos HTTP 4xx y 5xx, conserva el rechazo y no crea token ni usuario offline. Un `IOException` de transporte permite el modo local. Cancelación, errores de JSON y de persistencia no se convierten en desconexión.

Mantener los resultados y mensajes que consume la UI. Añadir interfaces pequeñas de API y almacenamiento de sesión para simular estos casos sin red y conservar una sola política de fallback.

### 4. Navegación derivada del estado de sesión

Publicar un estado explícito: `Restoring`, `SignedOut`, `Authenticated` o `RecoveryRequired`. El splash no decide una ruta por un temporizador; puede conservar su animación visual, pero la navegación espera el estado resuelto. La restauración de identidad no debe esperar además a descargar todo el catálogo, el ranking y el progreso: esa sincronización empieza después, ligada a la misma sesión.

Un 401 vigente pasa a `SignedOut`, muestra un mensaje y elimina el back stack privado. Una desconexión con un vínculo de sesión e identidad local válido permite mostrar la caché; una sesión antigua no vinculada requiere recuperación sin exponer otra cuenta. Un fallo sin caché ofrece reintentar o volver al login, sin un indicador de carga permanente.

La sesión persistida vincula token e ID local en una misma escritura. El logout invalida primero la sesión local y conserva la revocación remota con el token capturado. Los resultados tardíos se descartan usando una generación de sesión; un 401 de un token anterior no invalida un login nuevo.

El coordinador centraliza las escrituras de token, preferencias de usuario activo y estado bajo una misma serialización. AuthRepository solicita ese commit después de persistir la identidad; CampusApi informa el token rechazado y el coordinador decide su invalidación. No hay dos propietarios compitiendo para cerrar o abrir una sesión.

## Requisitos verificables

1. Un visitante local y un usuario remoto con el mismo número de ID mantienen perfiles y progreso separados.
2. Dos sincronizaciones de la misma cuenta o evento no crean duplicados.
3. Actualizar de v2 a v3 conserva filas y FK; también existe una ruta válida desde v1.
4. Un HTTP 400/401/403/422/429/500 del login de visitante no crea usuario ni token offline.
5. Un fallo de transporte sí permite el visitante local; una cancelación no lo crea.
6. Con preferencias lentas, la app permanece en restauración hasta conocer la sesión correcta.
7. Un 401 durante la restauración o con MAIN abierto lleva al login y Atrás no recupera pantallas privadas.
8. El ranking sigue identificando al usuario correcto aunque su ID local difiera del remoto. Esta adaptación de identidad no incluye reparar la actualización del puntaje del ranking, que es otro hallazgo.
9. Las pruebas funcionan con fakes, HTTP simulado o bases de prueba; no llaman a producción.

## Fuera del alcance

Outbox offline, reparación automática de historial ya mezclado, sincronización general de snapshots, actualización del ranking tras recompensas, mejoras del analizador QR, rediseño visual, nuevos roles y cambios del backend. No aplicar las seis observaciones de `agy` que no quedaron demostradas.

## Entorno

Kotlin 2.2.10, Compose, Room 2.8.5, Coroutines 1.10.2 y Navigation Compose 2.10.1, según el catálogo actual. Mantener minSdk 29, compileSdk/targetSdk 37 y JDK 21 para Gradle/CI. Añadir únicamente bibliotecas de prueba necesarias, con versiones ya existentes en el catálogo.
