# Revisión final de la rama — 2026-10-24

La revisión independiente examinó `5ba2b88..467741d`, incluido el código de
identidad, migración, autenticación, sesión y navegación. No identificó hallazgos
críticos; presentó dos hallazgos P2 importantes, aceptados tras contrastarlos
con los consumidores reales.

## Correcciones

1. **Completado confirmado seguido de fallo del perfil.** El evento se guarda
   en una transacción antes de consultar de nuevo el perfil. La recompensa
   confirmada actualiza la copia local una sola vez. Si falla el transporte de
   esa consulta auxiliar, se muestra el completado conservado; una respuesta
   HTTP, cancelación o cambio de identidad no se convierte en acceso offline.
   La consulta correcta del perfil sigue siendo autoritativa.
2. **Restauración con detalle guardado.** `Restoring` conserva el back stack y
   las pantallas privadas muestran carga. Resolver la misma cuenta conserva el
   detalle; resolver otra cuenta o cerrar sesión elimina el contexto anterior.
   Una pila sin propietario conocido se reinicia de forma conservadora.

## Pruebas

Antes de corregir, tres casos dirigidos en Android 35 reprodujeron dos fallos:
`IOException` tras el completado y pérdida del detalle durante restauración.
El caso de cambio de cuenta ya pasó. Se conserva el
[log RED](qa/identity-session-2026-10-23/final-review-red.log).

La verificación posterior usa la suite completa JVM, Lint, compilación debug/
androidTest/release y las suites instrumentadas completas API 29 y 35. Los
resultados y límites están en [QA](QA-identidad-sesion-2026-10-23.md). Esta es
la única pasada de correcciones posterior a la revisión; no se atribuye una
segunda revisión independiente.

## Alcance que el revisor dejó a decisión del implementador

Se mantienen fuera del alcance la subida offline/outbox, frescura del ranking,
reparación de autoría antigua y reconciliación general de snapshots. Los
motivos y costes están registrados en
[decisiones](DECISIONES-identidad-sesion.md).
