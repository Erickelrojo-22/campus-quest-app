# Decisiones de ejecución — identidad y sesión

| Decisión | Motivo | Coste o límite |
|---|---|---|
| Utilizar un worktree Git normal en una rama aislada | La creación/adjunto gestionados por la app rechazaron el contexto de este chat | Se administra con Git; no con el archivado de worktrees gestionados de la app |
| Aplicar el plan y delegar la matriz/documentación a agy | Autorización del usuario; cambios de UI limitados a sesión e identidad | La revisión de rama debe considerar las conexiones con pantallas compartidas |
| Usar encabezados `Task` en el plan y descartar el token ilustrativo inventado por agy | El helper solo reconoce ese encabezado; el token debe proceder del servidor | Cambio cosmético del plan; el código de ejemplo de agy no se reutilizó |
| Conservar `carrera` opcional en `RankingEntry` y agrupar regresiones Room en `IdentityCollisionTest` | Preservar la presentación existente y evitar otra suite duplicada | Un campo opcional de presentación; los casos del adaptador están en la suite de colisiones |
| Alinear JSON/serialization a 1.8.1 | El dispositivo reprodujo `AbstractMethodError` al cargar 1.7.3 con los serializers de Room testing | Cambia una dependencia transitiva de Navigation; los gates completos JVM/Android cubren sus consumidores actuales |
| Extraer el grafo antiguo antes de implementar el coordinador | Reproducir el fallo real de arranque y 401, sin un grafo de juguete | La preparación de tests cruza el orden de las tareas 3 y 4; la rama final está verificada completa |

La migración conserva las filas antiguas separadas y no reconstruye autorías ya
mezcladas. Esta decisión pertenece al diseño aprobado y se detalla en
[seguridad](SECURITY-E2E.md) y [QA](QA-identidad-sesion-2026-10-23.md).

## Alcance confirmado en la revisión final

Se aceptaron dos hallazgos P2: conservar el evento de una misión ya confirmada
si falla la consulta posterior del perfil y preservar el detalle guardado
mientras se restaura la identidad de la misma cuenta. Ambos están cubiertos por
pruebas de regresión sobre Room y el grafo de producción, ejecutadas en API 29/35.

Se mantienen estas exclusiones del diseño aprobado:

- Outbox y subida de progreso offline: no hay carga automática de eventos locales.
- Frescura de puntajes del ranking: pueden permanecer desactualizados hasta refrescar.
- Reparación de autoría histórica: v3 conserva registros ambiguos separados y no
  cambia la depuración de duplicados que ya hacía v1→v2.
- Reconciliación general de snapshots de catálogo/progreso: pueden quedar registros
  en caché. Conservar una respuesta de completado confirmada sí pertenece a esta
  corrección y no introduce una cola de operaciones.

## Entrega y auxiliares

Los cambios están en la rama `codex/campus-quest-identity-session`, con una
[descripción de PR preparada](PR-identidad-sesion.md). La evidencia reproducible
queda versionada en `docs/qa/identity-session-2026-10-23`.

Los auxiliares de ejecución y los AVD se conservan en la carpeta ignorada
`.superpowers/sdd/2026-10-23-identidad-sesion` para mantener el registro y el
entorno de pruebas. Su coste es espacio local adicional.
