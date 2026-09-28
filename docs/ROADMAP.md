# Roadmap — Mapa del campus

## Estado actual

El mapa de "Inicio" (`app/src/main/java/com/example/gamequest/ui/home/CampusMapView.kt`)
es un `Canvas` 2D dibujado a mano, estilo pixel art/RPG cenital: césped a
cuadros, caminos y árboles pintados manualmente, sin usar Google Maps ni
ningún proveedor externo (no requiere API key ni conexión).

Los puntos de interés / actividades se representan como círculos de
16–21 px sin etiqueta de texto, lo que los hace difíciles de ver y tocar
con precisión (reportado en QA como "puntos muy pequeños").

## Dirección futura (pendiente de evaluación)

Se evaluará migrar el mapa a un modo de alta definición, al estilo
Google Maps en 3D.

Antes de implementarlo hay que evaluar:

- **Costos**: uso de la API de Google Maps (facturación por carga/uso,
  necesidad de API key, límites de cuota).
- **Integración con lo demás**: el mapa actual funciona 100% offline
  (RF-20); pasar a Google Maps 3D implica conexión a internet y
  coordinar con el resto del flujo (permisos de ubicación, posiciones
  guardadas de los puntos de interés `posX`/`posY` habría que migrarlas
  a coordenadas reales, escaneo QR, etc).

Esta decisión aún no está tomada; este documento solo registra la
intención para no perder el contexto.
