package com.example.gamequest.ui.common

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptableTest {
    @Test
    fun clasificaLosLimitesDeAnchoDocumentados() {
        assertEquals(ClaseAnchoVentana.COMPACTO_PEQUENO, perfilVentanaCampus(359.dp, 800.dp).ancho)
        assertEquals(ClaseAnchoVentana.COMPACTO, perfilVentanaCampus(360.dp, 800.dp).ancho)
        assertEquals(ClaseAnchoVentana.MEDIO, perfilVentanaCampus(600.dp, 800.dp).ancho)
        assertEquals(ClaseAnchoVentana.EXPANDIDO, perfilVentanaCampus(840.dp, 800.dp).ancho)
    }

    @Test
    fun usaRielParaTabletYParaTelefonoHorizontal() {
        assertFalse(perfilVentanaCampus(393.dp, 852.dp).usarRielNavegacion)
        assertTrue(perfilVentanaCampus(600.dp, 960.dp).usarRielNavegacion)
        assertTrue(perfilVentanaCampus(568.dp, 320.dp).usarRielNavegacion)
    }
}
