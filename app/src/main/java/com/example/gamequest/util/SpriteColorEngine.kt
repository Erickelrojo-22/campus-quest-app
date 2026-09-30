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
) {
    DUDE(label = "Dude", folder = "dude"),
    PINK(label = "Pink", folder = "pink"),
    OWLET(label = "Owlet", folder = "owlet"),
}

/**
 * Motor unificado de palette swap pixel-a-pixel para todos los personajes.
 *
 * Mapea las paletas originales de cada monstruo (Dude, Pink, Owlet) hacia
 * las 8 variantes de color en tiempo real sin duplicar archivos.
 */
object SpriteColorEngine {

    // ── Paletas originales por especie ─────────────────────────────────────
    // Dude
    private val DUDE_BODY_DARK    = intArrayOf(  3,  57, 107)
    private val DUDE_BODY_MID     = intArrayOf(  6, 150, 219)
    private val DUDE_BODY_LIGHT   = intArrayOf( 15, 239, 251)
    private val DUDE_ACCENT_DARK  = intArrayOf(195,  36,  47)
    private val DUDE_ACCENT_LIGHT = intArrayOf(231,  51,  59)
    private val DUDE_OUTLINE      = intArrayOf(  4,  25,  63)
    private val DUDE_WHITE        = intArrayOf(252, 254, 254)

    // Pink
    private val PINK_BODY_DARK    = intArrayOf(120,  11, 247)
    private val PINK_BODY_MID     = intArrayOf(216,  64, 251)
    private val PINK_BODY_LIGHT   = intArrayOf(244, 137, 246)
    private val PINK_OUTLINE      = intArrayOf(  4,  25,  63)
    private val PINK_WHITE        = intArrayOf(252, 254, 254)

    // Owlet
    private val OWLET_BODY_LIGHT  = intArrayOf(252, 254, 254) // Plumaje exterior y capucha
    private val OWLET_HOOD_SHADE  = intArrayOf(148, 160, 186) // Sombra de transición capucha
    private val OWLET_FACE_MID    = intArrayOf(102, 114, 145) // Cara y manto derecho
    private val OWLET_CLOAK_DARK  = intArrayOf( 42,  48,  78) // Sombra profunda manto
    private val OWLET_OUTLINE_1   = intArrayOf( 28,  18,  27) // Contorno oscuro y pupilas
    private val OWLET_OUTLINE_2   = intArrayOf( 28,  26,  48) // Contorno posterior
    private val OWLET_BEAK_LIGHT  = intArrayOf(253, 162,  22) // Pico / amuleto claro
    private val OWLET_BEAK_DARK   = intArrayOf(252,  80,   3) // Pico / amuleto oscuro
    private val OWLET_COLLAR      = intArrayOf( 94,  44,  41) // Collar marrón
    private val OWLET_EYE_WHITE   = intArrayOf(255, 255, 255) // Ojos blancos

    private const val TOLERANCE = 45.0

    // ── Variantes de color disponibles ────────────────────────────────────
    enum class CharacterColor(
        val label: String,
        val bodyDark:    Int,
        val bodyMid:     Int,
        val bodyLight:   Int,
        val bodyDeep:    Int,
        val accentDark:  Int,
        val accentLight: Int,
    ) {
        BLUE_ORIGINAL(
            label        = "Azul",
            bodyDark     = 0xFF03396B.toInt(),
            bodyMid      = 0xFF0696DB.toInt(),
            bodyLight    = 0xFF0FEFFB.toInt(),
            bodyDeep     = 0xFF021E3C.toInt(),
            accentDark   = 0xFFC3242F.toInt(),
            accentLight  = 0xFFE7333B.toInt(),
        ),
        GREEN(
            label        = "Verde",
            bodyDark     = 0xFF14691E.toInt(),
            bodyMid      = 0xFF23B437.toInt(),
            bodyLight    = 0xFF50F064.toInt(),
            bodyDeep     = 0xFF0A370F.toInt(),
            accentDark   = 0xFFB45A0A.toInt(),
            accentLight  = 0xFFE68214.toInt(),
        ),
        RED(
            label        = "Rojo",
            bodyDark     = 0xFF871616.toInt(),
            bodyMid      = 0xFFC82828.toInt(),
            bodyLight    = 0xFFFF5F5F.toInt(),
            bodyDeep     = 0xFF4B0C0C.toInt(),
            accentDark   = 0xFF0F3C78.toInt(),
            accentLight  = 0xFF196EC8.toInt(),
        ),
        PURPLE(
            label        = "Morado",
            bodyDark     = 0xFF55168C.toInt(),
            bodyMid      = 0xFF872DD7.toInt(),
            bodyLight    = 0xFFC364FF.toInt(),
            bodyDeep     = 0xFF300A50.toInt(),
            accentDark   = 0xFFC8910A.toInt(),
            accentLight  = 0xFFF5BE1E.toInt(),
        ),
        ORANGE(
            label        = "Naranja",
            bodyDark     = 0xFF964108.toInt(),
            bodyMid      = 0xFFE16E14.toInt(),
            bodyLight    = 0xFFFFAA41.toInt(),
            bodyDeep     = 0xFF552305.toInt(),
            accentDark   = 0xFF194BA0.toInt(),
            accentLight  = 0xFF2D78DC.toInt(),
        ),
        PINK(
            label        = "Rosa",
            bodyDark     = 0xFFA02369.toInt(),
            bodyMid      = 0xFFEB55A5.toInt(),
            bodyLight    = 0xFFFF9FD7.toInt(),
            bodyDeep     = 0xFF5A123C.toInt(),
            accentDark   = 0xFF1464AA.toInt(),
            accentLight  = 0xFF28A0E6.toInt(),
        ),
        YELLOW(
            label        = "Amarillo",
            bodyDark     = 0xFF9B7D08.toInt(),
            bodyMid      = 0xFFE1BE0F.toInt(),
            bodyLight    = 0xFFFFEB3C.toInt(),
            bodyDeep     = 0xFF554605.toInt(),
            accentDark   = 0xFF321E82.toInt(),
            accentLight  = 0xFF5A3CC8.toInt(),
        ),
        GREY(
            label        = "Gris",
            bodyDark     = 0xFF464655.toInt(),
            bodyMid      = 0xFF828296.toInt(),
            bodyLight    = 0xFFC8C8D7.toInt(),
            bodyDeep     = 0xFF282832.toInt(),
            accentDark   = 0xFF642808.toInt(),
            accentLight  = 0xFFA54614.toInt(),
        ),
    }

