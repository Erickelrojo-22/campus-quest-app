package com.example.gamequest.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.math.sqrt

/**
 * Especies / tipos de personaje disponibles en la app.
 */
enum class CharacterSpecies(
    val label: String,
    val folder: String,
    /** Nombre legible de la zona primaria (cuerpo / capucha / pelaje). */
    val primaryZoneLabel: String,
    /** Nombre legible de la zona secundaria (pañuelo / rostro / vientre). */
    val secondaryZoneLabel: String,
) {
    DUDE(
        label = "Dude",
        folder = "dude",
        primaryZoneLabel = "Cuerpo",
        secondaryZoneLabel = "Pañuelo",
    ),
    PINK(
        label = "Pink",
        folder = "pink",
        primaryZoneLabel = "Pelaje",
        secondaryZoneLabel = "Vientre",
    ),
    OWLET(
        label = "Owlet",
        folder = "owlet",
        primaryZoneLabel = "Capucha",
        secondaryZoneLabel = "Rostro",
    ),
}

/**
 * Paleta de 16 colores pixel-art con rampas de sombreado (light → mid → dark → deep).
 *
 * Cada color se usa tanto para la zona primaria como la secundaria del avatar.
 * El campo [preview] es un ARGB compacto para la miniatura de selector de la UI.
 */
