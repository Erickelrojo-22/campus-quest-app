package com.example.gamequest.data.local

import com.example.gamequest.data.local.entity.Dificultad
import com.example.gamequest.data.local.entity.MisionEntity
import com.example.gamequest.data.local.entity.PuntoInteresEntity

/**
 * Contenido inicial del campus: los puntos de interés y misiones descritos en el
 * apartado 2.3 (necesidades detectadas) y en los escenarios de uso del documento.
 * Se inserta una sola vez, en la creación de la base de datos, para que la
 * aplicación funcione sin conexión desde el primer arranque (RF-20).
 */
object SeedData {

    data class PuntoSemilla(
        val punto: PuntoInteresEntity,
        val mision: MisionEntity

    )

    fun puntosConMisiones(): List<PuntoSemilla> = listOf(
        PuntoSemilla(
            punto = PuntoInteresEntity(
                nombre = "Biblioteca central",
                categoria = "Académico",
                descripcion = "Sala de lectura, préstamo de libros y salas de estudio grupal.",
                horarioAtencion = "Lunes a viernes, 07:30 a 19:00",
                tramites = "Préstamo y devolución de libros, carné de biblioteca, salas de estudio",
                posX = 0.22f,
                posY = 0.30f,
                codigoQr = "CQ-BIB-001"
            ),
            mision = MisionEntity(
                titulo = "Encuentra la biblioteca",
                descripcionPista = "Bloque B, planta baja. Llega al mostrador de atención y escanea el código QR para validar tu visita.",
                puntos = 50,
                tiempoEstimadoMin = 15,
                dificultad = Dificultad.MEDIA,
                puntoInteresId = 0,
                insigniaNombre = "Ratón de biblioteca",
                insigniaEmoji = "📚"
            )
        ),
        PuntoSemilla(
            punto = PuntoInteresEntity(
                nombre = "Secretaría académica",
                categoria = "Trámites",
                descripcion = "Edificio administrativo. Recepción de documentos de matrícula, certificados y constancias.",
                horarioAtencion = "Lunes a viernes, 08:00 a 16:30",
                tramites = "Entrega de documentos de matrícula, constancias, cambios de datos",
                posX = 0.62f,
                posY = 0.22f,
                codigoQr = "CQ-SEC-002"
            ),
            mision = MisionEntity(
                titulo = "Ubica secretaría académica",
                descripcionPista = "Edificio administrativo, primera planta, ventanilla 2. Escanea el código QR de la ventanilla.",
                puntos = 30,
                tiempoEstimadoMin = 10,
                dificultad = Dificultad.BAJA,
                puntoInteresId = 0,
                insigniaNombre = "Trámites resueltos",
                insigniaEmoji = "🏛️"
            )
        ),
        PuntoSemilla(
            punto = PuntoInteresEntity(
                nombre = "Laboratorio de software",
                categoria = "Académico",
                descripcion = "Bloque C, aula 204. Equipos para prácticas de programación y desarrollo móvil.",
                horarioAtencion = "Lunes a sábado, 07:00 a 21:00",
                tramites = "Reserva de equipos, soporte técnico de laboratorio",
                posX = 0.20f,
                posY = 0.68f,
                codigoQr = "CQ-LAB-003"
            ),
            mision = MisionEntity(
                titulo = "Visita el laboratorio de software",
                descripcionPista = "Bloque C, aula 204. Sube al segundo piso y busca la puerta con el mural de código.",
                puntos = 80,
                tiempoEstimadoMin = 20,
                dificultad = Dificultad.ALTA,
                puntoInteresId = 0,
                insigniaNombre = "Futuro ingeniero",
                insigniaEmoji = "💻"
            )
        ),
        PuntoSemilla(
            punto = PuntoInteresEntity(
                nombre = "Bienestar estudiantil",
                categoria = "Servicios",
                descripcion = "Orientación psicológica, becas y acompañamiento a estudiantes nuevos.",
                horarioAtencion = "Lunes a viernes, 08:00 a 17:00",
                tramites = "Solicitud de becas, orientación psicológica, tutorías",
                posX = 0.78f,
                posY = 0.55f,
                codigoQr = "CQ-BIE-004"
            ),
            mision = MisionEntity(
                titulo = "Conoce bienestar estudiantil",
                descripcionPista = "Junto al bloque administrativo. Pregunta por la oferta de becas para nuevo ingreso.",
                puntos = 40,
                tiempoEstimadoMin = 12,
                dificultad = Dificultad.MEDIA,
                puntoInteresId = 0,
                insigniaNombre = "Bien acompañado",
                insigniaEmoji = "💚"
            )
        ),
        PuntoSemilla(
            punto = PuntoInteresEntity(
                nombre = "Cafetería central",
                categoria = "Servicios",
                descripcion = "Zona de comida y descanso entre clases.",
                horarioAtencion = "Lunes a viernes, 07:00 a 18:00",
                tramites = "Venta de alimentos, punto de encuentro",
                posX = 0.45f,
                posY = 0.80f,
                codigoQr = "CQ-CAF-005"
            ),
            mision = MisionEntity(
                titulo = "Descubre la cafetería central",
                descripcionPista = "En el patio central. Ideal para tu primer descanso entre clases.",
                puntos = 20,
                tiempoEstimadoMin = 8,
                dificultad = Dificultad.BAJA,
                puntoInteresId = 0,
                insigniaNombre = "Buen provecho",
                insigniaEmoji = "☕"
            )
        ),
        PuntoSemilla(
            punto = PuntoInteresEntity(
                nombre = "Canchas deportivas",
                categoria = "Recreación",
                descripcion = "Espacios para fútbol, básquet y actividades del área de cultura física.",
                horarioAtencion = "Lunes a sábado, 07:00 a 20:00",
                tramites = "Reserva de canchas, inscripción a torneos internos",
                posX = 0.85f,
                posY = 0.82f,
                codigoQr = "CQ-CAN-006"
            ),
            mision = MisionEntity(
                titulo = "Explora las canchas deportivas",
                descripcionPista = "Al fondo del campus. Sigue el camino después de la cafetería.",
                puntos = 30,
                tiempoEstimadoMin = 10,
                dificultad = Dificultad.BAJA,
                puntoInteresId = 0,
                insigniaNombre = "Espíritu deportivo",
                insigniaEmoji = "⚽"
            )
        ),
        PuntoSemilla(
            punto = PuntoInteresEntity(
                nombre = "Ventanilla de certificados",
                categoria = "Trámites",
                descripcion = "Emisión de certificados de matrícula, notas y egresamiento.",
                horarioAtencion = "Lunes a viernes, 08:00 a 12:30 y 14:00 a 16:30",
                tramites = "Certificado de matrícula (cédula y número de matrícula), certificado de notas",
                posX = 0.60f,
                posY = 0.40f,
                codigoQr = "CQ-CER-007"
            ),
            mision = MisionEntity(
                titulo = "Solicita un certificado",
                descripcionPista = "Junto a secretaría académica. Lleva tu cédula y número de matrícula.",
                puntos = 35,
                tiempoEstimadoMin = 10,
                dificultad = Dificultad.MEDIA,
                puntoInteresId = 0,
                insigniaNombre = "Gestor eficiente",
                insigniaEmoji = "📄"
            )
        ),
        PuntoSemilla(
            punto = PuntoInteresEntity(
                nombre = "Centro de cómputo",
                categoria = "Académico",
                descripcion = "Sala de computadoras de uso libre para trabajos e investigación.",
                horarioAtencion = "Lunes a viernes, 08:00 a 20:00",
                tramites = "Uso libre de equipos, impresión de documentos",
                posX = 0.35f,
                posY = 0.50f,
                codigoQr = "CQ-COM-008"
            ),
            mision = MisionEntity(
                titulo = "Conoce el centro de cómputo",
                descripcionPista = "Cerca del laboratorio de software. Pregunta por el horario de uso libre.",
                puntos = 40,
                tiempoEstimadoMin = 10,
                dificultad = Dificultad.MEDIA,
                puntoInteresId = 0,
                insigniaNombre = "Conectado",
                insigniaEmoji = "🖥️"
            )
        ),
        PuntoSemilla(
            punto = PuntoInteresEntity(
                nombre = "Parqueadero de Informática",
                categoria = "Servicios",
                descripcion = "Parqueadero junto a la Facultad de Informática.",
                horarioAtencion = "Por confirmar",
                tramites = "Estacionamiento",
                posX = 0.775f,
                posY = 0.540f,
                codigoQr = "CQ-PINF-009"
            ),
            mision = MisionEntity(
                titulo = "Encuentra el parqueadero de Informática",
                descripcionPista = "Busca el parqueadero junto a la Facultad de Informática, cerca del camino al este de la pista atlética. Escanea el QR del lugar.",
                puntos = 20,
                tiempoEstimadoMin = 8,
                dificultad = Dificultad.BAJA,
                puntoInteresId = 0,
                insigniaNombre = "Llegada a Informática",
                insigniaEmoji = "🅿️"
            )
        ),
        PuntoSemilla(
            punto = PuntoInteresEntity(
                nombre = "Facultad de Informática",
                categoria = "Académico",
                descripcion = "Edificio de Informática al este de la pista atlética, entre Inglés y el parqueadero.",
                horarioAtencion = "Por confirmar",
                tramites = "Información académica de Informática",
                posX = 0.630f,
                posY = 0.445f,
                codigoQr = "CQ-INF-010"
            ),
            mision = MisionEntity(
                titulo = "Encuentra la Facultad de Informática",
                descripcionPista = "Desde el parqueadero, sigue el camino hacia el edificio intermedio al este de la pista atlética. Encuentra Informática y escanea su QR.",
                puntos = 30,
                tiempoEstimadoMin = 10,
                dificultad = Dificultad.BAJA,
                puntoInteresId = 0,
                insigniaNombre = "Explorador de Informática",
                insigniaEmoji = "💻"
            )
        )
    )
}
