# Propuesta de PR: identidad local/remota y ciclo de sesión

## Cambios

- Separar IDs de usuarios y progreso con DTO, mapeo transaccional e índices únicos.
- Migrar Room a v3 conservando registros antiguos sin inferir vínculos remotos.
- Respetar rechazos HTTP y errores de datos/persistencia al autenticar visitantes.
- Centralizar token, vínculo local, preferencias y estado de sesión.
- Navegar desde el estado resuelto, limpiar rutas privadas ante 401 y reiniciar
  el contexto al cambiar de cuenta.
- Adaptar ranking, probar migraciones/grafo real e incluir API 29/35 en CI de PR.

## Datos antiguos

`remoteId` empieza en NULL para todas las filas heredadas. El visitante conserva
su ID e historial local; el remoto validado utiliza una identidad canónica nueva
y su snapshot remoto. No se fusiona historial por nombre, correo o ID coincidente.

## Validación y límites

90 pruebas JVM y 25 instrumentadas en cada API (29/35), sin fallos. Lint sin errores (49 advertencias); APK debug y release con R8 compilados.

Consultar [evidencia de QA](QA-identidad-sesion-2026-10-23.md) y
[arquitectura de seguridad](SECURITY-E2E.md). El backend real no se certifica con
estas pruebas. No cerrar el issue general de backend ni el crash de Misiones.