enum class AvatarColor(
    val label: String,
    val preview: Int,
    val light: Int,
    val mid: Int,
    val dark: Int,
    val deep: Int,
) {
    COBALT_BLUE(
        label   = "Cobalto",
        preview = 0xFF0696DB.toInt(),
        light   = 0xFF0FEFFB.toInt(),
        mid     = 0xFF0696DB.toInt(),
        dark    = 0xFF03396B.toInt(),
        deep    = 0xFF021E3C.toInt(),
    ),
    ICE_BLUE(
        label   = "Celeste",
        preview = 0xFF7EC8E3.toInt(),
        light   = 0xFFB8E8F5.toInt(),
        mid     = 0xFF7EC8E3.toInt(),
        dark    = 0xFF3D8EAF.toInt(),
        deep    = 0xFF1E5A73.toInt(),
    ),
    EMERALD(
        label   = "Esmeralda",
        preview = 0xFF23B437.toInt(),
        light   = 0xFF50F064.toInt(),
        mid     = 0xFF23B437.toInt(),
        dark    = 0xFF14691E.toInt(),
        deep    = 0xFF0A370F.toInt(),
    ),
    LIME(
        label   = "Lima",
        preview = 0xFF8CD43C.toInt(),
        light   = 0xFFCCFF66.toInt(),
        mid     = 0xFF8CD43C.toInt(),
        dark    = 0xFF4E8C14.toInt(),
        deep    = 0xFF2D5508.toInt(),
    ),
    RUBY_RED(
        label   = "Rubí",
        preview = 0xFFC82828.toInt(),
        light   = 0xFFFF5F5F.toInt(),
        mid     = 0xFFC82828.toInt(),
        dark    = 0xFF871616.toInt(),
        deep    = 0xFF4B0C0C.toInt(),
    ),
    CRIMSON(
        label   = "Carmesí",
        preview = 0xFF8C1443.toInt(),
        light   = 0xFFD24678.toInt(),
        mid     = 0xFF8C1443.toInt(),
        dark    = 0xFF550A2D.toInt(),
        deep    = 0xFF370618.toInt(),
    ),
    FIRE_ORANGE(
        label   = "Fuego",
        preview = 0xFFE16E14.toInt(),
        light   = 0xFFFFAA41.toInt(),
        mid     = 0xFFE16E14.toInt(),
        dark    = 0xFF964108.toInt(),
        deep    = 0xFF552305.toInt(),
    ),
    AMBER(
        label   = "Ámbar",
        preview = 0xFFDCA01E.toInt(),
        light   = 0xFFFFC846.toInt(),
        mid     = 0xFFDCA01E.toInt(),
        dark    = 0xFF8C6E0A.toInt(),
        deep    = 0xFF554205.toInt(),
    ),
    SUN_YELLOW(
        label   = "Sol",
        preview = 0xFFE1BE0F.toInt(),
        light   = 0xFFFFEB3C.toInt(),
        mid     = 0xFFE1BE0F.toInt(),
        dark    = 0xFF9B7D08.toInt(),
        deep    = 0xFF554605.toInt(),
    ),
    ROYAL_PURPLE(
        label   = "Púrpura",
        preview = 0xFF872DD7.toInt(),
        light   = 0xFFC364FF.toInt(),
        mid     = 0xFF872DD7.toInt(),
        dark    = 0xFF55168C.toInt(),
        deep    = 0xFF300A50.toInt(),
    ),
    LAVENDER(
        label   = "Lavanda",
        preview = 0xFFAA82D2.toInt(),
        light   = 0xFFD4B4F0.toInt(),
        mid     = 0xFFAA82D2.toInt(),
        dark    = 0xFF6E4696.toInt(),
        deep    = 0xFF3C1E5A.toInt(),
    ),
    PASTEL_PINK(
        label   = "Rosa",
        preview = 0xFFEB55A5.toInt(),
        light   = 0xFFFF9FD7.toInt(),
        mid     = 0xFFEB55A5.toInt(),
        dark    = 0xFFA02369.toInt(),
        deep    = 0xFF5A123C.toInt(),
    ),
    NEON_MAGENTA(
        label   = "Magenta",
        preview = 0xFFE6289B.toInt(),
        light   = 0xFFFF6EC7.toInt(),
        mid     = 0xFFE6289B.toInt(),
        dark    = 0xFF9B1464.toInt(),
        deep    = 0xFF5A0A3C.toInt(),
    ),
    SNOW_WHITE(
        label   = "Nieve",
        preview = 0xFFE6E6F0.toInt(),
        light   = 0xFFF5F5FF.toInt(),
        mid     = 0xFFE6E6F0.toInt(),
        dark    = 0xFFBEBECE.toInt(),
        deep    = 0xFF8C8CA0.toInt(),
    ),
    STEEL_GREY(
        label   = "Acero",
        preview = 0xFF828296.toInt(),
        light   = 0xFFC8C8D7.toInt(),
        mid     = 0xFF828296.toInt(),
        dark    = 0xFF464655.toInt(),
        deep    = 0xFF282832.toInt(),
    ),
    OBSIDIAN(
        label   = "Obsidiana",
        preview = 0xFF3C3C50.toInt(),
        light   = 0xFF6E6E82.toInt(),
        mid     = 0xFF3C3C50.toInt(),
        dark    = 0xFF1E1E28.toInt(),
        deep    = 0xFF0F0F14.toInt(),
    ),
}

/**
 * Motor unificado de palette swap pixel-a-pixel para todos los personajes.
 *
 * Soporta **personalización dual**: zona primaria (cuerpo/capucha/pelaje)
 * y zona secundaria (pañuelo/rostro/vientre) con colores independientes.
 */
object SpriteColorEngine {

    // ── Paletas originales por especie ─────────────────────────────────────
    // Dude - Zona primaria: cuerpo
    private val DUDE_BODY_DARK    = intArrayOf(  3,  57, 107)
    private val DUDE_BODY_MID     = intArrayOf(  6, 150, 219)
    private val DUDE_BODY_LIGHT   = intArrayOf( 15, 239, 251)
    // Dude - Zona secundaria: pañuelo
    private val DUDE_ACCENT_DARK  = intArrayOf(195,  36,  47)
    private val DUDE_ACCENT_LIGHT = intArrayOf(231,  51,  59)
    // Dude - Protegidos
    private val DUDE_OUTLINE      = intArrayOf(  4,  25,  63)
    private val DUDE_WHITE        = intArrayOf(252, 254, 254)

