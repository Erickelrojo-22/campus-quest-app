package com.example.gamequest.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.math.sqrt

/**
 * Motor de palette swap pixel-a-pixel para los sprites del Dude Monster.
 *
 * Paleta original (medida empíricamente del sprite sheet):
 *   OUTLINE      #04193F  — contorno oscuro        → nunca se toca
 *   BODY_DARK    #03396B  — azul oscuro (cuerpo)
 *   BODY_MID     #0696DB  — azul medio (cuerpo)
 *   BODY_LIGHT   #0FEFFB  — azul claro/cyan (cuerpo)
 *   ACCENT_DARK  #C3242F  — rojo oscuro (pañuelo)
 *   ACCENT_LIGHT #E7333B  — rojo claro  (pañuelo)
 *   WHITE        #FCFEFE  — blanco (ojo)            → nunca se toca
 */
object SpriteColorEngine {

    // ── Paleta original RGB ────────────────────────────────────────────────
    private val ORIG_OUTLINE      = intArrayOf(  4,  25,  63)
    private val ORIG_BODY_DARK    = intArrayOf(  3,  57, 107)
    private val ORIG_BODY_MID     = intArrayOf(  6, 150, 219)
    private val ORIG_BODY_LIGHT   = intArrayOf( 15, 239, 251)
    private val ORIG_ACCENT_DARK  = intArrayOf(195,  36,  47)
    private val ORIG_ACCENT_LIGHT = intArrayOf(231,  51,  59)
    private val ORIG_WHITE        = intArrayOf(252, 254, 254)

    private const val TOLERANCE = 55.0

    // ── Variantes de color disponibles ────────────────────────────────────
    enum class CharacterColor(
        val label: String,
        val bodyDark:    Int,
        val bodyMid:     Int,
        val bodyLight:   Int,
        val accentDark:  Int,
        val accentLight: Int,
    ) {
        BLUE_ORIGINAL(
            label        = "Azul",
            bodyDark     = 0xFF03396B.toInt(),
            bodyMid      = 0xFF0696DB.toInt(),
            bodyLight    = 0xFF0FEFFB.toInt(),
            accentDark   = 0xFFC3242F.toInt(),
            accentLight  = 0xFFE7333B.toInt(),
        ),
        GREEN(
            label        = "Verde",
            bodyDark     = 0xFF0F5514.toInt(),
            bodyMid      = 0xFF23B437.toInt(),
            bodyLight    = 0xFF50F064.toInt(),
            accentDark   = 0xFFB45A0A.toInt(),
            accentLight  = 0xFFE68214.toInt(),
        ),
        RED(
            label        = "Rojo",
            bodyDark     = 0xFF5A0C0C.toInt(),
            bodyMid      = 0xFFBE2323.toInt(),
            bodyLight    = 0xFFFF5A5A.toInt(),
            accentDark   = 0xFF0F3C78.toInt(),
            accentLight  = 0xFF196EC8.toInt(),
        ),
        PURPLE(
            label        = "Morado",
            bodyDark     = 0xFF370A5A.toInt(),
            bodyMid      = 0xFF7828C8.toInt(),
            bodyLight    = 0xFFC364FF.toInt(),
            accentDark   = 0xFFC8910A.toInt(),
            accentLight  = 0xFFF5BE1E.toInt(),
        ),
        ORANGE(
            label        = "Naranja",
            bodyDark     = 0xFF823705.toInt(),
            bodyMid      = 0xFFD76414.toInt(),
            bodyLight    = 0xFFFFA53C.toInt(),
            accentDark   = 0xFF1E50AA.toInt(),
            accentLight  = 0xFF3282E6.toInt(),
        ),
        PINK(
            label        = "Rosa",
            bodyDark     = 0xFF8C1E5A.toInt(),
            bodyMid      = 0xFFE650A0.toInt(),
            bodyLight    = 0xFFFF9BD2.toInt(),
            accentDark   = 0xFF1464AA.toInt(),
            accentLight  = 0xFF28A0E6.toInt(),
        ),
        YELLOW(
            label        = "Amarillo",
            bodyDark     = 0xFF786405.toInt(),
            bodyMid      = 0xFFD2B40F.toInt(),
            bodyLight    = 0xFFFFEB3C.toInt(),
            accentDark   = 0xFF321E82.toInt(),
            accentLight  = 0xFF5A3CC8.toInt(),
        ),
        GREY(
            label        = "Gris",
            bodyDark     = 0xFF373746.toInt(),
            bodyMid      = 0xFF78788C.toInt(),
            bodyLight    = 0xFFC3C3D2.toInt(),
            accentDark   = 0xFF642808.toInt(),
            accentLight  = 0xFFA54614.toInt(),
        ),
    }

    // ── Cache: evita reprocesar el mismo sprite+color ──────────────────────
    private val cache = HashMap<Pair<String, CharacterColor>, Bitmap>()

    /**
     * Carga un sprite sheet desde assets (ej. "sprites/dude/idle.png"),
     * aplica el palette swap y devuelve un Bitmap ARGB_8888 listo para recortar frames.
     */
    fun getBitmap(context: Context, assetPath: String, color: CharacterColor): Bitmap {
        val key = Pair(assetPath, color)
        cache[key]?.let { return it }
        val original = BitmapFactory.decodeStream(
            context.assets.open(assetPath)
        ).copy(Bitmap.Config.ARGB_8888, true)
        val result = applyPaletteSwap(original, color)
        cache[key] = result
        return result
    }

    /** Devuelve el frame [frameIndex] del sheet como ImageBitmap de Compose. */
    fun getFrame(context: Context, assetPath: String, color: CharacterColor, frameIndex: Int, frameW: Int = 32): ImageBitmap {
        val sheet = getBitmap(context, assetPath, color)
        val x = frameIndex * frameW
        return Bitmap.createBitmap(sheet, x.coerceAtMost(sheet.width - frameW), 0, frameW, sheet.height)
            .asImageBitmap()
    }

    fun clearCache() = cache.clear()

    // ── Motor interno ─────────────────────────────────────────────────────
    private fun applyPaletteSwap(src: Bitmap, variant: CharacterColor): Bitmap {
        val w = src.width; val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        val swapTable = listOf(
            ORIG_BODY_DARK    to variant.bodyDark,
            ORIG_BODY_MID     to variant.bodyMid,
            ORIG_BODY_LIGHT   to variant.bodyLight,
            ORIG_ACCENT_DARK  to variant.accentDark,
            ORIG_ACCENT_LIGHT to variant.accentLight,
        )

        for (i in pixels.indices) {
            if (Color.alpha(pixels[i]) < 10) continue
            val rgb = intArrayOf(Color.red(pixels[i]), Color.green(pixels[i]), Color.blue(pixels[i]))
            if (colorDist(rgb, ORIG_OUTLINE) < TOLERANCE) continue
            if (colorDist(rgb, ORIG_WHITE)   < TOLERANCE) continue

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

    private fun colorDist(a: IntArray, b: IntArray): Double =
        sqrt(((a[0] - b[0]).toLong() * (a[0] - b[0]) +
              (a[1] - b[1]).toLong() * (a[1] - b[1]) +
              (a[2] - b[2]).toLong() * (a[2] - b[2])).toDouble())
}
