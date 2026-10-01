# Traspaso de trabajo para el siguiente agente

## Punto de partida

- Rama: `main`.
- Integración del remoto terminada en `e6b1834` (`Merge remote-tracking branch 'campusquest/main'`).
- La compilación `./gradlew :app:compileDebugKotlin` pasó después de integrar esos cambios. Hay advertencias por el enum obsoleto `CharacterColor`.
- Consulta `docs/ISSUES.md` para el seguimiento general y los documentos enlazados antes de cambiar producto o arquitectura.

## Orden recomendado

### 1. Investigar el cierre de Misiones

Es el pendiente que puede afectar directamente el uso actual de la app. El código ya se revisó y compila, pero todavía no hay una reproducción confirmada ni un logcat del fallo.

1. Reproducir en un dispositivo o emulador con la misma versión instalada.
2. Capturar desde el inicio de la app el bloque `FATAL EXCEPTION` / `AndroidRuntime`, además de los errores inmediatamente anteriores.
3. Si no ocurre, registrar dispositivo, versión Android, pasos probados y resultado; no atribuirlo a una causa sin evidencia.
4. Si se reproduce, corregir la causa y añadir una prueba que cubra el comportamiento afectado cuando sea viable.

Archivos principales: `MissionsScreen.kt`, `MissionsViewModel.kt`, `CampusRepository.kt`, los DAOs y `CampusQuestNavHost.kt`.

### 2. Alinear las decisiones de producto y arquitectura

Antes de iniciar cambios grandes, revisar discrepancias entre `docs/ISSUES.md` y `docs/BACKEND.md`: Issues presenta backend y roles como decisiones/tareas, mientras BACKEND todavía describe el servicio como propuesta por evaluar. Aclarar el alcance antes de agregar dependencias, migraciones o servicios externos.

- **Backend y sincronización:** usar `docs/BACKEND.md` como borrador. Definir proveedor, autenticación real y comportamiento offline antes de implementarlo.
- **Roles:** acordar la migración de `tutor` a `estudiante`/`visitante` y el destino de `ui/crud/`. El panel web está fuera de este repositorio.
- **Premios:** no crear catálogo ni canjes hasta que se defina si son insignias, premios canjeables o ambos.
- **Mapa de alta definición/3D:** el mapa actual es 2.5D offline. Acordar proveedor, costo y requisitos offline antes de integrar SDKs. Revisar `docs/ROADMAP.md` y `docs/MAPA_3D_PLANNING.md`, que describen alternativas distintas.

### 3. Actualizar el seguimiento

Después de cada tarea, marcar `docs/ISSUES.md` con el resultado y enlazar la evidencia o decisión. Mantener este traspaso actualizado si cambian las prioridades. No cerrar un pendiente solo porque compile: registrar también qué se reprodujo o verificó.