    // Pink - Zona primaria: pelaje exterior
    private val PINK_BODY_DARK    = intArrayOf(120,  11, 247)
    private val PINK_BODY_MID     = intArrayOf(216,  64, 251)
    // Pink - Zona secundaria: vientre / cara
    private val PINK_BODY_LIGHT   = intArrayOf(244, 137, 246)
    // Pink - Protegidos
    private val PINK_OUTLINE      = intArrayOf(  4,  25,  63)
    private val PINK_WHITE        = intArrayOf(252, 254, 254)

    // Owlet - Zona primaria: capucha y plumaje exterior
    private val OWLET_BODY_LIGHT  = intArrayOf(252, 254, 254)
    private val OWLET_HOOD_SHADE  = intArrayOf(148, 160, 186)
    // Owlet - Zona secundaria: rostro y manto interior
    private val OWLET_FACE_MID    = intArrayOf(102, 114, 145)
    private val OWLET_CLOAK_DARK  = intArrayOf( 42,  48,  78)
    // Owlet - Protegidos
    private val OWLET_OUTLINE_1   = intArrayOf( 28,  18,  27)
    private val OWLET_OUTLINE_2   = intArrayOf( 28,  26,  48)
    private val OWLET_BEAK_LIGHT  = intArrayOf(253, 162,  22)
    private val OWLET_BEAK_DARK   = intArrayOf(252,  80,   3)
    private val OWLET_COLLAR      = intArrayOf( 94,  44,  41)

    private const val TOLERANCE = 45.0

    // ── Retrocompatibilidad: CharacterColor se mapea a AvatarColor ────────
    /**
     * Mapeo de los 8 colores originales a AvatarColor para migración transparente.
     */
    @Deprecated("Usar AvatarColor directamente", ReplaceWith("AvatarColor"))
    enum class CharacterColor(
        val label: String,
        val primary: AvatarColor,
        val secondary: AvatarColor,
    ) {
        BLUE_ORIGINAL("Azul",      AvatarColor.COBALT_BLUE,   AvatarColor.RUBY_RED),
        GREEN        ("Verde",     AvatarColor.EMERALD,       AvatarColor.FIRE_ORANGE),
        RED          ("Rojo",      AvatarColor.RUBY_RED,      AvatarColor.COBALT_BLUE),
        PURPLE       ("Morado",    AvatarColor.ROYAL_PURPLE,  AvatarColor.AMBER),
        ORANGE       ("Naranja",   AvatarColor.FIRE_ORANGE,   AvatarColor.COBALT_BLUE),
        PINK         ("Rosa",      AvatarColor.PASTEL_PINK,   AvatarColor.ICE_BLUE),
        YELLOW       ("Amarillo",  AvatarColor.SUN_YELLOW,    AvatarColor.ROYAL_PURPLE),
        GREY         ("Gris",      AvatarColor.STEEL_GREY,    AvatarColor.CRIMSON),
    }

    // ── Cache de bitmaps procesados ────────────────────────────────────────
    private data class CacheKey(val path: String, val primary: AvatarColor, val secondary: AvatarColor)
    private val cache = HashMap<CacheKey, Bitmap>()

    /**
     * Carga un sprite sheet con doble color personalizado.
     */
    fun getBitmap(
        context: Context,
        species: CharacterSpecies,
        filename: String,
        primaryColor: AvatarColor,
        secondaryColor: AvatarColor,
    ): Bitmap {
        val assetPath = "sprites/${species.folder}/$filename"
        val key = CacheKey(assetPath, primaryColor, secondaryColor)
        cache[key]?.let { return it }

        val original = BitmapFactory.decodeStream(
            context.assets.open(assetPath)
        ).copy(Bitmap.Config.ARGB_8888, true)

        val result = applyDualPaletteSwap(original, species, primaryColor, secondaryColor)
        cache[key] = result
        return result
    }

