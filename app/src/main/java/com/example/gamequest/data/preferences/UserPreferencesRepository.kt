package com.example.gamequest.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.gamequest.util.AvatarColor
import com.example.gamequest.util.CharacterSpecies
import com.example.gamequest.util.SpriteColorEngine.CharacterColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "campus_quest_preferences")

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
 * Persistencia local ligera con DataStore (RF-18, RF-19): preferencias de la
 * aplicación y la sesión activa del usuario, para que se restauren
 * automáticamente la próxima vez que se abra la aplicación.
 */
class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val TEMA_OSCURO = booleanPreferencesKey("tema_oscuro")
        val TAMANO_TEXTO = stringPreferencesKey("tamano_texto")
        val RECORDATORIOS_MISION = booleanPreferencesKey("recordatorios_mision")
        val AVISOS_CAMPUS = booleanPreferencesKey("avisos_campus")
        val SONIDO_VIBRACION = booleanPreferencesKey("sonido_vibracion")
        val IDIOMA = stringPreferencesKey("idioma")
        val CAMPUS_DEFECTO = stringPreferencesKey("campus_por_defecto")
        val DESCARGAR_MAPA = booleanPreferencesKey("descargar_mapa_sin_conexion")
        val USUARIO_ACTIVO_ID = intPreferencesKey("usuario_activo_id")
        val CHARACTER_COLOR = stringPreferencesKey("character_color")
        val CHARACTER_SPECIES = stringPreferencesKey("character_species")
        val AVATAR_PRIMARY_COLOR = stringPreferencesKey("avatar_primary_color")
        val AVATAR_SECONDARY_COLOR = stringPreferencesKey("avatar_secondary_color")
        val TUTORIAL_VISTO = booleanPreferencesKey("tutorial_visto")
    }

    val preferencias: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        // Migrar del antiguo CharacterColor si no hay AvatarColor guardado aún
        @Suppress("DEPRECATION")
        val legacyColor = prefs[Keys.CHARACTER_COLOR]
            ?.let { runCatching { CharacterColor.valueOf(it) }.getOrNull() }
            ?: CharacterColor.BLUE_ORIGINAL

        val primaryColor = prefs[Keys.AVATAR_PRIMARY_COLOR]
            ?.let { runCatching { AvatarColor.valueOf(it) }.getOrNull() }
            ?: legacyColor.primary  // migración automática

        val secondaryColor = prefs[Keys.AVATAR_SECONDARY_COLOR]
            ?.let { runCatching { AvatarColor.valueOf(it) }.getOrNull() }
            ?: legacyColor.secondary  // migración automática

        @Suppress("DEPRECATION")
        UserPreferences(
            temaOscuro = prefs[Keys.TEMA_OSCURO] ?: true,
            tamanoTexto = prefs[Keys.TAMANO_TEXTO] ?: "Mediano",
            recordatoriosMision = prefs[Keys.RECORDATORIOS_MISION] ?: true,
            avisosCampus = prefs[Keys.AVISOS_CAMPUS] ?: true,
            sonidoVibracion = prefs[Keys.SONIDO_VIBRACION] ?: true,
            idioma = prefs[Keys.IDIOMA] ?: "Español",
            campusPorDefecto = prefs[Keys.CAMPUS_DEFECTO] ?: "Manta",
            descargarMapaSinConexion = prefs[Keys.DESCARGAR_MAPA] ?: true,
            usuarioActivoId = prefs[Keys.USUARIO_ACTIVO_ID] ?: -1,
            characterSpecies = prefs[Keys.CHARACTER_SPECIES]
                ?.let { runCatching { CharacterSpecies.valueOf(it) }.getOrNull() }
                ?: CharacterSpecies.DUDE,
            avatarPrimaryColor = primaryColor,
            avatarSecondaryColor = secondaryColor,
            tutorialVisto = prefs[Keys.TUTORIAL_VISTO] ?: false,
            characterColor = legacyColor,
        )
    }

    suspend fun setTemaOscuro(valor: Boolean) {
        context.dataStore.edit { it[Keys.TEMA_OSCURO] = valor }
    }

    suspend fun setTamanoTexto(valor: String) {
        context.dataStore.edit { it[Keys.TAMANO_TEXTO] = valor }
    }

    suspend fun setRecordatoriosMision(valor: Boolean) {
        context.dataStore.edit { it[Keys.RECORDATORIOS_MISION] = valor }
    }

    suspend fun setAvisosCampus(valor: Boolean) {
        context.dataStore.edit { it[Keys.AVISOS_CAMPUS] = valor }
    }

    suspend fun setSonidoVibracion(valor: Boolean) {
        context.dataStore.edit { it[Keys.SONIDO_VIBRACION] = valor }
    }

    suspend fun setCampusPorDefecto(valor: String) {
        context.dataStore.edit { it[Keys.CAMPUS_DEFECTO] = valor }
    }

    suspend fun setDescargarMapaSinConexion(valor: Boolean) {
        context.dataStore.edit { it[Keys.DESCARGAR_MAPA] = valor }
    }

    suspend fun setUsuarioActivoId(id: Int) {
        context.dataStore.edit { it[Keys.USUARIO_ACTIVO_ID] = id }
    }

    suspend fun setCharacterSpecies(species: CharacterSpecies) {
        context.dataStore.edit { it[Keys.CHARACTER_SPECIES] = species.name }
    }

    /** Guarda el color primario (cuerpo / capucha / pelaje). */
    suspend fun setAvatarPrimaryColor(color: AvatarColor) {
        context.dataStore.edit { it[Keys.AVATAR_PRIMARY_COLOR] = color.name }
    }

    /** Guarda el color secundario (pañuelo / rostro / vientre). */
    suspend fun setAvatarSecondaryColor(color: AvatarColor) {
        context.dataStore.edit { it[Keys.AVATAR_SECONDARY_COLOR] = color.name }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Usar setAvatarPrimaryColor / setAvatarSecondaryColor")
    suspend fun setCharacterColor(color: CharacterColor) {
        context.dataStore.edit { it[Keys.CHARACTER_COLOR] = color.name }
    }

    suspend fun setTutorialVisto(visto: Boolean) {
        context.dataStore.edit { it[Keys.TUTORIAL_VISTO] = visto }
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit { it[Keys.USUARIO_ACTIVO_ID] = -1 }
    }
}
