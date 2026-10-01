package com.example.gamequest.data.repository

import com.example.gamequest.data.local.dao.MisionDao
import com.example.gamequest.data.local.dao.ProgresoMisionDao
import com.example.gamequest.data.local.dao.PuntoInteresDao
import com.example.gamequest.data.local.dao.UsuarioDao
import com.example.gamequest.data.local.entity.Dificultad
import com.example.gamequest.data.local.entity.MisionEntity
import com.example.gamequest.data.local.entity.ProgresoMisionEntity
import com.example.gamequest.data.local.entity.PuntoInteresEntity
import com.example.gamequest.data.local.entity.Rol
import com.example.gamequest.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias para CampusRepository validando la logica de misiones,
 * puntos de interes, validacion de codigos QR y calculo de recompensas.
 */
class CampusRepositoryTest {

    private lateinit var fakePuntoDao: FakePuntoInteresDao
    private lateinit var fakeMisionDao: FakeMisionDao
    private lateinit var fakeProgresoDao: FakeProgresoMisionDao
    private lateinit var fakeUsuarioDao: FakeUsuarioDao
    private lateinit var repository: CampusRepository

    @Before
    fun setup() {
        runBlocking {
            fakePuntoDao = FakePuntoInteresDao()
            fakeMisionDao = FakeMisionDao()
            fakeProgresoDao = FakeProgresoMisionDao()
            fakeUsuarioDao = FakeUsuarioDao()

            repository = CampusRepository(
                database = null,
                puntoDao = fakePuntoDao,
                misionDao = fakeMisionDao,
                progresoDao = fakeProgresoDao,
                usuarioDao = fakeUsuarioDao
            )

            // Seed usuario
            fakeUsuarioDao.insertar(
                UsuarioEntity(
                    id = 1,
                    nombres = "Juan Perez",
                    correoInstitucional = "e123@live.uleam.edu.ec",
                    contrasenaHash = "hash",
                    rol = Rol.ESTUDIANTE,
                    puntajeAcumulado = 50,
                    nivel = 1,
                    carrera = "Software"
                )
            )

            // Seed punto y mision
            val puntoId = fakePuntoDao.insertar(
                PuntoInteresEntity(
                    id = 10,
                    nombre = "Biblioteca Central",
                    categoria = "Académico",
                    descripcion = "Libros y estudio",
                    horarioAtencion = "8am - 6pm",
                    tramites = "Préstamo",
                    posX = 0.5f,
                    posY = 0.5f,
                    codigoQr = "CQ-BIB-001"
                )
            ).toInt()

            fakeMisionDao.insertar(
                MisionEntity(
                    id = 100,
                    titulo = "Encuentra la biblioteca",
                    descripcionPista = "Bloque B",
                    puntos = 50,
                    tiempoEstimadoMin = 15,
                    dificultad = Dificultad.MEDIA,
                    puntoInteresId = puntoId,
                    insigniaNombre = "Lector",
                    insigniaEmoji = "📚"
                )
            )
        }
    }

    @Test
    fun observarMisionesConEstado_combinaCorrectamenteMisionYPunto() = runBlocking {
        val lista = repository.observarMisionesConEstado(usuarioId = 1).first()

        assertEquals(1, lista.size)
        val item = lista.first()
        assertEquals(100, item.mision.id)
        assertEquals(10, item.punto.id)
        assertEquals("Biblioteca Central", item.punto.nombre)
        assertEquals(false, item.completada)
    }

    @Test
    fun validarCodigo_completaMisionYActualizaPuntajeYNivel() = runBlocking {
        val resultado = repository.validarCodigo("CQ-BIB-001", usuarioId = 1)

        assertTrue(resultado is ValidacionQrResult.MisionCompletada)
        val completada = resultado as ValidacionQrResult.MisionCompletada
        assertEquals(50, completada.puntosGanados)
        assertEquals(100, completada.nuevoPuntajeTotal) // 50 inicial + 50 ganados
        assertEquals(2, completada.nuevoNivel) // 1 + (100 / 100) = 2

        // Verificar que el usuario en DAO fue actualizado
        val usuario = fakeUsuarioDao.buscarPorId(1)
        assertNotNull(usuario)
        assertEquals(100, usuario?.puntajeAcumulado)
        assertEquals(2, usuario?.nivel)

        // Ahora la mision debe figurar como completada
        val misiones = repository.observarMisionesConEstado(usuarioId = 1).first()
        assertTrue(misiones.first().completada)
    }