    // ── Cache de bitmaps procesados ────────────────────────────────────────
    private val cache = HashMap<Pair<String, CharacterColor>, Bitmap>()

    /**
     * Carga un sprite sheet desde assets según la especie y archivo.
     */
    fun getBitmap(context: Context, species: CharacterSpecies, filename: String, color: CharacterColor): Bitmap {
        val assetPath = "sprites/${species.folder}/$filename"
        val key = Pair(assetPath, color)
        cache[key]?.let { return it }

        val original = BitmapFactory.decodeStream(
            context.assets.open(assetPath)
        ).copy(Bitmap.Config.ARGB_8888, true)

        val result = applyPaletteSwap(original, species, color)
        cache[key] = result
        return result
    }

    /**
     * Compatibilidad directa con rutas completas de asset.
     */
    fun getBitmap(context: Context, assetPath: String, color: CharacterColor): Bitmap {
        val species = when {
            assetPath.contains("pink")  -> CharacterSpecies.PINK
            assetPath.contains("owlet") -> CharacterSpecies.OWLET
            else                        -> CharacterSpecies.DUDE
        }
        val filename = assetPath.substringAfterLast("/")
        return getBitmap(context, species, filename, color)
    }

    /** Devuelve el frame [frameIndex] recortado como ImageBitmap. */
    fun getFrame(
        context: Context,
        species: CharacterSpecies,
        filename: String,
        color: CharacterColor,
        frameIndex: Int,
        frameW: Int = 32
    ): ImageBitmap {
        val sheet = getBitmap(context, species, filename, color)
        val x = frameIndex * frameW
        return Bitmap.createBitmap(sheet, x.coerceAtMost(sheet.width - frameW), 0, frameW, sheet.height)
            .asImageBitmap()
    }

    /** Sobrecarga por assetPath para compatibilidad */
    fun getFrame(
        context: Context,
        assetPath: String,
        color: CharacterColor,
        frameIndex: Int,
        frameW: Int = 32
    ): ImageBitmap {
        val species = when {
            assetPath.contains("pink")  -> CharacterSpecies.PINK
            assetPath.contains("owlet") -> CharacterSpecies.OWLET
            else                        -> CharacterSpecies.DUDE
        }
        val filename = assetPath.substringAfterLast("/")
        return getFrame(context, species, filename, color, frameIndex, frameW)
    }

    fun clearCache() = cache.clear()

    // ── Motor interno de Palette Swap ─────────────────────────────────────
    private fun applyPaletteSwap(src: Bitmap, species: CharacterSpecies, variant: CharacterColor): Bitmap {
        val w = src.width; val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        if (species == CharacterSpecies.OWLET) {
            tagOwletEyes(pixels, w, h)
        }

        val swapTable = when (species) {
            CharacterSpecies.DUDE -> listOf(
                DUDE_BODY_DARK    to variant.bodyDark,
                DUDE_BODY_MID     to variant.bodyMid,
                DUDE_BODY_LIGHT   to variant.bodyLight,
                DUDE_ACCENT_DARK  to variant.accentDark,
                DUDE_ACCENT_LIGHT to variant.accentLight,
            )
            CharacterSpecies.PINK -> listOf(
                PINK_BODY_DARK  to variant.bodyDark,
                PINK_BODY_MID   to variant.bodyMid,
                PINK_BODY_LIGHT to variant.bodyLight,
            )
            CharacterSpecies.OWLET -> listOf(
                OWLET_BODY_LIGHT  to variant.bodyLight,
                OWLET_HOOD_SHADE  to variant.bodyMid,
                OWLET_FACE_MID    to variant.bodyDark,
                OWLET_CLOAK_DARK  to variant.bodyDeep,
            )
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
                    if (colorDist(rgb, PINK_WHITE)   < 20.0) continue
                }
                CharacterSpecies.OWLET -> {
                    // Ojos blancos protegidos
                    if (rgb[0] == 255 && rgb[1] == 255 && rgb[2] == 255) continue
                    if (colorDist(rgb, OWLET_OUTLINE_1)  < 15.0) continue
                    if (colorDist(rgb, OWLET_OUTLINE_2)  < 15.0) continue
                    if (colorDist(rgb, OWLET_BEAK_LIGHT) < 20.0) continue
                    if (colorDist(rgb, OWLET_BEAK_DARK)  < 20.0) continue
                    if (colorDist(rgb, OWLET_COLLAR)     < 15.0) continue
                }
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
     * Detecta y protege los píxeles de los ojos del Owlet Monster si no están pre-etiquetados con (255, 255, 255).
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
