package com.example.gamequest.data.preferences

import com.example.gamequest.util.AvatarColor
import com.example.gamequest.util.CharacterSpecies
import com.example.gamequest.util.SpriteColorEngine.CharacterColor
import kotlinx.coroutines.flow.Flow

@Suppress("DEPRECATION")
data class UserPreferences(
    val temaOscuro: Boolean = true,
    val tamanoTexto: String = "Mediano",
    val recordatoriosMision: Boolean = true,
    val avisosCampus: Boolean = true,
    val sonidoVibracion: Boolean = true,
    val idioma: String = "Español",
    val campusPorDefecto: String = "Manta",
    val descargarMapaSinConexion: Boolean = true,
    val usuarioActivoId: Int = -1,
    val characterSpecies: CharacterSpecies = CharacterSpecies.DUDE,
    /** Color primario del avatar (cuerpo / capucha / pelaje). */
    val avatarPrimaryColor: AvatarColor = AvatarColor.COBALT_BLUE,
    /** Color secundario del avatar (pañuelo / rostro / vientre). */
    val avatarSecondaryColor: AvatarColor = AvatarColor.RUBY_RED,
    val tutorialVisto: Boolean = false,
    // Retrocompatibilidad — se mantiene pero ya no se usa directamente
    @Deprecated("Usar avatarPrimaryColor / avatarSecondaryColor")
    val characterColor: CharacterColor = CharacterColor.BLUE_ORIGINAL,
)

/**
 * Contrato de preferencias y sesión activa (RF-18, RF-19). Los ViewModels y
 * el coordinador de sesión dependen de esta abstracción; la persistencia con
 * DataStore vive en [DataStoreUserPreferencesRepository].
 */
interface UserPreferencesRepository {
    val preferencias: Flow<UserPreferences>

    suspend fun setTemaOscuro(valor: Boolean)
    suspend fun setTamanoTexto(valor: String)
    suspend fun setRecordatoriosMision(valor: Boolean)
    suspend fun setAvisosCampus(valor: Boolean)
    suspend fun setSonidoVibracion(valor: Boolean)
    suspend fun setCampusPorDefecto(valor: String)
    suspend fun setDescargarMapaSinConexion(valor: Boolean)
    suspend fun setUsuarioActivoId(id: Int)
    suspend fun setCharacterSpecies(species: CharacterSpecies)
    suspend fun setAvatarPrimaryColor(color: AvatarColor)
    suspend fun setAvatarSecondaryColor(color: AvatarColor)
    @Deprecated("Usar setAvatarPrimaryColor / setAvatarSecondaryColor")
    suspend fun setCharacterColor(color: CharacterColor)
    suspend fun setTutorialVisto(visto: Boolean)
    suspend fun cerrarSesion()
}