    @Test
    fun validarCodigo_rechazaSiMisionYaFueCompletada() = runBlocking {
        repository.validarCodigo("CQ-BIB-001", usuarioId = 1)

        val segundoIntento = repository.validarCodigo("CQ-BIB-001", usuarioId = 1)
        assertTrue(segundoIntento is ValidacionQrResult.YaCompletada)
    }

    @Test
    fun validarCodigo_retornaCodigoNoReconocidoParaQrInexistente() = runBlocking {
        val resultado = repository.validarCodigo("CQ-QR-INEXISTENTE", usuarioId = 1)
        assertTrue(resultado is ValidacionQrResult.CodigoNoReconocido)
    }

    @Test
    fun observarHistorialProgreso_retornaListaDetalladaConFechaYPuntos() = runBlocking {
        repository.validarCodigo("CQ-BIB-001", usuarioId = 1)

        val historial = repository.observarHistorialProgreso(usuarioId = 1).first()
        assertEquals(1, historial.size)
        val item = historial.first()
        assertEquals(50, item.progreso.puntosObtenidos)
        assertEquals("Encuentra la biblioteca", item.mision?.titulo)
        assertEquals("Biblioteca Central", item.punto?.nombre)
    }

    @Test
    fun generarCodigoQr_incluyePrefijoDeCategoria() {
        val qr = repository.generarCodigoQr("Academico")
        assertTrue(qr.startsWith("CQ-ACA-"))
    }

    // --- Fake DAOs para pruebas ---

    private class FakePuntoInteresDao : PuntoInteresDao {
        val puntos = mutableListOf<PuntoInteresEntity>()
        private var nextId = 1

        override suspend fun insertar(punto: PuntoInteresEntity): Long {
            val id = if (punto.id > 0) punto.id else nextId++
            val conId = punto.copy(id = id)
            puntos.add(conId)
            return id.toLong()
        }

        override suspend fun actualizar(punto: PuntoInteresEntity) {
            val idx = puntos.indexOfFirst { it.id == punto.id }
            if (idx != -1) puntos[idx] = punto
        }

        override suspend fun eliminar(punto: PuntoInteresEntity) {
            puntos.removeAll { it.id == punto.id }
        }

        override fun observarTodos(): Flow<List<PuntoInteresEntity>> = flowOf(puntos)
        override suspend fun buscarPorId(id: Int): PuntoInteresEntity? = puntos.find { it.id == id }
        override fun observarPorId(id: Int): Flow<PuntoInteresEntity?> = flowOf(puntos.find { it.id == id })
        override suspend fun buscarPorCodigoQr(codigo: String): PuntoInteresEntity? =
            puntos.find { it.codigoQr.equals(codigo, ignoreCase = true) }
        override suspend fun contar(): Int = puntos.size
        override suspend fun existeOtroConCodigoQr(codigo: String, puntoId: Int): Boolean =
            puntos.any { it.codigoQr.equals(codigo, ignoreCase = true) && it.id != puntoId }
    }

    private class FakeMisionDao : MisionDao {
        val misiones = mutableListOf<MisionEntity>()
        private var nextId = 1

        override suspend fun insertar(mision: MisionEntity): Long {
            val id = if (mision.id > 0) mision.id else nextId++
            val conId = mision.copy(id = id)
            misiones.add(conId)
            return id.toLong()
        }

        override suspend fun actualizar(mision: MisionEntity) {
            val idx = misiones.indexOfFirst { it.id == mision.id }
            if (idx != -1) misiones[idx] = mision
        }

