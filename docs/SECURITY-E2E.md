# Seguridad y pruebas E2E

## Estado de la auditoría

La aplicación es actualmente **offline-first**: Room es la fuente local, no
hay API ni proveedor OAuth en el APK. Por tanto, los controles de identidad,
roles y progreso local son defensas de la aplicación, no una autorización
server-side. Antes de producción multiusuario se debe añadir un backend que
derive el usuario y el rol desde una sesión validada.

Controles aplicados en el cliente:

- El registro público solo crea usuarios estudiante; el rol tutor no se toma
  del formulario.
- Las rutas de administración comprueban el rol tutor antes de mostrar CRUD.
- Las contraseñas nuevas usan PBKDF2-HMAC-SHA256 con salt aleatorio y migran
  hashes SHA-256 heredados tras un login correcto.
- La base Room usa transacciones para validar QR y crear punto+misión.
- El progreso tiene una clave única por usuario y misión para evitar doble
  recompensa concurrente.
- Los QR tienen índice único y valores no predecibles.
- Las misiones se archivan en lugar de borrar el historial de progreso.
- La base y preferencias quedan fuera de backup y transferencia del dispositivo.
- Los errores de persistencia del formulario y del escáner se muestran como
  estados reintentables.

## Matriz E2E requerida

| Área | Caso | Resultado esperado |
|---|---|---|
| Arranque | Abrir instalación limpia | Splash lleva a login sin usuario activo |
| Sesión | Login institucional válido | Usuario llega a Home y la sesión sobrevive a reinicio |
| Sesión | Contraseña incorrecta/cuenta incompleta | Error visible, sin navegación |
| Sesión | Logout y cambio de cuenta | Sesión anterior no reaparece |
| Roles | Registro público solicita tutor | Siempre se crea estudiante |
| Roles | Estudiante abre ruta CRUD | Se muestra acceso no autorizado y no se modifica nada |
| Roles | Tutor autorizado abre CRUD | Puede listar, crear, editar y archivar |
| Actividades | Crear punto+m misión | Ambas filas se guardan o ninguna si falla la operación |
| Actividades | QR duplicado | Se rechaza sin dejar un punto huérfano |
| Actividades | Campos numéricos inválidos | No se permite guardar valores no válidos |
| QR | Código válido | Se registra una sola completación y se actualizan puntos/nivel |
| QR | Código inválido o de misión archivada | No se otorgan puntos |
| QR | Dos validaciones simultáneas | Solo una recompensa y un registro de progreso |
| Historial | Archivar misión completada | El progreso permanece consultable y no se duplica |
| Persistencia | Rotación/recreación de Activity | Se conserva pantalla y usuario |
| Backup | Restaurar backup de la app | No se restaura base local ni sesión autenticada |
| Release | Instalar APK release | Arranca, navega y conserva recursos tras R8 |

## Ejecución

Validación local reproducible:

```bash
./gradlew check --no-daemon --no-parallel
./gradlew assembleDebug --no-daemon --no-parallel
./gradlew assembleRelease --no-daemon --no-parallel
```

Las pruebas instrumentadas requieren un emulador o dispositivo conectado:

```bash
./gradlew connectedDebugAndroidTest --no-daemon --no-parallel
```

Si no existe un dispositivo conectado, el resultado no es una aprobación E2E:
solo quedan validados compilación, pruebas unitarias y lint. La prueba
instrumentada debe ejecutarse en CI con un emulador API 29+ antes de publicar.

## Requisitos para backend/OAuth

Cuando se incorpore Google OIDC o una API:

1. Validar firma, emisor, audiencia, expiración y `sub` del token en servidor.
2. Derivar `usuarioId` y rol del token/servidor; nunca aceptar esos campos como
   autoridad enviada por Android.
3. Autorizar cada operación CRUD en servidor y registrar auditoría.
4. Validar QR y otorgamiento de puntos en una transacción server-side.
5. Usar TLS, rotación de secretos, expiración/revocación de sesiones y rate
   limiting.
6. Ejecutar pruebas negativas de token expirado, audiencia incorrecta, usuario
   revocado, rol insuficiente y replay de QR.
