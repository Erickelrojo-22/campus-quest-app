package com.example.gamequest.data.remote

import com.example.gamequest.data.local.dao.UsuarioDao
import com.example.gamequest.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.flowOf

class FakeUsuarioDao : UsuarioDao {
    val users = mutableListOf<UsuarioEntity>()
    var failSave: Exception? = null
    override suspend fun guardarRemotos(items: List<UsuarioEntity>) {
        failSave?.let { throw it }
        items.forEach { user -> users.removeAll { it.id == user.id }; users += user }
    }
    override suspend fun insertar(usuario: UsuarioEntity): Long {
        val id = (users.maxOfOrNull { it.id } ?: 0) + 1
        users += usuario.copy(id = id)
        return id.toLong()
    }
    override suspend fun actualizar(usuario: UsuarioEntity) {
        users.removeAll { it.id == usuario.id }; users += usuario
    }
    override suspend fun actualizarContrasena(usuarioId: Int, hash: String) {
        buscarPorId(usuarioId)?.let { actualizar(it.copy(contrasenaHash = hash)) }
    }
    override suspend fun buscarPorCorreo(correo: String) = users.find { it.correoInstitucional.equals(correo, true) }
    override suspend fun buscarPorNombre(nombre: String) = users.find { it.nombres.equals(nombre, true) }
    override suspend fun buscarPorId(id: Int) = users.find { it.id == id }
    override fun observarPorId(id: Int) = flowOf(users.find { it.id == id })
    override fun observarRanking() = flowOf(users.sortedByDescending { it.puntajeAcumulado })
    override suspend fun sumarPuntos(usuarioId: Int, puntos: Int, nivel: Int) {
        buscarPorId(usuarioId)?.let { actualizar(it.copy(puntajeAcumulado = it.puntajeAcumulado + puntos, nivel = nivel)) }
    }
}