        override suspend fun archivar(misionId: Int) {
            val idx = misiones.indexOfFirst { it.id == misionId }
            if (idx != -1) misiones[idx] = misiones[idx].copy(activa = false)
        }

        override fun observarTodas(): Flow<List<MisionEntity>> = flowOf(misiones.filter { it.activa })
        override suspend fun buscarPorId(id: Int): MisionEntity? = misiones.find { it.id == id }
        override fun observarPorId(id: Int): Flow<MisionEntity?> = flowOf(misiones.find { it.id == id })
        override suspend fun buscarPorPunto(puntoId: Int): List<MisionEntity> =
            misiones.filter { it.puntoInteresId == puntoId && it.activa }
        override suspend fun contar(): Int = misiones.size
    }

    private class FakeProgresoMisionDao : ProgresoMisionDao {
        val progresos = mutableListOf<ProgresoMisionEntity>()
        private var nextId = 1

        override suspend fun insertar(progreso: ProgresoMisionEntity): Long {
            val conId = progreso.copy(id = nextId++)
            progresos.add(conId)
            return conId.id.toLong()
        }

        override fun observarPorUsuario(usuarioId: Int): Flow<List<ProgresoMisionEntity>> =
            flowOf(progresos.filter { it.usuarioId == usuarioId }.sortedByDescending { it.fechaHora })

        override fun observarMisionesCompletadasIds(usuarioId: Int): Flow<List<Int>> =
            flowOf(progresos.filter { it.usuarioId == usuarioId }.map { it.misionId })

        override suspend fun buscar(usuarioId: Int, misionId: Int): ProgresoMisionEntity? =
            progresos.find { it.usuarioId == usuarioId && it.misionId == misionId }

        override fun observarTotalCompletadas(usuarioId: Int): Flow<Int> =
            flowOf(progresos.count { it.usuarioId == usuarioId })

        override suspend fun contarCompletadas(usuarioId: Int): Int =
            progresos.count { it.usuarioId == usuarioId }

        override suspend fun contarPorMision(misionId: Int): Int =
            progresos.count { it.misionId == misionId }
    }

    private class FakeUsuarioDao : UsuarioDao {
        val usuarios = mutableListOf<UsuarioEntity>()
        private var nextId = 1

        override suspend fun insertar(usuario: UsuarioEntity): Long {
            val id = if (usuario.id > 0) usuario.id else nextId++
            val conId = usuario.copy(id = id)
            usuarios.add(conId)
            return id.toLong()
        }

        override suspend fun actualizar(usuario: UsuarioEntity) {
            val idx = usuarios.indexOfFirst { it.id == usuario.id }
            if (idx != -1) usuarios[idx] = usuario
        }

        override suspend fun actualizarContrasena(usuarioId: Int, hash: String) {
            val u = usuarios.find { it.id == usuarioId } ?: return
            actualizar(u.copy(contrasenaHash = hash))
        }

        override suspend fun buscarPorCorreo(correo: String): UsuarioEntity? =
            usuarios.find { it.correoInstitucional.equals(correo.trim(), ignoreCase = true) }

        override suspend fun buscarPorNombre(nombre: String): UsuarioEntity? =
            usuarios.find { it.nombres.equals(nombre.trim(), ignoreCase = true) }

        override suspend fun buscarPorId(id: Int): UsuarioEntity? = usuarios.find { it.id == id }
        override fun observarPorId(id: Int): Flow<UsuarioEntity?> = flowOf(usuarios.find { it.id == id })
        override fun observarRanking(): Flow<List<UsuarioEntity>> =
            flowOf(usuarios.sortedByDescending { it.puntajeAcumulado })

        override suspend fun sumarPuntos(usuarioId: Int, puntos: Int, nivel: Int) {
            val u = usuarios.find { it.id == usuarioId } ?: return
            actualizar(u.copy(puntajeAcumulado = u.puntajeAcumulado + puntos, nivel = nivel))
        }
    }
}
