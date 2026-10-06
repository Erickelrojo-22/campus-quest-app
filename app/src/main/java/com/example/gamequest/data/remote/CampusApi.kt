package com.example.gamequest.data.remote

import com.example.gamequest.BuildConfig
import com.example.gamequest.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ApiException(val status: Int, message: String) : IOException(message)

/** HTTPS requests run off the main thread and never retry a mutation automatically. */
class CampusApi(val tokens: SessionTokenStore) {
    var onUnauthorized: () -> Unit = {}
    suspend fun request(path: String, method: String = "GET", body: JSONObject? = null,
                        token: String? = tokens.token): String = withContext(Dispatchers.IO) {
        val connection = URL(BuildConfig.API_BASE_URL + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 100_000
            connection.readTimeout = 100_000
            connection.instanceFollowRedirects = false
            connection.useCaches = false
            connection.setRequestProperty("Accept", "application/json")
            token?.let { connection.setRequestProperty("Authorization", "Bearer $it") }
            if (body != null) {
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.doOutput = true
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val status = connection.responseCode
            val raw = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                if (status == 401 && token != null && tokens.token == token) {
                    tokens.clear()
                    onUnauthorized()
                }
                val detail = runCatching { JSONObject(raw).opt("detail") }.getOrNull()
                val message = when (detail) {
                    is String -> detail
                    is JSONArray -> (0 until detail.length()).joinToString(" · ") { detail.getJSONObject(it).optString("msg") }
                    else -> "El servidor no pudo completar la operación (HTTP $status)."
                }
                throw ApiException(status, message)
            }
            raw
        } catch (e: ApiException) { throw e
        } catch (e: IOException) {
            throw IOException("No se pudo conectar. Comprueba tu conexión y vuelve a intentarlo.", e)
        } finally { connection.disconnect() }
    }
    suspend fun me(): UsuarioEntity = usuario(JSONObject(request("auth/me")))
    suspend fun logout(token: String) { request("auth/logout", "POST", token = token) }
    suspend fun authenticate(path: String, body: JSONObject): UsuarioEntity {
        val json = JSONObject(request("auth/$path", "POST", body, token = null))
        tokens.save(json.getString("accessToken"))
        return usuario(json.getJSONObject("usuario"))
    }
    suspend fun puntos(): List<PuntoInteresEntity> = list(request("puntos")) { p ->
        PuntoInteresEntity(p.getInt("id"), p.getString("nombre"), p.getString("categoria"),
            p.getString("descripcion"), p.getString("horarioAtencion"), p.getString("tramites"),
            p.getDouble("posX").toFloat(), p.getDouble("posY").toFloat(), p.getString("codigoQr"))
    }
    suspend fun misiones(): List<MisionEntity> = list(request("catalogo")) { m ->
        MisionEntity(m.getInt("id"), m.getString("titulo"), m.getString("descripcionPista"),
            m.getInt("puntos"), m.getInt("tiempoEstimadoMin"), m.getString("dificultad"),
            m.getInt("puntoInteresId"), m.getString("insigniaNombre"), m.getString("insigniaEmoji"), m.getBoolean("activa"))
    }
    suspend fun progreso(id: Int): List<ProgresoMisionEntity> = list(request("usuarios/$id/progreso"), ::progreso)
    suspend fun completar(id: Int, mision: Int, codigo: String): ProgresoMisionEntity =
        progreso(JSONObject(request("usuarios/$id/progreso", "POST", JSONObject().put("misionId", mision).put("codigoQr", codigo))))
    suspend fun ranking(): List<UsuarioEntity> = list(request("ranking")) { u ->
        UsuarioEntity(id = u.getInt("id"), nombres = u.getString("nombres"), correoInstitucional = "",
            carrera = "", contrasenaHash = "", puntajeAcumulado = u.getInt("puntajeAcumulado"), nivel = u.getInt("nivel"))
    }
    private fun usuario(u: JSONObject) = UsuarioEntity(u.getInt("id"), u.getString("nombres"),
        u.optString("correoInstitucional"), u.optString("carrera"), "", u.getInt("puntajeAcumulado"), u.getInt("nivel"), u.getString("rol"))
    private fun progreso(p: JSONObject) = ProgresoMisionEntity(p.getInt("id"), p.getInt("usuarioId"),
        p.getInt("misionId"), p.getLong("fechaHora"), p.getInt("puntosObtenidos"), p.getString("codigoQrValidado"), p.getString("estado"))
    private fun <T> list(raw: String, map: (JSONObject) -> T): List<T> {
        val array = JSONArray(raw)
        return (0 until array.length()).map { map(array.getJSONObject(it)) }
    }
}
