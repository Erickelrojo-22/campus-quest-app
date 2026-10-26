# Investigación: UI para múltiples resoluciones

## Problema observado

En la barra inferior con cinco destinos (`Inicio`, `Misiones`, `QR`,
`Insignias` y `Perfil`), las etiquetas pueden quedarse sin ancho suficiente
en teléfonos compactos. El resultado visible es un salto de línea o texto
truncado —por ejemplo, `Insignias` en dos líneas— que invade el alto de la
barra y desalinéa los ítems.

La causa no es la densidad de píxeles del equipo: es el **ancho disponible en
dp** después de los insets laterales, dividido entre cinco destinos. Por eso
la corrección debe responder a restricciones de tamaño, no usar tamaños de
texto en píxeles ni recursos por modelo de teléfono.

## Hipótesis de solución

La barra compacta debe mostrar el texto únicamente en el destino activo y
usar icono + `contentDescription` en los no activos. Es el patrón previsto
para `NavigationBarItem(alwaysShowLabel = false)` y mantiene los cinco
destinos accesibles sin reducir la tipografía hasta volverla ilegible.

La implementación se validará visualmente; si la versión de Material no
oculta de forma fiable esas etiquetas, se reemplazará el ítem por un
componente equivalente que **no componga el `Text` no seleccionado**, en vez
de confiar en `maxLines`/`Ellipsis`. Esos atributos sólo maquillan el síntoma
y no resuelven el espacio vertical.

Para ancho expandido u orientación horizontal se conserva el riel lateral,
que ya se selecciona con el breakpoint `ANCHO_PANTALLA_AMPLIA` y ofrece ancho
suficiente para nombres completos.

## Matriz de validación inicial

| Perfil | Orientación | Qué debe comprobarse |
| --- | --- | --- |
| 320 x 568 dp | Vertical | Cinco íconos pulsables; sólo el activo expone etiqueta visual; barra sin cortes. |
| 360 x 640 dp | Vertical | Sin salto de línea ni solapamiento, incluidos `Misiones` e `Insignias`. |
| 393/411 x 852/915 dp | Vertical | Insets de cámara y navegación; cambio de pestaña y texto activo alineados. |
| 600 x 960 dp | Vertical | Contenido y navegación respetan insets; confirmar el modo de navegación esperado. |
| Teléfono compacto | Horizontal | Se usa riel lateral, sin perder contenido por barras del sistema. |
| 840 x 1280 dp o superior | Vertical/horizontal | Riel lateral, etiquetas completas y objetivo táctil cómodo. |

Cada perfil se probará con fuente del sistema predeterminada y con tamaño de
fuente grande. La aceptación mínima es: ningún texto cortado/superpuesto,
ningún objetivo táctil inaccesible y los cinco destinos anunciados por
TalkBack.

## Siguientes cambios en esta PR

1. Reproducir el defecto con capturas de los perfiles compactos de la matriz.
2. Añadir una prueba Compose que confirme la semántica de cada destino y una
   comprobación visual en emuladores de las medidas límite.
3. Aplicar y verificar la navegación compacta con etiqueta exclusiva del
   elemento seleccionado; ajustar el breakpoint sólo con evidencia de las
   pruebas.

