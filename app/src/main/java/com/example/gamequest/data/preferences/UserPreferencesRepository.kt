package com.example.gamequest.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.gamequest.util.CharacterSpecies
import com.example.gamequest.util.SpriteColorEngine.CharacterColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "campus_quest_preferences")

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
    val characterColor: CharacterColor = CharacterColor.BLUE_ORIGINAL,
    val characterSpecies: CharacterSpecies = CharacterSpecies.DUDE,
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
    }

    val preferencias: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
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
            characterColor = prefs[Keys.CHARACTER_COLOR]
                ?.let { runCatching { CharacterColor.valueOf(it) }.getOrNull() }
                ?: CharacterColor.BLUE_ORIGINAL,
            characterSpecies = prefs[Keys.CHARACTER_SPECIES]
                ?.let { runCatching { CharacterSpecies.valueOf(it) }.getOrNull() }
                ?: CharacterSpecies.DUDE,
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

    suspend fun setCharacterColor(color: CharacterColor) {
        context.dataStore.edit { it[Keys.CHARACTER_COLOR] = color.name }
    }

    suspend fun setCharacterSpecies(species: CharacterSpecies) {
        context.dataStore.edit { it[Keys.CHARACTER_SPECIES] = species.name }
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit { it[Keys.USUARIO_ACTIVO_ID] = -1 }
    }
}
