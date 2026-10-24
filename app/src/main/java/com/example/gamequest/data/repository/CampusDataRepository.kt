package com.example.gamequest.data.repository

import com.example.gamequest.data.local.entity.MisionEntity
import com.example.gamequest.data.local.entity.PuntoInteresEntity
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.model.RankingEntry
import kotlinx.coroutines.flow.Flow

/** Read contract consumed by presentation ViewModels. Mutations remain guarded by implementations. */
interface CampusDataRepository {
    fun observarUsuario(id: Int): Flow<UsuarioEntity?>
    fun observarRanking(): Flow<List<RankingEntry>>
    fun observarMisionesConEstado(usuarioId: Int): Flow<List<MisionConEstado>>
    fun observarMisionesConPunto(): Flow<List<Pair<MisionEntity, PuntoInteresEntity>>>
    fun observarMisionPorId(id: Int): Flow<MisionEntity?>
    fun observarPuntoPorId(id: Int): Flow<PuntoInteresEntity?>
    fun observarTotalInsignias(usuarioId: Int): Flow<Int>
    fun observarHistorialProgreso(usuarioId: Int): Flow<List<ProgresoDetallado>>
    suspend fun buscarMisionPorId(id: Int): MisionEntity?
    suspend fun buscarPuntoPorId(id: Int): PuntoInteresEntity?
    suspend fun buscarMisionPorPuntoId(puntoId: Int): MisionEntity?
}
