package com.example.gamequest.util

import androidx.camera.core.ImageProxy
import androidx.camera.core.ImageAnalysis
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

    private val reader = QRCodeReader()
    private var procesando = false

    override fun analyze(image: ImageProxy) {
        if (procesando) {
            image.close()
            return
        }
        procesando = true
        try {
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            val source = PlanarYUVLuminanceSource(
                bytes,
                image.width,
                image.height,
                0,
                0,
                image.width,
                image.height,
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
}
