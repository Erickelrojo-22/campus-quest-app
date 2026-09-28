package com.example.gamequest.util

import androidx.camera.core.ImageProxy
import androidx.camera.core.ImageAnalysis
import android.os.SystemClock
import com.google.zxing.BinaryBitmap
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader

/**
 * RF-10: analiza cada frame de la cámara (CameraX) en busca de un código QR,
 * usando ZXing de forma local (sin ML Kit ni descargas de modelos), lo que
 * mantiene el escaneo funcionando completamente sin conexión (RF-20).
 */
class QrAnalyzer(private val onQrDetectado: (String) -> Unit) : ImageAnalysis.Analyzer {

    private companion object {
        const val INTERVALO_ANALISIS_MS = 120L
    }

    private val reader = QRCodeReader()
    private var procesando = false
    private var ultimoAnalisisMs = 0L

    override fun analyze(image: ImageProxy) {
        val ahoraMs = SystemClock.elapsedRealtime()
        if (procesando || ahoraMs - ultimoAnalisisMs < INTERVALO_ANALISIS_MS) {
            image.close()
            return
        }
        procesando = true
        ultimoAnalisisMs = ahoraMs
        try {
            val plane = image.planes[0]
            val recorte = extraerRecorteCentral(
                buffer = plane.buffer,
                imageWidth = image.width,
                imageHeight = image.height,
                rowStride = plane.rowStride,
                pixelStride = plane.pixelStride
            )

            val source = PlanarYUVLuminanceSource(
                recorte.bytes,
                recorte.width,
                recorte.height,
                0,
                0,
                recorte.width,
                recorte.height,
                false
            )
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            try {
                val resultado = reader.decode(binaryBitmap)
                onQrDetectado(resultado.text)
            } catch (_: NotFoundException) {
                // Ningún código en este frame; se intenta con el siguiente.
            } finally {
                reader.reset()
            }
        } finally {
            procesando = false
            image.close()
        }
    }

    private fun extraerRecorteCentral(
        buffer: java.nio.ByteBuffer,
        imageWidth: Int,
        imageHeight: Int,
        rowStride: Int,
        pixelStride: Int
    ): LuminanceCrop {
        val left = imageWidth / 10
        val top = imageHeight / 10
        val width = imageWidth - (left * 2)
        val height = imageHeight - (top * 2)
        val bytes = ByteArray(width * height)
        val source = buffer.duplicate()
        val bufferStart = source.position()

        var destination = 0
        for (row in top until top + height) {
            val rowStart = bufferStart + row * rowStride + left * pixelStride
            for (column in 0 until width) {
                bytes[destination++] = source.get(rowStart + column * pixelStride)
            }
        }
        return LuminanceCrop(bytes, width, height)
    }

    private data class LuminanceCrop(
        val bytes: ByteArray,
        val width: Int,
        val height: Int
    )
}
