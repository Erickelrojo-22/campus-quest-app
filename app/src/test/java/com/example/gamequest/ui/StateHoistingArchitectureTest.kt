package com.example.gamequest.ui

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regla de state hoisting de la Etapa 3: ningún composable recibe la
 * instancia de un ViewModel como parámetro. Los ViewModels se obtienen con
 * `viewModel()` en el NavHost y las pantallas reciben estado y lambdas.
 */
class StateHoistingArchitectureTest {

    private val raizUi = listOf(
        File("src/main/java/com/example/gamequest/ui"),
        File("app/src/main/java/com/example/gamequest/ui")
    ).first { it.isDirectory }

    /** Firma completa de cada función @Composable, desde `fun` hasta el `{` del cuerpo. */
    private val firmaComposable = Regex(
        """@Composable\s+(?:(?:private|internal|public)\s+)?fun\s+(\w+)\s*\((.*?)\)\s*(?::\s*[\w<>?, .]+)?\s*\{""",
        RegexOption.DOT_MATCHES_ALL
    )
    private val parametroViewModel = Regex("""\w+\s*:\s*\w*ViewModel\b""")

    @Test
    fun ningunComposableRecibeUnViewModelComoParametro() {
        val infracciones = raizUi.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { archivo ->
                firmaComposable.findAll(archivo.readText()).mapNotNull { firma ->
                    val (nombre, parametros) = firma.destructured
                    if (parametroViewModel.containsMatchIn(parametros)) "${archivo.name}: $nombre" else null
                }
            }
            .toList()

        assertTrue(
            "Composables que reciben un ViewModel (elevar estado y eventos): $infracciones",
            infracciones.isEmpty()
        )
    }

    @Test
    fun laReglaDetectaUnaFirmaQueRecibeViewModel() {
        val ejemplo = """
            @Composable
            fun PantallaAcoplada(
                viewModel: HomeViewModel,
                onBack: () -> Unit = {},
                onListo: () -> Unit = { }
            ) {
            }
        """.trimIndent()
        val firma = firmaComposable.find(ejemplo)
        assertTrue(firma != null && parametroViewModel.containsMatchIn(firma.groupValues[2]))
    }
}