    /** Devuelve el frame [frameIndex] recortado como ImageBitmap. */
    fun getFrame(
        context: Context,
        species: CharacterSpecies,
        filename: String,
        primaryColor: AvatarColor,
        secondaryColor: AvatarColor,
        frameIndex: Int,
        frameW: Int = 32,
    ): ImageBitmap {
        val sheet = getBitmap(context, species, filename, primaryColor, secondaryColor)
        val x = frameIndex * frameW
        return Bitmap.createBitmap(sheet, x.coerceAtMost(sheet.width - frameW), 0, frameW, sheet.height)
            .asImageBitmap()
    }

    fun clearCache() = cache.clear()

    // ── Motor interno de Palette Swap dual ─────────────────────────────────
    private fun applyDualPaletteSwap(
        src: Bitmap,
        species: CharacterSpecies,
        primary: AvatarColor,
        secondary: AvatarColor,
    ): Bitmap {
        val w = src.width; val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        if (species == CharacterSpecies.OWLET) {
            tagOwletEyes(pixels, w, h)
            val numFrames = w / 32
            for (f in 0 until numFrames) {
                val fx = f * 32
                // Encontrar la coordenada Y del collar para este frame específico
                var collarY = 21
                for (y in 0 until h) {
                    for (x in fx until (fx + 32)) {
                        if (isColor(pixels[y * w + x], 94, 44, 41)) {
                            collarY = y
                            break
                        }
                    }
                    if (collarY != 21) break
                }

                for (y in 0 until h) {
                    for (x in fx until (fx + 32)) {
                        val idx = y * w + x
                        val p = pixels[idx]
                        if (Color.alpha(p) < 10) continue
                        val rgb = intArrayOf(Color.red(p), Color.green(p), Color.blue(p))

                        // Elementos protegidos que nunca deben cambiar
                        if (rgb[0] == 255 && rgb[1] == 255 && rgb[2] == 255) continue // Ojos blancos
                        if (colorDist(rgb, OWLET_OUTLINE_1)  < 15.0) continue // Contorno / pupila
                        if (colorDist(rgb, OWLET_OUTLINE_2)  < 15.0) continue // Contorno posterior
                        if (colorDist(rgb, OWLET_BEAK_LIGHT) < 20.0) continue // Pico / amuleto claro
                        if (colorDist(rgb, OWLET_BEAK_DARK)  < 20.0) continue // Pico / amuleto oscuro
                        if (colorDist(rgb, OWLET_COLLAR)     < 15.0) continue // Collar marrón

                        val lx = x % 32
                        // Contorno de la capucha y sombra profunda del cuerpo -> Primary deep
                        if (colorDist(rgb, OWLET_CLOAK_DARK) < 20.0) {
                            pixels[idx] = primary.deep
                        } else if (colorDist(rgb, OWLET_BODY_LIGHT) < 20.0) {
                            pixels[idx] = primary.light
                        } else if (colorDist(rgb, OWLET_HOOD_SHADE) < 20.0) {
                            pixels[idx] = primary.mid
                        } else if (colorDist(rgb, OWLET_FACE_MID) < 20.0) {
                            if (lx >= 11 && y < collarY) {
                                // Círculo facial real -> Color secundario
                                pixels[idx] = secondary.mid
                            } else {
                                // Sombra de punta de capucha o sombra de patas/cuerpo -> Color primario oscuro
                                pixels[idx] = primary.dark
                            }
                        }
                    }
                }
            }
            val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            result.setPixels(pixels, 0, w, 0, 0, w, h)
            return result
        }

        // Construir tabla de swap dual para Dude y Pink
        val swapTable: List<Pair<IntArray, Int>> = when (species) {
            CharacterSpecies.DUDE -> listOf(
                // Zona primaria: cuerpo
                DUDE_BODY_DARK    to primary.dark,
                DUDE_BODY_MID     to primary.mid,
                DUDE_BODY_LIGHT   to primary.light,
                // Zona secundaria: pañuelo
                DUDE_ACCENT_DARK  to secondary.dark,
                DUDE_ACCENT_LIGHT to secondary.mid,
            )
            CharacterSpecies.PINK -> listOf(
                // Zona primaria: TODO el cuerpo / pelaje exterior es primario
                PINK_BODY_DARK  to primary.dark,
                PINK_BODY_MID   to primary.mid,
                PINK_BODY_LIGHT to primary.light,
                // Zona secundaria: vientre y máscara facial
                PINK_WHITE to (if (secondary == AvatarColor.SNOW_WHITE) secondary.light else secondary.mid),
            )
            else -> emptyList()
        }

        for (i in pixels.indices) {
            if (Color.alpha(pixels[i]) < 10) continue
            val rgb = intArrayOf(Color.red(pixels[i]), Color.green(pixels[i]), Color.blue(pixels[i]))

            // Proteger colores que nunca deben cambiar según especie
            when (species) {
                CharacterSpecies.DUDE -> {
                    if (colorDist(rgb, DUDE_OUTLINE) < 20.0) continue
                    if (colorDist(rgb, DUDE_WHITE)   < 20.0) continue
                }
                CharacterSpecies.PINK -> {
                    if (colorDist(rgb, PINK_OUTLINE) < 20.0) continue
                }
                else -> {}
            }

            var bestDist = TOLERANCE; var bestColor: Int? = null
            for ((orig, newColor) in swapTable) {
                val d = colorDist(rgb, orig)
                if (d < bestDist) { bestDist = d; bestColor = newColor }
            }
            if (bestColor != null) {
                val a = Color.alpha(pixels[i])
                pixels[i] = Color.argb(a, Color.red(bestColor), Color.green(bestColor), Color.blue(bestColor))
            }
        }

        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, w, 0, 0, w, h)
        return result
    }

    /**
     * Detecta y protege los píxeles de los ojos del Owlet Monster.
     */
    private fun tagOwletEyes(pixels: IntArray, w: Int, h: Int) {
        val numFrames = w / 32
        for (f in 0 until numFrames) {
            val fx = f * 32
            for (y in 0 until h) {
                for (x in (fx + 10)..(fx + 24)) {
                    if (x + 6 < fx + 32) {
                        val iLPre = y * w + (x - 1)
                        val iW1 = y * w + x
                        val iW2 = y * w + (x + 1)
                        val iMid = y * w + (x + 2)
                        val iW3 = y * w + (x + 6)
                        val iRPost = y * w + (x + 7)

                        if (isColor(pixels[iW1], 252, 254, 254) &&
                            isColor(pixels[iW2], 252, 254, 254) &&
                            isColor(pixels[iW3], 252, 254, 254) &&
                            (isColor(pixels[iLPre], 42, 48, 78) || isColor(pixels[iLPre], 28, 26, 48)) &&
                            isColor(pixels[iMid], 102, 114, 145) &&
                            (isColor(pixels[iRPost], 42, 48, 78) || isColor(pixels[iRPost], 28, 26, 48))
                        ) {
                            pixels[iW1] = Color.argb(255, 255, 255, 255)
                            pixels[iW2] = Color.argb(255, 255, 255, 255)
                            pixels[iW3] = Color.argb(255, 255, 255, 255)
                        }
                    }
                }
            }
        }
    }

    private fun isColor(pixel: Int, r: Int, g: Int, b: Int): Boolean =
        Color.red(pixel) == r && Color.green(pixel) == g && Color.blue(pixel) == b

    private fun colorDist(a: IntArray, b: IntArray): Double =
        sqrt(((a[0] - b[0]).toLong() * (a[0] - b[0]) +
              (a[1] - b[1]).toLong() * (a[1] - b[1]) +
              (a[2] - b[2]).toLong() * (a[2] - b[2])).toDouble())
}
