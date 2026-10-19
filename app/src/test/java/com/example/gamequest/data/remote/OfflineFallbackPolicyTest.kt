package com.example.gamequest.data.remote

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineFallbackPolicyTest {

    @Test
    fun errorDeTransporte_permiteUsarCacheLocal() {
        assertTrue(IOException("sin conexión").permiteFallbackOffline())
        assertTrue(SocketTimeoutException("timeout").permiteFallbackOffline())
        assertTrue(UnknownHostException("sin DNS").permiteFallbackOffline())
    }

    @Test
    fun rechazoHttp_noOtorgaProgresoLocal() {
        assertFalse(ApiException(401, "Sesión vencida").permiteFallbackOffline())
        assertFalse(ApiException(409, "Misión duplicada").permiteFallbackOffline())
        assertFalse(ApiException(422, "Código rechazado").permiteFallbackOffline())
    }

    @Test
    fun errorDeDatos_noSeDisfrazaComoFaltaDeConexion() {
        assertFalse(IllegalStateException("respuesta inconsistente").permiteFallbackOffline())
    }
}
