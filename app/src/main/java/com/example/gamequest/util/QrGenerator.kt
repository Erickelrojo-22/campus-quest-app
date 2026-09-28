package com.example.gamequest.util

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/** RF-17: genera el código QR de un punto de interés para imprimirlo y pegarlo en el sitio. */
object QrGenerator {

    fun generarBitmap(contenido: String, tamano: Int = 512): ImageBitmap {
        val writer = QRCodeWriter()
        val matrix = writer.encode(contenido, BarcodeFormat.QR_CODE, tamano, tamano)
        val bitmap = Bitmap.createBitmap(tamano, tamano, Bitmap.Config.RGB_565)
        for (x in 0 until tamano) {
            for (y in 0 until tamano) {
                bitmap.setPixel(x, y, if (matrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap.asImageBitmap()
    }
}
